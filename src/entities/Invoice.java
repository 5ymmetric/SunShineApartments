package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import driver.Connector;

public class Invoice {

	//A logger to report errors
	public static Logger log = Logger.getLogger(Invoice.class);

	private String invoiceCode;
	private String invoiceDate;
	private LocalDate date;
	private Customer customer;
	private Person realtor;
	private ArrayList<Product> products;
	private ArrayList<Integer> quantity;
	private int productCount;

	// Constructor
	public Invoice(String invoiceCode, LocalDate date, Customer customer, Person realtor, ArrayList<Product> products,
			ArrayList<Integer> quantity, int productCount) {
		super();
		this.invoiceCode = invoiceCode;
		this.date = date;
		this.customer = customer;
		this.realtor = realtor;
		this.products = products;
		this.quantity = quantity;
		this.productCount = productCount;
	}
	
	
	public Invoice(String invoiceCode, String invoiceDate, Customer customer, Person realtor,
			ArrayList<Product> products) {
		super();
		this.invoiceCode = invoiceCode;
		this.invoiceDate = invoiceDate;
		this.customer = customer;
		this.realtor = realtor;
		this.products = products;
	}
	
	public Invoice(String invoiceCode, LocalDate date, Customer customer, Person realtor, ArrayList<Product> products, int productCount) {
		super();
		this.invoiceCode = invoiceCode;
		this.date = date;
		this.customer = customer;
		this.realtor = realtor;
		this.products = products;
		this.productCount = productCount;
	}

	// Getters and Setters

