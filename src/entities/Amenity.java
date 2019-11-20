package entities;

// Amenity Class
public class Amenity extends Product {

	// Variables
	private String name;
	private double cost;

	// Primary constructor
	public Amenity(String productCode, String type, String name, double cost) {
		super(productCode, type);
		this.name = name;
		this.cost = cost;
	}

	// Secondary Constructor
	public Amenity(String productCode, String type, int quantity, String name, double cost) {
		super(productCode, type, quantity);
		this.name = name;
		this.cost = cost;
	}

	// Getters & Setters
	// name
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// cost
	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	@Override
	public double getSubtotal() {
		return cost * this.getQuantity();
	}

	@Override
	public double getTax() {
		return 0.04 * getSubtotal();
	}
	
	public double getTax1() {
		return 0.04 * getSubtotal1();
	}

	@Override
	public double getDiscount() {
		return -0.1 * getSubtotal();
	}
	
	public double getSubtotal1() {
		return 0.95 * getSubtotal();
	}

}
