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
	
	double windowX = 0;
	double windowY = 0;
	double x = 0;
	double y = 0;
	
	Window window = scene.getWindow();
	if(window != null) {
	    windowX = window.getX();
	    windowY = window.getY();
	}
	
	switch(getToast().getAlignment()) {
	    case BOTTOM_LEFT:
		x = 0;
		y = scene.getHeight() - getHeight(); break;
	    case BOTTOM_CENTER:
		x = scene.getWidth() / 2 - (getWidth() / 2);
		y = scene.getHeight() - getHeight(); break;
	    case BOTTOM_RIGHT:
		x = scene.getWidth() - getWidth();
		y = scene.getHeight() - getHeight(); break;
	    case BASELINE_LEFT:
	    case TOP_LEFT:
		x = 0;
		y = 0; break;
	    case BASELINE_CENTER:
	    case TOP_CENTER:
		x = scene.getWidth() / 2 - (getWidth() / 2);
		y = 0; break;
	    case BASELINE_RIGHT:
	    case TOP_RIGHT:
		x = scene.getWidth() - getWidth();
		y = 0; break;
	    case CENTER_LEFT:
		x = 0;
		y = scene.getHeight() / 2 - (getHeight() / 2); break;
	    case CENTER_RIGHT:
		x = scene.getWidth() - getWidth();
		y = scene.getHeight() / 2 - (getHeight() / 2); break;
	    case CENTER:
		x = scene.getWidth() / 2 - (getWidth() / 2);
		y = scene.getHeight() / 2 - (getHeight() / 2); break;
	}
	
	setX(x + getToast().getOffset().getX() + scene.getX() + windowX);
	setY(y + getToast().getOffset().getY() + scene.getY() + windowY);
    }

}
