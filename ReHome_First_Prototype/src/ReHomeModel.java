import java.util.ArrayList;
import java.util.Locale;

/**
 * Manages donated items and their waiting lists in ReHome. This class contains
 * the application's main business logic, including item validation, searching,
 * FIFO waitlists, claims, passes, and timed offer expiration. The model does
 * not depend on the controller or view. Public operations are synchronized
 * because the menu and timer may access the model from different threads.
 */
public class ReHomeModel
{

    /**
     * Default offer duration of five minutes.
     */
    public static final long DEFAULT_OFFER_MILLIS = 300000L;

    private final ArrayList<Item> items;
    private final long offerWindowMillis;
    private int nextItemId;

    /**
     * Creates an empty ReHome model. Each student at the front of a waitlist
     * receives five minutes to respond.
     */
    public ReHomeModel()
    {
        this(DEFAULT_OFFER_MILLIS);
    }


    /**
     * Creates an empty model with a custom offer duration. A shorter duration
     * is useful for testing timeouts.
     *
     * @param offerWindowMillis
     *            the duration of each offer in milliseconds
     * @throws IllegalArgumentException
     *             if the duration is zero or negative
     */
    public ReHomeModel(long offerWindowMillis)
    {
        if (offerWindowMillis <= 0)
        {
            throw new IllegalArgumentException(
                "Offer length must be positive.");
        }

        items = new ArrayList<Item>();
        nextItemId = 1;
        this.offerWindowMillis = offerWindowMillis;
    }


    /**
     * Adds a new item to the catalog. The item name is required. Descriptions
     * and categories may be empty. An invalid item does not consume an
     * identification number.
     *
     * @param name
     *            the item name, 1 to 60 characters
     * @param description
     *            the description, 0 to 200 characters
     * @param category
     *            the category, 0 to 30 characters
     * @return the new item's unique ID
     * @throws IllegalArgumentException
     *             if any field contains invalid text
     */
    public synchronized
        int
        addItem(String name, String description, String category)
    {

        String cleanName = checkText(name, "Item name", 1, 60);

        String cleanDescription = checkText(description, "Description", 0, 200);

        String cleanCategory = checkText(category, "Category", 0, 30);

        Item item =
            new Item(nextItemId, cleanName, cleanDescription, cleanCategory);

        items.add(item);
        nextItemId++;

        return item.id;
    }


    /**
     * Returns all items that have not been claimed. Items appear in their
     * original ID order.
     *
     * @return a list of available item snapshots
     */
    public synchronized ArrayList<ItemSnapshot> getAvailableItems()
    {

        return searchItems("", "");
    }


    /**
     * Searches available items using a keyword and category. Keyword matching
     * checks item names and descriptions. Category matching requires an exact
     * category name. Both comparisons ignore capitalization. Empty search
     * values mean no filter.
     *
     * @param keyword
     *            the keyword to search for
     * @param category
     *            the category to filter by
     * @return a list of matching available items
     * @throws IllegalArgumentException
     *             if a search value is null, too long, or contains invalid
     *             characters
     */
    public synchronized
        ArrayList<ItemSnapshot>
        searchItems(String keyword, String category)
    {

        String cleanKeyword = checkText(keyword, "Keyword", 0, 200);

        String cleanCategory = checkText(category, "Category filter", 0, 30);

        ArrayList<ItemSnapshot> matches = new ArrayList<ItemSnapshot>();

        for (Item item : items)
        {
            boolean textMatches = cleanKeyword.isEmpty()
                || containsIgnoreCase(item.name, cleanKeyword)
                || containsIgnoreCase(item.description, cleanKeyword);

            boolean categoryMatches = cleanCategory.isEmpty()
                || item.category.equalsIgnoreCase(cleanCategory);

            if (!item.claimed && textMatches && categoryMatches)
            {

                matches.add(item.snapshot());
            }
        }

        return matches;
    }


    /**
     * Returns a snapshot of an item with the given ID. Claimed items may still
     * be looked up, even though they are excluded from available-item searches.
     *
     * @param itemId
     *            the item's unique ID
     * @return a snapshot of the matching item
     * @throws IllegalArgumentException
     *             if the ID is invalid
     */
    public synchronized ItemSnapshot getItemSnapshot(int itemId)
    {

        return findItem(itemId).snapshot();
    }


