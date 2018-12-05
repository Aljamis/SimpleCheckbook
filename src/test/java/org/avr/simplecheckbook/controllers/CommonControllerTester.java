package org.avr.simplecheckbook.controllers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.avr.simplecheckbook.controllers.CommonController;
import org.avr.simplecheckbook.controllers.SampleController;
import org.avr.simplecheckbook.dataobjects.Balance;
import org.avr.simplecheckbook.dataobjects.Transaction;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommonControllerTester {
	
	@Test
	public void makeAwithdrawl() {
		Balance bal = createBalance(5000.00);
		List<Transaction> trans = createSingleDebitTransaction(275.00);
		
		SampleController controller = new SampleController();
		controller.calculateTxBalance(bal, trans);
		assertTrue("Debit was not deducted from balance"
				, trans.get(0).getBalance().compareTo( BigDecimal.valueOf(4725) ) == 0 );
	}
	
	
	@Test
	public void makeAdeposit() {
		Balance bal = createBalance(5000.00);
		List<Transaction> trans = createSingleCreditTransaction(275.00);
		
		SampleController controller = new SampleController();
		controller.calculateTxBalance(bal, trans);
		assertTrue("Credit was not applied to balance"
				, trans.get(0).getBalance().compareTo( BigDecimal.valueOf(5275) ) == 0 );
	}
	
	
	@Test
	public void depositAndWithdrawl() {
		Balance bal = createBalance(5000.00);
		List<Transaction> trans = createMixedTransaction(250.00, 1250);
		
		SampleController controller = new SampleController();
		controller.calculateTxBalance(bal, trans);
		assertTrue("Deposit and withdrawl were not calculated correctly"
				, trans.get(1).getBalance().compareTo( BigDecimal.valueOf(4000) ) == 0 );
	}
	
	
	
	/**
	 * Instantiate a Balance object with an amount of ???
	 * @param amount
	 * @return
	 */
	private Balance createBalance(double amount) {
		Balance bal = new Balance();
		bal.setBalance(BigDecimal.valueOf( 5000.00 ) );
		bal.setDate( LocalDate.now() );
		return bal;
	}
	
	
	/**
	 * Create a single Debit Transaction
	 * @param amount
	 * @return
	 */
	private List<Transaction> createSingleDebitTransaction(double amount) {
		List<Transaction> trans = new ArrayList<>();
		Transaction tx = new Transaction();
		tx.setDebit( BigDecimal.valueOf( amount ));
		tx.setTxDate( Timestamp.valueOf( LocalDateTime.now() ) );
		
		trans.add( tx );
		return trans;
	}
	private List<Transaction> createSingleCreditTransaction(double amount) {
		List<Transaction> trans = new ArrayList<>();
		Transaction tx = new Transaction();
		tx.setCredit( BigDecimal.valueOf( amount ));
		tx.setTxDate( Timestamp.valueOf( LocalDateTime.now() ) );
		
		trans.add( tx );
		return trans;
	}
	private List<Transaction> createMixedTransaction(double deposit , double withdrawl) {
		List<Transaction> trans = new ArrayList<>();
		Transaction tx = new Transaction();
		tx.setDebit( BigDecimal.valueOf( withdrawl ));
		tx.setTxDate( Timestamp.valueOf( LocalDateTime.now() ) );
		trans.add( tx );
		
		Transaction tx2 = new Transaction();
		tx2.setCredit( BigDecimal.valueOf( deposit ));
		tx2.setTxDate( Timestamp.valueOf( LocalDateTime.now() ) );
		trans.add(tx2);
		
		return trans;
	}
}
