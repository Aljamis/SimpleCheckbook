package org.avr.simplecheckbook;

import java.util.List;
import java.util.Optional;

import org.avr.simplecheckbook.dataobjects.MasterCheckBook;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;




/**
 * Prompt the user with a list of available checkbooks.
 * @author Alfonso
 *
 */
public class SelectCheckBookDialog extends Dialog<MasterCheckBook>{
	
	Optional<MasterCheckBook> result = null;
	
	private ListView<MasterCheckBook> myList;
	
	
	
	public SelectCheckBookDialog(List<MasterCheckBook> books) {
		populateMyList( books );
		resetCellFactory();
		
		setTitle("Select from an existing checkbook");
		
		StackPane pane = new StackPane();
		pane.getChildren().add( myList );
		
		ButtonType btnSelect = new ButtonType("Select", ButtonData.OK_DONE );
		ButtonType btnCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		
		getDialogPane().setContent( pane );
		getDialogPane().getButtonTypes().addAll(btnSelect , btnCancel);
		
		setResultConverter( new Callback<ButtonType, MasterCheckBook>() {
			
			@Override
			public MasterCheckBook call(ButtonType param) {
				if (param == btnSelect) {
					return myList.getSelectionModel().getSelectedItem();
				}
				return null;  /* TODO maybe return an empty instead? */
			}
		});
	}
	
	
	/**
	 * Place holder to populate with temporary stuff.
	 */
	private void populateMyList(List<MasterCheckBook> list) {
		addForNew(list);
		
		myList = new ListView<MasterCheckBook>();
		
		ObservableList<MasterCheckBook> listObs = FXCollections.observableArrayList( list );
		myList.setItems( listObs );
		myList.setMaxHeight(120);
	}
	
	
	
	
	/**
	 * So that the list view only displays the checkbook name
	 */
	private void resetCellFactory() {
		myList.setCellFactory( new Callback<ListView<MasterCheckBook> , ListCell<MasterCheckBook>>() {
			
			@Override
			public ListCell<MasterCheckBook> call(ListView<MasterCheckBook> param) {
				ListCell<MasterCheckBook> cell = new ListCell<MasterCheckBook>(){
					
					@Override
					protected void updateItem(MasterCheckBook b , boolean bln) {
						super.updateItem(b, bln);
						if (b != null) {
							setText(b.getDescription());
						}
					}
				};
				return cell;
			}
		});
	}
	
	
	
	
	/**
	 * Add an empty element for a NEW check book
	 * @param list
	 */
	private void addForNew(List<MasterCheckBook> list) {
		MasterCheckBook newCB = new MasterCheckBook();
		newCB.setDescription("Not listed here");
		newCB.setDbName("FindExistingOne");
		list.add(newCB);
	}

}
