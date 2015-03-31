/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;
 
import java.lang.ref.WeakReference;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class ScreenToastPopup<T extends Screen> extends ToastPopup<T> {
    
    private static final Object MUTEX = new Object();
    private static WeakReference<Stage> backStage;

    public ScreenToastPopup(Toast<T> toast) {
	super(toast);
	
	//Init owner window
	synchronized(MUTEX) {
	    getToast().setOwnerWindow(getStage());
	}
    }

    @Override
    protected void onShowRequested() {
	Platform.runLater(() -> {
	    synchronized(MUTEX) {
		Stage stage = (Stage) getToast().getOwnerWindow();
		if(stage == getBackStage()) {
		    stage.show();
		    stage.toBack();
		    
		    //Fix focus for previous window
		    Iterator<Window> it = Window.impl_getWindows();
		    boolean finded = false;
		    while(it.hasNext()) {
			Window window = it.next();
			if(window instanceof Stage) {
			    if(finded == true) {
				window.requestFocus();
				break;
			    }
			    if(window == stage) {
				finded = true;
			    }
			}
		    }
		}
	    }
	});
    }

    @Override
    protected void onShowing() {	
	Region root = getToast().getNode();
	Point2D screenXY = root.localToScreen(0, 0);
	double offset = (getPopup().getX() - screenXY.getX());
	root.setMaxWidth(getToast().getOwner().getVisualBounds().getWidth() + offset * 2);
    }

    @Override
    protected void onHidden() {
	synchronized(MUTEX) {
	    Window window = getToast().getOwnerWindow();
	    if(window == getBackStage()) {
		window.hide();
	    }
	}
    }

    @Override
    public void relocate() {
	Screen screen = getToast().getOwner();
	Rectangle2D b = screen.getVisualBounds();
	relocate(this, b);
    }
    
    private static Stage getStage() {
	synchronized(MUTEX) {
	    Stage stage = getFocusedStage();
	    if(stage == null || !stage.isShowing()) {
		stage = getBackStage();
		if(stage == null) {
		    stage = new Stage();
		    stage.initStyle(StageStyle.UTILITY);
		    stage.setMaxWidth(0);
		    stage.setMaxHeight(0);
		    stage.setOpacity(0);
		    stage.setTitle("Back stage for screen toasts");
		    stage.setX(Double.MAX_VALUE);
		    stage.setY(Double.MAX_VALUE);
		    backStage = new WeakReference<>(stage);
		}
	    }
	    return stage;
	}
    }
    
    private static Stage getBackStage() {
	synchronized(MUTEX) {
	    return backStage != null ? backStage.get() : null;
	}
    }
    
    private static Stage getFocusedStage() {
	Iterator<Window> it = Window.impl_getWindows();
	while(it.hasNext()) {
	    Window window = it.next();
	    if(window instanceof Stage) {
		return (Stage) window;
	    }
	}
	return null;
    }

}
