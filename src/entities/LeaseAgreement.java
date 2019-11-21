package entities;

import java.time.LocalDate;
import java.time.Period;
import java.math.BigDecimal;
import java.math.RoundingMode;

// LeaseAgreement Class
public class LeaseAgreement extends Product {

	// Variables
	private LocalDate startDate;
	private LocalDate endDate;
	private String moveinDate;
	private String moveOutDate;
	private Address address;
	private Customer customer;
	private double deposit;
	private double monthlyCost;

	// Primary Constructor
	public LeaseAgreement(String productCode, String type, LocalDate startDate, LocalDate endDate, Address address,
			Customer customer, double deposit, double monthlyCost) {
		super(productCode, type);
		this.startDate = startDate;
		this.endDate = endDate;
		this.address = address;
		this.customer = customer;
		this.monthlyCost = monthlyCost;
		this.deposit = deposit;
	}

	public LeaseAgreement(String productCode, String type, String moveinDate, String moveOutDate, Address address,
			Customer customer, double deposit, double monthlyCost) {
		super(productCode, type);
		this.moveinDate = moveinDate;
		this.moveOutDate = moveOutDate;
		this.address = address;
		this.customer = customer;
		this.monthlyCost = monthlyCost;
		this.deposit = deposit;
	}

	public String getMoveinDate() {
		return moveinDate;
	}

	public void setMoveinDate(String moveinDate) {
		this.moveinDate = moveinDate;
	}

	public String getMoveOutDate() {
		return moveOutDate;
	}

	public void setMoveOutDate(String moveOutDate) {
		this.moveOutDate = moveOutDate;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	// Secondary Constructor
	public LeaseAgreement(String productCode, String type, int quantity, LocalDate startDate, LocalDate endDate,
			Address address, Customer customer, double deposit, double monthlyCost) {
		super(productCode, type, quantity);
		this.startDate = startDate;
		this.endDate = endDate;
		this.address = address;
		this.customer = customer;
		this.monthlyCost = monthlyCost;
		this.deposit = deposit;
	}

	// Getters & Setters method
	// startDate
	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	// endDate
	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	// customer
	public Customer getcustomer() {
		return customer;
	}

	public void setcustomer(Customer customer) {
		this.customer = customer;
	}

	// address
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	// deposit
	public double getDeposit() {
		return deposit;
	}

	public void setDeposit(double deposit) {
		this.deposit = deposit;
	}

	// monthly cost
	public double getMonthlyCost() {
		return monthlyCost;
	}

	public void setMonthlyCost(double monthlyCost) {
		this.monthlyCost = monthlyCost;
	}

	public double getSubtotal(LocalDate invoiceDate) {

		Period moveInDiff = Period.between(startDate, invoiceDate);
		Period moveOutDiff = Period.between(invoiceDate, endDate);

		int gapMonthsIn = moveInDiff.getMonths();
		int gapYearsIn = moveInDiff.getYears();

		int gapMonthsOut = moveOutDiff.getMonths();
		int gapYearsOut = moveOutDiff.getYears();

		double bd;

		if (gapMonthsIn == 0 && gapYearsIn == 0) {
			bd = (startDate.getMonth().length(false) - startDate.getDayOfMonth())
					/ (double) startDate.getMonth().length(false);

			bd *= monthlyCost;
			bd += deposit;
			bd += monthlyCost;
			bd *= this.getQuantity();

		} else if (gapMonthsOut == 0 && gapYearsOut == 0) {

			bd = (((monthlyCost * (endDate.getDayOfMonth() / (double) endDate.getMonth().length(false))) - deposit)
					* getQuantity());

		} else {

			bd = (getQuantity() * monthlyCost);
		}

		return bd;
	}

	public double getTax(LocalDate invoiceDate) {

		BigDecimal bd = new BigDecimal((0.06 * getSubtotal(invoiceDate))).setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public double getDiscount(LocalDate invoiceDate) {
		BigDecimal bd = new BigDecimal(((-.1) * getSubtotal(invoiceDate))).setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	@Override
	public double getSubtotal() {
		return 0.0;
	}

	@Override
	public double getTax() {
		return 0.0;
	}

	@Override
	public double getDiscount() {
		return 0.0;
	}
}