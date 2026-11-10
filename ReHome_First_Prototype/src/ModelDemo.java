/**
 * Demonstrates the ReHome model independently from the controller and console
 * view. This driver uses a short offer duration to demonstrate expiration
 * without waiting five minutes.
 */
public class ModelDemo
{

    /**
     * Demonstrates joining, expiration, and claiming.
     *
     * @param args
     *            command-line arguments; not used
     */
    public static void main(String[] args)
    {

        ReHomeModel model = new ReHomeModel(1000L);

        int lampId = model.addItem("Desk Lamp", "Working lamp", "Dorm");

        model.joinWaitlist(lampId, "Liam", 10000L);
        model.joinWaitlist(lampId, "Luc", 10001L);

        System.out.println("First: " + model.peekNext(lampId));

        System.out.println("Expired by t=11000: " + model.expireOffers(11000L));

        System.out.println("Now first: " + model.peekNext(lampId));

        System.out
            .println("Claimed by: " + model.claimItem(lampId, "Luc", 11500L));

        System.out.println(
            "Available items after claim: " + model.getAvailableItems().size());
    }
}
