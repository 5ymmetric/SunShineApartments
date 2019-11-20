package entities;

// ParkingPass class
public class ParkingPass extends Product {

	private double parkingFee;
	private Product product;
	
	//Secondary Constructor
	public ParkingPass(String productCode, String type, int quantity, double parkingFee, Product product) {
		super(productCode, type, quantity);
		this.parkingFee = parkingFee;
		this.product = product;
	}
	
	//Secondary Constructor
	public ParkingPass(String productCode, String type, int quantity, double parkingFee) {
		super(productCode, type, quantity);
		this.parkingFee = parkingFee;
	}

	// Primary Constructor
	public ParkingPass(String productCode, String type, double parkingFee) {
		super(productCode, type);
		this.parkingFee = parkingFee;

	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	// Setters & Getter methods
	// Parking Fee
	public double getParkingFee() {
		return parkingFee;
	}

	public void setParkingFee(double parkingFee) {
		this.parkingFee = parkingFee;
	}

	@Override
	public double getSubtotal() {
		return parkingFee * getQuantity();
	}
	
	public double getSubtotal1() {
		return parkingFee;
	}

	@Override
	public double getTax() {
		return 0.04 * getSubtotal();
	}

	@Override
	public double getDiscount() {
		return -0.1 * getSubtotal();
	}

}