	public String getInvoiceDate() {
		return invoiceDate;
	}



	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public int getProductCount() {
		return productCount;
	}

	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}

	public String getInvoiceCode() {
		return invoiceCode;
	}

	public void setInvoiceCode(String invoiceCode) {
		this.invoiceCode = invoiceCode;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Person getRealtor() {
		return realtor;
	}

	public void setRealtor(Person realtor) {
		this.realtor = realtor;
	}

	public ArrayList<Product> getProducts() {
		return products;
	}

	public void setProducts(ArrayList<Product> products) {
		this.products = products;
	}

	public ArrayList<Integer> getQuantity() {
		return quantity;
	}

	public void setQuantity(ArrayList<Integer> quantity) {
		this.quantity = quantity;
	}
	
	@Override
	public String toString() {
		return "Invoice [invoiceCode=" + invoiceCode + ", invoiceDate=" + invoiceDate + ", date=" + date + ", customer="
				+ customer + ", realtor=" + realtor + ", products=" + products + ", quantity=" + quantity
				+ ", productCount=" + productCount + "]";
	}
	
	public static Set<Invoice> getInvoices(Map<String, Person> personMap, Map<String, Customer> customerMap,
			Map<String, Product> productMap) {

		//Creates a connection to the database
		Connection connection = Connector.getConnection();

		//Constructs a query statement for obtaining detailed information about the invoices from the database
		String query = "SELECT i.invoiceCode, i.invoiceDate, c.customerCode, p.personCode, prod.productCode, item.units, item.agreement"
				+ " FROM Invoice i " + "JOIN Customer c ON c.customerId = i.customerId"
				+ " JOIN Person p ON p.personId = i.realtorId"
				+ " LEFT JOIN InvoiceItem item ON i.invoiceId = item.invoiceId"
				+ " LEFT JOIN Product prod ON prod.productId = item.productId";

		PreparedStatement ps = null;
		ResultSet rs = null;

		Invoice invoice = null;
		Set<Invoice> invoices = new HashSet<Invoice>();
		Product product = null;
		int productCount = 0;
		
		//Loads information from the database about its invoices, one line at a time.
		try {
			
			ps = connection.prepareStatement(query);
			rs = ps.executeQuery();

			while (rs.next()) {
				
				if (invoice != null && invoice.invoiceCode.equals(rs.getString("invoiceCode"))) {
					productCount = 0;

					product = productMap.get(rs.getString("productCode"));
					String agreement = null;
					Product ag = null;
					int breaker = 0;
					
					for(Product p : invoice.products) {
						if(p.getProductCode().equals(rs.getString("productCode"))) {
							int change = p.getQuantity();
							change += rs.getInt("units");
							p.setQuantity(change);
							breaker = 1;
						}
					}
					if(breaker == 1) {
						continue;
					}
					if(rs.getInt("agreement") != 0) {
						agreement = getProductCode(rs.getInt("agreement"));
						for (Product Pr : invoice.products) {
							if(Pr.getProductCode().equals(agreement)) {
								ag = Pr;
							}
							//System.out.println("bottom of for loop");	
						}
					}
					//System.out.println("before everything " + invoice.products.size());
					
					//A product of the correct type is constructed using the corresponding details pulled from the database
					if (product instanceof SaleAgreement) {
						SaleAgreement S = (SaleAgreement) product;
						SaleAgreement Sagreement = new SaleAgreement(S.getProductCode(), S.getType(), S.getDate(), S.getAddress(), S.gettotalCost(), S.getDownPayment(), S.getMonthlyPayment(), S.getPayableMonths(), S.getInterestRate());
						Sagreement.setQuantity(rs.getInt("units"));
						
						invoice.products.add(Sagreement);
						invoice.productCount++;
					} else if (product instanceof LeaseAgreement) {
						LeaseAgreement L = (LeaseAgreement) product;
						LeaseAgreement Lagreement = new LeaseAgreement(L.getProductCode(), L.getType(), L.getStartDate(), L.getEndDate(), L.getAddress(), L.getCustomer(), L.getDeposit(), L.getMonthlyCost()); 
						Lagreement.setQuantity(rs.getInt("units"));
						
						invoice.products.add(Lagreement);
						invoice.productCount++;
					} else if (product instanceof ParkingPass) {
						ParkingPass P = (ParkingPass) product;
						ParkingPass pass = new ParkingPass(P.getProductCode(), P.getType(), P.getParkingFee());
						pass.setQuantity(rs.getInt("units"));
						
						if(agreement != null) {
							if (ag instanceof LeaseAgreement) {
								LeaseAgreement Ls = (LeaseAgreement) ag;
								LeaseAgreement newAgreement = new LeaseAgreement(Ls.getProductCode(), Ls.getType(), Ls.getStartDate(), Ls.getEndDate(), Ls.getAddress(), Ls.getCustomer(), Ls.getDeposit(), Ls.getMonthlyCost()); 
								
								newAgreement.setQuantity(ag.getQuantity());

								pass.setProduct(newAgreement);
							} else if (ag instanceof SaleAgreement) {
								SaleAgreement Ss = (SaleAgreement) ag;
								SaleAgreement newSAgreement = new SaleAgreement(Ss.getProductCode(), Ss.getType(), Ss.getDate(), Ss.getAddress(), Ss.gettotalCost(), Ss.getDownPayment(), Ss.getMonthlyPayment(), Ss.getPayableMonths(), Ss.getInterestRate());
								newSAgreement.setQuantity(ag.getQuantity());

								pass.setProduct(newSAgreement);
							}

						}
						
						invoice.products.add(pass);
						invoice.productCount++;
					} else if (product instanceof Amenity) {
						Amenity A = (Amenity) product;
						Amenity amenity = new Amenity(A.getProductCode(), A.getType(), A.getName(), A.getCost());
						amenity.setQuantity(rs.getInt("units"));
						
						invoice.products.add(amenity);
						invoice.productCount++;
					}

				} else {

					if (invoice != null) {
						invoices.add(invoice);
					}

					ArrayList<Product> products = new ArrayList<Product>();

					//A product of the correct type is constructed using the corresponding details pulled from the database, for the new invoice
					product = productMap.get(rs.getString("productCode"));
					String agreement = null;
					Product ag = null;
					if(rs.getInt("agreement") != 0) {
						agreement = getProductCode(rs.getInt("agreement"));
					}
					
					for (Product Pr : products) {
						if(Pr.getProductCode().equals(agreement)) {
							ag = Pr;
						}
					}

					if (product instanceof SaleAgreement) {
						SaleAgreement S = (SaleAgreement) product;
						SaleAgreement Sagreement = new SaleAgreement(S.getProductCode(), S.getType(), S.getDate(), S.getAddress(), S.gettotalCost(), S.getDownPayment(), S.getMonthlyPayment(), S.getPayableMonths(), S.getInterestRate());
						Sagreement.setQuantity(rs.getInt("units"));
						
						products.add(Sagreement);
						productCount++;
					} else if (product instanceof LeaseAgreement) {
						LeaseAgreement L = (LeaseAgreement) product;
						LeaseAgreement Lagreement = new LeaseAgreement(L.getProductCode(), L.getType(), L.getStartDate(), L.getEndDate(), L.getAddress(), L.getCustomer(), L.getDeposit(), L.getMonthlyCost()); 
						Lagreement.setQuantity(rs.getInt("units"));
						
						products.add(Lagreement);
						productCount++;
					} else if (product instanceof ParkingPass) {
						ParkingPass P = (ParkingPass) product;
						ParkingPass pass = new ParkingPass(P.getProductCode(), P.getType(), P.getParkingFee());
						pass.setQuantity(rs.getInt("units"));
						
						if(agreement != null) {
							if (ag instanceof LeaseAgreement) {
								LeaseAgreement Ls = (LeaseAgreement) ag;
								LeaseAgreement newAgreement = new LeaseAgreement(Ls.getProductCode(), Ls.getType(), Ls.getStartDate(), Ls.getEndDate(), Ls.getAddress(), Ls.getCustomer(), Ls.getDeposit(), Ls.getMonthlyCost()); 
								
								newAgreement.setQuantity(ag.getQuantity());

								pass.setProduct(newAgreement);
							} else if (ag instanceof SaleAgreement) {
								SaleAgreement Ss = (SaleAgreement) ag;
								SaleAgreement newSAgreement = new SaleAgreement(Ss.getProductCode(), Ss.getType(), Ss.getDate(), Ss.getAddress(), Ss.gettotalCost(), Ss.getDownPayment(), Ss.getMonthlyPayment(), Ss.getPayableMonths(), Ss.getInterestRate());
								newSAgreement.setQuantity(ag.getQuantity());

								pass.setProduct(newSAgreement);
							}

						}
						
						products.add(pass);
						productCount++;
					} else if (product instanceof Amenity) {
						Amenity A = (Amenity) product;
						Amenity amenity = new Amenity(A.getProductCode(), A.getType(), A.getName(), A.getCost());
						amenity.setQuantity(rs.getInt("units"));
						
						products.add(amenity);
						productCount++;
					}

					
					LocalDate date = LocalDate.parse(rs.getString("invoiceDate"));
					
					//A new invoice is created
					invoice = new Invoice(rs.getString("invoiceCode"), date, customerMap.get(rs.getString("customerCode")),
							personMap.get(rs.getString("personCode")), products, productCount);

				}
			}
			
			invoices.add(invoice);

		} catch (SQLException sqle) {
			
			log.error("Connection was not succussful", sqle);
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

		return invoices;
	}
	
	/**
	 * Returns the customerId associated with a specific customerCode
	 * 
	 * @param customerCode
	 * @return
	 */
	public static String getProductCode(int agreementId) {

		Connection connect = Connector.getConnection();

		String productCode;
		String queryProduct = "SELECT productCode from Product where productId = ?";
		PreparedStatement psProduct = null;
		ResultSet rsProduct = null;

		try {
			psProduct = connect.prepareStatement(queryProduct);
			psProduct.setInt(1, agreementId);
			rsProduct = psProduct.executeQuery();
			rsProduct.next();

			productCode = rsProduct.getString("productCode");

			if (psProduct != null && !psProduct.isClosed())
				psProduct.close();

			if (rsProduct != null && !rsProduct.isClosed())
				rsProduct.close();

			if (connect != null && !connect.isClosed())
				connect.close();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return productCode;
	}


}
