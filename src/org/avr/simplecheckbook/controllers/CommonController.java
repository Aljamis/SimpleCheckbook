package org.avr.simplecheckbook.controllers;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.avr.simplecheckbook.dataobjects.Balance;
import org.avr.simplecheckbook.dataobjects.RecurringPymt;
import org.avr.simplecheckbook.dataobjects.Transaction;
import org.avr.simplecheckbook.db.master.CheckBookDAO;
import org.avr.simplecheckbook.utils.IncalculableDueDate;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * One controller to rule them,
 * One controller to find them.
 * One controller to bring them and in the logic bind them.
 * @author Alfonso
 *
 */
abstract class CommonController {

	protected CheckBookDAO checkBookDAO;
	
	
	
	
	
	/**
	 * Performed only once, when starting, and insert recurring payments that are now DUE.
	 */
	protected void payRecurringPayments() {
		LocalDate today = LocalDate.now();
		try {
			/* Iterate through all ACTIVE recurring payments */
			for (RecurringPymt pymt : checkBookDAO.getActiveRecurringPymts()) {
				
				if (pymt.dueDate().isAfter( today )) {
					/* TODO if within a week prompt user to enter pymt */
					continue;		/* if DueDate > Today  -- SKIP */
				}
				
				/* select all transactions where transDate > DueDate */
				List<Transaction> trans = checkBookDAO.getTransactionsForRecurring( pymt.dueDate(),  pymt.getId() );
				/* if RS is empty */
				if (trans.size() == 0 ) {
					/* Pay Bills */
					while (today.isAfter( pymt.dueDate() )) {
						System.out.println("DUE : "+ pymt.dueDate() );
						payRecurring(pymt);
						pymt.setDateOfLastPymt( pymt.dueDate() );
						checkBookDAO.updateLastPayment(pymt);
					}
				}
			}
			
			
		} catch (IncalculableDueDate ddEx) {
			displayErrorDialog("Could not process recurring payments b/c Due Date could not be calculated");
		}
	}
	
	
	
	
	/**
	 * Handle all the balance calculating logic:
	 * <ol>
	 * <li>Look for prior balance</li>
	 * <li>Collect all Tx from day of Tx</li>
	 * <li>New Balance = Prior balance - (Transactions on that day)</li>
	 * <li>Insert or Update new balance</li>
	 * <li>Insert transaction</li>  *** removed in order to hande UPDATES from the UI
	 * </ol>
	 * 
	 * @param tx
	 */
	protected void processTransaction(Transaction tx) {
		Balance bal = previousBalance(tx);
		Balance newBal = new Balance();
		newBal.setDate(tx.getTxDate().toLocalDateTime().toLocalDate());
		
		List<Transaction> trans = checkBookDAO.getTransactionsOn( tx );
		if ( trans.size() > 0 ) {
			calculateTxBalance(bal, trans);
			tx.calculateBalance( trans.get(trans.size()-1).getBalance() );
		} else {
			tx.calculateBalance( bal.getBalance() );
		}
		newBal.setBalance( tx.getBalance() );
		checkBookDAO.saveBalance(newBal);
		
		if (tx.getTransID() == null)
			checkBookDAO.insertTransaction(tx);
		else
			checkBookDAO.updateTransaction(tx);
	}
	
	
	
	
	/**
	 * Calculate the balance after each Transaction
	 * @param bal
	 * @param trans
	 */
	protected void calculateTxBalance(Balance bal , List<Transaction> trans) {
		//  Set balance for the first Tx based on Balance object
		trans.get(0).calculateBalance( bal.getBalance() );
		for (int i = 1; i < trans.size() ; i++) {
			Transaction t = trans.get(i);
			//  calculate based on previous Tx balance
			t.calculateBalance(trans.get(i-1).getBalance());
		}
	}
	
	
	
	private Balance previousBalance(Transaction tx) {
		Balance b = checkBookDAO.getPreviousBalance( new Date( tx.getTxDate().getTime() ) );
		if ( b == null) {
			b = new Balance();
			b.setBalance( new BigDecimal(0));
			b.setDate( tx.getTxDate().toLocalDateTime().toLocalDate().plusDays(-1) );
			checkBookDAO.saveBalance(b);
		}
		return b;
	}

	
	
	
	
	/**
	 * Add a recurring payment to the transactions - Pay the recurring payment.
	 * @param pymt
	 */
	protected void payRecurring(RecurringPymt pymt) throws IncalculableDueDate {
		/* Create a Transaction */
		Transaction t = new Transaction();
		t.setTxDate( Timestamp.valueOf( LocalDateTime.now() ) );
		t.setPayee( pymt.getPayTo() );
		t.setDebit( pymt.getAmount() );
		t.setMemo("DUE : "+ pymt.dueDate() );
		t.setRecurId( pymt.getId() );
//		checkBookDAO.insertTransaction(t);
		processTransaction(t);
	}
	
	
	
	
	
	/**
	 * Display an error dialog with a message
	 * @param message
	 */
	protected void displayErrorDialog(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setHeaderText("Please correct the following");
		alert.setContentText( message );
		alert.showAndWait();		
	}
}
