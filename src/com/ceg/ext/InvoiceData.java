package com.ceg.ext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import driver.Connector;

/*
* This is a collection of utility methods that define a general API for
* interacting with the database supporting this application.
* 15 methods in total, add more if required.
* Do not change any method signatures or the package name.
* 
*/

public class InvoiceData {
	
	public static Logger log = Logger.getLogger(InvoiceData.class);
	
	
	/**
	* 1. Method that removes every person record from the database
	*/
	public static void removeAllPersons() {
		
		//Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		//Defines a list of Strings which will represent personCodes of individuals to remove from the database
		List<String> personCodesList = new ArrayList<String>();
		
		//Pulls from the database the personCodes of every Person in the Person table
		String getAllPersonsQuery = "SELECT personCode FROM Person";
		PreparedStatement psGetAllPersons = null;
		ResultSet rsGetAllPersons = null;
		
		try {
			psGetAllPersons = connect.prepareStatement(getAllPersonsQuery);
			rsGetAllPersons = psGetAllPersons.executeQuery();
			
			//Adds each personCode to the list of personCodes
			while (rsGetAllPersons.next()) {
				String personCode = rsGetAllPersons.getString("personCode");
				personCodesList.add(personCode);
			}
			
			//Closes resources
			if (psGetAllPersons != null && !psGetAllPersons.isClosed())
			psGetAllPersons.close();
			
			if (rsGetAllPersons != null && !rsGetAllPersons.isClosed())
			rsGetAllPersons.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Could not load personCodes from database",e);
			throw new RuntimeException(e);
		}
		
		//Utilizes the removePerson method to remove each person from the database, using the list of personCodes
		for (String personCode : personCodesList) {
			removePerson(personCode);
		}
	}
	
