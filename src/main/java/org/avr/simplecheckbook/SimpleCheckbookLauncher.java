package org.avr.simplecheckbook;

import org.avr.simplecheckbook.controllers.SampleController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



/**
 * Rename the Main class to a something more readable
 * Also move all initialization tasks (and closing tasks) to the Controller
 * @author Alfonsovia
 *
 */
public class SimpleCheckbookLauncher extends Application {
	
	private SampleController myMainController = null;

	@Override
	public void start(Stage primaryStage) {
		System.out.println("In the new launcher");
		try {
			FXMLLoader loader = new FXMLLoader( getClass().getResource("CheckBook.fxml"));
			Parent root = (Parent)loader.load();
			this.myMainController = (SampleController)loader.getController();
			this.myMainController.setStage(primaryStage);
			
			Scene scene = new Scene(root,600,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setMinWidth(600);
			primaryStage.setMaxWidth(600);
			primaryStage.show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println("Exitting the new launcher");
	}
	
	
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	@Override
	public void stop() {
		System.out.println("disconnecting from all DBs");
		this.myMainController.shutDown();
	}

}
