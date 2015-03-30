/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.example;

import com.ndemyanovskyi.ui.toast.Toast;
import com.ndemyanovskyi.ui.toast.ToastQueue;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class Example extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
	Button button = new Button("Show toast without queue");
	Button queueButton = new Button("Show toast without queue");
	HBox root = new HBox(button, queueButton);
	Scene scene = new Scene(root, 400, 300);
	
	Object owner = scene;
	
	Toast.of(owner, 
		"This toast does not hide yourself, "
			+ "because duration defined as indefinite. "
			+ "To hide this toast double-click on it. "
			+ "It works for any toast.", Duration.INDEFINITE).show();
	
	button.setOnAction(new EventHandler<ActionEvent>() {

	    int count = 0;
	    
	    public void handle(ActionEvent e) {
		Toast.of(owner, "This toast hide yourself, because duration != indefinite. "
			+ "Also it is not placed in queue. "
			+ "This means that it will appear as soon method \"show\" will be invoked. "
			+ "Toast index " + count++ + ".",
			Pos.CENTER_LEFT).show();
	    }
	});
	
	ToastQueue queue = new ToastQueue();
	
	queueButton.setOnAction(new EventHandler<ActionEvent>() {
	    
	    int count = 0;

	    public void handle(ActionEvent e) {
		Toast<?> toast = Toast.of(owner, "This toast placed in queue. "
			+ "It can't be shown when calling the method \"show\". "
			+ "The queue shows all toasts have in order itself. "
			+ "Toast index " + count++ + ".");
		queue.getToasts().add(toast);
	    }
	});
	
	stage.setTitle("Toast example");
	stage.setScene(scene);
	
	stage.show();
    }

    public static void main(String[] args) {
	launch(args);
    }
    
}
