package entities;

public class LinkedList {
	// Variables
	private Node startInvoice;
	private Node endInvoice;
	private int size = 0;
	private int index;

	public Node getStartInvoice() {
		return startInvoice;
	}

	public void setStartInvoice(Node startInvoice) {
		this.startInvoice = startInvoice;
	}

	public Node getEndInvoice() {
		return endInvoice;
	}

	public void setEndInvoice(Node endInvoice) {
		this.endInvoice = endInvoice;
	}

	// TODO: add the head of your list here
	public LinkedList() {
		this.startInvoice = null;
		this.endInvoice = null;
	}

	public LinkedList(Node startInvoice, Node endInvoice) {
		this.startInvoice = startInvoice;
		this.endInvoice = endInvoice;
		startInvoice.setNext(endInvoice);
	}

	/**
	 * This function returns the size of the list, the number of elements currently
	 * stored in it.
	 * 
	 * @return
	 */

	public int getSize() {
		size = 0;
		Node invoice = startInvoice;
		if (invoice == null) {
			size = 0;
		} else {
			while (invoice.getNext() != null) {
				size++;
				invoice = invoice.getNext();
			}
		}
		size++;
		// return size;
		index = size - 1;
		return size;
	}

	/**
	 * This function clears out the contents of the list, making it an empty list.
	 */
	public void clear() {
		startInvoice = null;
		getSize();
	}

	/**
	 * This method adds the given {@link Invoice} instance to the beginning of the
	 * list.
	 * 
	 * @param t
	 */
	public void add(Invoice t) {
		Node newInvoice = new Node(t);
		size++;
		index++;
		if (startInvoice == null && endInvoice == null) {
			startInvoice = newInvoice;
			endInvoice = newInvoice;
		} else {
			Node currentInvoice = startInvoice;
			Node afterInvoice = currentInvoice.getNext();
			if (currentInvoice.getInvoice().getTotal() < newInvoice.getInvoice().getTotal()) {
				startInvoice = newInvoice;
				startInvoice.setNext(currentInvoice);
				currentInvoice.setNext(afterInvoice);
			} else {
				while (afterInvoice != null) {
					if (afterInvoice.getInvoice().getTotal() > newInvoice.getInvoice().getTotal()) {
						afterInvoice = afterInvoice.getNext();
					} else if (afterInvoice.getInvoice().getTotal() == newInvoice.getInvoice().getTotal()) {
						Node evenAfterInvoice = afterInvoice.getNext();
						afterInvoice.setNext(newInvoice);
						newInvoice.setNext(evenAfterInvoice);
						break;
					} else if (afterInvoice.getInvoice().getTotal() < newInvoice.getInvoice().getTotal()) {
						Node beforeInvoice = startInvoice;
						while (beforeInvoice.getNext() != afterInvoice) {
							beforeInvoice = beforeInvoice.getNext();
						}
						beforeInvoice.setNext(newInvoice);
						newInvoice.setNext(afterInvoice);
						break;
					}
				}
				Node beforeInvoice = startInvoice;
				while (beforeInvoice.getNext() != afterInvoice) {
					beforeInvoice = beforeInvoice.getNext();
				}
				if (beforeInvoice.getInvoice().getTotal() > newInvoice.getInvoice().getTotal()) {
					beforeInvoice.setNext(newInvoice);
					newInvoice.setNext(afterInvoice);
				}
			}

		}

		Node currentInvoice = startInvoice;
		while (currentInvoice.getNext() != null) {
			currentInvoice = currentInvoice.getNext();
		}
		endInvoice = currentInvoice;
	}

	/**
	 * This method removes the {@link Invoice} from the given <code>position</code>,
	 * indices start at 0. Implicitly, the remaining elements' indices are reduced.
	 * 
	 * @param position
	 */
	public void remove(int position) {
		getSize();
		Node currentNode = startInvoice;
		if (position < 0 || position > index) {
			System.out.println("IndexOutOfBoundsException");
		} else if (position == 0) {
			startInvoice = startInvoice.getNext();
			size--;
			index--;
		} else if (position == index) {
			for (int i = 0; i < position - 1; i++) {
				currentNode = currentNode.getNext();
			}
			currentNode.setNext(null);
			endInvoice = currentNode;
			size--;
			index--;
		} else {
			for (int i = 0; i < position - 1; i++) {
				currentNode = currentNode.getNext();
			}
			Node toBeRemoved = currentNode.getNext();
			Node nextNode = toBeRemoved.getNext();
			currentNode.setNext(nextNode);
			size--;
			index--;
		}
	}

	/**
	 * Returns the {@link Invoice} element stored at the given
	 * <code>position</code>.
	 * 
	 * @param position
	 * @return
	 */
	public Invoice getInvoice(int position) {
		getSize();
		Node headNode = startInvoice;
		if (position < index || position > index) {
			return null;
		} else if (position == 0) {
			return headNode.getInvoice();
		} else if (position == index) {
			return this.endInvoice.getInvoice();
		} else {
			for (int i = 0; i < position; i++) {
				if (headNode.getNext() == null) {
					return null;
				} else {
					headNode = headNode.getNext();
				}
			}
			return headNode.getInvoice();
		}
	}

}
