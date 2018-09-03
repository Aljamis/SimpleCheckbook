package org.avr.simplecheckbook.db.master;

public enum CheckBookColumnNames {
	TRANS_ID 
	, TX_DATE
	, CHECKNUM
	, PAYEE
	, MEMO
	, RECUR_ID
	, CLEARED
	, DEBIT
	, CREDIT
	, LGCL_DLT_DT
	;
	
	
	
	public static String getAllColumns() {
		return TRANS_ID +" , "+ TX_DATE +" , "+ CHECKNUM +" , "+ PAYEE +" , "+ 
				MEMO +" , "+ RECUR_ID +" , "+ CLEARED +" , "+ DEBIT +" , "+
				CREDIT +" , "+ LGCL_DLT_DT;
	}
	
	
	
	/**
	 * Return only the columns used to INSERT new transactions
	 * @return
	 */
	public static String getInsertColumns() {
		return TX_DATE +" , "+ CHECKNUM +" , "+ PAYEE +" , "+ RECUR_ID +" , "+ 
				MEMO +" , "+ CLEARED +" , "+ DEBIT +" , "+ CREDIT ;
	}
}
