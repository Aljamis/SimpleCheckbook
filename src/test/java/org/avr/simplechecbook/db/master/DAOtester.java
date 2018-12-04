package zorg.avr;

import java.util.List;

import org.avr.simplecheckbook.dataobjects.MasterCheckBook;
import org.avr.simplecheckbook.db.master.MasterColumnNames;
import org.avr.simplecheckbook.db.master.SpringMasterDAO;

public class DAOtester {

	public static void main(String[] args) {
		columnEnum();
		getMasterCheckbook();
	}
	
	
	
	/**
	 * open connection to ChkBookMaster
	 */
	public static void getMasterCheckbook() {
//		CheckBookDAO dao = new CheckBookDAO( "D:/Databases/NewDerby/AVR2" );
		SpringMasterDAO dao = new SpringMasterDAO();
		
		List<MasterCheckBook> books = dao.findAllCheckBooks();
		for (MasterCheckBook book : books) {
			System.out.println( book );
		}
		
		if (books.isEmpty()) 
			System.out.println("There arent any checkbooks yet");
	}
	
	
	public static void columnEnum() {
		System.out.println( MasterColumnNames.getAllColumnNames() );
	}

}
