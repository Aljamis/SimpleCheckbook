package org.avr.simplecheckbook.db.master;

public enum MasterColumnNames {
	DBNAME , DBLOCATION , DESCRIPTION ;
	
	
	public static String getAllColumnNames() {
		return DBNAME +" , "+ DBLOCATION +" , "+ DESCRIPTION ;
	}
}
