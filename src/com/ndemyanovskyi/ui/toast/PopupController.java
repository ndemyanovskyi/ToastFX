/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import java.util.Objects;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

final class PopupController<T> {
    private final Toast<T> toast;
    private final QueueThread<T> thread;
    private final FadeTransition transition = new FadeTransition(Duration.millis(300));
    private Popup popup;

    public PopupController(QueueThread<T> thread, Toast<T> toast) {
	super();
	this.thread = Objects.requireNonNull(thread, "thread");
	this.toast = Objects.requireNonNull(toast, "toast");
	thread.getPopups().put(toast, this);
    }

    public Toast<T> getToast() {
	return toast;
    }

    public QueueThread<T> getThread() {
	return thread;
    }

    private Popup getPopup() {
	if(popup == null) {
	    popup = new Popup();
	    popup.getContent().add(toast.getNode());
	}
	return popup;
    }

    public void setX(double x) {
	Popup localPopup = getPopup();
	localPopup.setX(x);
    }

    public void setY(double y) {
	Popup localPopup = getPopup();
	localPopup.setY(y);
    }

    public void setWidth(double width) {
	Popup localPopup = getPopup();
	localPopup.setWidth(width);
    }

    public void setHeight(double height) {
	Popup localPopup = getPopup();
	localPopup.setHeight(height);
    }

    public void show() {
	Window window = toast.getOwnerWindow();
	if(window != null) {
	    showImpl(window);
	} else {
	    toast.getNode().sceneProperty().addListener((sceneProperty, oldScene, newScene) -> {
		if(newScene != null) {
		    newScene.windowProperty().addListener((windowProperty, oldWindow, newWindow) -> {
			synchronized(toast) {
			    if(newWindow != null && toast.getQueue() != null && !toast.isCancelled()) {
				showImpl(newWindow);
			    }
			}
		    });
		}
	    });
	}
    }

    private void showImpl(Window window) {
	Platform.runLater(() -> {
	    Popup localPopup = getPopup();
	    localPopup.show(window);
	    toast.setShowing(true);
	    getThread().updatePopupPosition(this);
	    getThread().onToastShow(toast);
	    transition.setNode(toast.getNode());
	    transition.setOnFinished(null);
	    transition.setFromValue(0);
	    transition.setToValue(1);
	    transition.playFromStart();
	});
    }

    public final double getX() {
	return getPopup().getX();
    }

    public final double getY() {
	return getPopup().getY();
    }

    public final double getWidth() {
	return getPopup().getWidth();
    }

    public final double getHeight() {
	return getPopup().getHeight();
    }

    public void hide() {
	Platform.runLater(() -> {
	    Popup localPopup = getPopup();
	    transition.setNode(toast.getNode());
	    transition.setOnFinished((ActionEvent e) -> {
		localPopup.hide();
		getThread().onToastHide(toast);
		getThread().getPopups().remove(getToast());
		toast.setShowing(false);
	    });
	    transition.setFromValue(toast.getNode().getOpacity());
	    transition.setToValue(0);
	    transition.playFromStart();
	});
    }

}
