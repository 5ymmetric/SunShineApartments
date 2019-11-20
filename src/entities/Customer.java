package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import driver.Connector;


// Customer super class
public abstract class Customer {
	
	//Logger To report errors
	public static Logger log = Logger.getLogger(Customer.class);
	
	// Variables
	private String customerCode;
	private String type;
	private Person primaryContact;
	private String name;
	private Address address;

	// Primary Constructor
	public Customer(String customerCode, String type, Person primaryContact, String name, Address address) {
		super();
		this.customerCode = customerCode;
		this.primaryContact = primaryContact;
		this.name = name;
		this.address = address;
		this.type = type;

	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Person getPrimaryContact() {
		return primaryContact;
	}

	public void setPrimaryContact(Person primaryContact) {
		this.primaryContact = primaryContact;
	}

	// Setter & Getter methods
	// customerCode
	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	// primaryContact
	public Person getprimaryContact() {
		return primaryContact;
	}

	public void setprimaryContact(Person primaryContact) {
		this.primaryContact = primaryContact;
	}

	// name
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// address
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getModifiedType() {
		if (getType().equals("G")) {
			return "[General]";
		} else {
			return "[LowIncome]";
		}
	}

	public void printCustomerInfo() {
		System.out.println("Customer Info:");
		System.out.println("\t" + getName() + " (" + getCustomerCode() + ")");
		System.out.println("\t" + getModifiedType());
		System.out.println("\t" + getPrimaryContact().getFullName());
		System.out.println("\t" + getAddress().toString());
	}
	
	
	
@Override
	public String toString() {
		return "Customer [customerCode=" + customerCode + ", type=" + type + ", primaryContact=" + primaryContact
				+ ", name=" + name + ", address=" + address + "]";
	}

@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((customerCode == null) ? 0 : customerCode.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((primaryContact == null) ? 0 : primaryContact.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Customer other = (Customer) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (customerCode == null) {
			if (other.customerCode != null)
				return false;
		} else if (!customerCode.equals(other.customerCode))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (primaryContact == null) {
			if (other.primaryContact != null)
				return false;
		} else if (!primaryContact.equals(other.primaryContact))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

public static Set<Customer> getCustomers(Map<String, Person> personMap) {
		
		//Creates a connection to the database
		Connection connection = Connector.getConnection(); 

		//Define a query to obtain comprehensive information about the Customers from the database
		String query = "SELECT c.customerCode, c.customerType, c.name, c.addressId, p.personCode " + "FROM Customer c "
				+ "LEFT JOIN Person p ON c.primaryContact = p.personId";

		PreparedStatement ps = null;
		ResultSet rs = null;

		Set<Customer> customers = new HashSet<Customer>();

		/* 
		 * Creates a Customer for each customer from the database using the constructor corresponding to each customer's type,
		 * including the detailed information loaded from the database, and adds each to the set. 
		*/
		try {
			
			ps = connection.prepareStatement(query);
			rs = ps.executeQuery();

			while (rs.next()) {
				
				Customer customer = null;
				String Ctype = rs.getString("customerType");
				String char1 = Character.toString(Ctype.charAt(0));
				if (Ctype.charAt(0) == 'G') {
					customer = new GeneralCustomer(rs.getString("customerCode"),char1,
							personMap.get(rs.getString("personCode")), rs.getString("name"),
							Address.getAddress(rs.getInt("addressId")));
				} else if (Ctype.charAt(0) == 'L') {
					customer = new LowIncomeCustomer(rs.getString("customerCode"), char1,
							personMap.get(rs.getString("personCode")), rs.getString("name"),
							Address.getAddress(rs.getInt("addressId")));
				} else {
					log.error("Customer encountered with an invalid type");
					throw new RuntimeException("Customer Encountered with an invalid type");
				}
				customers.add(customer);
				
			}
			
		} catch (SQLException sqle) {
			
			log.error("Could not connect to the database", sqle);
			throw new RuntimeException(sqle);
			
		}
		
		//close the connections, preparedStatement and resultSet
		try {
			
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
			if (ps != null && !ps.isClosed()) {
				ps.close();
			}
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
			
		} catch (SQLException sqle) {
			
			log.error("Connection was not closed successfully", sqle);
			throw new RuntimeException(sqle);
			
		}

		//Return the result set of customer objects loaded from the database
		return customers;

	}
}
