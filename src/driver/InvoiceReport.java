package driver;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;

import entities.Amenity;
import entities.Customer;
import entities.Invoice;
import entities.Product;
import entities.LeaseAgreement;
import entities.LinkedList;
import entities.Node;
import entities.ParkingPass;
import entities.Person;
import entities.SaleAgreement;

public class InvoiceReport {

	// Main class to call the consoleWriter function
	public static void main(String[] args) {

		BasicConfigurator.configure();

		/*
		 * Creates a set of persons and utilizes the getPersons method to load contents
		 * to it from the database
		 */
		Set<Person> persons = null;
		try {
			persons = Person.getPersons();
		} catch (RuntimeException e) {
			System.exit(1);
		}

		// Creates a map to associate each person with their unique personCode
		Map<String, Person> personMap = new TreeMap<String, Person>();
		for (Person person : persons) {
			personMap.put(person.getPersonCode(), person);
		}

		/*
		 * Creates a set of customers and utilizes the getCustomers method to load
		 * contents to it from the database
		 */
		Set<Customer> customers = null;
		try {
			customers = Customer.getCustomers(personMap);
		} catch (RuntimeException e) {
			System.exit(1);
		}

		// Creates a map to associate each customer with their unique customerCode
		Map<String, Customer> customerMap = new TreeMap<String, Customer>();
		for (Customer customer : customers) {
			customerMap.put(customer.getCustomerCode(), customer);
		}

		/*
		 * Creates a set of products and utilizes the getProducts method to load
		 * contents to it from the database
		 */
		Set<Product> products = null;
		try {
			products = Product.getProducts(customerMap);
		} catch (RuntimeException e) {
			System.exit(1);
		}

		// Creates a map to associate each Product with its unique product code
		Map<String, Product> productMap = new TreeMap<String, Product>();
		for (Product product : products) {
			productMap.put(product.getProductCode(), product);
		}

		Set<Invoice> invoices = null;
		try {
			invoices = Invoice.getInvoices(personMap, customerMap, productMap);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		ConsoleWriter(invoices);
	}

	public static void ConsoleWriter(Set<Invoice> Invoices) {

		// Start of Summary Report
		// Generic
		System.out.println("Executive Summary Report");
		System.out.println("========================");

		// Titles of Generic for everything
		System.out.printf(String.format("%-10s %-42s %-25s %23s %12s %15s %15s %15s", "Invoice", "Customer", "Realtor",
				"Subtotal", "Fees", "Taxes", "Discount", "Total"));

		System.out.println();

		// Final variables to print in the TOTALS Section
		double finalSubtotal = 0;
		double finalFees = 0;
		double finalTaxes = 0;
		double finalDiscount = 0;
		double finalTotal = 0;
		LinkedList newList = new LinkedList();

		for (Invoice i : Invoices) { // Loops through all the Invoices

			// Variables for proper invoice declaration
			double subtotal = 0;
			double fees = 0;
			double taxes = 0;
			double perDiscount = 0; // This and totalDiscuont assist in calculating the final discount
			double totalDiscount = 0;
			boolean discounter = true; // Discount used counter
			double total = 0;
			boolean amenityDiscount = false; // Amenity Discount used boolean
			boolean parkingPassDiscount = false; // Parking Pass Discount used boolean
			int parkingPassDiscountCounter = 0; // # of free parking passes

			for (Product k : i.getProducts()) { // Iterates through all the products of a certain invoice

				double discount = 0; // Discount variable

				if (k.getType().equals("P")) { // Loop that determines the amount of free passes
					ParkingPass m = (ParkingPass) k;
					if (m.getProduct() != null) {
						parkingPassDiscountCounter = m.getProduct().getQuantity();
					} // gets the agreement its attached to and its quantity
				}

				// If statement to calculate subtotals
				if (k.getType().equals("L")) {
					LeaseAgreement m = (LeaseAgreement) k; // cast as lease agreement
					subtotal = subtotal + m.getSubtotal(i.getDate()); // Subtotal method
				} else if (k.getType().equals("S")) {
					SaleAgreement m = (SaleAgreement) k; // Look to Sale agreements^^
					subtotal = subtotal + m.getSubtotal(i.getDate());

				} else if (k.getType().equals("A")) { // If amenities
					for (Product j : i.getProducts()) {
						if (j.getType().equals("L")) { // Checks if a lease is attached
							Amenity m = (Amenity) k;
							subtotal = subtotal + m.getSubtotal1(); // If so 95% subtotal
							amenityDiscount = true;
						}
					}
					if (amenityDiscount == false) {
						subtotal = subtotal + k.getSubtotal(); // If not Full subtotal
					}

				} else if (k.getType().equals("P")) {
					for (Product j : i.getProducts()) {
						if ((j.getType().equals("L") || j.getType().equals("S")) && (parkingPassDiscountCounter > 0)) {
							parkingPassDiscountCounter = parkingPassDiscountCounter - k.getQuantity(); // Subtracts the
																										// free Parking
																										// pass counter

							ParkingPass m = (ParkingPass) k; // Cast to Parking pass

							if (parkingPassDiscountCounter > 0) {
								parkingPassDiscount = true;
								break;
							} else { // If more parking passes than free parking passes
								parkingPassDiscount = true;
								subtotal = subtotal + (m.getSubtotal1() * Math.abs(parkingPassDiscountCounter * 1.0)); // Gets
																														// the
																														// cost
																														// of
																														// the
																														// difference
																														// between
																														// both
																														// these
																														// variables
							}

						}

						if (parkingPassDiscount == false) {
							subtotal = subtotal + k.getSubtotal() / 2; // Calculates subtotal if not associated with a
																		// lease or sale
						}
					}
				}

				if (i.getCustomer().getType().equals("G")) { // Tax If statement
					if (k.getType().equals("L")) {
						LeaseAgreement m = (LeaseAgreement) k;
						taxes = taxes + m.getTax(i.getDate()); // Gets taxes for lease Agreements
					} else if (k.getType().equals("S")) {
						SaleAgreement m = (SaleAgreement) k;
						taxes = taxes + m.getTax(i.getDate()); // Look ^^ for sale also
					} else if (k.getType().equals("A")) {
						if (amenityDiscount == false) { // If discount on Amenities hasn't been applied
							taxes = taxes + k.getTax();
						} else {
							Amenity m = (Amenity) k;
							taxes = taxes + m.getTax1(); // Special tax that uses the getSubtotal1() method to calculate
															// tax
						}
					} else if (k.getType().equals("P")) { // If parkingPass
						if (parkingPassDiscount == false) {
							taxes = taxes + k.getTax(); // no discount parkingpass
						} else if (parkingPassDiscountCounter < 0) {
							ParkingPass m = (ParkingPass) k; //
							taxes = taxes + (0.04 * m.getSubtotal1() * Math.abs(parkingPassDiscountCounter * 1.0));
						} // Same equation to calculate the sub-total
					}
				}

				if (i.getCustomer().getType().equals("L")) { // Discount counter
					if (k.getType().equals("L") || k.getType().equals("S") && discounter == true) {
						discount = discount - 1000; // $1000 for low income
						perDiscount = discount; // Discount is transferred over to this variable b/c discount is local
												// to the products loop
						discounter = false;
					}

					discount = (-0.1) * subtotal; // Flat -0.1% discount on all items

				}

				if (i.getCustomer().getType().equals("L")) {
					fees = 50.75; // Fee levied to verify low-income status
				}

				totalDiscount = discount + perDiscount; // Total discount added up
				total = subtotal + fees + taxes + totalDiscount; // Total

				i.setTotal(total);
				i.setSubtotal(subtotal);
				i.setFees(fees);
				i.setTaxes(taxes);
				i.setTotalDiscount(totalDiscount);
			}

			newList.add(i);

			// These are the total variables at the bottom
			finalSubtotal += subtotal;
			finalFees += fees;
			finalTaxes += taxes;
			finalDiscount += totalDiscount;
			finalTotal += total;

		}

		// Basic report printing
//		newList.sort();
		Node currentNode = newList.getStartInvoice();
		while (currentNode.getNext() != null) {
			currentNode.getInvoice().print();
			currentNode = currentNode.getNext();
		}
		newList.setEndInvoice(currentNode);
		currentNode.getInvoice().print();

		// TOTALS printing
		System.out.println(
				"=======================================================================================================================================================================");
		System.out.printf(String.format("TOTALS    %91.2f $ %10.2f $ %15.2f $ %10.2f $ %15.2f $", finalSubtotal,
				finalFees, finalTaxes, finalDiscount, finalTotal));

		System.out.println("\n");

		// Beginning of Individual Reports
		System.out.println("Individual Invoice Detail Reports");
		System.out.println("==================================================");

		currentNode = newList.getStartInvoice();

		while (currentNode.getNext() != null) { // Loop for iterating and printing detailed reports

			Invoice i = currentNode.getInvoice();

			// variables for each detailed report
			double Finalsubtotal = 0.0;
			double FinalTax = 0.0;
			double Total = 0.0;

			// Invoices' basic attributes printing
			System.out.println("Invoice " + i.getInvoiceCode());
			System.out.println("========================");

			System.out.println("Realtor: " + i.getRealtor().getFullName());
			i.getCustomer().printCustomerInfo();
			System.out.println("------------------------------------------");

			// Printing format
			System.out.printf("%-10s%-75s    %-21s%-14s%-15s\n", "Code", "Item", "Subtotal", "Tax", "Total");

			for (int p = 0; p < i.getProductCount(); p++) { // Loop iterating through the productList
				Product k = i.getProducts().get(p);

				if (i.getCustomer().getType().equals("G")) { // General customer printing
					if (k.getType().equals("L")) {
						LeaseAgreement l = (LeaseAgreement) k;
						double subtotal = l.getSubtotal(i.getDate());
						double tax = l.getTax(i.getDate());
						double total = subtotal + tax;
						System.out.println(String.format("\n%-9s Lease Agreement @ %-56s %10.2f $   %11.2f $ %13.2f $",
								k.getProductCode(), l.getAddress().getStreet(), subtotal, tax, total));

						System.out.println("          " + l.getStartDate() + " (" + l.getQuantity() + " units @ $"
								+ l.getMonthlyCost() + " /unit)");

						Finalsubtotal += subtotal;
						FinalTax += tax;
						Total += total; // All the necessity of lease agreement + general customer

					} else if (k.getType().equals("S")) {
						SaleAgreement s = (SaleAgreement) k;
						double subtotal = s.getSubtotal(i.getDate());
						double tax = s.getTax(i.getDate());
						double total = subtotal + tax;
						int interest = (int) (s.getInterestRate() * s.getMonthlyPayment() / 100);

						System.out.println(String.format("\n%-9s Sale Agreement @ %-57s %10.2f $ %13.2f $ %13.2f $",
								k.getProductCode(), s.getAddress().getStreet(), subtotal, tax, total));

						System.out.println("          " + s.getQuantity() + " units @ $" + s.getMonthlyPayment()
								+ " monthly, $" + interest + " interest payment/unit ");

						Finalsubtotal += subtotal;
						FinalTax += tax;
						Total += total; // All the necessity of sale agreement + general customer

					} else if (k.getType().equals("P")) { // Parking Pass
						ParkingPass pp = (ParkingPass) k;
						double subtotal = pp.getSubtotal();
						double tax = pp.getTax();
						double total = subtotal + tax;
						int freePasses = 0;
						if (pp.getProduct() != null) {
							freePasses = pp.getProduct().getQuantity();
						}

						int Count = 0;

						for (int y = 0; y < i.getProductCount(); y++) {
							if (i.getProducts().get(y) instanceof LeaseAgreement
									|| i.getProducts().get(y) instanceof SaleAgreement) {
								Count++;// count if the products contains LeaseAgreements or Sales Agreement
							}
						}

						if (Count < 1) {

							System.out.print(String.format("\n%-9s Parking Pass (%d units @ $%.1f)",
									pp.getProductCode(), pp.getQuantity(), pp.getParkingFee()));

						} else {

							if (pp.getQuantity() < freePasses) {
								freePasses = pp.getQuantity();
								subtotal = 0;
								tax = 0;
								total = 0;

								System.out.print(
										String.format("\n%1s      Parking Pass %s (%d units @ $%.1f with %d free)",
												pp.getProductCode(), pp.getProduct().getProductCode(), pp.getQuantity(),
												pp.getParkingFee(), freePasses));
							} else {

								subtotal -= freePasses * pp.getParkingFee();
								tax = 0.04 * subtotal;
								total = subtotal + tax;

								System.out
										.print(String.format("\n%1s      Parking Pass (%d units @ $%.1f with %d free)",
												pp.getProductCode(), pp.getQuantity(), pp.getParkingFee(), freePasses));
							}

						}

						System.out.println(String.format("\t %30.2f $ %13.2f $ %13.2f $", subtotal, tax, total));

						Finalsubtotal += subtotal;
						FinalTax += tax;
						Total += total;

						// ^^ All the intricacies of the parkingPass class tax counter

					} else if (k.getType().equals("A")) {
						Amenity a = (Amenity) k;
						double subtotal = a.getSubtotal();
						double tax = a.getTax();
						double total = subtotal + tax;

						int Count = 0;

						for (int y = 0; y < i.getProductCount(); y++) {
							if (i.getProducts().get(y) instanceof LeaseAgreement) {
								Count++;// count if the products contains LeaseAgreements than 5% discount
							}
						}

						if (Count >= 1) { // if Loop to check through the count
							subtotal = a.getSubtotal1();
							tax = a.getTax1();
							total = subtotal + tax;

							System.out.print(String.format("\n%s      %s (%d units @ %.2f /unit 5%% off)",
									a.getProductCode(), a.getName(), a.getQuantity(), a.getCost()));
						} else {

							System.out.print(String.format("\n%s      %s (%d units @ $%.1f /unit)", a.getProductCode(),
									a.getName(), a.getQuantity(), a.getCost()));

						}

						System.out.println(String.format("\t %38.2f $ %13.2f $ %13.2f $", subtotal, tax, total)); // Formatting

						Finalsubtotal += subtotal;
						FinalTax += tax;
						Total += total;

						// All the necessities for the amenities class
					}
				} else if (i.getCustomer().getType().equals("L")) { // Customer type is L
					if (k.getType().equals("L")) { // lease agreement checker
						LeaseAgreement l = (LeaseAgreement) k;
						double subtotal = l.getSubtotal(i.getDate());
						double tax = 0.0;
						double total = subtotal + tax;
						System.out.println(String.format("\n%-9s Lease Agreement @ %-56s %10.2f $   %11.2f $ %13.2f $",
								k.getProductCode(), l.getAddress().getStreet(), subtotal, tax, total));

						System.out.println("          " + l.getStartDate() + " (" + l.getQuantity() + " units @ $"
								+ l.getMonthlyCost() + " /unit)");

						Finalsubtotal += subtotal;
						FinalTax += tax;
						Total += total;

						// Lease Agreement necessities printed
					} else if (k.getType().equals("S")) {
						SaleAgreement s = (SaleAgreement) k;
						double subtotal = s.getSubtotal(i.getDate());
						double tax = 0.0;
						double total = subtotal + tax;
						int interest = (int) (s.getInterestRate() * s.getMonthlyPayment() / 100);

						System.out.println(String.format("\n%-9s Sale Agreement @ %-57s %10.2f $ %13.2f $ %13.2f $",
								k.getProductCode(), s.getAddress().getStreet(), subtotal, tax, total));

						System.out.println("          " + s.getQuantity() + " units @ $" + s.getMonthlyPayment()
								+ " monthly, $" + interest + " interest payment/unit ");

						Finalsubtotal += subtotal;
						FinalTax += tax;
						Total += total;

						// Sale agreement necessities printed

					} else if (k.getType().equals("P")) {
						ParkingPass pp = (ParkingPass) k;
						double subtotal = pp.getSubtotal();
						double tax = 0.0;
						double total = subtotal + tax;
						int freePasses = 0;
						if (pp.getProduct() != null) {
							freePasses = pp.getProduct().getQuantity();
						}

						int Count = 0;

						for (int y = 0; y < i.getProductCount(); y++) {
							if (i.getProducts().get(y) instanceof LeaseAgreement
									|| i.getProducts().get(y) instanceof SaleAgreement) {
								Count++;// count if the products contains LeaseAgreement or Sales Agreement
							}
						}

						if (Count < 1) {

							System.out.print(String.format("\n%1s      Parking Pass (%d units @ $%.1f) \t",
									pp.getProductCode(), pp.getQuantity(), pp.getParkingFee()));

						} else {

							if (pp.getQuantity() < freePasses) {
								freePasses = pp.getQuantity();
								subtotal = 0;
								tax = 0;
								total = 0;

								System.out.print(
										String.format("\n%1s      Parking Pass %s (%d units @ $%.1f with %d free)",
												pp.getProductCode(), pp.getProduct().getProductCode(), pp.getQuantity(),
												pp.getParkingFee(), freePasses));
							} else {

								subtotal -= freePasses * pp.getParkingFee();
								total = subtotal + tax;

								System.out
										.print(String.format("\n%1s      Parking Pass (%d units @ $%.1f with %d free)",
												pp.getProductCode(), pp.getQuantity(), pp.getParkingFee(), freePasses));
							}

						}

						System.out.println(String.format("\t \t %30.2f $ %13.2f $ %13.2f $", subtotal, tax, total));

						Finalsubtotal += subtotal;
						FinalTax += tax;
						Total += total;

					} else if (k.getType().equals("A")) {
						Amenity a = (Amenity) k;
						double subtotal = a.getSubtotal();
						double tax = 0.0;
						double total = subtotal + tax;

						int Count = 0;

						for (int y = 0; y < i.getProductCount(); y++) {
							if (i.getProducts().get(y) instanceof LeaseAgreement) {
								Count++;// count if the products contains LeaseAgreements than 5% discount
							}
						}

						if (Count >= 1) {
							subtotal = a.getSubtotal1();
							tax = 0.0;
							total = subtotal + tax;

							System.out.print(String.format("\n%s      %s (%d units @ %.2f /unit 5%% off)      ",
									a.getProductCode(), a.getName(), a.getQuantity(), a.getCost()));
						} else {

							System.out.print(String.format("\n%s      %s (%d units @ $%.1f /unit) \t",
									a.getProductCode(), a.getName(), a.getQuantity(), a.getCost()));

						}

						System.out.println(String.format("\t %38.2f $ %13.2f $ %13.2f $", subtotal, tax, total));

						Finalsubtotal += subtotal;
						FinalTax += tax;
						Total += total;

					}
				}
			}
			System.out.println("\t\t\t\t\t\t\t\t\t\t     ============================================");
			System.out.printf("\nSUBTOTALS %85.2f $ %13.2f $ %13.2f $", Finalsubtotal, FinalTax, Total);

			if (i.getCustomer().getType().equals("G")) {
				System.out.printf("\nTOTAL %121.2f $", Total);
			} else if (i.getCustomer().getType().equals("L")) {

				int Count = 0;
				double Discount = 0.0;

				for (int y = 0; y < i.getProductCount(); y++) {
					if (i.getProducts().get(y) instanceof LeaseAgreement
							|| i.getProducts().get(y) instanceof SaleAgreement) {
						Count++;// count if the products contains LeaseAgreement or Sales Agreement
					}
				}

				if (Count != 0) {

					Discount = (Total * 0.1 + 1000) * -1;

					System.out.printf("\nDISCOUNT (10%% LOW INCOME + $1000 HOUSING CREDIT) %78.2f $", Discount);
					// Discount for low-income & housing customers

				} else {

					Discount = (Total * 0.1) * -1;

					System.out.printf("\nDISCOUNT (10%% LOW INCOME) %101.2f $", Total * -0.1);

				}

				double additionalFee = 50.75;

				System.out.printf("\nADDITIONAL FEE (LOW INCOME) %99.2f $", additionalFee);

				Total += Discount + additionalFee;
				System.out.printf("\nTOTAL %121.2f $", Total);
				// Additional fee for the low-income customers
			}

			System.out.println("\n				Thank you for your purchase! \n"); // Thank you statement!
			currentNode = currentNode.getNext();
		}

		newList.setEndInvoice(currentNode);
		Invoice i = currentNode.getInvoice();
		// variables for each detailed report
		double Finalsubtotal = 0.0;
		double FinalTax = 0.0;
		double Total = 0.0;

		// Invoices' basic attributes printing
		System.out.println("Invoice " + i.getInvoiceCode());
		System.out.println("========================");

		System.out.println("Realtor: " + i.getRealtor().getFullName());
		i.getCustomer().printCustomerInfo();
		System.out.println("------------------------------------------");

		// Printing format
		System.out.printf("%-10s%-75s    %-21s%-14s%-15s\n", "Code", "Item", "Subtotal", "Tax", "Total");

		for (int p = 0; p < i.getProductCount(); p++) { // Loop iterating through the productList
			Product k = i.getProducts().get(p);

			if (i.getCustomer().getType().equals("G")) { // General customer printing
				if (k.getType().equals("L")) {
					LeaseAgreement l = (LeaseAgreement) k;
					double subtotal = l.getSubtotal(i.getDate());
					double tax = l.getTax(i.getDate());
					double total = subtotal + tax;
					System.out.println(String.format("\n%-9s Lease Agreement @ %-56s %10.2f $   %11.2f $ %13.2f $",
							k.getProductCode(), l.getAddress().getStreet(), subtotal, tax, total));

					System.out.println("          " + l.getStartDate() + " (" + l.getQuantity() + " units @ $"
							+ l.getMonthlyCost() + " /unit)");

					Finalsubtotal += subtotal;
					FinalTax += tax;
					Total += total; // All the necessity of lease agreement + general customer

				} else if (k.getType().equals("S")) {
					SaleAgreement s = (SaleAgreement) k;
					double subtotal = s.getSubtotal(i.getDate());
					double tax = s.getTax(i.getDate());
					double total = subtotal + tax;
					int interest = (int) (s.getInterestRate() * s.getMonthlyPayment() / 100);

					System.out.println(String.format("\n%-9s Sale Agreement @ %-57s %10.2f $ %13.2f $ %13.2f $",
							k.getProductCode(), s.getAddress().getStreet(), subtotal, tax, total));

					System.out.println("          " + s.getQuantity() + " units @ $" + s.getMonthlyPayment()
							+ " monthly, $" + interest + " interest payment/unit ");

					Finalsubtotal += subtotal;
					FinalTax += tax;
					Total += total; // All the necessity of sale agreement + general customer

				} else if (k.getType().equals("P")) { // Parking Pass
					ParkingPass pp = (ParkingPass) k;
					double subtotal = pp.getSubtotal();
					double tax = pp.getTax();
					double total = subtotal + tax;
					int freePasses = 0;
					if (pp.getProduct() != null) {
						freePasses = pp.getProduct().getQuantity();
					}

					int Count = 0;

					for (int y = 0; y < i.getProductCount(); y++) {
						if (i.getProducts().get(y) instanceof LeaseAgreement
								|| i.getProducts().get(y) instanceof SaleAgreement) {
							Count++;// count if the products contains LeaseAgreements or Sales Agreement
						}
					}

					if (Count < 1) {

						System.out.print(String.format("\n%-9s Parking Pass (%d units @ $%.1f)", pp.getProductCode(),
								pp.getQuantity(), pp.getParkingFee()));

					} else {

						if (pp.getQuantity() < freePasses) {
							freePasses = pp.getQuantity();
							subtotal = 0;
							tax = 0;
							total = 0;

							System.out.print(String.format("\n%1s      Parking Pass %s (%d units @ $%.1f with %d free)",
									pp.getProductCode(), pp.getProduct().getProductCode(), pp.getQuantity(),
									pp.getParkingFee(), freePasses));
						} else {

							subtotal -= freePasses * pp.getParkingFee();
							tax = 0.04 * subtotal;
							total = subtotal + tax;

							System.out.print(String.format("\n%1s      Parking Pass (%d units @ $%.1f with %d free)",
									pp.getProductCode(), pp.getQuantity(), pp.getParkingFee(), freePasses));
						}

					}

					System.out.println(String.format("\t %30.2f $ %13.2f $ %13.2f $", subtotal, tax, total));

					Finalsubtotal += subtotal;
					FinalTax += tax;
					Total += total;

					// ^^ All the intricacies of the parkingPass class tax counter

				} else if (k.getType().equals("A")) {
					Amenity a = (Amenity) k;
					double subtotal = a.getSubtotal();
					double tax = a.getTax();
					double total = subtotal + tax;

					int Count = 0;

					for (int y = 0; y < i.getProductCount(); y++) {
						if (i.getProducts().get(y) instanceof LeaseAgreement) {
							Count++;// count if the products contains LeaseAgreements than 5% discount
						}
					}

					if (Count >= 1) { // if Loop to check through the count
						subtotal = a.getSubtotal1();
						tax = a.getTax1();
						total = subtotal + tax;

						System.out.print(String.format("\n%s      %s (%d units @ %.2f /unit 5%% off)",
								a.getProductCode(), a.getName(), a.getQuantity(), a.getCost()));
					} else {

						System.out.print(String.format("\n%s      %s (%d units @ $%.1f /unit)", a.getProductCode(),
								a.getName(), a.getQuantity(), a.getCost()));

					}

					System.out.println(String.format("\t %38.2f $ %13.2f $ %13.2f $", subtotal, tax, total)); // Formatting

					Finalsubtotal += subtotal;
					FinalTax += tax;
					Total += total;

					// All the necessities for the amenities class
				}
			} else if (i.getCustomer().getType().equals("L")) { // Customer type is L
				if (k.getType().equals("L")) { // lease agreement checker
					LeaseAgreement l = (LeaseAgreement) k;
					double subtotal = l.getSubtotal(i.getDate());
					double tax = 0.0;
					double total = subtotal + tax;
					System.out.println(String.format("\n%-9s Lease Agreement @ %-56s %10.2f $   %11.2f $ %13.2f $",
							k.getProductCode(), l.getAddress().getStreet(), subtotal, tax, total));

					System.out.println("          " + l.getStartDate() + " (" + l.getQuantity() + " units @ $"
							+ l.getMonthlyCost() + " /unit)");

					Finalsubtotal += subtotal;
					FinalTax += tax;
					Total += total;

					// Lease Agreement necessities printed
				} else if (k.getType().equals("S")) {
					SaleAgreement s = (SaleAgreement) k;
					double subtotal = s.getSubtotal(i.getDate());
					double tax = 0.0;
					double total = subtotal + tax;
					int interest = (int) (s.getInterestRate() * s.getMonthlyPayment() / 100);

					System.out.println(String.format("\n%-9s Sale Agreement @ %-57s %10.2f $ %13.2f $ %13.2f $",
							k.getProductCode(), s.getAddress().getStreet(), subtotal, tax, total));

					System.out.println("          " + s.getQuantity() + " units @ $" + s.getMonthlyPayment()
							+ " monthly, $" + interest + " interest payment/unit ");

					Finalsubtotal += subtotal;
					FinalTax += tax;
					Total += total;

					// Sale agreement necessities printed

				} else if (k.getType().equals("P")) {
					ParkingPass pp = (ParkingPass) k;
					double subtotal = pp.getSubtotal();
					double tax = 0.0;
					double total = subtotal + tax;
					int freePasses = 0;
					if (pp.getProduct() != null) {
						freePasses = pp.getProduct().getQuantity();
					}

					int Count = 0;

					for (int y = 0; y < i.getProductCount(); y++) {
						if (i.getProducts().get(y) instanceof LeaseAgreement
								|| i.getProducts().get(y) instanceof SaleAgreement) {
							Count++;// count if the products contains LeaseAgreement or Sales Agreement
						}
					}

					if (Count < 1) {

						System.out.print(String.format("\n%1s      Parking Pass (%d units @ $%.1f) \t",
								pp.getProductCode(), pp.getQuantity(), pp.getParkingFee()));

					} else {

						if (pp.getQuantity() < freePasses) {
							freePasses = pp.getQuantity();
							subtotal = 0;
							tax = 0;
							total = 0;

							System.out.print(String.format("\n%1s      Parking Pass %s (%d units @ $%.1f with %d free)",
									pp.getProductCode(), pp.getProduct().getProductCode(), pp.getQuantity(),
									pp.getParkingFee(), freePasses));
						} else {

							subtotal -= freePasses * pp.getParkingFee();
							total = subtotal + tax;

							System.out.print(String.format("\n%1s      Parking Pass (%d units @ $%.1f with %d free)",
									pp.getProductCode(), pp.getQuantity(), pp.getParkingFee(), freePasses));
						}

					}

					System.out.println(String.format("\t \t %30.2f $ %13.2f $ %13.2f $", subtotal, tax, total));

					Finalsubtotal += subtotal;
					FinalTax += tax;
					Total += total;

				} else if (k.getType().equals("A")) {
					Amenity a = (Amenity) k;
					double subtotal = a.getSubtotal();
					double tax = 0.0;
					double total = subtotal + tax;

					int Count = 0;

					for (int y = 0; y < i.getProductCount(); y++) {
						if (i.getProducts().get(y) instanceof LeaseAgreement) {
							Count++;// count if the products contains LeaseAgreements than 5% discount
						}
					}

					if (Count >= 1) {
						subtotal = a.getSubtotal1();
						tax = 0.0;
						total = subtotal + tax;

						System.out.print(String.format("\n%s      %s (%d units @ %.2f /unit 5%% off)      ",
								a.getProductCode(), a.getName(), a.getQuantity(), a.getCost()));
					} else {

						System.out.print(String.format("\n%s      %s (%d units @ $%.1f /unit) \t", a.getProductCode(),
								a.getName(), a.getQuantity(), a.getCost()));

					}

					System.out.println(String.format("\t %38.2f $ %13.2f $ %13.2f $", subtotal, tax, total));

					Finalsubtotal += subtotal;
					FinalTax += tax;
					Total += total;

				}
			}
		}
		System.out.println("\t\t\t\t\t\t\t\t\t\t     ============================================");
		System.out.printf("\nSUBTOTALS %85.2f $ %13.2f $ %13.2f $", Finalsubtotal, FinalTax, Total);

		if (i.getCustomer().getType().equals("G")) {
			System.out.printf("\nTOTAL %121.2f $", Total);
		} else if (i.getCustomer().getType().equals("L")) {

			int Count = 0;
			double Discount = 0.0;

			for (int y = 0; y < i.getProductCount(); y++) {
				if (i.getProducts().get(y) instanceof LeaseAgreement
						|| i.getProducts().get(y) instanceof SaleAgreement) {
					Count++;// count if the products contains LeaseAgreement or Sales Agreement
				}
			}

			if (Count != 0) {

				Discount = (Total * 0.1 + 1000) * -1;

				System.out.printf("\nDISCOUNT (10%% LOW INCOME + $1000 HOUSING CREDIT) %78.2f $", Discount);
				// Discount for low-income & housing customers

			} else {

				Discount = (Total * 0.1) * -1;

				System.out.printf("\nDISCOUNT (10%% LOW INCOME) %101.2f $", Total * -0.1);

			}

			double additionalFee = 50.75;

			System.out.printf("\nADDITIONAL FEE (LOW INCOME) %99.2f $", additionalFee);

			Total += Discount + additionalFee;
			System.out.printf("\nTOTAL %121.2f $", Total);
			// Additional fee for the low-income customers
		}

		System.out.println("\n				Thank you for your purchase! \n"); // Thank you statement!

	} // Method
}