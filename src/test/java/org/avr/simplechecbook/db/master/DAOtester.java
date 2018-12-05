package org.avr.simplechecbook.db.master;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.avr.simplecheckbook.dataobjects.Balance;
import org.avr.simplecheckbook.dataobjects.MasterCheckBook;
import org.avr.simplecheckbook.dataobjects.Transaction;
import org.avr.simplecheckbook.db.master.MasterColumnNames;
import org.avr.simplecheckbook.db.master.SpringMasterDAO;
import org.junit.Test;

import static org.junit.Assert.*;

public class DAOtester {
	
	
	
	/**
	 * open connection to ChkBookMaster
	 */
	@Test
	public void masterDbHasCheckbooks() {
//		CheckBookDAO dao = new CheckBookDAO( "D:/Databases/NewDerby/AVR2" );
		SpringMasterDAO dao = new SpringMasterDAO();
		
		List<MasterCheckBook> books = dao.findAllCheckBooks();
		for (MasterCheckBook book : books) {
			System.out.println( book.getDbName() );
		}
		
		if (books.isEmpty()) 
			System.out.println("There arent any checkbooks yet");
		
		assertTrue("Missing checkbooks", books.size() > 0 );
		
	}
	
	@Test
	public void columnEnum() {
		System.out.println( MasterColumnNames.getAllColumnNames() );
	}
}
