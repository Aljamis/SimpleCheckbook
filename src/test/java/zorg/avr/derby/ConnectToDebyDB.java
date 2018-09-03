package zorg.avr.derby;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectToDebyDB {
	
	public static void main(String[] args) {
		String connStr = "jdbc:derby:ChkBookTEST;create=true";
		
		
		Connection conn = null;
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			conn = DriverManager.getConnection("jdbc:derby:derbyDB;create=true");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(99);
		}
		
		
	}
}
