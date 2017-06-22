package org.avr.simplecheckbook.dataobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Balance {
	private LocalDate date;
	private BigDecimal balance;
	
	
	
	public LocalDate getDate() { return date; }
	public void setDate(LocalDate date) { this.date = date; }
	
	public BigDecimal getBalance() { return balance; }
	public void setBalance(BigDecimal balance) { this.balance = balance; }
	
	
	public String toString() {
		return date.toString() +" "+ balance;
	}
	
	
	public void addDeposit(BigDecimal credit) {
		setBalance( this.balance.add(credit) );
	}
	
	public void subrtractPayment(BigDecimal debit) {
		setBalance( this.balance.subtract(debit) );
	}
}
