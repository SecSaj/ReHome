import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Connects the ReHome console view to the model. The controller responds to
 * menu selections, calls model methods, and displays the results. It also owns
 * a timer that periodically checks whether waiting students' offers have
 * expired. The model determines which offers expire.
 */
public class ReHomeController
{

    private final ReHomeModel model;
    private final ConsoleView view;

    private Timer expiryTimer;
    private boolean running;

    /**
     * Creates a controller with its model and view.
     *
     * @param model
     *            the ReHome model
     * @param view
     *            the console interface
     * @throws IllegalArgumentException
     *             if either dependency is null
     */
    public ReHomeController(ReHomeModel model, ConsoleView view)
    {

        if (model == null || view == null)
        {
            throw new IllegalArgumentException(
                "A model and view are required.");
        }

        this.model = model;
        this.view = view;
    }


    /**
     * Starts the timer and displays the main menu. Menu selections are
     * processed until the user quits or the input source is exhausted. The
     * timer checks for expired offers every second, even when the console is
     * waiting for user input.
     */
    public void run()
    {
        running = true;

        expiryTimer = new Timer("ReHome offer timer", true);

        expiryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                model.expireOffers(System.currentTimeMillis());
            }
        }, 1000L, 1000L);

        try
        {
            while (running)
            {
                int choice = view.readMenuChoice();

                switch (choice)
                {
                    case 0:
                        onQuit();
                        break;

                    case 1:
                        onAddItem(
                            view.readItemName(),
                            view.readDescription(),
                            view.readCategory());
                        break;

                    case 2:
                        onViewItems();
                        break;

                    case 3:
                        onSearch(view.readKeyword(), view.readCategory());
                        break;

                    case 4:
                        onJoin(view.readItemId(), view.readStudentName());
                        break;

                    case 5:
                        onShowNext(view.readItemId());
                        break;

                    case 6:
                        onPass(view.readItemId(), view.readExpectedFront());
                        break;

                    case 7:
                        onClaim(view.readItemId(), view.readExpectedFront());
                        break;

                    default:
                        view.showError("Choose a menu option from 0 to 7.");
                        break;
                }
            }
        }
        catch (NoSuchElementException exception)
        {
            view.showMessage("Input ended. Exiting ReHome.");
        }
        finally
        {
            onQuit();
        }
    }


    /**
     * Attempts to add a donated item. The model validates the provided
     * information. Any validation error is displayed to the user.
     *
     * @param name
     *            the item name
     * @param description
     *            the item description
     * @param category
     *            the item category
     */
    public void onAddItem(String name, String description, String category)
    {

        try
        {
            int id = model.addItem(name, description, category);

            view.showMessage("Added item #" + id + ".");
        }
        catch (IllegalArgumentException exception)
        {
            view.showError(exception.getMessage());
        }
    }


    /**
     * Displays all currently available items. Expired offers are checked before
     * displaying the latest item information.
     */
    public void onViewItems()
    {
        model.expireOffers(System.currentTimeMillis());

        view.showItems(model.getAvailableItems());
    }


    /**
     * Searches the catalog using a keyword and category.
     *
     * @param keyword
     *            the search keyword
     * @param category
     *            the optional category filter
     */
    public void onSearch(String keyword, String category)
    {

        try
        {
            model.expireOffers(System.currentTimeMillis());

            ArrayList<ItemSnapshot> matches =
                model.searchItems(keyword, category);

            if (matches.isEmpty())
            {
                view.showMessage("No matching items.");
            }
            else
            {
                view.showItems(matches);
            }
        }
        catch (IllegalArgumentException exception)
        {
            view.showError(exception.getMessage());
        }
    }


    /**
     * Adds a student to an item's waitlist. The student is added at the back of
     * the queue. The controller then displays the current front.
     *
     * @param itemId
     *            the item's unique ID
     * @param studentName
     *            the student's name
     */
    public void onJoin(int itemId, String studentName)
    {

        try
        {
            model.joinWaitlist(itemId, studentName, System.currentTimeMillis());

            ItemSnapshot snapshot = model.getItemSnapshot(itemId);

            view.showMessage(
                "Joined item #" + itemId + ". Position: "
                    + snapshot.getWaitingNames().size() + ".");

            onShowNext(itemId);
        }
        catch (IllegalArgumentException exception)
        {
            view.showError(exception.getMessage());
        }
        catch (IllegalStateException exception)
        {
            view.showError(exception.getMessage());
        }
    }


    /**
     * Displays the student currently at the front of an item's queue. If a
     * student is waiting, the remaining offer time is displayed in seconds.
     *
     * @param itemId
     *            the item's unique ID
     */
    public void onShowNext(int itemId)
    {
        try
        {
            long now = System.currentTimeMillis();

            model.expireOffers(now);

            String front = model.peekNext(itemId);

            if (front == null)
            {
                view.showNext(null, 0);
            }
            else
            {
                long deadline =
                    model.getItemSnapshot(itemId).getOfferDeadlineMillis();

                long millisecondsLeft = Math.max(0L, deadline - now);

                long secondsLeft = millisecondsLeft / 1000L;

                if (millisecondsLeft % 1000L != 0)
                {
                    secondsLeft++;
                }

                view.showNext(front, secondsLeft);
            }
        }
        catch (IllegalArgumentException exception)
        {
            view.showError(exception.getMessage());
        }
        catch (IllegalStateException exception)
        {
            view.showError(exception.getMessage());
        }
    }


    /**
     * Records that the current front student has passed. The model validates
     * that the supplied student is still the actual front before removing them.
     *
     * @param itemId
     *            the item's unique ID
     * @param expectedFront
     *            the expected front student's name
     */
    public void onPass(int itemId, String expectedFront)
    {

        try
        {
            String passed = model
                .passStudent(itemId, expectedFront, System.currentTimeMillis());

            view.showMessage(passed + " passed on item #" + itemId + ".");

            onShowNext(itemId);
        }
        catch (IllegalArgumentException exception)
        {
            view.showError(exception.getMessage());
        }
        catch (IllegalStateException exception)
        {
            view.showError(exception.getMessage());
        }
    }


    /**
     * Records a successful claim by the current front student. A valid claim
     * marks the item unavailable.
     *
     * @param itemId
     *            the item's unique ID
     * @param expectedFront
     *            the expected front student's name
     */
    public void onClaim(int itemId, String expectedFront)
    {

        try
        {
            String claimant = model
                .claimItem(itemId, expectedFront, System.currentTimeMillis());

            view.showMessage(
                "Item #" + itemId + " claimed by " + claimant + ".");
        }
        catch (IllegalArgumentException exception)
        {
            view.showError(exception.getMessage());
        }
        catch (IllegalStateException exception)
        {
            view.showError(exception.getMessage());
        }
    }


    /**
     * Ends the menu loop and stops the expiration timer. Calling this method
     * more than once is safe.
     */
    public void onQuit()
    {
        running = false;

        if (expiryTimer != null)
        {
            expiryTimer.cancel();
            expiryTimer = null;
        }
    }
}
