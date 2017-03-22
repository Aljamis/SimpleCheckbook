package org.avr.simplecheckbook;

import java.io.File;
import java.util.Optional;

import org.avr.simplecheckbook.dataobjects.MasterCheckBook;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;



/**
 * This class is identical to NewCheckBookDialog.
 * @author Alfonso
 *
 */
public class ExistingCheckBookDialog extends Dialog<MasterCheckBook> {
	
	Optional<MasterCheckBook> result = null;
	
	private TextField txtDesc = new TextField();
	private DirectoryChooser dirChoice = new DirectoryChooser();
	
	private File dirLocation = null;
	private Label lblDirLoc = new Label("");
	
	private StringBuffer errorMessages;
	
	
	/**
	 * Set the Dialog layout
	 */
	public ExistingCheckBookDialog(Stage primaryStg) {
		this.setTitle("Create a new Checkbook");
		this.setHeaderText(headerTxt());
		this.setResizable(false);
		
		dirChoice.setTitle("Location");
		
		
		GridPane grid = new GridPane();
		/* DB Name row */
		grid.add( new Label("DB Name"), 1, 1);
		grid.add( new Label() , 2, 1);
		/* Description row */
		grid.add( new Label("Description"), 1, 2);
		grid.add(txtDesc, 2, 2);
		/* Location row */ 
		grid.add( dbLocationButton(primaryStg), 1, 3);
		grid.add(lblDirLoc, 2, 3);
		
		grid.setHgap(10);
		grid.setVgap(2);
		grid.setPadding( new Insets(20, 10, 10, 10));
		this.getDialogPane().setContent(grid);
		
		ButtonType btnOk = new ButtonType("Okay", ButtonData.OK_DONE);
		ButtonType btnCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		this.getDialogPane().getButtonTypes().add(btnOk);
		this.getDialogPane().getButtonTypes().add(btnCancel);
		
		/* Must be AFTER the Button has been added to the list of buttons */
		final Button btnOK = (Button) this.getDialogPane().lookupButton( btnOk );
		btnOK.addEventFilter(ActionEvent.ACTION, event -> {
			if (!validEntries()) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Correct the following : ");
				alert.setContentText( this.errorMessages.toString() );
				alert.showAndWait();
				event.consume();
			}
		});
		
		this.setResultConverter( new Callback<ButtonType, MasterCheckBook>() {
			
			@Override
			public MasterCheckBook call(ButtonType param) {
				if (param == btnOk) {
					MasterCheckBook mcb = new MasterCheckBook();
					mcb.setDbLocation( dirLocation.getParent() );
					mcb.setDbName( dirLocation.getName() );
					mcb.setDescription( txtDesc.getText() );
					return mcb;
				}
				return null;
			}
		});
	}
	
	
	/**
	 * Provide the text that goes into the Header.
	 * @return
	 */
	private String headerTxt() {
		StringBuffer str = new StringBuffer();
		str.append("Please provide a Name, Description and Location ");
		str.append("for this new Checkbook.");
		return str.toString();
	}
	
	
	
	/**
	 * Validate user input.
	 * @return
	 */
	private boolean validEntries() {
		this.errorMessages = new StringBuffer();
		if (dirLocation == null) {
			errorMessages.append("- Missing Directory Location \n");
		}
		
		if (errorMessages.length() > 0)
			return false;
		return true;
	}
	
	
	
	
	/**
	 * Button to prompt user for a location 
	 * @param stg
	 * @return
	 */
	private Button dbLocationButton(Stage stg) {
		Button btn = new Button("Location");
		btn.setOnAction(
				new EventHandler<ActionEvent>() {
					
					@Override
					public void handle(ActionEvent event) {
						dirLocation = dirChoice.showDialog(stg);
						if (dirLocation != null)
							lblDirLoc.setText( dirLocation.getAbsolutePath() );
					}
				} );
		return btn;
	}

}