    /**
     * Adds a student to the back of an item's waitlist. Students are served in
     * FIFO order. If the queue was empty, the new student becomes the front and
     * receives an active offer deadline. Duplicate names are rejected
     * regardless of capitalization or surrounding spaces.
     *
     * @param itemId
     *            the ID of the requested item
     * @param studentName
     *            the student's name
     * @param nowMillis
     *            the current time in milliseconds
     * @throws IllegalArgumentException
     *             if the ID, name, or time is invalid, or the name is a
     *             duplicate
     * @throws IllegalStateException
     *             if the item is claimed
     */
    public synchronized
        void
        joinWaitlist(int itemId, String studentName, long nowMillis)
    {

        Item item = findItem(itemId);

        String cleanName = checkText(studentName, "Student name", 1, 60);

        checkTime(nowMillis);
        requireAvailable(item);

        expireItem(item, nowMillis);

        for (String existing : item.waitlist.toList())
        {
            if (existing.equalsIgnoreCase(cleanName))
            {
                throw new IllegalArgumentException(
                    "That student is already waiting for this item.");
            }
        }

        boolean wasEmpty = item.waitlist.isEmpty();

        item.waitlist.enqueue(cleanName);

        if (wasEmpty)
        {
            item.offerDeadlineMillis = deadlineAfter(nowMillis);
        }
    }


    /**
     * Returns the student currently at the front of an item's waitlist. This
     * method does not remove the student.
     *
     * @param itemId
     *            the item's unique ID
     * @return the next student's name, or null if empty
     * @throws IllegalArgumentException
     *             if the ID is invalid
     * @throws IllegalStateException
     *             if the item is claimed
     */
    public synchronized String peekNext(int itemId)
    {
        Item item = findItem(itemId);

        requireAvailable(item);

        if (item.waitlist.isEmpty())
        {
            return null;
        }

        return item.waitlist.peek();
    }


    /**
     * Removes the current front student when they pass. Expired offers are
     * processed before the requested student is checked. A stale name cannot
     * remove the student who has since moved to the front. The next student
     * receives a new full offer window.
     *
     * @param itemId
     *            the item's unique ID
     * @param expectedFront
     *            the name of the current front student
     * @param nowMillis
     *            the current time in milliseconds
     * @return the name of the student who passed
     * @throws IllegalArgumentException
     *             if an argument is invalid
     * @throws IllegalStateException
     *             if the queue is empty, the item is claimed, or the offer is
     *             stale
     */
    public synchronized
        String
        passStudent(int itemId, String expectedFront, long nowMillis)
    {

        Item item = findItem(itemId);

        String expected = checkText(expectedFront, "Expected student", 1, 60);

        checkTime(nowMillis);
        requireAvailable(item);

        expireItem(item, nowMillis);
        checkExpectedFront(item, expected);

        String removed = item.waitlist.dequeue();

        if (item.waitlist.isEmpty())
        {
            item.offerDeadlineMillis = 0;
        }
        else
        {
            item.offerDeadlineMillis = deadlineAfter(nowMillis);
        }

        return removed;
    }


    /**
     * Marks an item as claimed by its current front student. Expired offers are
     * processed before accepting the claim. The item becomes unavailable and
     * its active deadline is cleared. The waiting list is retained for possible
     * future no-show handling, which is not implemented yet.
     *
     * @param itemId
     *            the item's unique ID
     * @param expectedFront
     *            the expected front student's name
     * @param nowMillis
     *            the current time in milliseconds
     * @return the name of the student who claimed the item
     * @throws IllegalArgumentException
     *             if an argument is invalid
     * @throws IllegalStateException
     *             if the queue is empty, the item is claimed, or the offer is
     *             stale
     */
    public synchronized
        String
        claimItem(int itemId, String expectedFront, long nowMillis)
    {

        Item item = findItem(itemId);

        String expected = checkText(expectedFront, "Expected student", 1, 60);

        checkTime(nowMillis);
        requireAvailable(item);

        expireItem(item, nowMillis);
        checkExpectedFront(item, expected);

        item.claimed = true;
        item.offerDeadlineMillis = 0;

        return item.waitlist.peek();
    }


    /**
     * Removes expired front students from all available items' waitlists. If
     * several offer windows have passed, the method can expire multiple
     * students during one call.
     *
     * @param nowMillis
     *            the current time in milliseconds
     * @return the total number of expired students removed
     * @throws IllegalArgumentException
     *             if time is negative
     */
    public synchronized int expireOffers(long nowMillis)
    {
        checkTime(nowMillis);

        int expired = 0;

        for (Item item : items)
        {
            if (!item.claimed)
            {
                expired += expireItem(item, nowMillis);
            }
        }

        return expired;
    }


