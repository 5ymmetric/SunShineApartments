package entities;

public class Node {

	private Invoice item;
	private Node next;

	public Node() {
		this.item = null;
		this.next = null;
	}

	public Node(Invoice item) {
		this.item = item;
		this.next = null;
	}

	public Node(Invoice item, Node next) {
		this.item = item;
		this.next = next;
	}

	public Invoice getInvoice() {
		return item;
	}

	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}

	public void setItem(Invoice item) {
		this.item = item;
	}
}