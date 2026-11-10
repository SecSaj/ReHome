import java.util.ArrayList;

/**
 * Stores a read-only snapshot of a ReHome item. A snapshot contains copies of
 * item information and waiting student names at one point in time. Modifying a
 * list returned by this class does not modify the actual model or waitlist.
 */
public class ItemSnapshot
{

    private final int id;
    private final String name;
    private final String description;
    private final String category;
    private final boolean claimed;
    private final long offerDeadlineMillis;
    private final ArrayList<String> waitingNames;

    /**
     * Creates a snapshot containing item information.
     *
     * @param id
     *            the item's unique identification number
     * @param name
     *            the item's name
     * @param description
     *            the item's description
     * @param category
     *            the item's category
     * @param claimed
     *            whether the item has been claimed
     * @param offerDeadlineMillis
     *            the active offer deadline, or zero if no offer is active
     * @param waitingNames
     *            the students waiting for the item
     */
    ItemSnapshot(
        int id,
        String name,
        String description,
        String category,
        boolean claimed,
        long offerDeadlineMillis,
        ArrayList<String> waitingNames)
    {

        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.claimed = claimed;
        this.offerDeadlineMillis = offerDeadlineMillis;

        this.waitingNames = new ArrayList<String>(waitingNames);
    }


    /**
     * Returns the item's unique ID.
     *
     * @return the item ID
     */
    public int getId()
    {
        return id;
    }


    /**
     * Returns the item's name.
     *
     * @return the item name
     */
    public String getName()
    {
        return name;
    }


    /**
     * Returns the item's description.
     *
     * @return the item description
     */
    public String getDescription()
    {
        return description;
    }


    /**
     * Returns the item's category.
     *
     * @return the item category
     */
    public String getCategory()
    {
        return category;
    }


    /**
     * Determines whether the item has been claimed.
     *
     * @return true if claimed; false otherwise
     */
    public boolean isClaimed()
    {
        return claimed;
    }


    /**
     * Returns the deadline for the active offer.
     *
     * @return the deadline in milliseconds, or zero if no active offer exists
     */
    public long getOfferDeadlineMillis()
    {
        return offerDeadlineMillis;
    }


    /**
     * Returns a copy of the waiting student names. The students appear in FIFO
     * order.
     *
     * @return a new ArrayList containing waiting names
     */
    public ArrayList<String> getWaitingNames()
    {
        return new ArrayList<String>(waitingNames);
    }
}
