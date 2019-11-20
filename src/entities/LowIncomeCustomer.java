package entities;

// Low income customer class
public class LowIncomeCustomer extends Customer {

//	private double processingFee = 50.75;
//	private int housingCredit = 1000;
//	private double taxExemption = 0.9;

	public LowIncomeCustomer(String customerCode, String type, Person primaryContact, String name, Address address) {
		super(customerCode, type, primaryContact, name, address);

	}

}
