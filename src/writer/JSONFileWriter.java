package writer;

// Import statements
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import entities.Customer;
import entities.Person;
import entities.Product;

public class JSONFileWriter {

	// Method for converting persons
	public void jsonConverterPerson(List<Person> persons) {

		// GSON Imports for writing in pretty printed JSON
		// Gson gson = new Gson();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File jsonOutput = new File("data/Persons.json");

		PrintWriter jsonPrintWriter = null;

		try {
			jsonPrintWriter = new PrintWriter(jsonOutput);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (Person aPerson : persons) {
			// Use toJson method to convert Person object into a String
			String personOutput = gson.toJson(aPerson);
			jsonPrintWriter.write(personOutput + "\n");
		}

		jsonPrintWriter.close();
	}

	// Customer conversion method
	public void jsonConverterCustomer(List<Customer> customers) {
		
		// GSON Imports for writing in pretty printed JSON
		// Gson gson = new Gson();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File jsonOutput = new File("data/Customers.json");

		PrintWriter jsonPrintWriter = null;

		try { 
			jsonPrintWriter = new PrintWriter(jsonOutput);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (Customer aCustomer : customers) { // For loop for formatting
			// Use toJson method to convert Customer object into a String
			String customerOutput = gson.toJson(aCustomer);
			jsonPrintWriter.write(customerOutput + "\n");
		}

		jsonPrintWriter.close();
	}

	// Products conversion
	public void jsonConverterProducts(List<Product> products) {
		
		// GSON Imports for writing in pretty printed JSON
		// Gson gson = new Gson();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File jsonOutput = new File("data/Products.json");

		PrintWriter jsonPrintWriter = null;

		try {
			jsonPrintWriter = new PrintWriter(jsonOutput);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (Product aProduct : products) {
			// Use toJson method to convert Product object into a String
			String productOutput = gson.toJson(aProduct);
			jsonPrintWriter.write(productOutput + "\n");
		}

		jsonPrintWriter.close();
	}

}