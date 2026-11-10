import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Tests the ReHomeModel without using a controller or view. These tests verify
 * listing management, searching, waitlist behavior, input validation, and timed
 * offers. Fixed timestamps are used so the tests do not need to wait for an
 * actual timer.
 */
public class ReHomeModelTest
{

    /**
     * Tests the default five-minute offer duration and an initially empty item
     * catalog.
     */
    @Test
    public void testDefaultWindowAndEmptyCatalog()
    {
        ReHomeModel model = new ReHomeModel();

        assertTrue(model.getAvailableItems().isEmpty());

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);

        assertEquals(
            310000L,
            model.getItemSnapshot(id).getOfferDeadlineMillis());
    }


    /**
     * Tests a custom offer duration and rejects zero or negative durations.
     */
    @Test
    public void testCustomWindowAndBadDuration()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);

        assertEquals(
            11000L,
            model.getItemSnapshot(id).getOfferDeadlineMillis());

        try
        {
            new ReHomeModel(0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("Liam", model.peekNext(id));
        }

        try
        {
            new ReHomeModel(-1);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("Liam", model.peekNext(id));
        }
    }


    /**
     * Tests item validation and verifies that an invalid item does not consume
     * an ID.
     */
    @Test
    public void testAddItemValidationAndStableIds()
    {
        ReHomeModel model = new ReHomeModel();

        try
        {
            model.addItem("  ", "", "Dorm");
            fail("Blank name should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertTrue(model.getAvailableItems().isEmpty());
        }

        try
        {
            model.addItem("Lamp", repeat('x', 201), "Dorm");

            fail("Long description should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertTrue(model.getAvailableItems().isEmpty());
        }

        try
        {
            model.addItem("Lamp", "OK", repeat('x', 31));

            fail("Long category should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertTrue(model.getAvailableItems().isEmpty());
        }

        assertEquals(1, model.addItem(" Lamp ", " ", "Dorm"));

        assertEquals(2, model.addItem("Book", "Calculus", "Books"));

        assertEquals("Lamp", model.getItemSnapshot(1).getName());

        assertEquals("", model.getItemSnapshot(1).getDescription());
    }


    /**
     * Tests keyword and category searches. Searches must ignore capitalization
     * and preserve the original item IDs.
     */
    @Test
    public void testSearchKeywordAndCategory()
    {
        ReHomeModel model = new ReHomeModel();

        model.addItem("Desk Lamp", "Bright light", "Dorm");

        model.addItem("Textbook", "Lamp illustrations", "Books");

        model.addItem("Mini Fridge", "Good condition", "Dorm");

        assertEquals(1, model.searchItems("LaMp", "dOrM").size());

        assertEquals(1, model.searchItems("lamp", "Dorm").get(0).getId());

        assertEquals(2, model.searchItems("lamp", "").size());

        assertEquals(0, model.searchItems("lamp", "Food").size());

        assertEquals(3, model.getAvailableItems().size());
    }


    /**
     * Tests that invalid search filters and item IDs are rejected without
     * changing existing items.
     */
    @Test
    public void testBadSearchAndUnknownId()
    {
        ReHomeModel model = new ReHomeModel();

        model.addItem("Lamp", "", "Dorm");

        try
        {
            model.searchItems(null, "");
            fail("Null keyword should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals(1, model.getAvailableItems().size());
        }

        try
        {
            model.searchItems("", "Dorm\nOther");
            fail("Control character should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals(1, model.getAvailableItems().size());
        }

        try
        {
            model.getItemSnapshot(999);
            fail("Unknown ID should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("Lamp", model.getItemSnapshot(1).getName());
        }
    }


    /**
     * Tests that different items have independent queues and that an empty
     * queue has no front student.
     */
    @Test
    public void testSeparateQueuesAndPeekEmpty()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int lamp = model.addItem("Lamp", "", "");
        int book = model.addItem("Book", "", "");

        assertNull(model.peekNext(lamp));

        model.joinWaitlist(lamp, "Luc", 10000L);
        model.joinWaitlist(book, "Sajjad", 10000L);

        assertEquals("Luc", model.peekNext(lamp));
        assertEquals("Sajjad", model.peekNext(book));

        assertEquals(
            Arrays.asList("Luc"),
            model.getItemSnapshot(lamp).getWaitingNames());
    }


    /**
     * Tests that duplicate names are rejected without changing the original
     * queue order.
     */
    @Test
    public void testDuplicateNameDoesNotChangeQueue()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);
        model.joinWaitlist(id, "Luc", 10001L);

        try
        {
            model.joinWaitlist(id, " liAM ", 10002L);
            fail("Duplicate should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals(
                Arrays.asList("Liam", "Luc"),
                model.getItemSnapshot(id).getWaitingNames());

            assertEquals(
                11000L,
                model.getItemSnapshot(id).getOfferDeadlineMillis());
        }
    }


    /**
     * Tests blank, overlong, null, and invalid student names. Rejected names
     * must leave the queue empty.
     */
    @Test
    public void testInvalidStudentNamesLeaveQueueUnchanged()
    {
        ReHomeModel model = new ReHomeModel();

        int id = model.addItem("Lamp", "", "");

        String[] badNames = { "", "  ", repeat('a', 61), "Name\nOther", null };

        for (String name : badNames)
        {
            try
            {
                model.joinWaitlist(id, name, 100L);
                fail("Invalid name should fail");
            }
            catch (IllegalArgumentException expected)
            {
                assertNull(model.peekNext(id));

                assertEquals(
                    0L,
                    model.getItemSnapshot(id).getOfferDeadlineMillis());
            }
        }
    }


    /**
     * Tests that null text and control characters cannot be used to create an
     * item.
     */
    @Test
    public void testInvalidItemCharacters()
    {
        ReHomeModel model = new ReHomeModel();

        try
        {
            model.addItem("Lamp\u0007", "", "");
            fail("Control character should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals(0, model.getAvailableItems().size());
        }

        try
        {
            model.addItem(null, "", "");
            fail("Null item name should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals(1, model.addItem("Lamp", "", ""));
        }
    }


    /**
     * Tests that passing removes the current front and gives the next student a
     * full offer window.
     */
    @Test
    public void testPassStudentGivesNextFullWindow()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);
        model.joinWaitlist(id, "Luc", 10001L);

        assertEquals("Liam", model.passStudent(id, "Liam", 10500L));

        assertEquals("Luc", model.peekNext(id));

        assertEquals(
            11500L,
            model.getItemSnapshot(id).getOfferDeadlineMillis());

        assertEquals("Luc", model.passStudent(id, "Luc", 11000L));

        assertNull(model.peekNext(id));

        assertEquals(0L, model.getItemSnapshot(id).getOfferDeadlineMillis());
    }


    /**
     * Tests that an incorrect expected-front name cannot remove the actual
     * front student.
     */
    @Test
    public void testWrongExpectedFrontIsRejected()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);

        try
        {
            model.passStudent(id, "Someone Else", 10500L);

            fail("Wrong front should fail");
        }
        catch (IllegalStateException expected)
        {
            assertEquals("Liam", model.peekNext(id));

            assertEquals(
                11000L,
                model.getItemSnapshot(id).getOfferDeadlineMillis());
        }
    }


    /**
     * Tests that passing or claiming an item with an empty waitlist produces an
     * appropriate error.
     */
    @Test
    public void testEmptyPassAndClaim()
    {
        ReHomeModel model = new ReHomeModel();

        int id = model.addItem("Lamp", "", "");

        try
        {
            model.passStudent(id, "Liam", 100L);
            fail("Empty pass should fail");
        }
        catch (IllegalStateException expected)
        {
            assertNull(model.peekNext(id));
        }

        try
        {
            model.claimItem(id, "Liam", 100L);
            fail("Empty claim should fail");
        }
        catch (IllegalStateException expected)
        {
            assertFalse(model.getItemSnapshot(id).isClaimed());
        }
    }


    /**
     * Tests that claiming an item changes its availability and rejects future
     * waitlist actions.
     */
    @Test
    public void testClaimEndsAvailabilityAndPreservesWaitlist()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "Dorm");

        model.joinWaitlist(id, "Liam", 10000L);
        model.joinWaitlist(id, "Luc", 10001L);

        assertEquals("Liam", model.claimItem(id, "Liam", 10500L));

        assertTrue(model.getItemSnapshot(id).isClaimed());

        assertEquals(0L, model.getItemSnapshot(id).getOfferDeadlineMillis());

        assertEquals(
            Arrays.asList("Liam", "Luc"),
            model.getItemSnapshot(id).getWaitingNames());

        assertTrue(model.getAvailableItems().isEmpty());
        assertTrue(model.searchItems("lamp", "Dorm").isEmpty());

        try
        {
            model.joinWaitlist(id, "Sajjad", 10501L);
            fail("Claimed item should reject joins");
        }
        catch (IllegalStateException expected)
        {
            assertEquals(2, model.getItemSnapshot(id).getWaitingNames().size());
        }

        try
        {
            model.passStudent(id, "Liam", 10501L);
            fail("Claimed item should reject passes");
        }
        catch (IllegalStateException expected)
        {
            assertTrue(model.getItemSnapshot(id).isClaimed());
        }
    }


    /**
     * Tests that an offer expires exactly at its deadline but not one
     * millisecond earlier.
     */
    @Test
    public void testExpireAtBoundaryAndToEmpty()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);
        model.joinWaitlist(id, "Luc", 10001L);

        assertEquals(0, model.expireOffers(10999L));
        assertEquals("Liam", model.peekNext(id));

        assertEquals(1, model.expireOffers(11000L));
        assertEquals("Luc", model.peekNext(id));

        assertEquals(
            12000L,
            model.getItemSnapshot(id).getOfferDeadlineMillis());

        assertEquals(1, model.expireOffers(12000L));

        assertNull(model.peekNext(id));

        assertEquals(0L, model.getItemSnapshot(id).getOfferDeadlineMillis());
    }


    /**
     * Tests multiple expired offers and confirms that different items expire
     * independently.
     */
    @Test
    public void testExpireCatchUpAndIndependentItems()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int lamp = model.addItem("Lamp", "", "");
        int book = model.addItem("Book", "", "");

        model.joinWaitlist(lamp, "A", 10000L);
        model.joinWaitlist(lamp, "B", 10000L);
        model.joinWaitlist(lamp, "C", 10000L);
        model.joinWaitlist(book, "D", 11500L);

        assertEquals(2, model.expireOffers(12000L));

        assertEquals("C", model.peekNext(lamp));

        assertEquals(
            13000L,
            model.getItemSnapshot(lamp).getOfferDeadlineMillis());

        assertEquals("D", model.peekNext(book));

        assertEquals(2, model.expireOffers(13000L));

        assertNull(model.peekNext(lamp));
        assertNull(model.peekNext(book));
    }


    /**
     * Tests that an expired student cannot claim another student's active
     * offer.
     */
    @Test
    public void testLateClaimIsStale()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);
        model.joinWaitlist(id, "Luc", 10000L);

        try
        {
            model.claimItem(id, "Liam", 11000L);

            fail("Expired student should not claim");
        }
        catch (IllegalStateException expected)
        {
            assertFalse(model.getItemSnapshot(id).isClaimed());

            assertEquals("Luc", model.peekNext(id));
        }

        assertEquals("Luc", model.claimItem(id, "Luc", 11500L));
    }


    /**
     * Tests that invalid negative timestamps do not change the active offer.
     */
    @Test
    public void testInvalidClock()
    {
        ReHomeModel model = new ReHomeModel(1000L);

        int id = model.addItem("Lamp", "", "");

        model.joinWaitlist(id, "Liam", 10000L);

        try
        {
            model.expireOffers(-1L);
            fail("Negative time should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("Liam", model.peekNext(id));
        }

        try
        {
            model.passStudent(id, "Liam", -1L);
            fail("Negative time should fail");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals(
                11000L,
                model.getItemSnapshot(id).getOfferDeadlineMillis());
        }
    }


    /**
     * Creates a string containing a repeated character. Used to test maximum
     * input lengths.
     *
     * @param c
     *            the character to repeat
     * @param count
     *            the number of repetitions
     * @return the generated string
     */
    private String repeat(char c, int count)
    {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < count; i++)
        {
            builder.append(c);
        }

        return builder.toString();
    }
}
