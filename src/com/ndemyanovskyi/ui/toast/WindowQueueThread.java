/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.toast;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.stage.Window;

final class WindowQueueThread extends QueueThread<Window> implements ChangeListener<Object> {

    private final ChangeListener<Scene> sceneListener = (observable, oldScene, newScene) -> {
	/*if(oldScene != null) {
	    oldScene.xProperty().removeListener(this);
	    oldScene.yProperty().removeListener(this);
	    oldScene.widthProperty().removeListener(this);
	    oldScene.heightProperty().removeListener(this);
	}
	if(newScene != null) {
	    newScene.xProperty().addListener(this);
	    newScene.yProperty().addListener(this);
	    newScene.widthProperty().addListener(this);
	    newScene.heightProperty().addListener(this);
	}*/
    };

    public WindowQueueThread(ToastQueue<Window> queue) {
	super(queue);
    }

    @Override
    protected void onFinish() {
	getQueue().getOwner().xProperty().removeListener(this);
	getQueue().getOwner().yProperty().removeListener(this);
	getQueue().getOwner().widthProperty().removeListener(this);
	getQueue().getOwner().heightProperty().removeListener(this);
	getQueue().getOwner().sceneProperty().removeListener(sceneListener);
    }

    @Override
    protected void onStart() {
	getQueue().getOwner().xProperty().addListener(this);
	getQueue().getOwner().yProperty().addListener(this);
	getQueue().getOwner().widthProperty().addListener(this);
	getQueue().getOwner().heightProperty().addListener(this);
	getQueue().getOwner().sceneProperty().addListener(sceneListener);
    }

    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
	synchronized(getPopups()) {
	    getPopups().values().forEach(this::updatePopupPosition);
	}
    }

    @Override
    protected void updatePopupPosition(com.ndemyanovskyi.ui.toast.PopupController<Window> pc) {
	double x;
	double y;
	Scene s = pc.getToast().getOwner().getScene();
	double offsetX = s.getX() + s.getWindow().getX();
	double offsetY = s.getY() + s.getWindow().getY();
	
	switch(pc.getToast().getAlignment()) {

	    //Bottom
	    case BOTTOM_LEFT:
		x = 0;
		y = s.getHeight() - pc.getHeight();
		break;

	    case BOTTOM_CENTER:
		x = s.getWidth() / 2 - (pc.getWidth() / 2);
		y = s.getHeight() - pc.getHeight();
		break;

	    case BOTTOM_RIGHT:
		x = s.getWidth() - pc.getWidth();
		y = s.getHeight() - pc.getHeight();
		break;

	    //Top and baseline
	    case BASELINE_LEFT:
	    case TOP_LEFT:
		x = 0;
		y = 0;
		break;

	    case BASELINE_CENTER:
	    case TOP_CENTER:
		x = s.getWidth() / 2 - (pc.getWidth() / 2);
		y = 0;
		break;

	    case BASELINE_RIGHT:
	    case TOP_RIGHT:
		x = s.getWidth() - pc.getWidth();
		y = 0;
		break;

	    //Center
	    case CENTER_LEFT:
		x = 0;
		y = s.getHeight() / 2 - (pc.getHeight() / 2);
		break;

	    case CENTER_RIGHT:
		x = s.getWidth() - pc.getWidth();
		y = s.getHeight() / 2 - (pc.getHeight() / 2);
		break;

	    case CENTER:
		x = s.getWidth() / 2 - (pc.getWidth() / 2);
		y = s.getHeight() / 2 - (pc.getHeight() / 2);
		break;

	    default:
		x = 0;
		y = 0;
	}
	
	pc.setX(x + pc.getToast().getOffset().getX() + offsetX);
	pc.setY(y + pc.getToast().getOffset().getY() + offsetY);
    }

}
