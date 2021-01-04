
/*  Student information for assignment:


 *
 *  On my honor, Ayush Patel, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 2
 *
 *  Student 1 (Student whose turning account is being used)
 *  UTEID: ap55837
 *  email address: patayush01@utexas.edu
 *  Grader name: Tony
 */
import java.util.LinkedList;

public class PriorityQueue<E extends Comparable<? super E>> {
	private LinkedList<E> list;

	// intializes array
	public PriorityQueue() {
		list = new LinkedList<E>();
	}

	// pre: none
	// post: returns true if n was added, false otherwise
	public boolean enqueue(E n) {
		int oldSize = list.size();
		int i = 0;
		// traversing through arrayList while size hasn't changed
		while (i < list.size() && oldSize == list.size()) {
			// if get to a point where n is strictly less than 0, then found
			// appropriate spot to add n
			if (n.compareTo(list.get(i)) < 0)
				list.add(i, n);
			i++;
		}
		// if size is the same, then element wasn't added, so add it to end of
		// list (appropriate spot)
		if (oldSize == list.size())
			return list.add(n);
		// return whether or not size changed
		return oldSize != list.size();
	}

	// pre: none
	// post: returns next element to dequeue (elem at index 0)
	public E dequeue() {
		return list.remove(0);
	}

	// pre: none
	// post: returns size
	public int size() {
		return list.size();
	}

	// pre: none
	// post: returns toString of queue
	public String toString() {
		return list.toString();
	}

}
