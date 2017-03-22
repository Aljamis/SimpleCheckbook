package org.avr.simplecheckbook.db.master;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.avr.simplecheckbook.dataobjects.MasterCheckBook;

public class MasterDAO {
	private String connectionURL = "jdbc:derby:ChkBookMaster";
	//  Can't append ;create=true b/c need to create a table
	
	private Connection masterConn;
	
	
	/**
	 * Connect to the MASTER database:  Storing all the checkbooks defined
	 * on this local installation.  
	 * 
	 * If the database does not exist, create it.
	 * 
	 * @throws SQLException only when it can not create an embedded database
	 */
	public MasterDAO() throws SQLException {
		try {
			this.masterConn = DriverManager.getConnection(connectionURL);
			System.out.println("Got a connection");
		} catch (SQLException sqlEx) {
			if ("XJ004".equalsIgnoreCase( sqlEx.getSQLState() ) ) {
				createMasterDB();
			} else
				throw sqlEx;
		}
	}
	
	
	
	
	/**
	 * Create the Database.
	 * @throws SQLException
	 */
	private void createMasterDB() throws SQLException {
		System.out.println("Creating a MASTER DB");
		this.masterConn = DriverManager.getConnection(connectionURL +";create=true");

		createTable();
	}
	
	
	
	/**
	 * Create the MASTER Table
	 * @throws SQLException

	 */
	private void createTable() throws SQLException{
		StringBuffer str = new StringBuffer("create table checkbooks ( ");
		str.append("  dbName      varChar(20) ");
		str.append(", dbLocation  varChar(200) ");
		str.append(", description varChar(100) ");
		str.append(" ) ");
		
		Statement stmt = this.masterConn.createStatement();
		stmt.execute( str.toString() );
		stmt.close();
	}
	
	
	
	/**
	 * Select all the checkbooks from the MASTER table
	 * @return
	 */
	public List<MasterCheckBook> queryCheckBooks() throws SQLException {
		List<MasterCheckBook> returnList = new ArrayList<>();
		StringBuffer str = new StringBuffer();
		str.append("select dbName , dbLocation , description from checkbooks");
		
		Statement stmt = this.masterConn.createStatement();
		ResultSet rs = stmt.executeQuery(str.toString());
		while (rs.next()) {
			MasterCheckBook cb = new MasterCheckBook();
			cb.setDbName( rs.getString("dbName"));
			cb.setDbLocation( rs.getString( "dbLocation"));
			cb.setDescription( rs.getString("description"));
			
			returnList.add(cb);
		}
		
		return returnList;
	}
}
