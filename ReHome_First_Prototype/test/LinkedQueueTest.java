import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

/**
 * Tests the custom singly linked FIFO queue. These tests verify enqueue,
 * dequeue, peek, isEmpty, and toList, including empty and single-element queue
 * cases.
 */
public class LinkedQueueTest
{

    /**
     * Tests that a new queue begins empty.
     */
    @Test
    public void testNewQueueEmpty()
    {
        LinkedQueue<String> queue = new LinkedQueue<String>();

        assertTrue(queue.isEmpty());
        assertEquals(new ArrayList<String>(), queue.toList());
    }


    /**
     * Tests that enqueue preserves FIFO order and peek does not remove the
     * first element.
     */
    @Test
    public void testEnqueueAndPeek()
    {
        LinkedQueue<String> queue = new LinkedQueue<String>();

        queue.enqueue("Liam");
        queue.enqueue("Luc");
        queue.enqueue("Sajjad");

        assertEquals("Liam", queue.peek());
        assertEquals("Liam", queue.peek());

        assertEquals(Arrays.asList("Liam", "Luc", "Sajjad"), queue.toList());

        assertFalse(queue.isEmpty());
    }


    /**
     * Tests that dequeue removes the oldest element and advances the head to
     * the next element.
     */
    @Test
    public void testDequeueFirstOfSeveral()
    {
        LinkedQueue<String> queue = new LinkedQueue<String>();

        queue.enqueue("A");
        queue.enqueue("B");

        assertEquals("A", queue.dequeue());
        assertEquals("B", queue.peek());

        assertEquals(Arrays.asList("B"), queue.toList());
    }


    /**
     * Tests that removing the only node empties the queue and allows another
     * element to be added.
     */
    @Test
    public void testRemoveLastAndReuse()
    {
        LinkedQueue<String> queue = new LinkedQueue<String>();

        queue.enqueue("First");

        assertEquals("First", queue.dequeue());
        assertTrue(queue.isEmpty());

        queue.enqueue("Second");

        assertEquals("Second", queue.peek());
        assertEquals("Second", queue.dequeue());
        assertTrue(queue.isEmpty());
    }


    /**
     * Tests that dequeue on an empty queue throws an exception without
     * corrupting the queue.
     */
    @Test
    public void testDequeueEmpty()
    {
        LinkedQueue<String> queue = new LinkedQueue<String>();

        try
        {
            queue.dequeue();
            fail("Expected NoSuchElementException");
        }
        catch (NoSuchElementException expected)
        {
            assertTrue(queue.isEmpty());
        }

        queue.enqueue("Still usable");
        assertEquals("Still usable", queue.peek());
    }


    /**
     * Tests that peek on an empty queue throws the expected exception.
     */
    @Test
    public void testPeekEmpty()
    {
        LinkedQueue<String> queue = new LinkedQueue<String>();

        try
        {
            queue.peek();
            fail("Expected NoSuchElementException");
        }
        catch (NoSuchElementException expected)
        {
            assertTrue(queue.isEmpty());
        }
    }


    /**
     * Tests that null cannot be enqueued and existing queue elements remain
     * unchanged.
     */
    @Test
    public void testNullDoesNotChangeQueue()
    {
        LinkedQueue<String> queue = new LinkedQueue<String>();

        queue.enqueue("A");

        try
        {
            queue.enqueue(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals(Arrays.asList("A"), queue.toList());
        }
    }


    /**
     * Tests that changing an ArrayList returned by toList does not change the
     * queue.
     */
    @Test
    public void testToListDefensiveCopy()
    {
        LinkedQueue<String> queue = new LinkedQueue<String>();

        queue.enqueue("A");
        queue.enqueue("B");

        ArrayList<String> copy = queue.toList();
        copy.clear();

        assertEquals(Arrays.asList("A", "B"), queue.toList());
    }


    /**
     * Tests that the generic queue supports types other than student names.
     */
    @Test
    public void testGenericQueue()
    {
        LinkedQueue<Integer> queue = new LinkedQueue<Integer>();

        queue.enqueue(10);
        queue.enqueue(20);

        assertEquals(Integer.valueOf(10), queue.dequeue());

        assertEquals(Integer.valueOf(20), queue.dequeue());

        assertTrue(queue.isEmpty());
    }
}
