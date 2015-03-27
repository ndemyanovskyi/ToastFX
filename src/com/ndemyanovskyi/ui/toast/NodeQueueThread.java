/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.toast;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;

final class NodeQueueThread extends QueueThread<Node> implements ChangeListener<Object> {

    public NodeQueueThread(ToastQueue<Node> queue) {
	super(queue);
    } //Bottom
    //Baseline and Top
    //Center

    @Override
    protected void onStart() {
	getQueue().getOwner().layoutBoundsProperty().addListener(this);
	getQueue().getOwner().boundsInParentProperty().addListener(this);
    }

    @Override
    protected void onFinish() {
	getQueue().getOwner().layoutBoundsProperty().removeListener(this);
	getQueue().getOwner().boundsInParentProperty().removeListener(this);
    }

    @Override
    protected void onToastShow(Toast<Node> toast) {	
	toast.getOwnerWindow().xProperty().addListener(this);
	toast.getOwnerWindow().yProperty().addListener(this);
	toast.getOwnerWindow().widthProperty().addListener(this);
	toast.getOwnerWindow().heightProperty().addListener(this);
    }

    @Override
    protected void onToastHide(Toast<Node> toast) {
	toast.getOwnerWindow().xProperty().removeListener(this);
	toast.getOwnerWindow().yProperty().removeListener(this);
	toast.getOwnerWindow().widthProperty().removeListener(this);
	toast.getOwnerWindow().heightProperty().removeListener(this);
    }

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
	synchronized(getPopups()) {
	    getPopups().values().forEach(this::updatePopupPosition);
	}
    }

    @Override
    protected void updatePopupPosition(PopupController<Node> pc) {
	double x;
	double y;
	Bounds b = pc.getToast().getOwner().localToScreen(pc.getToast().getOwner().getBoundsInLocal());
	switch(pc.getToast().getAlignment()) {
	    //Bottom
	    case BOTTOM_LEFT:
		x = b.getMinX();
		y = b.getMaxY() - pc.getHeight();
		break;
	    case BOTTOM_CENTER:
		x = b.getMinX() + b.getWidth() / 2 - (pc.getWidth() / 2);
		y = b.getMaxY() - pc.getHeight();
		break;
	    case BOTTOM_RIGHT:
		x = b.getMaxX() - pc.getWidth();
		y = b.getMaxY() - pc.getHeight();
		break;
	    //Baseline and Top
	    case BASELINE_LEFT:
	    case TOP_LEFT:
		x = b.getMinX();
		y = b.getMinY();
		break;
	    case BASELINE_CENTER:
	    case TOP_CENTER:
		x = b.getMinX() + b.getWidth() / 2 - (pc.getWidth() / 2);
		y = b.getMinY();
		break;
	    case BASELINE_RIGHT:
	    case TOP_RIGHT:
		x = b.getMaxX() - pc.getWidth();
		y = b.getMinY();
		break;
	    //Center
	    case CENTER_LEFT:
		x = b.getMinX();
		y = b.getMinY() + b.getHeight() / 2 - (pc.getHeight() / 2);
		break;
	    case CENTER_RIGHT:
		x = b.getMaxX() - pc.getWidth();
		y = b.getMinY() + b.getHeight() / 2 - (pc.getHeight() / 2);
		break;
	    case CENTER:
		x = b.getMinX() + b.getWidth() / 2 - (pc.getWidth() / 2);
		y = b.getMinY() + b.getHeight() / 2 - (pc.getHeight() / 2);
		break;
	    default:
		x = 0;
		y = 0;
		break;
	}
	pc.setX(x + pc.getToast().getOffset().getX());
	pc.setY(y + pc.getToast().getOffset().getY());
    }

}
