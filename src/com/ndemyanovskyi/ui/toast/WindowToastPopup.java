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
	relocate(this,
		window.getX(), window.getY(),
		window.getWidth(), window.getHeight());
    }

}
