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
import java.time.LocalDate;
import java.time.LocalDateTime;

// Product Superclass
public abstract class Product {
	
	//A logger to report errors
	public static Logger log = Logger.getLogger(Product.class);

	private String productCode;
	private String type;
	private int quantity;
	protected LocalDate invoiceDate;

	// Primary Constructor
	public Product(String productCode, String type) {
		super();
		this.productCode = productCode;
		this.type = type;

	}

	// Secondary Constructor
	public Product(String productCode, String type, int quantity) {
		super();
		this.productCode = productCode;
		this.type = type;
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public LocalDate getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(LocalDate invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	// Setters & Getters method
	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	
	public double getAgreementDiscount() {
		return -1000;
	}

	public abstract double getSubtotal();

	public abstract double getTax();

	public abstract double getDiscount();
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((invoiceDate == null) ? 0 : invoiceDate.hashCode());
		result = prime * result + ((productCode == null) ? 0 : productCode.hashCode());
		result = prime * result + quantity;
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
		Product other = (Product) obj;
		if (invoiceDate == null) {
			if (other.invoiceDate != null)
				return false;
		} else if (!invoiceDate.equals(other.invoiceDate))
			return false;
		if (productCode == null) {
			if (other.productCode != null)
				return false;
		} else if (!productCode.equals(other.productCode))
			return false;
		if (quantity != other.quantity)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
	@Override
	public String toString() {
		return "Product [productCode=" + productCode + ", type=" + type + ", quantity=" + quantity + ", invoiceDate="
				+ invoiceDate + "]";
	}

	public static Set<Product> getProducts(Map<String, Customer> customerMap) {
		
		//Creates a connection to the database
		Connection connection = Connector.getConnection(); 

		//Creates a query to retrieve detailed information about the products in the database
		String query = "SELECT prod.productCode, prod.name, prod.addressId, c.customerCode, prod.productType, prod.serviceFee, prod.moveinDate, prod.moveoutDate, prod.monthlyPayment, prod.deposit, prod.totalCost, prod.downPayment, prod.payableMonths, prod.interestRate\n"
				+ " FROM Product prod " + " LEFT JOIN Customer c ON prod.customerId = c.customerId";
		PreparedStatement ps = null;
		ResultSet rs = null;

		Set<Product> products = new HashSet<Product>();
		try {
			
			ps = connection.prepareStatement(query);
			rs = ps.executeQuery();

			while (rs.next()) {

				/* 
				 * A new product is created and added to the set, using the information
				 * loaded from the database.
				 */
				Product product = null;
				if (rs.getString("productType").equals("S")) {
					
					String Startdate = rs.getString("moveinDate");

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
					
					product = new SaleAgreement(rs.getString("productCode"), rs.getString("productType"), dateTime, Address.getAddress(rs.getInt("addressId")),
							rs.getDouble("totalCost"), rs.getDouble("downPayment"), rs.getDouble("monthlyPayment"), rs.getInt("payableMonths"), rs.getDouble("interestRate"));
				} else if (rs.getString("productType").equals("L")) {
					
					
					String Startdate = rs.getString("moveinDate");

					LocalDate startDate = LocalDate.parse(Startdate);

					String Enddate = rs.getString("moveoutDate");

					LocalDate endDate = LocalDate.parse(Enddate);
					
					product = new LeaseAgreement(rs.getString("productCode"), rs.getString("productType"), startDate, endDate, Address.getAddress(rs.getInt("addressId")),
							customerMap.get(rs.getString("customerCode")), rs.getDouble("deposit"), rs.getDouble("monthlyPayment"));
				} else if (rs.getString("productType").equals("P")) {
					product = new ParkingPass(rs.getString("productCode"), rs.getString("productType"), rs.getDouble("serviceFee"));
				} else if (rs.getString("productType").equals("A")) {
					product = new Amenity(rs.getString("productCode"), rs.getString("productType"), rs.getString("name"), rs.getDouble("serviceFee"));
				} else {
					log.error("Product encountered with an invalid type");
					throw new RuntimeException("Product Encountered with an invalid type");
				}
				products.add(product);

			}
			
		} catch (SQLException sqle) {
			
			log.error("Connection was not set up successfully", sqle);
			throw new RuntimeException(sqle);
			
		}

		//Close the connection, preparedStatement and resultSet
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

		return products;
	}


}
