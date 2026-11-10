import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

/**
 * Tests interactions between ReHomeController, ConsoleView, and ReHomeModel.
 * Simulated console input is used so the tests can run without a person typing
 * commands.
 */
public class ReHomeControllerTest
{

    /**
     * Tests a complete add, join, pass, and claim interaction using the console
     * menu.
     */
    @Test
    public void testEndToEndMenu()
    {
        String input =
            "1\nDesk Lamp\nWorking\nDorm\n" + "4\n1\nLiam\n" + "4\n1\nLuc\n"
                + "5\n1\n" + "6\n1\nLiam\n" + "7\n1\nLuc\n" + "2\n0\n";

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        ConsoleView view =
            new ConsoleView(new Scanner(input), new PrintStream(result));

        ReHomeModel model = new ReHomeModel();

        ReHomeController controller = new ReHomeController(model, view);

        controller.run();

        String output = result.toString();

        assertTrue(output.contains("Added item #1"));
        assertTrue(output.contains("Position: 2"));
        assertTrue(output.contains("Next student: Liam"));
        assertTrue(output.contains("Liam passed"));
        assertTrue(output.contains("claimed by Luc"));
        assertTrue(output.contains("No available items"));

        assertTrue(model.getItemSnapshot(1).isClaimed());
    }


    /**
     * Tests recovery from non-numeric input, invalid menu selections, invalid
     * IDs, and blank names.
     */
    @Test
    public void testBadInputRecovery()
    {
        String input = "letters\n99\n" + "1\n \n\nDorm\n"
            + "1\nBook\nGood\nBooks\n" + "4\nabc\n999\nLiam\n" + "4\n1\n  \n"
            + "5\n1\n" + "6\n1\nLiam\n" + "0\n";

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        ReHomeModel model = new ReHomeModel();

        ConsoleView view =
            new ConsoleView(new Scanner(input), new PrintStream(result));

        ReHomeController controller = new ReHomeController(model, view);

        controller.run();

        String output = result.toString();

        assertTrue(output.contains("Enter a whole number"));

        assertTrue(output.contains("Choose a menu option"));

        assertTrue(output.contains("Item name must have"));

        assertTrue(output.contains("Unknown item ID: 999"));

        assertTrue(output.contains("Student name must have"));

        assertTrue(output.contains("Nobody is waiting"));

        assertEquals(1, model.getAvailableItems().size());

        assertEquals("Book", model.getItemSnapshot(1).getName());
    }


    /**
     * Tests controller methods directly, including successful searches, empty
     * search results, and operations on empty waitlists.
     */
    @Test
    public void testControllerMethodsAndSearch()
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        ReHomeModel model = new ReHomeModel();

        ConsoleView view =
            new ConsoleView(new Scanner(""), new PrintStream(result));

        ReHomeController controller = new ReHomeController(model, view);

        controller.onAddItem("Lamp", "Great light", "Dorm");

        controller.onAddItem("Textbook", "Algebra", "Books");

        controller.onSearch("lamp", "dorm");
        controller.onSearch("not found", "");

        controller.onShowNext(1);
        controller.onPass(1, "No one");
        controller.onQuit();

        String output = result.toString();

        assertTrue(output.contains("#1 - Lamp"));
        assertTrue(output.contains("No matching items"));
        assertTrue(output.contains("Nobody is waiting"));

        assertEquals(2, model.getAvailableItems().size());
    }
}
