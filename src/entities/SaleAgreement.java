	package entities;
	
	import java.time.LocalDate;
	import java.time.LocalDateTime;
	import java.time.Period;
	
	// SaleAgreement Class
	public class SaleAgreement extends Product {
	
		// Variables
		
		private LocalDateTime dateTime;
		private String moveinDate;
		private Address address;
		private double totalCost;
		private double downPayment;
		private double monthlyPayment;
		private int payableMonths;
		private double interestRate;
	
		// Primary Constructor
		
		public SaleAgreement(String productCode,String type, LocalDateTime dateTime, Address address, double totalCost, double downPayment,
				double monthlyPayment, int payableMonths, double interestRate) {
			super(productCode, type);
			this.dateTime = dateTime;
			this.address = address;
			this.totalCost = totalCost;
			this.downPayment = downPayment;
			this.monthlyPayment = monthlyPayment;
			this.payableMonths = payableMonths;
			this.interestRate = interestRate;
		}
		
		public SaleAgreement(String productCode,String type, String moveinDate, Address address, double totalCost, double downPayment,
				double monthlyPayment, int payableMonths, double interestRate) {
			super(productCode, type);
			this.moveinDate = moveinDate;
			this.address = address;
			this.totalCost = totalCost;
			this.downPayment = downPayment;
			this.monthlyPayment = monthlyPayment;
			this.payableMonths = payableMonths;
			this.interestRate = interestRate;
		}
		
		// Secondary Constructor
		public SaleAgreement(String productCode,String type,int quantity, LocalDateTime dateTime, Address address, double totalCost, double downPayment,
				double monthlyPayment, int payableMonths, double interestRate) {
			super(productCode, type, quantity);
			this.dateTime = dateTime;
			this.address = address;
			this.totalCost = totalCost;
			this.downPayment = downPayment;
			this.monthlyPayment = monthlyPayment;
			this.payableMonths = payableMonths;
			this.interestRate = interestRate;
		}
		
		
		// Getter & Setter methods
		// totalCost
		public double gettotalCost() {
			return totalCost;
		}
	
		public void settotalCost(double totalCost) {
			this.totalCost = totalCost;
		}
	
		// address
		public Address getAddress() {
			return address;
		}
	
		public void setAddress(Address address) {
			this.address = address;
		}
	
		// date
		public LocalDateTime getDate() {
			return dateTime;
		}
	
		public void setDate(LocalDateTime dateTime) {
			this.dateTime = dateTime;
		}
		
		public String getMoveinDate() {
			return moveinDate;
		}
		
		public void setMoveinDate(String moveinDate) {
			this.moveinDate = moveinDate;
		}
		
		// Down Payment
		public double getDownPayment() {
			return downPayment;
		}
	
		public void setDownPayment(double downPayment) {
			this.downPayment = downPayment;
		}
		
		// Monthly Payment
		public double getMonthlyPayment() {
			return monthlyPayment;
		}
	
		public void setMonthlyPayment(double monthlyPayment) {
			this.monthlyPayment = monthlyPayment;
		}
		
		// Payable Months
		public int getPayableMonths() {
			return payableMonths;
		}
	
		public void setPayableMonths(int payableMonths) {
			this.payableMonths = payableMonths;
		}
	
		// Interest Rate
		public double getInterestRate() {
			return interestRate;
		}
	
		public void setInterestRate(double interestRate) {
			this.interestRate = interestRate;
		}
	
		public double getSubtotal(LocalDate invoiceDate) {
			
			LocalDate dateOnly = dateTime.toLocalDate();
			Period timeGap = Period.between(invoiceDate, dateOnly);
			
			int monthsDiff = timeGap.getMonths();
			int yearsDiff = timeGap.getYears();
			
			if(monthsDiff == 0 && yearsDiff == 0) {
				return getQuantity() * (downPayment + monthlyPayment + ((interestRate/100) * (totalCost - monthlyPayment - downPayment)));
			} else {
				return getQuantity() * (monthlyPayment + ((interestRate/100) * (totalCost - downPayment - ((Math.abs(monthsDiff) + (Math.abs(yearsDiff) * 12)) * monthlyPayment))));
			}
		}
	
		public double getTax(LocalDate invoiceDate) {
			return 0.06 * getSubtotal(invoiceDate);
		}
	
		public double getDiscount(LocalDate invoiceDate) {
			return (-0.1 * getSubtotal(invoiceDate));
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
