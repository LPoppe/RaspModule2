package framework.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple collection that mimics a {@link LinkedBlockingQueue} with the following additional
 * guarantees:
 * <ul>
 *   <li>There are no duplicates, as defined by the element's .equals(), in the queue.</li>
 *   <li>If an element that already exists in the queue is re-added to the queue, its position in
 *   the queue is refreshed per FIFO.</li>
 * </ul>
 *
 * All operations have the same Big-O time complexity and memory impact as that of a
 * {@link LinkedBlockingQueue}. Addition of duplicate events does not guarantee no memory impact.
 *
 * @author navkast
 *
 * Retrieved from: https://gist.github.com/navkast/6371a71fe94711100989d3930b941968.
 */

public final class SetLinkedBlockingQueue<T> {

    private final Lock lock;
    private final Condition notEmpty;
    private final BlockingQueue<T> blockingQueue;
    /*
      Keeps references to the elements in the backing blocking queue along with a counter on how many
      times the element is present in the queue.
    */
    private final Map<T, Integer> weakMap;

    public SetLinkedBlockingQueue() {
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.blockingQueue = new LinkedBlockingQueue<>();
        this.weakMap = new HashMap<>();
    }

    /**
     * @see LinkedBlockingQueue#peek()
     */

    public T peek() {
        try {
            lock.lock();
            T obj = blockingQueue.peek();
            if (obj == null) {
                // empty queue
                return null;
            }
            int objCount = weakMap.getOrDefault(obj, 1);
            if (objCount == 1) {
                // this is the last element of its kind in the queue.
                weakMap.remove(obj);
                return obj;
            } else {
                // This element is a dupe. Proceed to the next.
                blockingQueue.poll(); // remove this from the map proactively.
                weakMap.put(obj, objCount - 1);
                return peek(); // Recursive call to maintain dupe-removing logic.
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds an element if it's not already present in the queue. If the element is already present in
     * the queue, the element's position is refreshed.
     *
     * @see BlockingQueue#add(Object)
     */
    public void add(T obj) {
        if (obj == null) {
            return;
        }
        try {
            lock.lock();
            Integer objCount = weakMap.get(obj);
            if (objCount == null) {
                // Does not exist in the queue.
                weakMap.put(obj, 1);
            } else {
                // Already in the queue.
                weakMap.put(obj, objCount + 1);
            }
            peek(); // Proactively reduce any adjacent duplicates.
            blockingQueue.add(obj);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @see LinkedBlockingQueue#take()
     */
    public T take() throws InterruptedException {
        try {
            lock.lockInterruptibly();
            T obj;
            while ((obj = poll()) == null) {
                notEmpty.await();
            }
            return obj;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @see LinkedBlockingQueue#poll()
     */

    public T poll() {
        try {
            lock.lock();
            T obj = peek(); // takes care of the de-duping
            if (obj == null) {
                // empty queue
                return null;
            }
            return blockingQueue.poll();
        } finally {
            lock.unlock();
        }
    }

    // Added. Not in original.
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            lock.lock();
            T obj = peek(); // takes care of the de-duping
            if (obj == null) {
                // empty queue
                return null;
            }
            return blockingQueue.poll(timeout, unit);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @see BlockingQueue#drainTo(Collection)
     */
    public int drainTo(Collection<? super T> c) {
        try {
            lock.lock();
            int result = 0;
            T obj = poll();
            while (obj != null) {
                c.add(obj);
                result++;
                obj = poll();
            }
            return result;
        } finally {
            lock.unlock();
        }
    }
}
