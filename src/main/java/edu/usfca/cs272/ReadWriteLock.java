package edu.usfca.cs272;
import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Maintains a pair of associated locks, one for read-only operations and one
 * for writing. The read lock may be held simultaneously by multiple reader
 * threads, so long as there are no writers. The write lock is exclusive. The
 * active writer is able to acquire read or write locks as long as it is active.
 *
 * <!-- simplified lock used for this class -->
 * @see SimpleLock
 *
 * <!-- built-in Java locks that are similar (but more complex) -->
 * @see Lock
 * @see ReentrantLock
 * @see ReadWriteLock
 * @see ReentrantReadWriteLock
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 * Nino Estrada
 */
public class ReadWriteLock {
	/** The conditional lock used for reading. */
	private final SimpleLock readerLock;

	/** The conditional lock used for writing. */
	private final SimpleLock writerLock;

	/** The number of active readers. */
	private int readers;

	/** The number of active writers; */
	private int writers;

	/** The thread that holds the write lock. */
	private Thread activeWriter;

	/** The log4j2 logger. */
	private static final Logger log = LogManager.getLogger();

	/**
	 * The lock object used for synchronized access of readers and writers. For
	 * security reasons, a separate private final lock object is used.
	 *
	 * @see <a href=
	 *   "https://wiki.sei.cmu.edu/confluence/display/java/LCK00-J.+Use+private+final+lock+objects+to+synchronize+classes+that+may+interact+with+untrusted+code">
	 *   SEI CERT Oracle Coding Standard for Java</a>
	 */
	private final Object lock;

	/**
	 * Initializes a new simple read/write lock.
	 */
	public ReadWriteLock() {
		readerLock = new SimpleReadLock();
		writerLock = new SimpleWriteLock();

		lock = new Object();

		readers = 0;
		writers = 0;

		activeWriter = null;
	}

	/**
	 * Returns the reader lock.
	 *
	 * @return the reader lock
	 */
	public SimpleLock read() {
		return readerLock;
	}

	/**
	 * Returns the writer lock.
	 *
	 * @return the writer lock
	 */
	public SimpleLock write() {
		return writerLock;
	}

	/**
	 * Returns the number of active readers.
	 *
	 * @return the number of active readers
	 */
	public int readers() {
		synchronized (lock) {
			return readers;
		}
	}

	/**
	 * Returns the number of active writers.
	 *
	 * @return the number of active writers
	 */
	public int writers() {
		synchronized (lock) {
			return writers;
		}
	}

	/**
	 * Determines whether the thread running this code and the writer thread are in
	 * fact the same thread.
	 *
	 * @return true if the thread running this code and the writer thread are not
	 *   null and are the same thread
	 *
	 * @see Thread#currentThread()
	 */
	public boolean isActiveWriter() {
		synchronized (lock) {
			return Thread.currentThread().equals(activeWriter);
		}
	}

	/**
	 * A simple lock used for conditional synchronization as an alternative to using
	 * a {@code synchronized} block.
	 *
	 * Similar but simpler than {@link Lock}.
	 *
	 * @author CS 272 Software Development (University of San Francisco)
	 * @version Fall 2022
	 */
	public static interface SimpleLock {
		/**
		 * Acquires the lock. If the lock is not available then the current thread
		 * becomes disabled for thread scheduling purposes and lies dormant until the
		 * lock has been acquired.
		 */
		public void lock();

		/**
		 * Releases the lock.
		 */
		public void unlock();
	}

	/**
	 * Used to maintain simultaneous read operations.
	 */
	private class SimpleReadLock implements SimpleLock {
		/**
		 * Controls access to the read lock. The active thread is forced to wait while
		 * there are any active writers and it is not the active writer thread. Once
		 * safe, the thread is allowed to acquire a read lock by incrementing the number
		 * of active readers.
		 */
		@Override
		public void lock() {
			/*
			 * This starts with the basic implementation from lecture (plus some
			 * logging). You will eventually need to modify it to check for whether
			 * there is an active writer.
			 */

			log.debug("Acquiring read lock...");

			try {
				synchronized (lock) {
					// as long as someone is writing that data can't read it
					while (writers > 0 && !isActiveWriter())
					{
						lock.wait();
					}

					// To be certain that it is zero, or we are active writer
					assert writers == 0 || isActiveWriter();

					// as long as there are readers, can't write anything
					readers++;
				}
			}
			catch (InterruptedException ex) {
				log.catching(Level.DEBUG, ex);
				Thread.currentThread().interrupt();
			}

			log.debug("Read lock acquired");
		}

		/**
		 * Will decrease the number of active readers and notify any waiting threads if
		 * necessary.
		 *
		 * @throws IllegalStateException if no readers to unlock
		 */
		@Override
		public void unlock() throws IllegalStateException {
			// don't run code until have lock
			synchronized (lock)
			{
				if (readers == 0)
				{
					throw new IllegalStateException();
				}

				readers -= 1;

				if (readers == 0)
				{
					lock.notifyAll();
				}
			}
		}
	}

	/**
	 * Used to maintain exclusive write operations.
	 */
	private class SimpleWriteLock implements SimpleLock {
		/**
		 * Controls access to the write lock. The active thread is forced to wait while
		 * there are any active readers or writers, and it is not the active writer
		 * thread. Once safe, the thread is allowed to acquire a write lock by
		 * incrementing the number of active writers and setting the active writer
		 * reference.
		 */
		@Override
		public void lock() {

			log.debug("Acquiring write lock...");

			try {
				// Note the lock object being used here and elsewhere
				synchronized (lock) {
					// as long as someone is writing that data can't read it
					while ((writers > 0 || readers > 0) && !isActiveWriter())
					{
						lock.wait();
					}

					// To be certain that it is zero
					assert (writers == 0 && readers == 0) || isActiveWriter();


					// as long as there are readers, can't write anything
					writers++;

					// Whatever thread running this code is now the active writer
					activeWriter = Thread.currentThread();
				}
			}
			catch (InterruptedException ex) {
				log.catching(Level.DEBUG, ex);
				Thread.currentThread().interrupt();
			}

			log.debug("Write lock acquired");
		}

		/**
		 * Will decrease the number of active writers and notify any waiting threads if
		 * necessary. Also unsets the active writer if appropriate.
		 *
		 * @throws IllegalStateException if no writers to unlock
		 * @throws ConcurrentModificationException if there are writers but unlock is
		 *   called by a thread that does not hold the write lock
		 */
		@Override
		public void unlock() throws IllegalStateException, ConcurrentModificationException {
			// don't run code until have lock
			synchronized (lock)
			{
				if (writers == 0)
				{
					throw new IllegalStateException();
				}
				else if (!isActiveWriter())
				{
					throw new ConcurrentModificationException();
				}

				writers -= 1;

				// unsetting the active writer when the lock is fully released
				if (writers == 0 && readers == 0)
				{
					lock.notifyAll();
					activeWriter = null;
				}
			}
		}
	}
}
