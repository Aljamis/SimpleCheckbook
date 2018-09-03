package org.avr.simplecheckbook.db.master;

public enum RecurringPymtColumnNames {
	ID , PAY_TO , AMOUNT , EFF_DT , TERM_DT , INACTIVE_DT , DATE_OF_LAST_PYMT , FREQUENCY ;
	
	public static String getAllColumnNames() {
		return ID +" , "+ PAY_TO +" , "+ AMOUNT +" , "+ EFF_DT +" , "+ TERM_DT +" , "+ 
				INACTIVE_DT +" , "+ DATE_OF_LAST_PYMT +" , "+ FREQUENCY;
	}
	
	
	/**
	 * Return only columns used in Insert
	 * @return
	 */
	public static String getInsertColumns() {
		return PAY_TO +" , "+ AMOUNT +" , "+ EFF_DT +" , "+ FREQUENCY;
	}
}