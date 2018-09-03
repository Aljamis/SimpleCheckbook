package org.avr.simplecheckbook.controllers;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.avr.simplecheckbook.dataobjects.Balance;
import org.avr.simplecheckbook.dataobjects.RecurringPymt;
import org.avr.simplecheckbook.dataobjects.Transaction;
import org.avr.simplecheckbook.db.master.CheckBookDAO;
import org.avr.simplecheckbook.utils.IncalculableDueDate;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
	 * 
	 * Query for all Active recurring payments.  If the due-date has passed or it is 
	 * within 7 days of the due-date (and the user AGREES to make the early payment)
	 * send this RecurringPymt to get paid.
	 */
	protected void findRecurringPayments() {
		LocalDate today = LocalDate.now();
		try {
			/* Iterate through all ACTIVE recurring payments */
			for (RecurringPymt pymt : checkBookDAO.getActiveRecurringPymts()) {
				
				if ( today.isBefore( pymt.dueDate() ) ) {
					/* payment is not DUE yet */
					if ( ChronoUnit.DAYS.between( today , pymt.dueDate() ) <= 7 ) {
						/* But it is due in the next 7 days */
						if ( promptToPayRecurringPaymentEarly( pymt ) ){
							/* User has selected to record this payment early */
							makeThisPayment( pymt );
						}
						continue;
					} else 
						continue;		/* if DueDate > Today  -- SKIP */
				}
				
				payRecurringPayment(pymt);
				
			}
			
			
		} catch (IncalculableDueDate ddEx) {
			displayErrorDialog("Could not process recurring payments b/c Due Date could not be calculated");
		}
	}
	
	
	
	
	
	/**
	 * Has this payment already been made?  If not, make the payments until it 
	 * is caught up. 
	 * @param pymt
	 */
	private void payRecurringPayment( RecurringPymt pymt) {
		try {
			if ( !recurringTxAlreadyPaid( pymt ) ) {
				while ( LocalDate.now().isAfter( pymt.dueDate() ) 
						|| LocalDate.now().isEqual( pymt.dueDate() ) ) {
					makeThisPayment( pymt );
				}
			}
		} catch (IncalculableDueDate ddEx) {
			displayErrorDialog("Could not process recurring payments b/c Due Date could not be calculated");
		}
	}
	
	
	
	
	/**
	 * Has this payment already been made?  Is there a row in the checkbook for this 
	 * Recurring Payment occurring after the due-date?
	 * 
	 * @param pymt
	 * @return
	 * @throws IncalculableDueDate
	 */
	private boolean recurringTxAlreadyPaid( RecurringPymt pymt ) throws IncalculableDueDate {
		/* select all transactions where transDate > DueDate */
		List<Transaction> trans = checkBookDAO.getRecurringTxAfter( pymt.dueDate(),  pymt.getId() );
		if (trans.size() == 0 ) {
			return false;			//  Has not yet been paid
		}
		return true;				//  Has been paid
		
	}
	
	
	
	
	/**
	 * Record the transaction, update LastPayment column
	 * @param pymt
	 * @throws IncalculableDueDate
	 */
	private void makeThisPayment( RecurringPymt pymt ) throws IncalculableDueDate {
		payRecurring(pymt);
		pymt.setDateOfLastPymt( pymt.dueDate() );
		checkBookDAO.updateLastPayment(pymt);
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
		
		updateFutureBalances(tx);
	}
	
	
	
	
	/**
	 * If a transaction has been entered in the past, need to update all the 
	 * future balances
	 * @param tx
	 */
	private void updateFutureBalances(Transaction tx) {
		List<Balance> balances = checkBookDAO.getBalancesAfter( new Date(tx.getTxDate().getTime() ) );
		for (Balance balance : balances) {
			calculateBalance(balance, tx);
			checkBookDAO.saveBalance(balance);
		}
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
	private void payRecurring(RecurringPymt pymt) throws IncalculableDueDate {
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
	
	
	
	
	/**
	 * If this payment has not been recorded yet, prompt the user to pay it
	 * 
	 * @param pymt
	 * @return
	 */
	private boolean promptToPayRecurringPaymentEarly( RecurringPymt pymt ) throws IncalculableDueDate {
		if ( recurringTxAlreadyPaid( pymt ) )
			return false;
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Pay "+ pymt.getPayTo() +" Early?");
		alert.setHeaderText(pymt.getPayTo() +" is due "+ pymt.dueDate().toString() );
		alert.setContentText("Do you want to record $"+ pymt.getAmount() +" today?");
		
		Optional<ButtonType> result = alert.showAndWait();
		if ( result.get() == ButtonType.OK )
			return true;
		else 
			return false;
	}
	
	
	
	
	/**
	 * Go through the entire checkbook recalculating daily balances
	 */
	protected void reCalculateBalances() {
		Balance currentBalance = checkBookDAO.getBalance(180);
		if (currentBalance == null) {
			return;  // This must be the start of the check book
		}
		List<Transaction> trans = checkBookDAO.getTransactionsAfter( currentBalance );
		LocalDate transDate = null;
		
		for (Transaction transaction : trans) {
			transDate =  transaction.getTxDate().toLocalDateTime().toLocalDate();
			
			if ( transDate.isAfter( currentBalance.getDate() )) {
				// Save the balance
				checkBookDAO.saveBalance(currentBalance); 
				BigDecimal prevBal = currentBalance.getBalance();
				
				// and create a new balance
				currentBalance = new Balance();
				currentBalance.setDate( transDate );
				currentBalance.setBalance(prevBal);
			}
			
			calculateBalance( currentBalance , transaction );
		}
		checkBookDAO.saveBalance(currentBalance); 
	}
	
	
	
	private void calculateBalance(Balance bal , Transaction tx) {
		if (tx.getCredit() == null )
			bal.subrtractPayment( tx.getDebit() );
		else 
			bal.addDeposit( tx.getCredit() );
	}
	
}
