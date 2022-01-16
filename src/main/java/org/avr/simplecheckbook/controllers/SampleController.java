package org.avr.simplecheckbook.controllers;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avr.simplecheckbook.ExistingCheckBookDialog;
import org.avr.simplecheckbook.NewCheckBookDialog;
import org.avr.simplecheckbook.SelectCheckBookDialog;
import org.avr.simplecheckbook.dataobjects.Balance;
import org.avr.simplecheckbook.dataobjects.MasterCheckBook;
import org.avr.simplecheckbook.dataobjects.Transaction;
import org.avr.simplecheckbook.db.master.CheckBookDAO;
import org.avr.simplecheckbook.db.master.SpringMasterDAO;
import org.avr.simplecheckbook.utils.CheckBookException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SampleController extends CommonController {
	private static Logger logger = LogManager.getLogger( SampleController.class );
	
	private SpringMasterDAO masterDAO;
	
	@FXML private TableView<Transaction> transactionTable;
	@FXML private DatePicker txDate;
	@FXML private TextField txAmount;
	@FXML private TextField txPayee;
	@FXML private TextField txMemo;
	@FXML private TextField txCheckNumber;
	@FXML private Button btnDeposit;
	@FXML private Button btnPayment;
	@FXML private Button btnUpdate;
	@FXML private Button btnClear;


	public SampleController() {
		this.masterDAO = new SpringMasterDAO();
	}
	
	
	private Stage primaryStage;
	public void setStage(Stage stg) { 
		this.primaryStage = stg;
		stg.setTitle( this.checkbookName );
	}
	
	private String checkbookName = "";
	
	
	
	
	
	/**
	 * Copied logic from preLaunch()
	 */
	@FXML
	public void initialize() {
		logger.debug("Running from initialize() ... ");

		List<MasterCheckBook> books = masterDAO.findAllCheckBooks();
		
		switch ( books.size() ) {
		case 0:
			logger.debug("Did not find a single checkbook.  Prompting user to create one");
			promptForNewCheckBook();
			break;

		case 1:
			logger.debug("Found a single checkbook :  "+  books.get(0).getDescription() );
			try{
				setupCheckBookDao( books.get(0) );
				findRecurringPayments();
				refreshTableView();
			} catch (CannotGetJdbcConnectionException noDBex) {
				displayErrorDialog("Could not connect to "+ books.get(0).getDbLocation() );
			}
			break;

		default:
			logger.debug("WHOA!  Found "+ books.size() +" checkbooks.  Let the user decide");
			selectFromList( books );
			break;
		}
	
	}
	
	
	
	/**
	 * From Context menu User has selected to start a new checkbook
	 * @param event
	 */
	@FXML protected void handleNewCheckBook(ActionEvent event) {
		promptForNewCheckBook();
		refreshTableView();
	}
	@FXML protected void handleOpenExisting(ActionEvent event) {
		List<MasterCheckBook> books = masterDAO.findAllCheckBooks();
		selectFromList( books ) ;
	}
	
	
	
	
	@FXML protected void handleManageRecurring(ActionEvent event) {
		Parent root;
		try {
			FXMLLoader loader  = new FXMLLoader( getClass().getResource("/org/avr/simplecheckbook/ManageRecurring.fxml") );
//			( (RecurringController) loader.getController()).setStage(stage);
			root = (Parent) loader.load();
			( (RecurringController) loader.getController()).preLaunch(checkBookDAO);
			Stage stage = new Stage();
			stage.setTitle("Manage Frequencies");
			stage.setScene( new Scene(root, 600, 300));
			
			stage.show();
		} catch (IOException ioEx){
			ioEx.printStackTrace();
		}
	}
	
	
	
	
	
	/**
	 * Create/Edit Frequency terms.  Values stored in TERM_R.
	 * @param event
	 */
	@FXML protected void handleManageFrequency(ActionEvent event) {
		Parent root;
		try {
			FXMLLoader loader  = new FXMLLoader( getClass().getResource("/org/avr/simplecheckbook/ManageFrequency.fxml") );
			root = (Parent) loader.load();
			( (FrequencyController) loader.getController()).preLaunch(checkBookDAO);
			Stage stage = new Stage();
			stage.setTitle("Manage Recurring");
			stage.setScene( new Scene(root, 600, 300));
			
			stage.show();
		} catch (IOException ioEx){
			ioEx.printStackTrace();
		}
	}
	
	
	
	@FXML protected void handleClose() {
		shutDown();
		Platform.exit();
	}
	
	
	
	
	/**
	 * Display a custom Dialog prompting the user for Checkbook info
	 * @param primaryStage
	 */
	private void promptForNewCheckBook() {
		NewCheckBookDialog x = new NewCheckBookDialog(primaryStage);
		Optional<MasterCheckBook> result = x.showAndWait();
		
		result.ifPresent( newCheckBook -> {
			this.masterDAO.saveCheckbook(newCheckBook);
			setupCheckBookDao(newCheckBook);
			try {
				this.checkBookDAO.createTables();
			} catch (CheckBookException cbEx) {
				displayErrorDialog( cbEx.getMessage() );
			}
		});
	}
	
	
	
	
	
	/**
	 * Present user with a list of possible checkbooks
	 */
	private void selectFromList(List<MasterCheckBook> books) {
		SelectCheckBookDialog dlg = new SelectCheckBookDialog( books );
		Optional<MasterCheckBook> result = dlg.showAndWait();
		
		result.ifPresent( newCheckBook -> {
			if (newCheckBook.getDbName().equalsIgnoreCase("FindExistingOne")) {
				ExistingCheckBookDialog x = new ExistingCheckBookDialog(primaryStage);
				Optional<MasterCheckBook> existing = x.showAndWait();
				
				existing.ifPresent( cb -> {
					this.masterDAO.saveCheckbook(cb);
					setupCheckBookDao(cb);
					findRecurringPayments();
					refreshTableView();
				});
			} else {
				setupCheckBookDao( newCheckBook );
				findRecurringPayments();
				refreshTableView();
			}
		});
	}
	
	
	
	
	/**
	 * Setup connections to a Checkbook DB
	 * @param cb
	 */
	private void setupCheckBookDao(MasterCheckBook cb) {
		logger.debug("Opening connection to selected DB : "+ cb.getDescription() );
		this.checkbookName  = cb.getDbName() +" - "+ cb.getDescription();
		this.checkBookDAO = new CheckBookDAO( cb.getDbLocation() + File.separatorChar + cb.getDbName());
		
		/* TODO see if the versions are compatible */
//		if (!cb.getAppVersion().equals( CheckBookVersion.getVersion() ))
//			promptForDBupdate();
	}
	
	
	
	
	/**
	 * Display transactions within the last 180 days :
	 *   get balance from 180 days ago
	 *   get all transactions after that balance.
	 *   Calculate balance after each transaction
	 */
	private void refreshTableView() {
		Balance bal = checkBookDAO.getBalance(180);
		if (bal == null) {
			bal = checkBookDAO.getNextMostRecentBalance();
			if (bal == null) {
				// This is a new Checkbook b/c there are no balances.
				this.transactionTable.setItems(null);
				return;
			}
		}
		List<Transaction> trans = checkBookDAO.getTransactionsAfter(bal);
		
		calculateTxBalance(bal , trans );
		this.transactionTable.setItems( FXCollections.observableArrayList( trans ));
		setRowHover();
		clearInputFields();
	}
	
	
	
	
	/**
	 * Display the "Comment" on the tooltip when the mouse hovers of
	 */
	private void setRowHover() {
		transactionTable.setRowFactory( tableView -> {
			final TableRow<Transaction> row = new TableRow<>();
			
			row.hoverProperty().addListener((observable) -> {
				final Transaction trans = row.getItem();
				if (row.isHover() && trans != null ) {
					if ( trans.getMemo() != null && trans.getMemo().trim().length() > 0) {
						Tooltip tooltip = new Tooltip( trans.getMemo() );
						row.setTooltip( tooltip );
					}
				}
			});
			
			return row;
		});
	}
	
	
	
	@FXML protected void handleDeposit(ActionEvent event) {
		handleTransaction( txAmount.getText() , null );
	}
	@FXML protected void handlePayment(ActionEvent event) {
		handleTransaction( null, txAmount.getText() );
	}
	
	
	
	/**
	 * Validate all input, create a Transaction and populate with either
	 * a deposit or payment.
	 * @param deposit
	 * @param payment
	 */
	private void handleTransaction( String deposit , String payment ) {
		try {
			validateInput();
			Transaction tx = createTransactionFromUI();
			if ( deposit == null ) {
				tx.setDebit( new BigDecimal( payment ) );
			} else {
				tx.setCredit( new BigDecimal( deposit ) );
			}
			
			processTransaction( tx );
		} catch (CheckBookException cbEx) {
			displayErrorDialog( cbEx.getMessage() );
		}
		refreshTableView();
	}
	
	
	
	
	
	/**
	 * User has selected a row.  Change screen to allow for updates.
	 * @param event
	 */
	@FXML protected void handleSelectedRow(MouseEvent event) {
		Transaction trans = this.transactionTable.getSelectionModel().getSelectedItem();
		if (trans ==  null)
			return;  // This occurs if the user tries sorting by column
		populateInputFields(trans);
		toggleButtons(true);
	}
	
	
	
	
	/**
	 * Get the row selected by the user, validate, make changes.
	 * @param event
	 */
	@FXML protected void handleUpdate(ActionEvent event) {
		Transaction trans = this.transactionTable.getSelectionModel().getSelectedItem();
		try {
			validateInput();
			Transaction newTrans = createTransactionFromUI();
			newTrans.setTransID( trans.getTransID() );
			
			if (trans.getCredit() == null) {
				newTrans.setDebit( new BigDecimal(txAmount.getText() ) );
			} else {
				newTrans.setCredit( new BigDecimal(txAmount.getText() ) );
			}
			processTransaction(newTrans);
//			checkBookDAO.updateTransaction( newTrans );
			
		} catch (CheckBookException cbEx) {
			displayErrorDialog( cbEx.getMessage() );
		}
		
		clearInputFields();
		toggleButtons(false);
		refreshTableView();
		txDate.requestFocus();
	}
	@FXML protected void handleClear(ActionEvent event) {
		clearInputFields();
		toggleButtons(false);
	}
	@FXML protected void handleToggleClear(ActionEvent event) {
		Transaction trans = this.transactionTable.getSelectionModel().getSelectedItem();
		trans.setCleared( !trans.getCleared() );
		checkBookDAO.updateTransaction(trans);
		
		refreshTableView();
		txDate.requestFocus();	
	}
	@FXML protected void handleRefresh(ActionEvent event) {
		refreshTableView();
		clearInputFields();
	}
	@FXML protected void handleDeleteTransaction(ActionEvent event) {
		Transaction trans = this.transactionTable.getSelectionModel().getSelectedItem();
		deleteTransaction( trans );
		refreshTableView();
		txDate.requestFocus();	
	}
	@FXML protected void handleAbout(ActionEvent event) {
		Parent root;
		try {
			FXMLLoader loader  = new FXMLLoader( getClass().getResource("/org/avr/simplecheckbook/About.fxml") );
//			( (RecurringController) loader.getController()).setStage(stage);
			root = (Parent) loader.load();
			( (AboutController) loader.getController()).setVersion();
			Stage stage = new Stage();
			stage.setTitle("About");
			stage.setScene( new Scene(root, 300, 150));
			
			stage.show();
		} catch (IOException ioEx){
			ioEx.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * Toggle visibility of buttons - Ask yourself:  Should I hide Deposit and Payment?
	 * @param boo
	 */
	private void toggleButtons(boolean boo) {
		btnClear.setVisible(boo);
		btnUpdate.setVisible(boo);
		btnDeposit.setVisible(!boo);
		btnPayment.setVisible(!boo);
		
	}
	
	
	/**
	 * Create a transaction from UI fields - EXCEPT debit or credit.
	 * @return
	 * @throws CheckBookException
	 */
	private Transaction createTransactionFromUI() throws CheckBookException {
		Transaction tx = new Transaction();
		tx.setPayee( txPayee.getText() );
		tx.setMemo( txMemo.getText() );
		if (txCheckNumber.getText().trim().length() > 0)
			tx.setCheckNumber( new BigDecimal( txCheckNumber.getText() ));
		
		LocalDateTime dtm = txDate.getValue().atTime( LocalTime.now() );
		tx.setTxDate( Timestamp.valueOf( dtm ) );
		
		return tx;
	}
	
	
	
	
	/**
	 * Validate all REQUIRED FIELDS have been entered and their values are
	 * within thresholds.
	 * 
	 * NOTE :  DatePicker fields are automatically validated to correct dates.
	 * @throws CheckBookException
	 */
	private void validateInput() throws CheckBookException {
		StringBuffer str = new StringBuffer();
		
		if( txPayee.getText() == null || txPayee.getText().trim().length() == 0 )
			str.append("- Missing Payee \n");
		
		if (txDate.getValue() == null )
			str.append("- Missing Date\n");
		
		if(txAmount.getText() == null || txAmount.getText().length() == 0)
			str.append("- Amount missing\n");
		
		try {
			new BigDecimal(txAmount.getText());
		} catch (NumberFormatException nfEx) {
			str.append("- Amount is not a valid number\n");
		}
		
		try {
			if (txCheckNumber.getText().trim().length() > 0 )
				Integer.parseInt( txCheckNumber.getText() );
		} catch (NumberFormatException nfEx) {
			str.append("- Check Number is not a number");
		}
		
		if (str.length() > 0 )
			throw new CheckBookException( str.toString() );
	}
	
	
	
	/**
	 * Similar to processTransaction but
	 * <ol>
	 * <li>Get balance for day of transaction</li>
	 * <li>Subtract (or add) the transaction amount</li>
	 * <li>Update balance</li>
	 * </ol>
	 * 
	 * @param tx
	 */
	private void deleteTransaction(Transaction tx) {
		checkBookDAO.deleteTrans( tx );
		
		Balance b = checkBookDAO.getBalanceOn( new Date(tx.getTxDate().getTime() ) );
		
		if (tx.getDebit() == null)
			b.setBalance( b.getBalance().subtract( tx.getCredit() ) );
		else 
			b.setBalance( b.getBalance().add( tx.getDebit() ) );
		checkBookDAO.saveBalance( b );
	}
	
	
	
	
	/**
	 * Remove all text/values from the input fields
	 */
	private void clearInputFields() {
		txDate.setValue(null);
		txCheckNumber.clear();
		txPayee.clear();;
		txAmount.clear();
		txMemo.clear();
		
		transactionTable.getSelectionModel().clearSelection();
		toggleButtons(false);
	}
	
	private void populateInputFields(Transaction trans) {
		txDate.setValue(trans.getTxDate().toLocalDateTime().toLocalDate());
		txCheckNumber.setText( trans.getCheckNumber() == null ? "" : trans.getCheckNumber().toString() );
		txPayee.setText( trans.getPayee() );
		txAmount.setText( trans.getAmount() );
		txMemo.setText( trans.getMemo() );
	}
	
	
	
	
	/**
	 * ONly call this when the application is shutting down.
	 */
	public void shutDown() {
		/* DAO is null when user does not select a DB
		 * and exits the application.
		 */
		if (checkBookDAO != null) {
			reCalculateBalances();
			checkBookDAO.shutDown();
		}
	}
	
	
	
	
	/**
	 * Export data from checkbook
	 */
	private void upgradeCheckBook() {
		checkBookDAO.exportCheckBook();
	}
}
