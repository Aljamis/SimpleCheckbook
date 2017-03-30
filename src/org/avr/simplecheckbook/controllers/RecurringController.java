package org.avr.simplecheckbook.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.avr.simplecheckbook.dataobjects.RecurringPymt;
import org.avr.simplecheckbook.dataobjects.RecurringTerm;
import org.avr.simplecheckbook.db.master.CheckBookDAO;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class RecurringController extends CommonController {

	
	@FXML private TableView<RecurringPymt> paymentTable;
	@FXML private DatePicker effDate;
	@FXML private DatePicker termDate;
	@FXML private TextField txPayee;
	@FXML private TextField txAmount;
	@FXML private Button btnSave;
	@FXML private Button btnUpdate;
	@FXML private Button btnClear;
	@FXML private ComboBox<RecurringTerm> cboxFrequency;
	
	
	public void preLaunch(CheckBookDAO dao) {
		checkBookDAO = dao;
		/* populate screen table */
		refresh();
		
		/* populate combo box */
		List<RecurringTerm> allTerm = checkBookDAO.getAllTermR();
		cboxFrequency.setItems( FXCollections.observableArrayList(allTerm) );
	}
	
	
	
	private void refresh() {
		List<RecurringPymt> pymts = checkBookDAO.getAllRecurringPymts();
		paymentTable.setItems(FXCollections.observableArrayList( pymts ));
		
		clearInputFields();
	}

	
	
	@FXML protected void handleSelectedRow(MouseEvent event) {
		RecurringPymt pymt = this.paymentTable.getSelectionModel().getSelectedItem();
		if (pymt ==  null) {
			clearInputFields();  // This occurs if the user tries sorting by column
			return;
		}
		populateInputFields(pymt);
		toggleButtons(true);
	}
	
	@FXML protected void handleToggleClear(ActionEvent event) {
		
	}
	
	@FXML protected void handleDeleteTransaction(ActionEvent event) {
		
	}
	
	@FXML protected void handleSaveRecurring(ActionEvent event) {
		if (validInput()) {
			checkBookDAO.insertRecurringPayment( prepPymt() );
			if ( prepPymt().getEffDt().isBefore( LocalDate.now() ))
				payRecurringPayments();
			refresh();
		}
	}
	
	@FXML protected void handleUpdateRecurring(ActionEvent event) {
		if (validInput()) {
			checkBookDAO.updateRecurringPayment( prepPymt() );
			if ( prepPymt().getEffDt().isBefore( LocalDate.now() ))
				payRecurringPayments();
			refresh();
		}
	}
	
	@FXML protected void handleClear(ActionEvent event) {
		clearInputFields();
	}
	
	
	
	
	/**
	 * Make sure all required fields have been entered
	 */
	private boolean validInput() {
		StringBuffer errMsg = new StringBuffer();
		if ( txPayee.getText() == null || txPayee.getText().trim().length() == 0 ) 
			errMsg.append("- Missing Payee\n");
		if ( txAmount.getText() == null || txAmount.getText().trim().length() == 0 ) {
			errMsg.append("- Missing Amount\n");
			try {
				new BigDecimal(txAmount.getText());
			} catch (NumberFormatException nfEx) {
				errMsg.append("- [").append( txAmount.getText() ).append( "] is not a number");
			}
		}
		if ( effDate.getValue() == null )
			errMsg.append("- Missing Effective Date\n");
		if ( cboxFrequency.getSelectionModel().getSelectedItem() == null )
			errMsg.append("- Missing Frequency\n");
		
		if (errMsg.length() > 0) {
			displayErrorDialog( errMsg.toString() );
			return false;
		}
		return true;
	}
	
	
	
	/**
	 * Prepare a RecurringPymt from input fields.
	 * @return
	 */
	private RecurringPymt prepPymt() {
		RecurringPymt pymt = paymentTable.getSelectionModel().getSelectedItem();
		if (pymt == null ) pymt = new RecurringPymt();
		pymt.setPayTo( txPayee.getText() );
		pymt.setAmount( new BigDecimal( txAmount.getText() ) );
		pymt.setEffDt( effDate.getValue() );
		if (termDate.getValue() != null ) 
			pymt.setTermDt( termDate.getValue() );
		pymt.setFrequency( cboxFrequency.getSelectionModel().getSelectedItem() );
		return pymt;
	}
	
	
	
	
	
	private void populateInputFields(RecurringPymt pymt) {
		txPayee.setText( pymt.getPayTo() );
		txAmount.setText( pymt.getAmount().toString() );
		effDate.setValue( pymt.getEffDt() );
		termDate.setValue( pymt.getTermDt() );
		cboxFrequency.getSelectionModel().select(pymt.getFrequency());
	}
	private void clearInputFields() {
		paymentTable.getSelectionModel().clearSelection();
		cboxFrequency.getSelectionModel().clearSelection();
		txPayee.clear();
		txAmount.clear();
		effDate.setValue(null);
		termDate.setValue(null);
		toggleButtons(false);
	}
	
	
	
	private void toggleButtons(boolean boo) {
		btnSave.setVisible( !boo );
		btnUpdate.setVisible( boo );
		btnClear.setVisible( boo );
	}

	

}
