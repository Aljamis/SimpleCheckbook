package org.avr.simplecheckbook;

import org.avr.simplecheckbook.controllers.SampleController;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	
	private SampleController myController;
	
	@Override
	public void start(Stage primaryStage) {
		try {
//			Pane root = (Pane)FXMLLoader.load(getClass().getResource("Sample.fxml"));
//			FXMLLoader loader = new FXMLLoader( getClass().getResource("Sample.Resizable.fxml"));
			FXMLLoader loader = new FXMLLoader( getClass().getResource("CheckBook.fxml"));
			Parent root = (Parent)loader.load();
//			Pane root = (Pane)loader.load();
//			SampleController cntl = (SampleController)loader.getController();
			this.myController = (SampleController)loader.getController();
			this.myController.setStage(primaryStage);
			
			/**/
			this.myController.preLaunch();
			
			Scene scene = new Scene(root,600,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
//			primaryStage.setResizable(false);   // Make this NOT resizable
			primaryStage.setMinWidth(600);
			primaryStage.setMaxWidth(600);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	@Override
	public void stop() {
		// must disconnect from all Derby instances gracefully.
		// prompting cached memory objects to get written to file.
		this.myController.shutDown();
	}
	
}
