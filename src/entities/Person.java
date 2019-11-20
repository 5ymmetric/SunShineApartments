package entities;

//Import statements
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import java.util.HashSet;
import java.util.Set;

import driver.Connector;

// Person class
public class Person {
	
	//A logger to report errors
	static Logger log = Logger.getLogger(Person.class);

	// Variables
	private String personCode;
	private String firstName;
	private String lastName;
	private Address address;
	private Set<String> emailAddresses;

	// Primary constructor
	public Person(String personCode, String firstName, String lastName, Address address,
			Set<String> emailAddresses) {
		this.personCode = personCode;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.emailAddresses = emailAddresses;
	}

	// Setter & Getter Methods
	// personCode
	public String getPersonCode() {
		return personCode;
	}

	public void setPersonCode(String personCode) {
		this.personCode = personCode;
	}

	// FullName
	public String getFullName() {
		return(getLastName() + ", " + getFirstName());
	}

	// firstName
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	// lastName
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	// address
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	// emailAddresses
	public Set<String> getEmail() {
		return emailAddresses;
	}

	public void setEmail(Set<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
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
		Person other = (Person) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		return true;
	}
	
	
	
	@Override
	public String toString() {
		return "Person [personCode=" + personCode + ", firstName=" + firstName + ", lastName=" + lastName + ", address="
				+ address + ", emailAddresses=" + emailAddresses + "]";
	}

	public static Set<Person> getPersons() {

		//Opens up a connection to the database
		Connection connection = Connector.getConnection();

		//Creates a query to retrieve detailed information about the persons in a database
		String query = "SELECT p.personId, p.personCode, p.firstName, p.lastName, p.addressId, e.address AS \"Email Address\" FROM Person p "
				+ "LEFT JOIN PersonEmail pe ON p.personId = pe.personId "
				+ "LEFT JOIN Email e ON pe.emailId = e.emailId";
		PreparedStatement ps = null;
		ResultSet rs = null;

		Person person = null;
		Set<Person> persons = new HashSet<Person>();
		String email = null;
		try {
			
			ps = connection.prepareStatement(query);
			rs = ps.executeQuery();

			while (rs.next()) {
				
				if (person != null && person.firstName.equals(rs.getString("firstName")) //Correctly identifies the first line as a new person, since person is initialized to null
						&& person.lastName.equals(rs.getString("lastName"))) {

					email = rs.getString("Email Address");
					person.emailAddresses.add(email);
					
				} else {
		
					if (person != null) {
						persons.add(person);
					}

					Set<String> emails = new HashSet<String>();
					email = rs.getString("Email Address");
					emails.add(email);
					person = new Person(rs.getString("personCode"), rs.getString("firstName"), rs.getString("lastName"),
							Address.getAddress(rs.getInt("addressId")), emails);
					
				}

			}
			
			persons.add(person);
			
		} catch (SQLException sqle) {
			
			log.error("Connection was not set up successfully", sqle);
			throw new RuntimeException(sqle);
			
		}

		//Close the connections, preparedStatement and resultSet
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

		return persons;
	}


}
