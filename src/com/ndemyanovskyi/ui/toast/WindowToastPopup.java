/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import javafx.beans.InvalidationListener;
import javafx.geometry.Point2D;
import javafx.scene.layout.Region;
import javafx.stage.Window;

class WindowToastPopup<T extends Window> extends ToastPopup<T> {
    
    private final InvalidationListener listener = e -> relocate();

    public WindowToastPopup(Toast<T> toast) {
	super(toast);
	
	//Init owner window
	toast.setOwnerWindow(toast.getOwner());
    }

    @Override
    protected void onShowing() {
	Window window = getToast().getOwnerWindow();
		
	if(window != null) {
	    Region root = getToast().getNode();
	    Point2D screenXY = root.localToScreen(0, 0);
	    double offset = (getPopup().getX() - screenXY.getX());
	    root.setMaxWidth(getToast().getOwner().getWidth() + offset * 2);
	    
	    window.xProperty().addListener(listener);
	    window.yProperty().addListener(listener);
	    window.widthProperty().addListener(listener);
	    window.heightProperty().addListener(listener);
	}
    }

    @Override
    protected void onHidden() {	
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
	Window window = getToast().getOwner();
	
	double x = 0;
	double y = 0;
	
	switch(getToast().getAlignment()) {
	    case BOTTOM_LEFT:
		x = 0;
		y = window.getHeight() - getHeight(); break;
	    case BOTTOM_CENTER:
		x = window.getWidth() / 2 - (getWidth() / 2);
		y = window.getHeight() - getHeight(); break;
	    case BOTTOM_RIGHT:
		x = window.getWidth() - getWidth();
		y = window.getHeight() - getHeight(); break;
	    case BASELINE_LEFT:
	    case TOP_LEFT:
		x = 0;
		y = 0; break;
	    case BASELINE_CENTER:
	    case TOP_CENTER:
		x = window.getWidth() / 2 - (getWidth() / 2);
		y = 0; break;
	    case BASELINE_RIGHT:
	    case TOP_RIGHT:
		x = window.getWidth() - getWidth();
		y = 0; break;
	    case CENTER_LEFT:
		x = 0;
		y = window.getHeight() / 2 - (getHeight() / 2); break;
	    case CENTER_RIGHT:
		x = window.getWidth() - getWidth();
		y = window.getHeight() / 2 - (getHeight() / 2); break;
	    case CENTER:
		x = window.getWidth() / 2 - (getWidth() / 2);
		y = window.getHeight() / 2 - (getHeight() / 2); break;
	}
	
	setX(x + getToast().getOffset().getX() + window.getX());
	setY(y + getToast().getOffset().getY() + window.getY());
    }

}
