package org.avr.simplecheckbook.db.master;

public enum TermColumnNames {
	ID , DESCRIPTION , ON_THIS_DATE , ON_THIS_DAY_OF_WEEK , TYPE , ALTERNATE ;
	
	
	public static String getAllColumns() {
		return ID +" , "+ DESCRIPTION +" , "+ ON_THIS_DATE  +" , "+ ON_THIS_DAY_OF_WEEK  +" , "+ TYPE +" , "+ ALTERNATE ;
	}
	
	
	
	/**
	 * Return only the columns used to insert
	 * @return
	 */
	public static String getInsertColumns() {
		return DESCRIPTION +" , "+ ON_THIS_DATE  +" , "+ ON_THIS_DAY_OF_WEEK  +" , "+ TYPE +" , "+ ALTERNATE ;
	}
}
