
/**
 * Represents a donated item in the ReHome application. Each item stores its
 * listing information, claim status, waitlist of students, and deadline for the
 * current offer. The ReHomeModel class manages changes to an item's state.
 */
class Item
{

    /**
     * The unique identification number assigned to this item.
     */
    final int id;

    /**
     * The name of the donated item.
     */
    final String name;

    /**
     * A short description of the item's condition or details.
     */
    final String description;

    /**
     * The category that the item belongs to.
     */
    final String category;

    /**
     * The FIFO queue containing student names in the order they requested the
     * item.
     */
    final LinkedQueue<String> waitlist;

    /**
     * Indicates whether a student has claimed this item. True means the item is
     * no longer available.
     */
    boolean claimed;

    /**
     * The expiration time of the current student's offer, measured in
     * milliseconds. A value of zero means no active offer exists.
     */
    long offerDeadlineMillis;

    /**
     * Creates a new item with an empty waiting list. The item starts as
     * available with no active offer.
     *
     * @param id
     *            the item's unique identification number
     * @param name
     *            the name of the donated item
     * @param description
     *            a short description of the item
     * @param category
     *            the category of the item
     */
    Item(int id, String name, String description, String category)
    {

        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;

        waitlist = new LinkedQueue<String>();

        claimed = false;
        offerDeadlineMillis = 0;
    }


    /**
     * Creates a snapshot containing the item's current information and a copy
     * of its waiting list. The snapshot prevents other classes from directly
     * modifying the item's actual queue.
     *
     * @return an ItemSnapshot containing the item's current information
     */
    ItemSnapshot snapshot()
    {
        return new ItemSnapshot(
            id,
            name,
            description,
            category,
            claimed,
            offerDeadlineMillis,
            waitlist.toList());
    }
}
