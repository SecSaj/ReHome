import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests the ItemSnapshot class. The tests verify that item fields are copied
 * correctly and that returned lists cannot change the actual model's waitlist.
 */
public class ItemSnapshotTest
{

    /**
     * Tests all snapshot getters and verifies that listing information is
     * correctly trimmed.
     */
    @Test
    public void testGettersAndTrimming()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem(" Desk Lamp ", " Bright ", " Dorm ");

        ItemSnapshot snapshot = model.getItemSnapshot(id);

        assertEquals(1, snapshot.getId());
        assertEquals("Desk Lamp", snapshot.getName());
        assertEquals("Bright", snapshot.getDescription());
        assertEquals("Dorm", snapshot.getCategory());
        assertFalse(snapshot.isClaimed());

        assertEquals(0L, snapshot.getOfferDeadlineMillis());

        assertTrue(snapshot.getWaitingNames().isEmpty());
    }


    /**
     * Tests that removing names from a returned list does not modify the real
     * waitlist.
     */
    @Test
    public void testWaitingNamesIsARealCopy()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);
        model.joinWaitlist(id, "Luc", 10001L);

        ItemSnapshot snapshot = model.getItemSnapshot(id);

        ArrayList<String> copy = snapshot.getWaitingNames();

        copy.clear();

        assertEquals(Arrays.asList("Liam", "Luc"), snapshot.getWaitingNames());

        assertEquals(
            Arrays.asList("Liam", "Luc"),
            model.getItemSnapshot(id).getWaitingNames());
    }


    /**
     * Tests that an existing snapshot remains unchanged after the model is
     * updated.
     */
    @Test
    public void testSnapshotDoesNotChangeWithModel()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);
        model.joinWaitlist(id, "Luc", 10000L);

        ItemSnapshot before = model.getItemSnapshot(id);

        model.passStudent(id, "Liam", 10500L);
        model.claimItem(id, "Luc", 11000L);

        assertFalse(before.isClaimed());

        assertEquals(11000L, before.getOfferDeadlineMillis());

        assertEquals(Arrays.asList("Liam", "Luc"), before.getWaitingNames());

        assertTrue(model.getItemSnapshot(id).isClaimed());
    }
}
