package org.avr.simplecheckbook.db.master;

public enum MasterColumnNames {
	DBNAME , DBLOCATION , DESCRIPTION , APP_VERSION ;
	
	
	public static String getAllColumnNames() {
		return DBNAME +" , "+ DBLOCATION +" , "+ DESCRIPTION +" , "+ APP_VERSION;
	}
}
