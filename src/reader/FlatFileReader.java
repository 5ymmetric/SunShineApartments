package reader;

// Import files
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import entities.Address;
import entities.Amenity;
import entities.Customer;
import entities.GeneralCustomer;
import entities.Invoice;
import entities.LeaseAgreement;
import entities.LowIncomeCustomer;
import entities.ParkingPass;
import entities.Person;
import entities.Product;
import entities.SaleAgreement;

public class FlatFileReader {

	// Reads persons
	public ArrayList<Person> readPersons() {

		Scanner s = null;

		try { // Try statement for the file
			s = new Scanner(new File("data/Persons.dat"));
			s.nextLine();

			ArrayList<Person> personList = new ArrayList<Person>();

			while (s.hasNext()) { // While loop that adds certain parts of data to a list
				String line = s.nextLine();
				String data[] = line.split(";"); // Splits the line based on ";"
				String personCode = data[0];

				String name[] = data[1].split(","); // Name split by first/last name

				String firstName = name[1];
				String lastName = name[0];

				String address = data[2];

				String temp[] = address.split(","); // Address split

				// Various parts of the address
				String street = temp[0];
				String city = temp[1];
				String state = temp[2];
				String zip = temp[3];
				String country = temp[4];

				// Array list for the email addresses
				Set<String> email = new HashSet<String>();

				if (data.length == 4) {

					String temporary[] = data[3].split(",");

					for (int i = 0; i < temporary.length; i++) {
						email.add(temporary[i]);
					}
				}

				Address newAddress = new Address(street, city, state, zip, country); // Address object

				Person newPerson = new Person(personCode, firstName, lastName, newAddress, email);

				personList.add(newPerson); // Adds new person
			}
			s.close();

			return personList;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

	}

	// Method for reading customers
	public ArrayList<Customer> readCustomers() {

		Scanner s = null;

		try {
			s = new Scanner(new File("data/Customers.dat"));
			s.nextLine();

			// Array list of customers
			ArrayList<Customer> customerList = new ArrayList<Customer>();

			// While loop to extract customers
			while (s.hasNext()) {
				String line = s.nextLine();
				String data[] = line.split(";");

				String customerCode = data[0];
				String type = data[1];

				// Persons ArrayList
				ArrayList<Person> personList = new ArrayList<Person>();
				personList = readPersons();

				Person newPerson = null;

				for (int i = 0; i < personList.size(); i++) {
					if (personList.get(i).getPersonCode().equals(data[2])) {
						newPerson = personList.get(i);
					}
				}

				String customerName = data[3];

				String address = data[4];

				String temp[] = address.split(",");

				String street = temp[0];
				String city = temp[1];
				String state = temp[2];
				String zip = temp[3];
				String country = temp[4];

				Address newAddress = new Address(street, city, state, zip, country);

				// if blocks to differentiate between types of customers
				if (type.equals("G")) {
					GeneralCustomer newCustomer = new GeneralCustomer(customerCode, type, newPerson, customerName,
							newAddress);

					customerList.add(newCustomer);
				}

				if (type.equals("L")) {
					LowIncomeCustomer newCustomer = new LowIncomeCustomer(customerCode, type, newPerson, customerName,
							newAddress);

					customerList.add(newCustomer);
				}

			}
			s.close();

			return customerList;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

	}

	// Method to read products
	public ArrayList<Product> readProducts() {

		Scanner s = null;

		try {
			s = new Scanner(new File("data/Products.dat"));
			s.nextLine();

			ArrayList<Product> productList = new ArrayList<Product>();

			while (s.hasNext()) {
				String line = s.nextLine();
				String data[] = line.split(";");

				String productCode = data[0];
				String type = data[1];

				// If blocks to differentiate between various products
				// Lease agreements
				if (type.equals("L")) {
					
					String Startdate = data[2];

					LocalDate startDate = LocalDate.parse(Startdate);

					String Enddate = data[3];

					LocalDate endDate = LocalDate.parse(Enddate);

					String address = data[4];

					String temp[] = address.split(",");

					String street = temp[0];
					String city = temp[1];
					String state = temp[2];
					String zip = temp[3];
					String country = temp[4];

					Address newAddress = new Address(street, city, state, zip, country);

					String customerName = data[5];

					ArrayList<Customer> customerList = new ArrayList<Customer>();

					customerList = readCustomers();

					Customer newCustomer = null;

					for (Customer aCustomer : customerList) {
						if (aCustomer.getName().equals(customerName)) {
							newCustomer = aCustomer;
						}
					}

					double deposit = Double.parseDouble(data[6]);
					double monthlyCost = Double.parseDouble(data[7]);

					LeaseAgreement leaseAgreement = new LeaseAgreement(productCode, type, startDate, endDate,
							newAddress, newCustomer, deposit, monthlyCost);

					productList.add(leaseAgreement);
				}

				// Sale agreements
				if (type.equals("S")) {
					
					String Startdate = data[2];

					String temp1[] = Startdate.split(" ");
					String date = temp1[0];
					String time = temp1[1];
					
					String temp2[] = date.split("-");
					int year = Integer.parseInt(temp2[0]);
					int month = Integer.parseInt(temp2[1]);
					int day = Integer.parseInt(temp2[2]);
					
					String temp3[] = time.split(":");
					int hours = Integer.parseInt(temp3[0]);
					int minutes = Integer.parseInt(temp3[1]);
					
					LocalDateTime dateTime = LocalDateTime.of(year, month, day, hours, minutes);

					String address = data[3];

					String temp[] = address.split(",");

					String street = temp[0];
					String city = temp[1];
					String state = temp[2];
					String zip = temp[3];
					String country = temp[4];

					Address newAddress = new Address(street, city, state, zip, country); // New address

					double cost = Double.parseDouble(data[4]);
					double downPayment = Double.parseDouble(data[5]);
					double monthlyPayment = Double.parseDouble(data[6]);
					int payableMonths = Integer.parseInt(data[7]);
					double interestRate = Double.parseDouble(data[8]);

					SaleAgreement saleAgreement = new SaleAgreement(productCode, type, dateTime, newAddress, cost,
							downPayment, monthlyPayment, payableMonths, interestRate);

					productList.add(saleAgreement);
				}

				// Amenity
				if (type.equals("A")) {
					String name = data[2];
					double cost = Double.parseDouble(data[3]);

					Amenity amenity = new Amenity(productCode, type, name, cost);

					productList.add(amenity);
				}

				// Parking Pass
				if (type.equals("P")) {
					double fee = Double.parseDouble(data[2]);// parser

					ParkingPass parkingPass = new ParkingPass(productCode, type, fee);

					productList.add(parkingPass);
				}
			}
			s.close();

			return productList;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

	}

	public ArrayList<Invoice> readInvoices() {

		Scanner s = null;

		try { // Try statement for the file
			s = new Scanner(new File("data/Invoices.dat"));
			s.nextLine();

			ArrayList<Invoice> invoiceList = new ArrayList<Invoice>();

			while (s.hasNext()) { // While loop that adds certain parts of data to a list
				String line = s.nextLine();
				String data[] = line.split(";"); // Splits the line based on ";"

				ArrayList<Product> productsList = new ArrayList<Product>();
				ArrayList<Product> allProducts = readProducts();
				ArrayList<Integer> quantities = new ArrayList<Integer>();

				String invoiceCode = data[0];

				String customerCode = data[1];

				ArrayList<Customer> customerList = new ArrayList<Customer>();

				customerList = readCustomers();

				Customer newCustomer = null;

				for (Customer aCustomer : customerList) {
					if (aCustomer.getCustomerCode().equals(customerCode)) {
						newCustomer = aCustomer;
					}
				}

				if (newCustomer.getType().equals("G")) {
					GeneralCustomer newGeneralCustomer = (GeneralCustomer) newCustomer;
					newCustomer = newGeneralCustomer;

				} else if (newCustomer.getType().equals("L")) {
					LowIncomeCustomer newLowIncomeCustomer = (LowIncomeCustomer) newCustomer;
					newCustomer = newLowIncomeCustomer;
				}

				String realtor = data[2];

				ArrayList<Person> personList = new ArrayList<Person>();
				personList = readPersons();

				Person newPerson = null;

				for (int i = 0; i < personList.size(); i++) {
					if (personList.get(i).getPersonCode().equals(realtor)) {
						newPerson = personList.get(i);
					}
				}

				LocalDate date = LocalDate.parse(data[3]);

				String productList = data[4];

				String data2[] = productList.split(",");
				int productCount = data2.length;

				for (int i = 0; i < data2.length; i++) {
					String products = data2[i];
					String temp[] = products.split(":");

					String product = temp[0];
					int quantity = Integer.parseInt(temp[1]);

					Product newProduct = null;
					
					for (Product aProduct : allProducts) {
						if (aProduct.getProductCode().equals(product)) {
							newProduct = aProduct;
						}
					}

					if (newProduct.getType().equals("L")) {
						LeaseAgreement newLeaseAgreement = (LeaseAgreement) newProduct;
						newProduct.setQuantity(quantity);

						productsList.add(newLeaseAgreement);
					} else if (newProduct.getType().equals("S")) {
						SaleAgreement newSaleAgreement = (SaleAgreement) newProduct;
						newProduct.setQuantity(quantity);
						
						productsList.add(newSaleAgreement);
					} else if (newProduct.getType().equals("P")) {
						ParkingPass newParkingPass = (ParkingPass) newProduct;
						newProduct.setQuantity(quantity);
						
						if(temp.length > 2) {
							String newProductCode = temp[2];
							
							Product agreement = null;

							for (Product aProduct : allProducts) {
								if (aProduct.getProductCode().equals(newProductCode)) {
									agreement = aProduct;
								}
							}

							if (agreement.getType().equals("L")) {
								LeaseAgreement newAgreement = (LeaseAgreement) agreement;
								
								newAgreement.setQuantity(agreement.getQuantity());

								newParkingPass.setProduct(newAgreement);
							} else if (agreement.getType().equals("S")) {
								SaleAgreement newSAgreement = (SaleAgreement) agreement;
								
								newSAgreement.setQuantity(agreement.getQuantity());

								newParkingPass.setProduct(newSAgreement);
							}

						}
						
						productsList.add(newParkingPass);
					} else if (newProduct.getType().equals("A")) {
						Amenity newAmenity = (Amenity) newProduct;
						newProduct.setQuantity(quantity);
						
						productsList.add(newAmenity);
					}

					quantities.add(quantity);

				}
				System.out.println(productsList.size());
				Invoice newInvoice = new Invoice(invoiceCode, date, newCustomer, newPerson, productsList, quantities, productCount);

				invoiceList.add(newInvoice);

			}
			s.close();

			return invoiceList;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

	}

}