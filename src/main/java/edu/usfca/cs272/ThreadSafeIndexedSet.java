package edu.usfca.cs272;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A thread-safe version of {@link IndexedSet} using a read/write lock.
 *
 * @param <E> element type
 * @see IndexedSet
 * @see ReadWriteLock
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 * Nino Estrada
 */
public class ThreadSafeIndexedSet<E> extends IndexedSet<E> {
	/** The lock used to protect concurrent access to the underlying set. */
	private final ReadWriteLock lock;

	/**
	 * Initializes an unsorted thread-safe indexed set.
	 */
	public ThreadSafeIndexedSet() {
		this(false);
	}

	/**
	 * Initializes a thread-safe indexed set.
	 *
	 * @param sorted whether the set should be sorted
	 */
	public ThreadSafeIndexedSet(boolean sorted) {
		super(sorted);
		lock = new ReadWriteLock();
	}

	/**
	 * Returns the identity hashcode of the lock object. Not particularly useful.
	 *
	 * @return the identity hashcode of the lock object
	 */
	public int lockCode() {
		return System.identityHashCode(lock);
	}

	/*
	 * TODO Override methods as necessary to make this class thread-safe using the
	 * simple read write lock.
	 */

	/**
	 * Adds an element to our set.
	 *
	 * @param element element to add
	 * @return true if the element was added (false if it was a duplicate)
	 *
	 * @see Set#add(Object)
	 */
	public boolean add(E element) {
		// gets the simpleWriteLock object and then lock it
		lock.write().lock();
		boolean b = super.add(element);
		lock.write().unlock();
		return b;
	}

	/**
	 * Adds the collection of elements to our set.
	 *
	 * @param elements elements to add
	 * @return true if any elements were added (false if were all duplicates)
	 *
	 * @see Set#addAll(Collection)
	 */
	public boolean addAll(Collection<E> elements) {
		// gets the simpleWriteLock object and then lock it
		lock.write().lock();
		boolean b = super.addAll(elements);
		lock.write().unlock();
		return b;
	}

	/**
	 * Adds values from one {@link IndexedSet} to another.
	 *
	 * @param elements elements to add
	 * @return true if any elements were added (false if were all duplicates)
	 *
	 * @see Set#addAll(Collection)
	 */
	public boolean addAll(IndexedSet<E> elements) {
		// gets the simpleWriteLock object and then lock it
		lock.write().lock();
		boolean b = super.addAll(elements);
		lock.write().unlock();
		return b;
	}

	/**
	 * Returns the number of elements in our set.
	 *
	 * @return number of elements
	 *
	 * @see Set#size()
	 */
	public int size() {
		lock.read().lock();
		int s = super.size();
		lock.read().unlock();
		return s;
	}

	/**
	 * Returns whether the element is contained in our set.
	 *
	 * @param element element to search for
	 * @return true if the element is contained in our set
	 *
	 * @see Set#contains(Object)
	 */
	public boolean contains(E element) {
		lock.read().lock();
		boolean b = super.contains(element);
		lock.read().unlock();
		return b;
	}

	/**
	 * Gets the element at the specified index based on iteration order. The element
	 * at this index may change over time as new elements are added.
	 *
	 * @param index index of element to get
	 * @return element at the specified index or null of the index was invalid
	 * @throws IndexOutOfBoundsException if index is out of bounds
	 */
	public E get(int index) throws IndexOutOfBoundsException {
		lock.read().lock();
		E b = super.get(index);
		lock.read().unlock();
		return b;
	}

	/**
	 * Gets the first element if it exists.
	 *
	 * @return first element
	 * @throws NoSuchElementException if no first element
	 *
	 * @see #get(int)
	 */
	public E first() throws NoSuchElementException {
		lock.read().lock();
		E b = super.first();
		lock.read().unlock();
		return b;
	}

	/**
	 * Gets the last element if it exists.
	 *
	 * @return last element
	 * @throws NoSuchElementException if no last element
	 *
	 * @see #get(int)
	 */
	public E last() throws NoSuchElementException {
		lock.read().lock();
		E b = super.last();
		lock.read().unlock();
		return b;
	}

	/**
	 * Returns an unsorted copy (shallow) of this set.
	 *
	 * @return unsorted copy
	 */
	public IndexedSet<E> unsortedCopy() {
		lock.read().lock();
		IndexedSet<E> b = super.unsortedCopy();
		lock.read().unlock();
		return b;
	}

	/**
	 * Returns a sorted copy (shallow) of this set.
	 *
	 * @return sorted copy
	 */
	public IndexedSet<E> sortedCopy() {
		lock.read().lock();
		IndexedSet<E> b = super.sortedCopy();
		lock.read().unlock();
		return b;
	}

	@Override
	public String toString() {
		lock.read().lock();
		String b = super.toString();
		lock.read().unlock();
		return b;
	}
}
