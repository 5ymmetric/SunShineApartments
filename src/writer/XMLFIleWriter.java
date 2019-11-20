package writer;

// Import filesa
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import entities.Amenity;
import entities.Customer;
import entities.GeneralCustomer;
import entities.LeaseAgreement;
import entities.LowIncomeCustomer;
import entities.ParkingPass;
import entities.Person;
import entities.Product;
import entities.SaleAgreement;

// Main class
public class XMLFIleWriter {

	// Conversion of persons
	public void xmlConverterPerson(List<Person> persons) {

		// New Xstream
		XStream xstream = new XStream();

		// File reader for XML
		File xmlOutput = new File("data/Persons.xml");

		PrintWriter xmlPrintWriter = null; // writer

		try { // file try
			xmlPrintWriter = new PrintWriter(xmlOutput);
		} catch (FileNotFoundException e) { // Catch statement for file not found exception
			e.printStackTrace();
		}

		// XML encoder
		xmlPrintWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

		xstream.alias("Person", Person.class);

		for (Person aPerson : persons) {
			// Use toXML method to convert Person object into a String
			String personOutput = xstream.toXML(aPerson);
			xmlPrintWriter.write(personOutput);
			xmlPrintWriter.write("\n");
		}
		xmlPrintWriter.close();// Writer closes
	}

	public void xmlConverterCustomer(List<Customer> customers) {
		XStream xstream = new XStream();

		File xmlOutput = new File("data/Customers.xml");

		PrintWriter xmlPrintWriter = null;

		try {
			xmlPrintWriter = new PrintWriter(xmlOutput);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Xml encoder
		xmlPrintWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

		// Various types of customers
		xstream.alias("Customer", Customer.class);
		xstream.alias("GeneralCustomer", GeneralCustomer.class);
		xstream.alias("LowIncomeCustomer", LowIncomeCustomer.class);

		for (Customer aCustomer : customers) { // Prints all the customers
			// Use toXML method to convert Customer object into a String
			String customerOutput = xstream.toXML(aCustomer);
			xmlPrintWriter.write(customerOutput);
			xmlPrintWriter.write("\n");// New line
		}
		xmlPrintWriter.close(); // Closes XMl writer
	}

	public void xmlConverterProduct(List<Product> products) {
		// new XStream
		XStream xstream = new XStream();

		File xmlOutput = new File("data/Products.xml");

		// Initial null declaration
		PrintWriter xmlPrintWriter = null;

		try {
			xmlPrintWriter = new PrintWriter(xmlOutput);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		xmlPrintWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

		// Various types of products which are stored
		xstream.alias("Product", Product.class);
		xstream.alias("Lease", LeaseAgreement.class);
		xstream.alias("Amenity", Amenity.class);
		xstream.alias("Sale", SaleAgreement.class);
		xstream.alias("ParkingPass", ParkingPass.class);
		xstream.alias("GeneralCustomer", GeneralCustomer.class);
		xstream.alias("LowIncomeCustomer", LowIncomeCustomer.class);

		for (Product aProduct : products) {
			// Use toXML method to convert Product object into a String
			String productOutput = xstream.toXML(aProduct);
			xmlPrintWriter.write(productOutput);
			xmlPrintWriter.write("\n");
		}
		xmlPrintWriter.close();
	}

}
