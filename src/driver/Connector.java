package driver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class Connector {
	
	//A logger is defined to report errors and informational messages within the class
		public static Logger log = Logger.getLogger(Connector.class);	

		public static Connection getConnection() {
			
			//Load the JDBC driver
			try {
				Class.forName(Database.DRIVER_CLASS).newInstance();
			} catch (InstantiationException e) {
				log.error("Failed to load JDBC Driver", e);
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				log.error("Failed to load JDBC Driver", e);
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				log.error("Failed to load JDBC Driver", e);
				throw new RuntimeException(e);
			}

			//Create a connection to the database
			try {
				return DriverManager.getConnection(Database.url, Database.username, Database.password);
			} catch (SQLException sqle) {
				log.error("Cannot connect to the server", sqle);
				throw new RuntimeException(sqle);
			}

		}

}