    /**
     * Finds an item using its permanent ID.
     *
     * @param itemId
     *            the item ID to locate
     * @return the matching Item object
     * @throws IllegalArgumentException
     *             if the ID is unknown
     */
    private Item findItem(int itemId)
    {
        if (itemId <= 0)
        {
            throw new IllegalArgumentException("Unknown item ID: " + itemId);
        }

        for (Item item : items)
        {
            if (item.id == itemId)
            {
                return item;
            }
        }

        throw new IllegalArgumentException("Unknown item ID: " + itemId);
    }


    /**
     * Checks whether an item is still available.
     *
     * @param item
     *            the item being checked
     * @throws IllegalStateException
     *             if the item is claimed
     */
    private void requireAvailable(Item item)
    {
        if (item.claimed)
        {
            throw new IllegalStateException(
                "Item unavailable: already claimed.");
        }
    }


    /**
     * Ensures that a pass or claim refers to the student currently at the front
     * of the queue.
     *
     * @param item
     *            the item being processed
     * @param expected
     *            the expected student's name
     * @throws IllegalStateException
     *             if the waitlist is empty or the expected name is no longer at
     *             the front
     */
    private void checkExpectedFront(Item item, String expected)
    {

        if (item.waitlist.isEmpty())
        {
            throw new IllegalStateException("Nobody is waiting.");
        }

        if (!item.waitlist.peek().equalsIgnoreCase(expected))
        {
            throw new IllegalStateException(
                "Offer expired or changed. Check who is next.");
        }
    }


    /**
     * Processes expired offers for one item. When a deadline passes, the front
     * student is removed. The next deadline is based on the previous deadline
     * so delayed timer checks do not give expired requests extra time.
     *
     * @param item
     *            the item whose queue is checked
     * @param nowMillis
     *            the current time
     * @return the number of students removed
     */
    private int expireItem(Item item, long nowMillis)
    {
        int count = 0;

        while (!item.waitlist.isEmpty() && item.offerDeadlineMillis > 0
            && nowMillis >= item.offerDeadlineMillis)
        {

            long oldDeadline = item.offerDeadlineMillis;

            item.waitlist.dequeue();
            count++;

            if (item.waitlist.isEmpty())
            {
                item.offerDeadlineMillis = 0;
            }
            else
            {
                item.offerDeadlineMillis = deadlineAfter(oldDeadline);
            }
        }

        return count;
    }


    /**
     * Calculates a student's offer deadline.
     *
     * @param startTime
     *            the beginning of the offer
     * @return the deadline in milliseconds
     */
    private long deadlineAfter(long startTime)
    {
        if (startTime > Long.MAX_VALUE - offerWindowMillis)
        {
            return Long.MAX_VALUE;
        }

        return startTime + offerWindowMillis;
    }


    /**
     * Validates a supplied time value.
     *
     * @param nowMillis
     *            the time being checked
     * @throws IllegalArgumentException
     *             if time is negative
     */
    private void checkTime(long nowMillis)
    {
        if (nowMillis < 0)
        {
            throw new IllegalArgumentException("Time cannot be negative.");
        }
    }


    /**
     * Checks whether one string contains another, ignoring capitalization.
     *
     * @param text
     *            the original string
     * @param search
     *            the search text
     * @return true if text contains search; false otherwise
     */
    private boolean containsIgnoreCase(String text, String search)
    {

        return text.toLowerCase(Locale.ROOT)
            .contains(search.toLowerCase(Locale.ROOT));
    }


    /**
     * Validates text and removes surrounding spaces. Control characters are
     * checked before trimming, so invalid characters cannot be silently
     * removed.
     *
     * @param value
     *            the original text
     * @param field
     *            the field name used in error messages
     * @param min
     *            the minimum permitted length
     * @param max
     *            the maximum permitted length
     * @return the validated and trimmed text
     * @throws IllegalArgumentException
     *             if text is null, too short, too long, or contains invalid
     *             characters
     */
    private String checkText(String value, String field, int min, int max)
    {

        if (value == null)
        {
            throw new IllegalArgumentException(field + " cannot be null.");
        }

        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);

            if (Character.isISOControl(c) || Character.isSurrogate(c)
                || Character.getType(c) == Character.FORMAT)
            {

                throw new IllegalArgumentException(
                    field + " has an invalid character.");
            }
        }

        String clean = value.trim();

        if (clean.length() < min || clean.length() > max)
        {
            throw new IllegalArgumentException(
                field + " must have " + min + " to " + max + " characters.");
        }

        return clean;
    }
}
