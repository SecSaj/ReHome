import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * A generic first-in, first-out queue implemented using singly linked nodes.
 * The head stores the oldest entry, and the tail stores the newest entry.
 * Adding to the tail and removing from the head both take constant time. This
 * queue does not depend on any ReHome-specific classes.
 *
 * @param <T>
 *            the type of element stored in the queue
 */
public class LinkedQueue<T>
{

    /**
     * Represents one node in the linked queue.
     *
     * @param <E>
     *            the type of data stored in the node
     */
    private static class Node<E>
    {
        private final E data;
        private Node<E> next;

        /**
         * Creates a node containing the given data.
         *
         * @param data
         *            the value stored in the node
         */
        Node(E data)
        {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /**
     * Creates an empty linked queue. Both head and tail start as null because
     * the queue contains no elements.
     */
    public LinkedQueue()
    {
        head = null;
        tail = null;
        size = 0;
    }


    /**
     * Adds an element to the back of the queue. If the queue is empty, the new
     * node becomes both the head and the tail.
     *
     * @param entry
     *            the element to add
     * @throws IllegalArgumentException
     *             if entry is null
     */
    public void enqueue(T entry)
    {
        if (entry == null)
        {
            throw new IllegalArgumentException("Cannot enqueue null.");
        }

        Node<T> newNode = new Node<T>(entry);

        if (isEmpty())
        {
            head = newNode;
        }
        else
        {
            tail.next = newNode;
        }

        tail = newNode;
        size++;
    }


    /**
     * Removes and returns the element at the front of the queue. If the last
     * element is removed, the head and tail are both reset to null.
     *
     * @return the element removed from the front
     * @throws NoSuchElementException
     *             if the queue is empty
     */
    public T dequeue()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Queue is empty.");
        }

        T removed = head.data;
        head = head.next;
        size--;

        if (head == null)
        {
            tail = null;
        }

        return removed;
    }


    /**
     * Returns the element at the front without removing it from the queue.
     *
     * @return the element at the front
     * @throws NoSuchElementException
     *             if the queue is empty
     */
    public T peek()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException("Queue is empty.");
        }

        return head.data;
    }


    /**
     * Determines whether the queue is empty.
     *
     * @return true if the queue contains no elements; false otherwise
     */
    public boolean isEmpty()
    {
        return size == 0;
    }


    /**
     * Creates an ArrayList containing the queue's elements in first-in,
     * first-out order. Changing the returned list does not change the original
     * linked queue.
     *
     * @return an independent list of queue elements
     */
    public ArrayList<T> toList()
    {
        ArrayList<T> result = new ArrayList<T>();

        Node<T> current = head;

        while (current != null)
        {
            result.add(current.data);
            current = current.next;
        }

        return result;
    }
}
