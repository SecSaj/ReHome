/**
 * Starts the ReHome first-prototype application. This class creates the model,
 * view, and controller and connects them together.
 */
public class ReHomeApp
{

    /**
     * Creates and starts the ReHome application.
     *
     * @param args
     *            command-line arguments; not used
     */
    public static void main(String[] args)
    {

        ReHomeModel model = new ReHomeModel();

        ConsoleView view = new ConsoleView();

        ReHomeController controller = new ReHomeController(model, view);

        controller.run();
    }
}
