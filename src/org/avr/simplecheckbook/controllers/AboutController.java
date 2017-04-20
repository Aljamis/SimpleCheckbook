package org.avr.simplecheckbook.controllers;

import org.avr.simplecheckbook.utils.CheckBookVersion;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AboutController {
	
	@FXML private Label lblVersion;
	@FXML private Label lblReleaseDate;
	
	public AboutController() { }
	
	
	public void setVersion() {
		if (lblVersion == null) 
			lblVersion = new Label();
		lblVersion.setText( CheckBookVersion.getVersionOnly() );
		lblReleaseDate.setText( CheckBookVersion.getReleaseDate() );
	}
}