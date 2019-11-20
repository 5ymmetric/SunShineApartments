package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

import driver.Connector;

// Address class
public class Address {
	
	//To report errors
	public static Logger log = Logger.getLogger(Address.class);
	
	// Variables
	private String street;
	private String city;
	private String state;
	private String zip;
	private String country;

	// Primary Constructor
	public Address(String street, String city, String state, String zip, String country) {
		super();
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.country = country;
	}

	// Setter & Getter methods
	// street
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	// city
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	// state
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	// zip
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	// country
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((zip == null) ? 0 : zip.hashCode());
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
		Address other = (Address) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		if (zip == null) {
			if (other.zip != null)
				return false;
		} else if (!zip.equals(other.zip))
			return false;
		return true;
	}

	public String toString() {
		return String.format("%s\n  \t%s, %s %s %s", this.street, this.city, this.state, this.zip, this.country);
	}
	
	public static Address getAddress(int addressId) {
		
		//Creates a connection to the database
		Connection connection = Connector.getConnection(); 
		
		//Creates a query statement in order to retrieve detailed information about an address from the database
		String query = "SELECT a.street, a.city, a.zip, s.name as State, c.name as Country FROM Address a "
				+ "JOIN State s on a.stateId = s.stateId "
				+ "JOIN Country c on a.countryId = c.countryId "
				+ "WHERE a.addressId = ?";
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		Address address = null;
		
		/* 
		 * Constructs a new address object using the information from the database, throws an error if no address with such an ID exists
		 */
		try {
			
			ps = connection.prepareStatement(query);
			ps.setInt(1, addressId);
			rs = ps.executeQuery();
			
			if(rs.next()) {
				address = new Address(rs.getString("street"), rs.getString("city"), rs.getString("State"),
						rs.getString("zip"), rs.getString("Country"));
			} else {
				log.error("There is no such address with addressId =" + addressId);
				throw new IllegalStateException();
			}
			rs.close();
			
		} catch (SQLException sqle) {

			log.error("Could not connect to the database", sqle);
			throw new RuntimeException(sqle);

		}
		
		
		//Close the connection, preparedStatement and resultSet
		try {

			if(rs != null && !rs.isClosed())
				rs.close();
			if(ps != null && !ps.isClosed())
				ps.close();
			if(connection != null && !connection.isClosed())
				connection.close();
		} catch (SQLException sqle) {
			
			log.error("Connection was not closed successfully", sqle);
			throw new RuntimeException(sqle);
			
		}

		return address;
		
	}

}
