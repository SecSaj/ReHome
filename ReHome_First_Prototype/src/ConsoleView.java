import java.io.PrintStream;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Provides the console interface for ReHome. The view displays menus, reads
 * user input, and presents results supplied by the controller. It does not
 * directly access or modify the model's linked queues or item state.
 */
public class ConsoleView
{

    private final Scanner scanner;
    private final PrintStream out;

    /**
     * Creates a console view using keyboard input and standard output.
     */
    public ConsoleView()
    {
        this(new Scanner(System.in), System.out);
    }


    /**
     * Creates a console view with a selected input source and output
     * destination. This constructor also allows controller tests to simulate
     * user input.
     *
     * @param scanner
     *            the input scanner
     * @param out
     *            the output destination
     * @throws IllegalArgumentException
     *             if either argument is null
     */
    public ConsoleView(Scanner scanner, PrintStream out)
    {
        if (scanner == null || out == null)
        {
            throw new IllegalArgumentException(
                "Input and output are required.");
        }

        this.scanner = scanner;
        this.out = out;
    }


    /**
     * Displays the main menu and reads the user's choice. Non-numeric input is
     * rejected and requested again.
     *
     * @return the entered menu number
     * @throws NoSuchElementException
     *             if input has ended
     */
    public int readMenuChoice()
    {
        out.println();
        out.println("=== ReHome (First Prototype) ===");
        out.println("1. Add Item");
        out.println("2. View Available Items");
        out.println("3. Search Items");
        out.println("4. Join Waitlist");
        out.println("5. Show Next Student");
        out.println("6. Pass Student");
        out.println("7. Claim Item");
        out.println("0. Quit");

        return readNumber("Choose: ");
    }


    /**
     * Reads an item's numeric ID.
     *
     * @return the entered item ID
     * @throws NoSuchElementException
     *             if input has ended
     */
    public int readItemId()
    {
        return readNumber("Item ID: ");
    }


    /**
     * Reads an item's name.
     *
     * @return the entered item name
     * @throws NoSuchElementException
     *             if input has ended
     */
    public String readItemName()
    {
        return readLine("Item name: ");
    }


    /**
     * Reads an optional item description.
     *
     * @return the entered description
     * @throws NoSuchElementException
     *             if input has ended
     */
    public String readDescription()
    {
        return readLine("Description (optional): ");
    }


    /**
     * Reads an optional item category.
     *
     * @return the entered category
     * @throws NoSuchElementException
     *             if input has ended
     */
    public String readCategory()
    {
        return readLine("Category (optional): ");
    }


    /**
     * Reads the name of a student joining a waitlist.
     *
     * @return the entered student name
     * @throws NoSuchElementException
     *             if input has ended
     */
    public String readStudentName()
    {
        return readLine("Student name: ");
    }


    /**
     * Reads the student name expected to be at the front of the waitlist.
     *
     * @return the entered front student name
     * @throws NoSuchElementException
     *             if input has ended
     */
    public String readExpectedFront()
    {
        return readLine("Name of student being offered this item: ");
    }


    /**
     * Reads an optional keyword used to search items.
     *
     * @return the entered search keyword
     * @throws NoSuchElementException
     *             if input has ended
     */
    public String readKeyword()
    {
        return readLine("Search keyword (blank for any): ");
    }


    /**
     * Displays the provided item snapshots.
     *
     * @param items
     *            the items to display
     */
    public void showItems(ArrayList<ItemSnapshot> items)
    {
        if (items.isEmpty())
        {
            out.println("No available items or matching items.");
            return;
        }

        for (ItemSnapshot item : items)
        {
            out.println(
                "#" + item.getId() + " - " + item.getName() + " ["
                    + item.getCategory() + "]");

            out.println("  " + item.getDescription());

            out.println("  Waiting: " + item.getWaitingNames().size());
        }
    }


    /**
     * Displays the current front student and their remaining offer time.
     *
     * @param student
     *            the front student's name, or null if the waitlist is empty
     * @param remainingSeconds
     *            the remaining time in seconds
     */
    public void showNext(String student, long remainingSeconds)
    {

        if (student == null)
        {
            out.println("Nobody is waiting.");
        }
        else
        {
            out.println("Next student: " + student);
            out.println(
                "Offer time remaining: " + remainingSeconds + " second(s).");
        }
    }


    /**
     * Displays a normal status message.
     *
     * @param message
     *            the message to display
     */
    public void showMessage(String message)
    {
        out.println(message);
    }


    /**
     * Displays an error message.
     *
     * @param message
     *            the error message
     */
    public void showError(String message)
    {
        out.println("Error: " + message);
    }


    /**
     * Displays a prompt and reads one complete line.
     *
     * @param prompt
     *            the prompt shown to the user
     * @return the entered line
     * @throws NoSuchElementException
     *             if no more input is available
     */
    private String readLine(String prompt)
    {
        out.print(prompt);
        out.flush();

        if (!scanner.hasNextLine())
        {
            throw new NoSuchElementException("Input ended.");
        }

        return scanner.nextLine();
    }


    /**
     * Reads an integer, repeating the prompt if the user enters non-numeric
     * text.
     *
     * @param prompt
     *            the prompt shown to the user
     * @return the valid integer entered
     * @throws NoSuchElementException
     *             if input has ended
     */
    private int readNumber(String prompt)
    {
        while (true)
        {
            String text = readLine(prompt).trim();

            try
            {
                return Integer.parseInt(text);
            }
            catch (NumberFormatException exception)
            {
                showError("Enter a whole number, please.");
            }
        }
    }
}
