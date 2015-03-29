/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;

class NodeToastPopup<T extends Node> extends ToastPopup<T> {
    
    private final InvalidationListener listener = e -> relocate();

    public NodeToastPopup(Toast<T> toast) {
	super(toast);
	
	//Init owner window
	ChangeListener<Window> windowListener
		= (wp, oldWindow, newWindow) -> toast.setOwnerWindow(newWindow);

	Scene scene = toast.getOwner().getScene();
	if(scene != null) {
	    toast.setOwnerWindow(scene.getWindow());
	    scene.windowProperty().addListener(windowListener);
	}

	toast.getOwner().sceneProperty().addListener((sp, oldScene, newScene) -> {
	    if(oldScene != null) oldScene.windowProperty().removeListener(windowListener);
	    if(newScene != null) newScene.windowProperty().addListener(windowListener);
	});
    }

    @Override
    protected void onShow(Window window) {
	getToast().getOwner().layoutXProperty().addListener(listener);
	getToast().getOwner().layoutYProperty().addListener(listener);
	getToast().getOwner().layoutBoundsProperty().addListener(listener);
	getToast().getOwner().boundsInLocalProperty().addListener(listener);
	getToast().getOwner().boundsInParentProperty().addListener(listener);
	
	window.xProperty().addListener(listener);
	window.yProperty().addListener(listener);
	window.widthProperty().addListener(listener);
	window.heightProperty().addListener(listener);
    }

    @Override
    protected void onHide(Window window) {
	getToast().getOwner().layoutXProperty().removeListener(listener);
	getToast().getOwner().layoutYProperty().removeListener(listener);
	getToast().getOwner().layoutBoundsProperty().removeListener(listener);
	getToast().getOwner().boundsInLocalProperty().removeListener(listener);
	getToast().getOwner().boundsInParentProperty().removeListener(listener);
	
	window.xProperty().removeListener(listener);
	window.yProperty().removeListener(listener);
	window.widthProperty().removeListener(listener);
	window.heightProperty().removeListener(listener);
    }

    @Override
    public void relocate() {
	Bounds b = getToast().getOwner().localToScreen(
		getToast().getOwner().getBoundsInLocal());
	
	double x = 0;
	double y = 0;
	
	switch(getToast().getAlignment()) {
	    case BOTTOM_LEFT:
		x = b.getMinX();
		y = b.getMaxY() - getHeight(); break;
	    case BOTTOM_CENTER:
		x = b.getMinX() + b.getWidth() / 2 - (getWidth() / 2);
		y = b.getMaxY() - getHeight(); break;
	    case BOTTOM_RIGHT:
		x = b.getMaxX() - getWidth();
		y = b.getMaxY() - getHeight(); break;
	    case BASELINE_LEFT:
	    case TOP_LEFT:
		x = b.getMinX();
		y = b.getMinY(); break;
	    case BASELINE_CENTER:
	    case TOP_CENTER:
		x = b.getMinX() + b.getWidth() / 2 - (getWidth() / 2);
		y = b.getMinY(); break;
	    case BASELINE_RIGHT:
	    case TOP_RIGHT:
		x = b.getMaxX() - getWidth();
		y = b.getMinY(); break;
	    case CENTER_LEFT:
		x = b.getMinX();
		y = b.getMinY() + b.getHeight() / 2 - (getHeight() / 2); break;
	    case CENTER_RIGHT:
		x = b.getMaxX() - getWidth();
		y = b.getMinY() + b.getHeight() / 2 - (getHeight() / 2); break;
	    case CENTER:
		x = b.getMinX() + b.getWidth() / 2 - (getWidth() / 2);
		y = b.getMinY() + b.getHeight() / 2 - (getHeight() / 2); break;
	}
	setX(x + getToast().getOffset().getX());
	setY(y + getToast().getOffset().getY());
    }

}
