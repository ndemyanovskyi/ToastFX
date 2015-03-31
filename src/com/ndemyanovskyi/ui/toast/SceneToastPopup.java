/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Window;

final class SceneToastPopup<T extends Scene> extends ToastPopup<T> {
    
    private final InvalidationListener listener = e -> relocate();
    
    private final ChangeListener<Window> windowListener = (wp, oldWindow, newWindow) -> {
	if(oldWindow != null) {
	    oldWindow.xProperty().removeListener(listener);
	    oldWindow.yProperty().removeListener(listener);
	    oldWindow.widthProperty().removeListener(listener);
	    oldWindow.heightProperty().removeListener(listener);
	}
	if(newWindow != null) {
	    newWindow.xProperty().addListener(listener);
	    newWindow.yProperty().addListener(listener);
	    newWindow.widthProperty().addListener(listener);
	    newWindow.heightProperty().addListener(listener);
	}
    };

    public SceneToastPopup(Toast<T> toast) {
	super(toast);
	
	//Init owner window
	toast.ownerWindowPropertyImpl().bind(
		toast.getOwner().windowProperty());
    }

    @Override
    protected void onShowing() {
	getToast().getOwner().xProperty().addListener(listener);
	getToast().getOwner().yProperty().addListener(listener);
	getToast().getOwner().widthProperty().addListener(listener);
	getToast().getOwner().heightProperty().addListener(listener);
	getToast().ownerWindowProperty().addListener(windowListener);
	
	try {
	    Region root = getToast().getNode();
	    Point2D screenXY = root.localToScreen(0, 0);
	    double offset = (getPopup().getX() - screenXY.getX());
	    root.setMaxWidth(getToast().getOwner().getWidth() + offset * 2);
	} catch(IllegalStateException ise) {}
	
	Window window = getToast().getOwnerWindow();
	if(window != null) {
	    window.xProperty().addListener(listener);
	    window.yProperty().addListener(listener);
	    window.widthProperty().addListener(listener);
	    window.heightProperty().addListener(listener);
	}
    }

    @Override
    protected void onHidden() {
	getToast().getOwner().xProperty().removeListener(listener);
	getToast().getOwner().yProperty().removeListener(listener);
	getToast().getOwner().widthProperty().removeListener(listener);
	getToast().getOwner().heightProperty().removeListener(listener);
	getToast().ownerWindowProperty().removeListener(windowListener);
		
	Window window = getToast().getOwnerWindow();
	if(window != null) {
	    window.xProperty().removeListener(listener);
	    window.yProperty().removeListener(listener);
	    window.widthProperty().removeListener(listener);
	    window.heightProperty().removeListener(listener);
	}
    }

    @Override
    public void relocate() {
	Scene scene = getToast().getOwner();
	Window window = scene.getWindow();
	
	relocate(this,
		window.getX() + scene.getX(), 
		window.getY() + scene.getY(),
		scene.getWidth(), scene.getHeight());
    }

}
