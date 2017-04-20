package org.avr.simplecheckbook.controllers;

import org.avr.simplecheckbook.utils.CheckBookVersion;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AboutController {
	
	@FXML private Label lblVersion;
	
	public AboutController() { }
	
	
	public void setVersion() {
		if (lblVersion == null) 
			lblVersion = new Label();
		this.lblVersion.setText( CheckBookVersion.getVersionOnly() );
	}
}