	/**
	* Returns the personId associated with a specific personCode
	* 
	* @param personCode
	* @return
	*/
	public static int getPersonId(String personCode) {
		
		Connection connect = Connector.getConnection();
		
		int personId;
		String queryPerson = "SELECT personId from Person where personCode = ?";
		PreparedStatement psPerson = null;
		ResultSet rsPerson = null;
		try {
			psPerson = connect.prepareStatement(queryPerson);
			psPerson.setString(1, personCode);
			rsPerson = psPerson.executeQuery();
			rsPerson.next();
			personId = rsPerson.getInt("personId");
			
			if (psPerson != null && !psPerson.isClosed())
			psPerson.close();
			
			if (rsPerson != null && !rsPerson.isClosed())
			rsPerson.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		try {
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return personId;
	}
	
	/**
	* Removes the person record from the database corresponding to the provided
	* <code>personCode</code>
	* 
	* @param personCode
	*/
	public static void removePerson(String personCode) {
		
		//Establishes the connection to the database and identifies the personId of the Person to be removed
		Connection connect = Connector.getConnection();
		int personId = getPersonId(personCode);
		
		//Identifies customers having the Person as their primary contact and removes them from the database
		String removeCustomersQuery = "SELECT customerCode FROM Customer WHERE primaryContact = ?";
		PreparedStatement psRemoveCustomers = null;
		ResultSet rsRemoveCustomers = null;
		
		try {
			psRemoveCustomers = connect.prepareStatement(removeCustomersQuery);
			psRemoveCustomers.setInt(1, personId);
			rsRemoveCustomers = psRemoveCustomers.executeQuery();
			
			while (rsRemoveCustomers.next()) {
				removeCustomer(rsRemoveCustomers.getString("customerCode"));
			}
			
			if (psRemoveCustomers != null && !psRemoveCustomers.isClosed())
			psRemoveCustomers.close();
			
		} catch (SQLException e) {
			log.error("Error Removing Customers Associated with Person to be Removed",e);
			throw new RuntimeException(e);
		}
		
		//Identifies invoices having the Person as their realtor and removes them from the database
		String getInvoicesQuery = "SELECT invoiceCode FROM Invoice where realtorId = ?";
		PreparedStatement psGetInvoices = null;
		ResultSet rsGetInvoices = null;
		
		try {
			psGetInvoices = connect.prepareStatement(getInvoicesQuery);
			psGetInvoices.setInt(1, personId);
			rsGetInvoices = psGetInvoices.executeQuery();
			
			while (rsGetInvoices.next()) {
				removeInvoice(rsGetInvoices.getString("invoiceCode"));
			}
			
			if (psGetInvoices != null && !psGetInvoices.isClosed())
			psGetInvoices.close();
			
			if (rsGetInvoices != null && !rsGetInvoices.isClosed())
			rsGetInvoices.close();
			
		} catch (SQLException e) {
			log.error("Error Removing Invoices Associated with Person to be Removed",e);
			throw new RuntimeException(e);
		}
		
		// Removes email addresses associated with the Person
		String getEmailsQuery = "SELECT pe.personEmailId, e.emailId FROM Person p "
		+ "LEFT JOIN PersonEmail pe ON pe.personId = p.personId "
		+ "LEFT JOIN Email e ON e.emailId = pe.emailId " + "WHERE p.personId = ?";
		PreparedStatement psGetEmails = null;
		ResultSet rsGetEmails = null;
		
		try {
			
			psGetEmails = connect.prepareStatement(getEmailsQuery);
			psGetEmails.setInt(1, personId);
			rsGetEmails = psGetEmails.executeQuery();
			
			while (rsGetEmails.next()) {
				
				// Deletes the PersonEmail instance from the join table
				String deletePersonEmails = "DELETE FROM PersonEmail WHERE personEmailId = ?";
				PreparedStatement psDeletePersonEmails = connect.prepareStatement(deletePersonEmails);
				psDeletePersonEmails.setInt(1, rsGetEmails.getInt("personEmailId"));
				psDeletePersonEmails.executeUpdate();
				
				if (psDeletePersonEmails != null && !psDeletePersonEmails.isClosed())
				psDeletePersonEmails.close();
				
				// Deletes one of the Person's Email Addresses from the database
				String deleteEmails = "DELETE FROM Email WHERE emailId = ?";
				PreparedStatement psDeleteEmails = connect.prepareStatement(deleteEmails);
				psDeleteEmails.setInt(1, rsGetEmails.getInt("emailId"));
				psDeleteEmails.executeUpdate();
				
				if (psDeleteEmails != null && !psDeleteEmails.isClosed())
				psDeleteEmails.close();
				
			}
			
		} catch (SQLException e) {
			log.error("Error Removing Email Addresses Associated with Person to be Removed",e);
			throw new RuntimeException(e);
		}
		
		// Determines the address of the Person (this cannot be determined after the person is removed)
		int addressId = 0;
		String addressQuery = "SELECT addressId from Person WHERE personCode = ?";
		PreparedStatement psAddress = null;
		ResultSet rsAddress = null;
		
		try {
			
			psAddress = connect.prepareStatement(addressQuery);
			psAddress.setString(1, personCode);
			rsAddress = psAddress.executeQuery();
			
			if (rsAddress.next()) 
			addressId = rsAddress.getInt("addressId");
			
			if (psAddress != null && !psAddress.isClosed())
			psAddress.close();
			
			if (rsAddress != null && !rsAddress.isClosed())
			rsAddress.close();
			
		} catch (SQLException e) {
			log.error("Error Determining Address of Person to be removed");
			throw new RuntimeException(e);
		}
		
		// Removes the Person from the database
		String deletePersonQuery = "DELETE FROM Person WHERE personCode = ?";
		PreparedStatement psDeletePerson = null;
		
		try {
			psDeletePerson = connect.prepareStatement(deletePersonQuery);
			psDeletePerson.setString(1, personCode);
			psDeletePerson.executeUpdate();
			
			if (psDeletePerson != null && !psDeletePerson.isClosed())
			psDeletePerson.close();
			
			if (connect != null && !connect.isClosed()) {
				connect.close();
			}
			
		} catch (SQLException e) {
			log.error("Error Deleting Person from Database",e);
			throw new RuntimeException(e);
		}
		
		/* Uses the previously determined addressId value to remove the Person's address from the database if it is
		no longer in use */
		deleteAddressIfNotInUse(addressId);
		
	}
	
	
	/**
	* 2. Method to add a person record to the database with the provided data.
	* 
	* @param personCode
	* @param firstName
	* @param lastName
	* @param street
	* @param city
	* @param state
	* @param zip
	* @param country
	*/
	public static void addPerson(String personCode, String firstName, String lastName, String street, String city, String state, String zip, String country) {
		
		/* Calls a method to either fetch the addressId of the Person's address from the database, or add it and 
		then return the id if not already present */
		int addressId = makeAddress(street, city, state, zip, country);
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Adds the Person to the Database with the Appropriate Column Values
		String queryPerson = "INSERT INTO Person(personCode, firstName, lastName, addressId) VALUES (?, ?, ?, ?)";
		PreparedStatement psAddPerson = null;
		
		try {
			
			psAddPerson = connect.prepareStatement(queryPerson);
			psAddPerson.setString(1, personCode);
			psAddPerson.setString(2, firstName);
			psAddPerson.setString(3, lastName);
			psAddPerson.setInt(4, addressId);
			psAddPerson.executeUpdate();
			
			if (psAddPerson != null && !psAddPerson.isClosed())
			psAddPerson.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error Adding Person to Database",e);
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	* 3. Adds an email record corresponding person record corresponding to the
	* provided <code>personCode</code>
	* 
	* @param personCode
	* @param email
	*/
	public static void addEmail(String personCode, String email) {
		
		//Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Determines the personId of the Person the email belongs to
		int personId = getPersonId(personCode);
		
		//Inserts the Email Address into the Database
		int emailId;
		String emailQuery = "INSERT INTO Email(address) VALUES(?)";
		
		try {
			
			PreparedStatement psEmail = null;
			psEmail = connect.prepareStatement(emailQuery);
			psEmail.setString(1, email);
			psEmail.executeUpdate();
			
			if (psEmail != null && !psEmail.isClosed())
			psEmail.close();
			
			PreparedStatement psGetLastEmail = null;
			psGetLastEmail = connect.prepareStatement("SELECT LAST_INSERT_ID()");
			ResultSet rsGetLastEmail = psGetLastEmail.executeQuery();
			
			//Determines the value of the emailId of the email just added
			if (rsGetLastEmail.next()) {
				emailId = rsGetLastEmail.getInt("LAST_INSERT_ID()");
			} else {
				log.error("Email was not inserted properly");
				throw new RuntimeException("Email was not inserted properly");
			}
			
			if (psGetLastEmail != null && !psGetLastEmail.isClosed())
			psGetLastEmail.close();
			
			if (rsGetLastEmail != null && !rsGetLastEmail.isClosed())
			rsGetLastEmail.close();
			
		} catch (SQLException e) {
			log.error("Error Inserting Email into Database",e);
			throw new RuntimeException(e);
		}
		
		// Inserts a new PersonEmail into the Database, Linking the Correct Person to the Inserted Email Address
		String personEmailQuery = "INSERT INTO PersonEmail(personId, emailId) VALUES(?, ?)";
		
		try {
			PreparedStatement psPersonEmail = null;
			psPersonEmail = connect.prepareStatement(personEmailQuery);
			psPersonEmail.setInt(1, personId);
			psPersonEmail.setInt(2, emailId);
			psPersonEmail.executeUpdate();
			
			if (psPersonEmail != null && !psPersonEmail.isClosed())
			psPersonEmail.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error inserting PersonEmail");
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	* 4. Method that removes every customer record from the database
	*/
	public static void removeAllCustomers() {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Creates a list which will be filled with the customerCodes of customers to remove from the database
		List<String> customerCodeList = new ArrayList<String>();
		
		// Returns the Customer Codes of Every Customer in the Database
		String getAllCustomersQuery = "SELECT customerCode FROM Customer";
		PreparedStatement psGetAllCustomers = null;
		ResultSet rsGetAllCustomers = null;
		
		try {
			
			psGetAllCustomers = connect.prepareStatement(getAllCustomersQuery);
			rsGetAllCustomers = psGetAllCustomers.executeQuery();
			
			// Adds all customerCodes for customers in the database into the list of customerCodes of customers to be removed
			while (rsGetAllCustomers.next()) {
				customerCodeList.add((rsGetAllCustomers.getString("customerCode")));
			}
			
			if (psGetAllCustomers != null && !psGetAllCustomers.isClosed())
			psGetAllCustomers.close();
			
			if (rsGetAllCustomers != null && !rsGetAllCustomers.isClosed())
			rsGetAllCustomers.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Could not load customerCodes from database",e);
			throw new RuntimeException(e);
		}
		
		// Removes each customer from the database by calling the removeCustomer method
		for (String customerCode : customerCodeList) {
			removeCustomer(customerCode);
		}
		
	}
	
	/**
	* Returns the customerId associated with a specific customerCode
	* 
	* @param customerCode
	* @return
	*/
	public static int getCustomerId(String customerCode) {
		
		Connection connect = Connector.getConnection();
		
		int customerId;
		String queryCustomer = "SELECT customerId from Customer where customerCode = ?";
		PreparedStatement psCustomer = null;
		ResultSet rsCustomer = null;
		
		try {
			psCustomer = connect.prepareStatement(queryCustomer);
			psCustomer.setString(1, customerCode);
			rsCustomer = psCustomer.executeQuery();
			rsCustomer.next();
			
			customerId = rsCustomer.getInt("customerId");
			
			if (psCustomer != null && !psCustomer.isClosed())
			psCustomer.close();
			
			if (rsCustomer != null && !rsCustomer.isClosed())
			rsCustomer.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return customerId;
	}
	
	/**
	* Returns the customerId associated with a specific customerName
	* 
	* @param customerCode
	* @return
	*/
	public static int getCustomerIdWithName(String customerName) {
		
		Connection connect = Connector.getConnection();
		
		int customerId;
		String queryCustomer = "SELECT customerId from Customer where name = ?";
		PreparedStatement psCustomer = null;
		ResultSet rsCustomer = null;
		
		try {
			psCustomer = connect.prepareStatement(queryCustomer);
			psCustomer.setString(1, customerName);
			rsCustomer = psCustomer.executeQuery();
			rsCustomer.next();
			
			customerId = rsCustomer.getInt("customerId");
			
			if (psCustomer != null && !psCustomer.isClosed())
			psCustomer.close();
			
			if (rsCustomer != null && !rsCustomer.isClosed())
			rsCustomer.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return customerId;
	}
	
	
	/**
	* This method removes a customer record from the database
	*/
	public static void removeCustomer(String customerCode) {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Determines the customerId of the customer to be removed from the database
		int customerId = getCustomerId(customerCode);
		
		// Determines the invoiceCodes of all invoices belonging to the customer to be deleted
		String deleteInvoicesQuery = "SELECT invoiceCode FROM Invoice WHERE customerId = ?";
		PreparedStatement psDeleteInvoices = null;
		ResultSet rsDeleteInvoices = null;
		
		try {
			psDeleteInvoices = connect.prepareStatement(deleteInvoicesQuery);
			psDeleteInvoices.setInt(1, customerId);
			rsDeleteInvoices = psDeleteInvoices.executeQuery();
			
			// Deletes each invoice belonging to the customer from the database using the removeInvoice method
			while (rsDeleteInvoices.next()) {
				removeInvoice(rsDeleteInvoices.getString("invoiceCode"));
			}
			
			if (psDeleteInvoices != null && !psDeleteInvoices.isClosed())
			psDeleteInvoices.close();
			
			if (rsDeleteInvoices != null && !rsDeleteInvoices.isClosed())
			rsDeleteInvoices.close();
			
		} catch (SQLException e) {
			log.error("Error Removing Invoices Associated with Customer to be Deleted",e);
			throw new RuntimeException(e);
		}
		
		// Determines the productCodes of all products belonging to the customer to be deleted
		String deleteProductsQuery = "SELECT productCode FROM Product WHERE customerId = ?";
		PreparedStatement psDeleteProducts = null;
		ResultSet rsDeleteProducts = null;
		
		try {
			psDeleteProducts = connect.prepareStatement(deleteProductsQuery);
			psDeleteProducts.setInt(1, customerId);
			rsDeleteProducts = psDeleteProducts.executeQuery();
			
			// Deletes each product belonging to the customer from the database using the removeProduct method
			while (rsDeleteProducts.next()) {
				removeProduct(rsDeleteProducts.getString("productCode"));
			}
			
			if (psDeleteProducts != null && !psDeleteProducts.isClosed())
			psDeleteProducts.close();
			
			if (rsDeleteProducts != null && !rsDeleteProducts.isClosed())
			rsDeleteProducts.close();
			
		} catch (SQLException e) {
			log.error("Error Removing Products Associated with Customer to be Deleted",e);
			throw new RuntimeException(e);
		}
		
		// Determines the addressId of the customer to be deleted (cannot be done after customer is deleted)
		String getAddressQuery = "SELECT addressId FROM Customer WHERE customerCode = ?";
		PreparedStatement psGetAddressId = null;
		ResultSet rsGetAddressId = null;
		int addressId;
		
		try {
			psGetAddressId = connect.prepareStatement(getAddressQuery);
			psGetAddressId.setString(1, customerCode);
			rsGetAddressId = psGetAddressId.executeQuery();
			rsGetAddressId.next();
			addressId = rsGetAddressId.getInt("addressId");
			
			if (psGetAddressId != null && !psGetAddressId.isClosed())
			psGetAddressId.close();
			
			if (rsGetAddressId != null && !rsGetAddressId.isClosed())
			rsGetAddressId.close();
			
		} catch (SQLException e) {
			log.error("Error Determining Address Associated with Customer to be Deleted",e);
			throw new RuntimeException(e);
		}
		
		// Deletes the customer
		String deleteCustomerQuery = "DELETE FROM Customer WHERE customerCode = ?";
		PreparedStatement psDeleteCustomer = null;
		
		try {
			
			psDeleteCustomer = connect.prepareStatement(deleteCustomerQuery);
			psDeleteCustomer.setString(1, customerCode);
			psDeleteCustomer.executeUpdate();
			
			if (psDeleteCustomer != null && !psDeleteCustomer.isClosed())
			psDeleteCustomer.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error removing customer",e);
			throw new RuntimeException(e);
		}
		
		// Deletes the address of the customer if it is no longer in use
		deleteAddressIfNotInUse(addressId);
		
	}
	
	
	public static void addCustomer(String customerCode, String customerType, String primaryContactPersonCode, String name, String street, String city, String state, String zip, String country) {
		
		// Determines the addressId of the customer's address, and creates the address if not already present
		int addressId = makeAddress(street, city, state, zip, country);
		
		// Determines the personId of the customer's primary contact
		int personId = getPersonId(primaryContactPersonCode);
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Adds a customer to the database with the given properties
		String queryCustomer = "INSERT INTO Customer(customerCode, customerType, primaryContact, name, addressId) VALUES(?, ?, ?, ?, ?)";
		PreparedStatement psAddCustomer = null;
		
		try {
			
			psAddCustomer = connect.prepareStatement(queryCustomer);
			psAddCustomer.setString(1, customerCode);
			psAddCustomer.setString(2, customerType);
			psAddCustomer.setInt(3, personId);
			psAddCustomer.setString(4, name);
			psAddCustomer.setInt(5, addressId);
			psAddCustomer.executeUpdate();
			
			if (psAddCustomer != null && !psAddCustomer.isClosed())
			psAddCustomer.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error Inserting Customer into Database",e);
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	* 5. Removes all product records from the database
	*/
	public static void removeAllProducts() {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Creates a list to be populated with the productCodes of every product in the database
		List<String> productCodeList = new ArrayList<String>();
		
		// Determine the productCodes of every product in the database
		String getAllProductsQuery = "SELECT productCode FROM Product";
		PreparedStatement psGetAllProducts = null;
		ResultSet rsGetAllProducts = null;
		
		try {
			psGetAllProducts = connect.prepareStatement(getAllProductsQuery);
			rsGetAllProducts = psGetAllProducts.executeQuery();
			
			// Adds the productCode of each product in the database to the list of productCodes
			while (rsGetAllProducts.next()) {
				productCodeList.add(rsGetAllProducts.getString("productCode"));
			}
			
			if (psGetAllProducts != null && !psGetAllProducts.isClosed())
			psGetAllProducts.close();
			
			if (rsGetAllProducts != null && !rsGetAllProducts.isClosed())
			rsGetAllProducts.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error Obtaining productCodes from Database",e);
			throw new RuntimeException(e);
		}
		
		// Removes each product from the database according to productCode, utilizing the removeProduct method
		for (String productCode : productCodeList) {
			removeProduct(productCode);
		}
	}
	
	/**
	* Removes a particular product record from the database corresponding to the
	* provided <code>productCode</code>
	* 
	* @param assetCode
	*/
	public static void removeProduct(String productCode) {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Determines the productId of the product to be deleted
		String getProductIdQuery = "SELECT productId FROM Product WHERE productCode = ?";
		PreparedStatement psGetProductId = null;
		ResultSet rsGetProductId = null;
		int productId;
		
		try {
			
			psGetProductId = connect.prepareStatement(getProductIdQuery);
			psGetProductId.setString(1, productCode);
			rsGetProductId = psGetProductId.executeQuery();
			
			if (rsGetProductId.next()) {
				productId = rsGetProductId.getInt("productId");
			} else {
				return;
			}
			
			if (psGetProductId != null && !psGetProductId.isClosed())
			psGetProductId.close();
			
			if (rsGetProductId != null && !rsGetProductId.isClosed())
			rsGetProductId.close();
			
		} catch (SQLException e) {
			log.error("Error Determining ProductId of Product to be Deleted",e);
			throw new RuntimeException(e);
		}
		
		// Deletes invoiceItems from the database corresponding to the product to be deleted
		String deleteInvoiceItemQuery = "DELETE FROM InvoiceItem WHERE productId = ? OR agreement = ?";
		PreparedStatement psDeleteInvoiceItem = null;
		
		try {
			
			psDeleteInvoiceItem = connect.prepareCall(deleteInvoiceItemQuery);
			psDeleteInvoiceItem.setInt(1, productId);
			psDeleteInvoiceItem.setInt(2, productId);
			psDeleteInvoiceItem.executeUpdate();
			
			if (psDeleteInvoiceItem != null && !psDeleteInvoiceItem.isClosed())
			psDeleteInvoiceItem.close();
			
		} catch (SQLException e) {
			log.error("Error Deleting InvoiceItems from the Database Associated with Product to be Removed",e);
			throw new RuntimeException(e);
		}
		
		// Removes the product from the database
		String removeProductQuery = "DELETE FROM Product WHERE productCode = ?";
		PreparedStatement psRemoveProduct = null;
		
		try {
			psRemoveProduct = connect.prepareStatement(removeProductQuery);
			psRemoveProduct.setString(1, productCode);
			psRemoveProduct.executeUpdate();
			
			if (psRemoveProduct != null && !psRemoveProduct.isClosed())
			psRemoveProduct.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error Removing Product", e);
			throw new RuntimeException(e);
		}
		
	}
	
	
	/**
	* 6. Adds a SaleAgreement record to the database with the provided data.
	*/
	public static void addSaleAgreement(String productCode, String dateTime, String street, String city,String state, String zip, String country, double totalCost, double downPayment, double monthlyPayment, 
	int payableMonths, double interestRate) {
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		int addressId = makeAddress( street, city, state, zip, country);
		
		//Inserts product with type 'S' for saleAgreement into the database
		String saleAgreementQuery = "INSERT INTO Product(productCode, productType, moveinDate, addressId, totalCost, downPayment, monthlyPayment, payableMonths, interestRate) VALUES (?, 'S', ?, ?, ?, ?, ?, ?, ?)";
		
		try {
			PreparedStatement psSaleAgreement = null;
			psSaleAgreement = connect.prepareStatement(saleAgreementQuery);
			psSaleAgreement.setString(1, productCode);
			psSaleAgreement.setString(2, dateTime);
			psSaleAgreement.setInt(3, addressId);
			psSaleAgreement.setDouble(4, totalCost);
			psSaleAgreement.setDouble(5, downPayment);
			psSaleAgreement.setDouble(6, monthlyPayment);
			psSaleAgreement.setInt(7, payableMonths);
			psSaleAgreement.setDouble(8, interestRate);
			psSaleAgreement.executeUpdate();
			
			if (psSaleAgreement != null && !psSaleAgreement.isClosed())
			psSaleAgreement.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error Adding Sale Agreement");
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	* 7. Adds a LeaseAgreement record to the database with the provided data.
	*/
	public static void addLeaseAgreement(String productCode, String name, String startDate, String endDate, String street, String city, String state, String zip, String country, double deposit, double monthlyCost) {
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		int addressId = makeAddress( street, city, state, zip, country);
		int customerId = getCustomerIdWithName(name);
		
		//Inserts product with type 'L' for Lease Agreement into the database
		String leaseAgreementQuery = "INSERT INTO Product(productCode, productType, moveinDate, moveoutDate, addressId, customerId, deposit, monthlyPayment) VALUES (?, 'L', ?, ?, ?, ?, ?, ?)";
		
		try {
			PreparedStatement psLeaseAgreement = null;
			psLeaseAgreement = connect.prepareStatement(leaseAgreementQuery);
			psLeaseAgreement.setString(1, productCode);
			psLeaseAgreement.setString(2, startDate);
			psLeaseAgreement.setString(3, endDate);
			psLeaseAgreement.setInt(4, addressId);
			psLeaseAgreement.setInt(5, customerId);
			psLeaseAgreement.setDouble(6, deposit);
			psLeaseAgreement.setDouble(7, monthlyCost);
			psLeaseAgreement.executeUpdate();
			
			if (psLeaseAgreement != null && !psLeaseAgreement.isClosed())
			psLeaseAgreement.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error Adding Lease Agreement");
			throw new RuntimeException(e);
		}
	}
	
	/**
	* 8. Adds a ParkingPass record to the database with the provided data.
	*/
	public static void addParkingPass(String productCode, double parkingFee) {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		//Inserts product with type 'P' for Parking Pass into the database
		String parkingPassQuery = "INSERT INTO Product(productCode, productType, serviceFee) VALUES (?, 'P', ?)";
		
		try {
			PreparedStatement psParkingPass = null;
			psParkingPass = connect.prepareStatement(parkingPassQuery);
			psParkingPass.setString(1, productCode);
			psParkingPass.setDouble(2, parkingFee);
			psParkingPass.executeUpdate();
			
			if (psParkingPass != null && !psParkingPass.isClosed())
			psParkingPass.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error Adding Parking Pass");
			throw new RuntimeException(e);
		}
	}
	
	/**
	* 9. Adds an Amenity record to the database with the provided data.
	*/
	public static void addAmenity(String productCode, String name, double cost) {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		//Inserts product with type 'A' for Amenity into the database
		String amenityQuery = "INSERT INTO Product(productCode, productType, name, serviceFee) VALUES (?, 'A', ?, ?)";
		
		try {
			PreparedStatement psAmenity = null;
			psAmenity = connect.prepareStatement(amenityQuery);
			psAmenity.setString(1, productCode);
			psAmenity.setString(2, name);
			psAmenity.setDouble(3, cost);
			psAmenity.executeUpdate();
			
			if (psAmenity != null && !psAmenity.isClosed())
			psAmenity.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error Adding Amenity");
			throw new RuntimeException(e);
		}
	}
	
	/**
	* 10. Removes all invoice records from the database
	*/
	public static void removeAllInvoices() {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Retrieves the invoice codes of every invoice in the database
		String getAllInvoicesQuery = "SELECT invoiceCode FROM Invoice";
		PreparedStatement psGetAllInvoices = null;
		ResultSet rsGetAllInvoices = null;
		
		try {
			psGetAllInvoices = connect.prepareStatement(getAllInvoicesQuery);
			rsGetAllInvoices = psGetAllInvoices.executeQuery();
			
			// Utilizes the removeInvoice Method to remove each invoice from the database based on its invoiceCode
			while (rsGetAllInvoices.next()) {
				removeInvoice(rsGetAllInvoices.getString("invoiceCode"));
			}
			
			if (psGetAllInvoices != null && !psGetAllInvoices.isClosed())
			psGetAllInvoices.close();
			
			if (rsGetAllInvoices != null && !rsGetAllInvoices.isClosed())
			rsGetAllInvoices.close();
			
		} catch (SQLException e) {
			log.error("Error removing all invoices from database",e);
			throw new RuntimeException(e);
		}
		
		// Closes the connection to the database
		try {
			if (connect != null && !connect.isClosed())
			connect.close();
		} catch (SQLException e) {
			log.error("Error closing connection to database",e);
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	* Returns the invoiceId associated with a specific invoiceCode
	* 
	* @param invoiceCode
	* @return
	*/
	public static int getInvoiceId(String invoiceCode) {
		
		Connection connect = Connector.getConnection();
		
		int invoiceId = 0;
		String getInvoiceQuery = "SELECT invoiceId from Invoice WHERE invoiceCode = ?";
		PreparedStatement psGetInvoice = null;
		ResultSet rsGetInvoice = null;
		
		try {
			psGetInvoice = connect.prepareStatement(getInvoiceQuery);
			psGetInvoice.setString(1, invoiceCode);
			rsGetInvoice = psGetInvoice.executeQuery();
			
			if(rsGetInvoice != null) {
				rsGetInvoice.next();
				invoiceId = rsGetInvoice.getInt("invoiceId");
			}
			if (psGetInvoice != null && !psGetInvoice.isClosed())
			psGetInvoice.close();
			
			if (rsGetInvoice != null && !rsGetInvoice.isClosed())
			rsGetInvoice.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return invoiceId;
	}
	
	/**
	* Removes the invoice record from the database corresponding to the provided
	* <code>invoiceCode</code>
	* 
	* @param invoiceCode
	*/
	public static void removeInvoice(String invoiceCode) {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Retrieves the invoiceId of the invoice to be removed
		int invoiceId = getInvoiceId(invoiceCode);
		
		// Removes invoiceItems from the database which are associated with the given invoice
		String removeInvoiceItemQuery = "DELETE FROM InvoiceItem WHERE invoiceId = ?";
		PreparedStatement psRemoveInvoiceItem = null;
		
		try {
			psRemoveInvoiceItem = connect.prepareStatement(removeInvoiceItemQuery);
			psRemoveInvoiceItem.setInt(1, invoiceId);
			psRemoveInvoiceItem.executeUpdate();
			
			if (psRemoveInvoiceItem != null && !psRemoveInvoiceItem.isClosed())
			psRemoveInvoiceItem.close();
			
		} catch (SQLException e) {
			log.error("Error Deleting InvoiceItems Associated with Invoice to be Removed",e);
			throw new RuntimeException(e);
		}
		
		// Removes the given invoice from the database
		String removeInvoiceQuery = "DELETE FROM Invoice WHERE invoiceCode = ?";
		PreparedStatement psRemoveInvoice = null;
		
		try {
			psRemoveInvoice = connect.prepareStatement(removeInvoiceQuery);
			psRemoveInvoice.setString(1, invoiceCode);
			psRemoveInvoice.executeUpdate();
			
			if (psRemoveInvoice != null && !psRemoveInvoice.isClosed())
			psRemoveInvoice.close();
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			log.error("Error removing invoice from database",e);
			throw new RuntimeException(e);
		}
		
	}
	
	
	/**
	* 11. Adds an invoice record to the database with the given data.
	*/
	public static void addInvoice(String invoiceCode, String customerCode, String realtorCode, String invoiceDate) {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Determines the personId of the realtor associated with the invoice
		int realtorId = getPersonId(realtorCode);
		
		// Determines the customerId of the customer associated with the invoice
		int customerId = getCustomerId(customerCode);
		
		// Inserts the invoice into the database with the given properties
		String invoiceQuery = "INSERT INTO Invoice(invoiceCode, invoiceDate, customerId, realtorId) VALUES (?, ?, ?, ?)";
		
		try {
			PreparedStatement psInvoice = null;
			psInvoice = connect.prepareStatement(invoiceQuery);
			psInvoice.setString(1, invoiceCode);
			psInvoice.setString(2, invoiceDate);
			psInvoice.setInt(3, customerId);
			psInvoice.setInt(4, realtorId);
			psInvoice.executeUpdate();
			
			if (psInvoice != null && !psInvoice.isClosed())
			psInvoice.close();
			
		} catch (SQLException e) {
			log.error("Error inserting invoice into database");
			throw new RuntimeException(e);
		}
		
		// Closes the connection to the database
		try {
			if (connect != null && !connect.isClosed())
			connect.close();
		} catch (SQLException e) {
			log.error("Error closing connection",e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	* 12. Adds a particular SaleAgreement (corresponding to <code>productCode</code>
	* to an invoice corresponding to the provided <code>invoiceCode</code> with
	* the given number of units
	*/
	
	public static void addSaleAgreementToInvoice(String invoiceCode, String productCode, int quantity) {
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Determines the invoiceId of the invoice the item is to be added to
		int invoiceId = getInvoiceId(invoiceCode);
		
		// Determines the productId and serves to verify the type
		int productId;
		String getProductQuery = "SELECT productId FROM Product WHERE productType = 'S' AND productCode = ?";
		PreparedStatement psGetProduct = null;
		ResultSet rsGetProduct = null;
		
		try {
			psGetProduct = connect.prepareStatement(getProductQuery);
			psGetProduct.setString(1, productCode);
			rsGetProduct = psGetProduct.executeQuery();
			rsGetProduct.next();
			productId = rsGetProduct.getInt("productId");
			
			if (psGetProduct != null && !psGetProduct.isClosed())
			psGetProduct.close();
			
			if (rsGetProduct != null && !rsGetProduct.isClosed())
			rsGetProduct.close();
			
		} catch (SQLException e) {
			log.error("Error determining productId of product to be added to invoice",e);
			throw new RuntimeException(e);
		}
		
		// Adds the new item to the invoice
		String addSaleAgreementToInvoiceQuery = "INSERT INTO InvoiceItem(invoiceId, productId, units) VALUES(?, ?, ?)";
		
		try {
			PreparedStatement psAddSaleAgreementToInvoice = null;
			psAddSaleAgreementToInvoice = connect.prepareStatement(addSaleAgreementToInvoiceQuery);
			psAddSaleAgreementToInvoice.setInt(1, invoiceId);
			psAddSaleAgreementToInvoice.setInt(2, productId);
			psAddSaleAgreementToInvoice.setInt(3, quantity);
			psAddSaleAgreementToInvoice.executeUpdate();
			
			if (psAddSaleAgreementToInvoice != null && !psAddSaleAgreementToInvoice.isClosed())
			psAddSaleAgreementToInvoice.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		// Closes the connection
		try {
			if (connect != null && !connect.isClosed())
			connect.close();
		} catch (SQLException e) {
			log.error("Error closing connection to database",e);
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	* 13. Adds a particular LeaseAgreement (corresponding to <code>productCode</code>
	* to an invoice corresponding to the provided <code>invoiceCode</code> with
	* the given begin/end dates
	*/
	public static void addLeaseAgreementToInvoice(String invoiceCode, String productCode, int quantity) {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Determines the invoiceId of the invoice the item is to be added to
		int invoiceId = getInvoiceId(invoiceCode);
		
		// Determines the productId and serves to verify the type
		int productId;
		String getProductQuery = "SELECT productId FROM Product WHERE productType = 'L' AND productCode = ?";
		PreparedStatement psGetProduct = null;
		ResultSet rsGetProduct = null;
		
		try {
			psGetProduct = connect.prepareStatement(getProductQuery);
			psGetProduct.setString(1, productCode);
			rsGetProduct = psGetProduct.executeQuery();
			rsGetProduct.next();
			productId = rsGetProduct.getInt("productId");
			
			if (psGetProduct != null && !psGetProduct.isClosed())
			psGetProduct.close();
			
			if (rsGetProduct != null && !rsGetProduct.isClosed())
			rsGetProduct.close();
			
		} catch (SQLException e) {
			log.error("Error determining productId of product to be added to invoice",e);
			throw new RuntimeException(e);
		}
		
		// Adds the new item to the invoice
		String addLeaseAgreementToInvoiceQuery = "INSERT INTO InvoiceItem(invoiceId, productId, units) VALUES(?, ?, ?)";
		
		try {
			PreparedStatement psAddLeaseAgreementToInvoice = null;
			psAddLeaseAgreementToInvoice = connect.prepareStatement(addLeaseAgreementToInvoiceQuery);
			psAddLeaseAgreementToInvoice.setInt(1, invoiceId);
			psAddLeaseAgreementToInvoice.setInt(2, productId);
			psAddLeaseAgreementToInvoice.setInt(3, quantity);
			psAddLeaseAgreementToInvoice.executeUpdate();
			
			if (psAddLeaseAgreementToInvoice != null && !psAddLeaseAgreementToInvoice.isClosed())
			psAddLeaseAgreementToInvoice.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		// Closes the connection
		try {
			if (connect != null && !connect.isClosed())
			connect.close();
		} catch (SQLException e) {
			log.error("Error closing connection to database",e);
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	* 14. Adds a particular ParkingPass (corresponding to <code>productCode</code> to an 
	* invoice corresponding to the provided <code>invoiceCode</code> with the given
	* number of quantity.
	* NOTE: agreementCode may be null
	*/
	public static void addParkingPassToInvoice(String invoiceCode, String productCode, int quantity, String agreementCode) {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Determines the invoiceId of the invoice the item is to be added to
		int invoiceId = getInvoiceId(invoiceCode);
		
		// Determines the productId and serves to verify the type
		int productId;
		String getProductQuery = "SELECT productId FROM Product WHERE productType = 'P' AND productCode = ?";
		PreparedStatement psGetProduct = null;
		ResultSet rsGetProduct = null;
		
		try {
			psGetProduct = connect.prepareStatement(getProductQuery);
			psGetProduct.setString(1, productCode);
			rsGetProduct = psGetProduct.executeQuery();
			rsGetProduct.next();
			productId = rsGetProduct.getInt("productId");
			
			if (psGetProduct != null && !psGetProduct.isClosed())
			psGetProduct.close();
			
			if (rsGetProduct != null && !rsGetProduct.isClosed())
			rsGetProduct.close();
			
		} catch (SQLException e) {
			log.error("Error determining productId of product to be added to invoice",e);
			throw new RuntimeException(e);
		}
		
		// Establishes a connection to the database
		Connection connect1 = Connector.getConnection();
		int agreement;
		String getAgreementQuery = "SELECT productId FROM Product WHERE productType = 'S' OR productType = 'L' AND productCode = ?";
		PreparedStatement psGetAgreement = null;
		ResultSet rsGetAgreement = null;
		
		try {
			psGetAgreement = connect1.prepareStatement(getAgreementQuery);
			psGetAgreement.setString(1, productCode);
			rsGetAgreement = psGetAgreement.executeQuery();
			rsGetAgreement.next();
			agreement = rsGetAgreement.getInt("productId");
			
			if (psGetAgreement != null && !psGetAgreement.isClosed())
			psGetAgreement.close();
			
			if (rsGetAgreement != null && !rsGetAgreement.isClosed())
			rsGetAgreement.close();
			
		} catch (SQLException e) {
			log.error("Error determining productId of agreement related to the parking pass",e);
			throw new RuntimeException(e);
		}
		// Adds the new item to the invoice
		String addParkingPassToInvoiceQuery = "INSERT INTO InvoiceItem(invoiceId, productId, units, agreement) VALUES(?, ?, ?, ?)";
		
		try {
			PreparedStatement psAddParkingPassToInvoice = null;
			psAddParkingPassToInvoice = connect.prepareStatement(addParkingPassToInvoiceQuery);
			psAddParkingPassToInvoice.setInt(1, invoiceId);
			psAddParkingPassToInvoice.setInt(2, productId);
			psAddParkingPassToInvoice.setInt(3, quantity);
			psAddParkingPassToInvoice.setInt(4, agreement);
			psAddParkingPassToInvoice.executeUpdate();
			
			if (psAddParkingPassToInvoice != null && !psAddParkingPassToInvoice.isClosed())
			psAddParkingPassToInvoice.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		// Closes the connection
		try {
			if (connect != null && !connect.isClosed())
			connect.close();
		} catch (SQLException e) {
			log.error("Error closing connection to database",e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	* 15. Adds a particular amenity (corresponding to <code>productCode</code> to an 
	* invoice corresponding to the provided <code>invoiceCode</code> with the given
	* number of quantity. 
	*/
	public static void addAmenityToInvoice(String invoiceCode, String productCode, int quantity) {
		
		// Establishes a connection to the database
		Connection connect = Connector.getConnection();
		
		// Determines the invoiceId of the invoice the item is to be added to
		int invoiceId = getInvoiceId(invoiceCode);
		
		// Determines the productId and serves to verify the type
		int productId;
		String getProductQuery = "SELECT productId FROM Product WHERE productType = 'A' AND productCode = ?";
		PreparedStatement psGetProduct = null;
		ResultSet rsGetProduct = null;
		
		try {
			psGetProduct = connect.prepareStatement(getProductQuery);
			psGetProduct.setString(1, productCode);
			rsGetProduct = psGetProduct.executeQuery();
			rsGetProduct.next();
			productId = rsGetProduct.getInt("productId");
			
			if (psGetProduct != null && !psGetProduct.isClosed())
			psGetProduct.close();
			
			if (rsGetProduct != null && !rsGetProduct.isClosed())
			rsGetProduct.close();
			
		} catch (SQLException e) {
			log.error("Error determining productId of product to be added to invoice",e);
			throw new RuntimeException(e);
		}
		
		// Adds the new item to the invoice
		String addAmenityToInvoiceQuery = "INSERT INTO InvoiceItem(invoiceId, productId, units) VALUES(?, ?, ?)";
		
		try {
			PreparedStatement psAddAmenityToInvoice = null;
			psAddAmenityToInvoice = connect.prepareStatement(addAmenityToInvoiceQuery);
			psAddAmenityToInvoice.setInt(1, invoiceId);
			psAddAmenityToInvoice.setInt(2, productId);
			psAddAmenityToInvoice.setInt(3, quantity);
			psAddAmenityToInvoice.executeUpdate();
			
			if (psAddAmenityToInvoice != null && !psAddAmenityToInvoice.isClosed())
			psAddAmenityToInvoice.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		// Closes the connection
		try {
			if (connect != null && !connect.isClosed())
			connect.close();
		} catch (SQLException e) {
			log.error("Error closing connection to database",e);
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	* This method either returns the pre-existing addressId of the address with the given properties, or adds it 
	* to the database and then returns its addressId.
	* 
	* @param street
	* @param city
	* @param state
	* @param zip
	* @param country
	* @return
	*/
	public static int makeAddress(String street, String city, String state, String zip, String country) {
		
		Connection connect = Connector.getConnection();
		
		// Insert the State into the database if it is not already there
		int stateId;
		String queryState = "SELECT stateId from State where name = ?";
		PreparedStatement psState = null;
		ResultSet rsState = null;
		PreparedStatement psInsertState = null;
		PreparedStatement psGetLastState = null;
		ResultSet rsGetLastState = null;
		
		try {
			psState = connect.prepareStatement(queryState);
			psState.setString(1, state);
			rsState = psState.executeQuery();
			
			if (rsState.next()) {
				stateId = rsState.getInt("stateId");
			} else {
				String addStateQuery = "INSERT INTO State(name) VALUES (?)";
				psInsertState = connect.prepareStatement(addStateQuery);
				psInsertState.setString(1, state);
				psInsertState.executeUpdate();
				psGetLastState = connect.prepareStatement("SELECT LAST_INSERT_ID()");
				rsGetLastState = psGetLastState.executeQuery();
				rsGetLastState.next();
				stateId = rsGetLastState.getInt("LAST_INSERT_ID()");
				
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (psInsertState != null && !psInsertState.isClosed())
				psInsertState.close();
				
				if (psGetLastState != null && !psGetLastState.isClosed())
				psGetLastState.close();
				
				if (rsGetLastState != null && !rsGetLastState.isClosed())
				rsGetLastState.close();
				
				if (rsState != null && !rsState.isClosed())
				rsState.close();
				
				if (psState != null && !psState.isClosed())
				psState.close();
				
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		
		// Inserts the country into the database if it is not already there
		int countryId;
		String queryCountry = "SELECT countryId from Country where name = ?";
		PreparedStatement psCountry = null;
		ResultSet rsCountry = null;
		
		try {
			psCountry = connect.prepareStatement(queryCountry);
			psCountry.setString(1, country);
			rsCountry = psCountry.executeQuery();
			
			if (rsCountry.next() == true) {
				countryId = rsCountry.getInt("countryId");
			} else {
				String addCountry = "INSERT INTO Country(name) VALUES (?)";
				PreparedStatement psInsertCountry = null;
				psInsertCountry = connect.prepareStatement(addCountry);
				psInsertCountry.setString(1, country);
				psInsertCountry.executeUpdate();
				
				if (psInsertCountry != null && !psInsertCountry.isClosed())
				psInsertCountry.close();
				
				PreparedStatement psGetLastCountry = null;
				psGetLastCountry = connect.prepareStatement("SELECT LAST_INSERT_ID()");
				ResultSet rsGetLastCountry = psGetLastCountry.executeQuery();
				
				if (rsGetLastCountry.next()) {
					countryId = rsGetLastCountry.getInt("LAST_INSERT_ID()");
				} else {
					throw new RuntimeException("The country was not inserted properly");
				}
				
				if (psGetLastCountry != null && !psGetLastCountry.isClosed())
				psGetLastCountry.close();
				
				if (rsGetLastCountry != null && !rsGetLastCountry.isClosed())
				rsGetLastCountry.close();
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		try {
			if (rsCountry != null && !rsCountry.isClosed())
			rsCountry.close();
			if (psCountry != null && !psCountry.isClosed())
			psCountry.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		// Adds the address to the database if it is not already there
		int addressId;
		String queryCheckAddress = "SELECT addressId from Address where " + "street = ? and " + "city = ? and "
		+ "stateId = ? and " + "zip = ? and " + "countryId = ?";
		PreparedStatement psCheckAddress = null;
		
		try {
			
			psCheckAddress = connect.prepareStatement(queryCheckAddress);
			psCheckAddress.setString(1, street);
			psCheckAddress.setString(2, city);
			psCheckAddress.setInt(3, stateId);
			psCheckAddress.setString(4, zip);
			psCheckAddress.setInt(5, countryId);
			
			ResultSet rsCheckAddress = psCheckAddress.executeQuery();
			if (rsCheckAddress.next()) {
				addressId = rsCheckAddress.getInt("addressId");
			} else {
				
				String queryAddress = "INSERT INTO Address(street, city, stateId, zip, countryId) VALUES (?, ?, ?, ?, ?)";
				PreparedStatement psAddAddress = null;
				
				try {
					
					psAddAddress = connect.prepareStatement(queryAddress);
					psAddAddress.setString(1, street);
					psAddAddress.setString(2, city);
					psAddAddress.setInt(3, stateId);
					psAddAddress.setString(4, zip);
					psAddAddress.setInt(5, countryId);
					psAddAddress.executeUpdate();
					
					// check if it works, or else close it after getting the last address
					if (psAddAddress != null && !psAddAddress.isClosed())
					psAddAddress.close();
					
					PreparedStatement psGetLastAddress = null;
					psGetLastAddress = connect.prepareStatement("SELECT LAST_INSERT_ID()");
					ResultSet rsGetLastAddress = psGetLastAddress.executeQuery();
					rsGetLastAddress.next();
					addressId = rsGetLastAddress.getInt("LAST_INSERT_ID()");
					
					if (psGetLastAddress != null && !psGetLastAddress.isClosed())
					psGetLastAddress.close();
					
					if (rsGetLastAddress != null && !rsGetLastAddress.isClosed())
					rsGetLastAddress.close();
					
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				
			}
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return addressId;
	}
	
	/**
	* This method checks if the address is not in use by customer or person
	* then it deletes  it.
	* @param addressId
	*/
	public static void deleteAddressIfNotInUse(int addressId) {
		
		Connection connect = Connector.getConnection();
		
		String checkAddressQuery = "SELECT a.addressId FROM Address a "
		+ "LEFT JOIN Person p ON a.addressId = p.addressId "
		+ "LEFT JOIN Customer c ON c.addressId = a.addressId " + "WHERE a.addressId = ?";
		PreparedStatement psCheckAddress = null;
		ResultSet rsCheckAddress = null;
		
		try {
			psCheckAddress = connect.prepareStatement(checkAddressQuery);
			psCheckAddress.setInt(1, addressId);
			rsCheckAddress = psCheckAddress.executeQuery();
			
			if (!rsCheckAddress.next()) {
				
				// Deleting the address here
				String addressQuery = "DELETE FROM Address where addressId = ?";
				PreparedStatement psDeleteAddress = null;
				
				try {
					psDeleteAddress = connect.prepareStatement(addressQuery);
					psDeleteAddress.setInt(1, addressId);
					psDeleteAddress.executeUpdate();
					
					if (psDeleteAddress != null && !psDeleteAddress.isClosed())
					psDeleteAddress.close();
					
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				
			}
			
			if (connect != null && !connect.isClosed())
			connect.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
