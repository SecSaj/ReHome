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

    /**
     * Tests removing the first student from a queue.
     */
    @Test
    public void testRemoveFront() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
    
        queue.enqueue("Liam");
        queue.enqueue("Luc");
        queue.enqueue("Tayshaun");
    
        assertTrue(queue.remove("Liam"));
    
        assertEquals("Luc", queue.peek());
        assertEquals(
            Arrays.asList("Luc", "Tayshaun"),
            queue.toList());
    }
    
    /**
     * Tests removing a student from the middle
     * without changing the remaining FIFO order.
     */
    @Test
    public void testRemoveMiddle() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
    
        queue.enqueue("Liam");
        queue.enqueue("Luc");
        queue.enqueue("Tayshaun");
    
        assertTrue(queue.remove("Luc"));
    
        assertEquals(
            Arrays.asList("Liam", "Tayshaun"),
            queue.toList());
    
        assertEquals("Liam", queue.peek());
    }
    
    /**
     * Tests removing the tail and then adding
     * a new student to verify the tail is updated.
     */
    @Test
    public void testRemoveTailAndEnqueue() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
    
        queue.enqueue("Liam");
        queue.enqueue("Luc");
        queue.enqueue("Tayshaun");
    
        assertTrue(queue.remove("Tayshaun"));
    
        queue.enqueue("Sajjad");
    
        assertEquals(
            Arrays.asList("Liam", "Luc", "Sajjad"),
            queue.toList());
    }
    
    /**
     * Tests removing the only student and reusing
     * the queue afterward.
     */
    @Test
    public void testRemoveOnlyStudent() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
    
        queue.enqueue("Liam");
    
        assertTrue(queue.remove("Liam"));
        assertTrue(queue.isEmpty());
    
        queue.enqueue("Luc");
    
        assertEquals("Luc", queue.peek());
        assertEquals("Luc", queue.dequeue());
        assertTrue(queue.isEmpty());
    }
    
    /**
     * Tests removing a student who is not waiting.
     * The queue should remain unchanged.
     */
    @Test
    public void testRemoveMissingStudent() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
    
        queue.enqueue("Liam");
        queue.enqueue("Luc");
    
        assertFalse(queue.remove("Unknown"));
    
        assertEquals(
            Arrays.asList("Liam", "Luc"),
            queue.toList());
    }
    
    /**
     * Tests attempting to remove an entry
     * from an empty queue.
     */
    @Test
    public void testRemoveFromEmptyQueue() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
    
        assertFalse(queue.remove("Liam"));
        assertTrue(queue.isEmpty());
    }
    
    /**
     * Tests rejecting a null cancellation request
     * without modifying the queue.
     */
    @Test
    public void testRemoveNull() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
    
        queue.enqueue("Liam");
    
        assertThrows(
            IllegalArgumentException.class,
            () -> queue.remove(null));
    
        assertEquals(
            Arrays.asList("Liam"),
            queue.toList());
    }
    
    /**
     * Tests that interleaved enqueue and dequeue
     * operations preserve FIFO order.
     *
     * This explicitly covers the Deliverable 4
     * Exceeds requirement.
     */
    @Test
    public void testInterleavedEnqueueDequeue() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
    
        queue.enqueue("Liam");
        queue.enqueue("Luc");
    
        assertEquals("Liam", queue.dequeue());
    
        queue.enqueue("Tayshaun");
    
        assertEquals("Luc", queue.dequeue());
    
        queue.enqueue("Sajjad");
        queue.enqueue("Alex");
    
        assertEquals(
            Arrays.asList("Tayshaun", "Sajjad", "Alex"),
            queue.toList());
    
        assertEquals("Tayshaun", queue.dequeue());
        assertEquals("Sajjad", queue.dequeue());
    
        queue.enqueue("Jordan");
    
        assertEquals(
            Arrays.asList("Alex", "Jordan"),
            queue.toList());
    
        assertEquals("Alex", queue.dequeue());
        assertEquals("Jordan", queue.dequeue());
    
        assertTrue(queue.isEmpty());
    }
    
    /**
     * Tests that remove deletes only the first
     * matching entry when duplicates are present.
     */
    @Test
    public void testRemoveFirstMatchingEntry() {
        LinkedQueue<String> queue = new LinkedQueue<String>();
    
        queue.enqueue("Liam");
        queue.enqueue("Luc");
        queue.enqueue("Liam");
    
        assertTrue(queue.remove("Liam"));
    
        assertEquals(
            Arrays.asList("Luc", "Liam"),
            queue.toList());
    }

}
