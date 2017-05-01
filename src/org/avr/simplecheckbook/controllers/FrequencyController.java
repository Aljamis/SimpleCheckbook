package org.avr.simplecheckbook.controllers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.avr.simplecheckbook.dataobjects.RecurringTerm;
import org.avr.simplecheckbook.dataobjects.TermType;
import org.avr.simplecheckbook.db.master.CheckBookDAO;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * Control actions from the Frequency screen; manages terms like 
 * <ul>
 *   <li>Every Other Week</li>
 *   <li>Every Other Month</li>
 *   <li>Monthly on the 15th</li>
 *   <li>Weekly on Thursday</li>
 * </ul>
 * @author Alfonso
 *
 */
public class FrequencyController extends CommonController {
	
	@FXML private TableView<RecurringTerm> tblFrequency;
	@FXML private TextField txAlternate;
	@FXML private TextField txDescription;
	@FXML private ComboBox<TermType> cboxMonthOrWeek;
	@FXML private ComboBox<DayOfWeek> cboxDayOfWeek;
	@FXML private DatePicker dateDayOfMonth;
	@FXML private Button btnSave;
	@FXML private Button btnUpdate;
	@FXML private Button btnClear;
	
	private final boolean showSave = true;

	
	
	public void preLaunch(CheckBookDAO dao) {
		checkBookDAO = dao;
		/* populate screen table */
		refresh();
		
		/* populate combo box */
		cboxMonthOrWeek.setItems( FXCollections.observableArrayList( TermType.values() ) );
		cboxAddChangeListener();
		cboxDayOfWeek.setItems(FXCollections.observableArrayList( DayOfWeek.values() ) );
	}
	
	
	
	
	/**
	 * Populate the table & disable input into the datepickers
	 */
	private void refresh() {
		List<RecurringTerm> allTerm = checkBookDAO.getAllTermR();
		tblFrequency.setItems( FXCollections.observableArrayList(allTerm) );
		
	}
	
	
	
	
	@FXML protected void handleSelectedRow(MouseEvent event) {
		RecurringTerm term = this.tblFrequency.getSelectionModel().getSelectedItem();
		
		txDescription.setText( term.getDescription() );
		txAlternate.setText( term.getAlternate() +"" );
//		if ( term.getOnThisDayOfWeek() > 0 ) {
		if ( term.getOnThisDayOfWeek() != null ) {
//			cboxDayOfWeek.getSelectionModel().select( DayOfWeek.of( term.getOnThisDayOfWeek() ) );
			cboxDayOfWeek.getSelectionModel().select( term.getOnThisDayOfWeek() );
			dateDayOfMonth.setValue( null );
		} else {
			cboxDayOfWeek.getSelectionModel().clearSelection();
			dateDayOfMonth.setValue( term.getDayOfMonthAsDate() );
		}
		cboxMonthOrWeek.getSelectionModel().select( term.getType() );
		
		toggleButtons( !showSave );
	}
	
	@FXML protected void handleToggleClear(ActionEvent event) {}
	
	@FXML protected void handleDeleteTransaction(ActionEvent event) {
	}
	
	@FXML protected void handleSaveRecurring(ActionEvent event) {
	}
	
	@FXML protected void handleUpdateRecurring(ActionEvent event) {
	}
	
	@FXML protected void handleClear(ActionEvent event) {
		toggleButtons( showSave );
		txDescription.setText("");
		cboxMonthOrWeek.getSelectionModel().clearSelection();
		cboxDayOfWeek.getSelectionModel().clearSelection();
		txAlternate.setText("");
		dateDayOfMonth.getEditor().clear();
	}
	
	
	
	
	
	/**
	 * Started this .. but not complete yet
	 * TODO complete this
	 * @return
	 */
	private boolean validInput() {
		StringBuffer errMsg = new StringBuffer();
		
		if (errMsg.length() == 0 )
			return true;
		
		displayErrorDialog( errMsg.toString() );
		return false;
	}
	
	
	
	
	/**
	 * Add ChangeListener to comboBox.  If a value is selected, disable 
	 * the datePicker.
	 */
	private void cboxAddChangeListener() {
		cboxMonthOrWeek.valueProperty().addListener( new ChangeListener<TermType>() {

			@Override
			public void changed(ObservableValue<? extends TermType> observable, TermType oldValue, TermType newValue) {
				if ( newValue == TermType.MONTHLY) {
					cboxDayOfWeek.setDisable( true );
					cboxDayOfWeek.getSelectionModel().clearSelection();
					dateDayOfMonth.setDisable( false );
				}
				else {
					cboxDayOfWeek.setDisable( false );
					dateDayOfMonth.setDisable( true );
					dateDayOfMonth.getEditor().clear();
				}
			}
			
		});
	}
	
	
	
	/**
	 * Toggle display of buttons - Ask yourself, should I hide the SAVE button?
	 * @param boo
	 */
	private void toggleButtons(boolean boo) {
		btnSave.setVisible( boo );
		btnUpdate.setVisible( !boo );
	}
}
