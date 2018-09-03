package org.avr.simplecheckbook.db.master;

public enum DailyBalanceColumnNames {
	DATE , AMOUNT ;
	
	
	public static String getAllColumns() {
		return DATE +" , "+ AMOUNT;
	}
}
