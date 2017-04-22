package org.avr.simplecheckbook.controllers;

import java.io.File;

import org.avr.simplecheckbook.utils.CheckBookVersion;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AboutController {
	
	@FXML private ImageView imgAbout;
	
	@FXML private Label lblVersion;
	@FXML private Label lblReleaseDate;
	
	public AboutController() { }
	
	
	public void setVersion() {
		if (lblVersion == null) 
			lblVersion = new Label();
		lblVersion.setText( CheckBookVersion.getVersion() );
		lblReleaseDate.setText( CheckBookVersion.getReleaseDate() );
		
		File image = new File("D:/Workspaces/JavaFX/SimpleCheckbook/resources/images/Checkbook.png");
		Image img = new Image( image.toURI().toString() );
		imgAbout = new ImageView(img);
	}
}