/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

abstract class ToastPopup<T> {
    
    private static final Map<Class<?>, Function<Toast, ToastPopup>> FACTORIES
	    = new HashMap<Class<?>, Function<Toast, ToastPopup>>() {{
		put(Node.class, NodeToastPopup::new);
		put(Scene.class, SceneToastPopup::new);
		put(Window.class, WindowToastPopup::new);
	    }};
    
    private final Toast<T> toast;
    
    private final FadeTransition transition = new FadeTransition(Duration.millis(300));
    private Popup popup;
    
    private boolean showingRequsted = false;
    private boolean hidingRequested = false;

    ToastPopup(Toast<T> toast) {
	super();
	this.toast = Objects.requireNonNull(toast, "toast");
    }
    
    static <T> ToastPopup<T> of(Toast<T> toast) {
	Objects.requireNonNull(toast, "toast");
	return getFactory((Class<T>) toast.getOwner().getClass()).apply(toast);
    }
    
    public static boolean isSupported(Object object) {
	return object != null && isSupported(object.getClass());
    } 
    
    public static boolean isSupported(Class<?> type) {
	return type != null && getFactoryImpl(type) != null;
    } 
    
    private static <T> Function<Toast<T>, ToastPopup<T>> getFactory(Class<T> type) {
	Function<Toast<T>, ToastPopup<T>> factory = getFactoryImpl(type);
	if(factory != null) return factory;
	
	throw new IllegalArgumentException(
		"ToastPopup factory for " + type + " doesn`t exist.");
    } 
    
    private static <T> Function<Toast<T>, ToastPopup<T>> getFactoryImpl(Class<T> type) {
	for(Map.Entry<Class<?>, Function<Toast, ToastPopup>> e : FACTORIES.entrySet()) {
	    if(e.getKey().isAssignableFrom(type)) {
		return (Function) e.getValue();
	    }
	}
	return null;
    } 

    public final Toast<T> getToast() {
	return toast;
    }

    public boolean isShowingRequsted() {
	return showingRequsted;
    }

    public boolean isHidingRequested() {
	return hidingRequested;
    }

    private Popup getPopup() {
	if(popup == null) {
	    popup = new Popup();
	    popup.getContent().add(toast.getNode());
	}
	return popup;
    }

    public final void setX(double x) {
	Popup localPopup = getPopup();
	localPopup.setX(x);
    }

    public final void setY(double y) {
	Popup localPopup = getPopup();
	localPopup.setY(y);
    }

    public final void setWidth(double width) {
	Popup localPopup = getPopup();
	localPopup.setWidth(width);
    }

    public final void setHeight(double height) {
	Popup localPopup = getPopup();
	localPopup.setHeight(height);
    }
    
    public abstract void relocate();

    public final void show() {
	Window window = toast.getOwnerWindow();
	showingRequsted = true;
	
	if(window != null) {
	    showImpl(window);
	} else {
	    toast.ownerWindowProperty().addListener((wp, oldWindow, newWindow) -> {
		if(newWindow != null && !hidingRequested) {
		    showImpl(newWindow);
		}
	    });
	}
    }
    
    protected void onShow(Window window) {}
    protected void onHide(Window window) {}

    private void showImpl(Window window) {
	Platform.runLater(() -> {
	    Popup localPopup = getPopup();
	    localPopup.show(window);
	    hidingRequested = false;
	    toast.setShowing(true);
	    onShow(window);
	    relocate();
	    
	    EventHandler<? super ActionEvent> onShowing = toast.getOnShowing();
	    if(onShowing != null) {
		onShowing.handle(new ActionEvent(
			toast, Event.NULL_SOURCE_TARGET));
	    }
	    
	    transition.setNode(toast.getNode());
	    transition.setOnFinished(e -> {
		EventHandler<? super ActionEvent> onShown = toast.getOnShown();
		if(onShown != null) {
		    onShown.handle(new ActionEvent(
			    toast, Event.NULL_SOURCE_TARGET));
		}
	    });
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
	hidingRequested = true;
	
	Platform.runLater(() -> {
	    Popup localPopup = popup;	
	    if(localPopup != null) {
		onHide(popup.getOwnerWindow());

		EventHandler<? super ActionEvent> onHiding = toast.getOnHiding();
		if(onHiding != null) {
		    onHiding.handle(new ActionEvent(
			    toast, Event.NULL_SOURCE_TARGET));
		}

		transition.setNode(toast.getNode());
		transition.setOnFinished((ActionEvent e) -> {
		    localPopup.hide();
		    showingRequsted = false;
		    toast.setShowing(false);
		    
		    EventHandler<? super ActionEvent> onHidden = toast.getOnHidden();
		    if(onHidden != null) {
			onHidden.handle(new ActionEvent(
				toast, Event.NULL_SOURCE_TARGET));
		    }
		});
		transition.setFromValue(toast.getNode().getOpacity());
		transition.setToValue(0);
		transition.playFromStart();
	    }
	});
    }

}
