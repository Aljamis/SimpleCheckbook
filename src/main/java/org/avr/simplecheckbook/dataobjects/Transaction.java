package org.avr.simplecheckbook.dataobjects;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * An individual row from the CheckBook table.
 * @author Alfonso
 *
 */
public class Transaction {
	private Integer transID;
	
	private Timestamp txDate;
	private BigDecimal checkNumber;
	private String payee;
	private Integer recurId;
	private String memo;
	private Boolean cleared = false;
	private BigDecimal debit;
	private BigDecimal credit;
	
	private BigDecimal balance;
	
	
	
	public Integer getTransID() { return transID; }
	public void setTransID(Integer ID) { this.transID = ID; }
	
	public Timestamp getTxDate() {
		return txDate;
	}
	public void setTxDate(Timestamp txDate) {
		this.txDate = txDate;
	}
	public BigDecimal getCheckNumber() {
		return checkNumber;
	}
	public void setCheckNumber(BigDecimal checkNumber) {
		this.checkNumber = checkNumber;
	}
	public String getPayee() {
//		if (memo != null && memo.trim().length() > 0 )
//			return payee +" *";  /* NOt quite ready .. recurring payments are adding this */ 
		return payee;
	}
	public void setPayee(String payee) {
		this.payee = payee;
	}
	public Integer getRecurId() {
		return recurId;
	}
	public void setRecurId(Integer recurId) {
		this.recurId = recurId;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Boolean getCleared() {
		return cleared;
	}
	public void setCleared(Boolean cleared) {
		this.cleared = cleared;
	}
	public BigDecimal getDebit() {
		return debit;
	}
	public void setDebit(BigDecimal debit) {
		this.debit = debit;
	}
	public BigDecimal getCredit() {
		return credit;
	}
	public void setCredit(BigDecimal credit) {
		this.credit = credit;
	}
	
	
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal bal) {
		this.balance = bal;
	}
	
	
	/* 
	 * For TableView only
	 */
	public String getDateOnly() {
		return txDate.toLocalDateTime().toLocalDate().toString();
	}
	
	
	/**
	 * return either Debit or Credit
	 * @return
	 */
	public String getAmount() {
		if ( this.getDebit() == null)
			return this.getCredit().toString();
		return this.getDebit().toString();
	}
	
	
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("\n\t     Date : ").append( this.getDateOnly() );
		out.append("\n\t    Payee : ").append( this.getPayee() );
		out.append("\n\t   Amount : ").append( this.getDebit() == null ?
				"+"+ this.getCredit() :
				"-"+ this.getDebit() );
		return out.toString();
	}
	
	
	
	/**
	 * Calculate balance by passing in balance from previous transaction & adding
	 * or subtracting
	 * @param prevBal
	 */
	public void calculateBalance(BigDecimal prevBal) {
		if (this.credit == null)
			this.balance = prevBal.subtract(getDebit());
		else
			this.balance = prevBal.add( getCredit() );
	}
	
	
	
	public String getHasCleared() {
		return ( cleared ? "X" : "" );
	}
}
