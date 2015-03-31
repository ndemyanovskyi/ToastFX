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
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

abstract class ToastPopup<T> {
    
    private static final Duration TRANSITION_DURATION = Duration.millis(250);
    
    private static final Map<Class<?>, Function<Toast, ToastPopup>> FACTORIES
	    = new HashMap<Class<?>, Function<Toast, ToastPopup>>() {{
		put(Node.class, NodeToastPopup::new);
		put(Scene.class, SceneToastPopup::new);
		put(Window.class, WindowToastPopup::new);
		put(Screen.class, ScreenToastPopup::new);
	    }};
    
    private final InvalidationListener relocater = p -> relocate();
    
    private final Toast<T> toast;
    
    private final FadeTransition showTransition;
    private final FadeTransition hideTransition;
    
    private Popup popup;
    
    private boolean showingRequsted = false;
    private boolean hidingRequested = false;

    ToastPopup(Toast<T> toast) {
	super();
	this.toast = Objects.requireNonNull(toast, "toast");
	
	showTransition = new FadeTransition(TRANSITION_DURATION, toast.getNode());
	showTransition.setFromValue(0);
	showTransition.setToValue(1);
	showTransition.setOnFinished(e -> {
	    onShown();
	    EventHandler<? super ActionEvent> onShown = toast.getOnShown();
	    if(onShown != null) {
		onShown.handle(new ActionEvent(
			toast, Event.NULL_SOURCE_TARGET));
	    }
	});

	toast.getNode().opacityProperty().addListener(e -> {
	    relocate();
	});
	
	hideTransition = new FadeTransition(TRANSITION_DURATION, toast.getNode());
	hideTransition.setFromValue(1);
	hideTransition.setToValue(0);
	hideTransition.setOnFinished((ActionEvent e) -> {
	    getPopup().hide();
	    showingRequsted = false;
	    toast.setShowing(false);
	    
	    onHidden();	 
	    
	    getToast().getNode().widthProperty().removeListener(relocater);
	    getToast().getNode().heightProperty().removeListener(relocater);
	    getToast().getNode().layoutBoundsProperty().removeListener(relocater);
	    getToast().getNode().boundsInLocalProperty().removeListener(relocater);
	    getToast().getNode().boundsInParentProperty().removeListener(relocater);
	    
	    EventHandler<? super ActionEvent> onHidden = toast.getOnHidden();
	    if(onHidden != null) {
		onHidden.handle(new ActionEvent(
			toast, Event.NULL_SOURCE_TARGET));
	    }
	});
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

    protected Popup getPopup() {
	if(popup == null) {
	    popup = new Popup();
	    popup.getScene().setRoot(toast.getNode());
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
	
	onShowRequested();
	
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
    
    protected void onShowRequested() {}
    protected void onShowing() {}
    protected void onShown() {}
    protected void onHidden() {}
    protected void onHiding() {}

    private void showImpl(Window window) {
	Platform.runLater(() -> {
	    Popup localPopup = getPopup();
	    localPopup.show(window);
	    hidingRequested = false;
	    toast.setShowing(true);
	    
	    EventHandler<? super ActionEvent> onShowing = toast.getOnShowing();
	    if(onShowing != null) {
		onShowing.handle(new ActionEvent(
			toast, Event.NULL_SOURCE_TARGET));
	    }
	    
	    onShowing();	    
	    
	    getToast().getNode().widthProperty().addListener(relocater);
	    getToast().getNode().heightProperty().addListener(relocater);
	    getToast().getNode().layoutBoundsProperty().addListener(relocater);
	    getToast().getNode().boundsInLocalProperty().addListener(relocater);
	    getToast().getNode().boundsInParentProperty().addListener(relocater);
	    
	    hideTransition.stop();
	    showTransition.playFromStart();
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
	    onHiding();
	    EventHandler<? super ActionEvent> onHiding = toast.getOnHiding();
	    if(onHiding != null) {
		onHiding.handle(new ActionEvent(
			toast, Event.NULL_SOURCE_TARGET));
	    }
	    
	    Popup localPopup = popup;	
	    if(localPopup != null) {
		showTransition.stop();
		hideTransition.setFromValue(toast.getNode().getOpacity());
		hideTransition.playFromStart();
	    }
	});
    }
    
    static final void relocate(ToastPopup<?> popup, Bounds bounds) {
	relocate(popup, 
		bounds.getMinX(), bounds.getMinY(), 
		bounds.getWidth(), bounds.getHeight());
    }
    
    static final void relocate(ToastPopup<?> popup, Rectangle2D rect) {
	relocate(popup, 
		rect.getMinX(), rect.getMinY(), 
		rect.getWidth(), rect.getHeight());
    }
    
    static final void relocate(ToastPopup<?> popup, 
	    double x, double y, double width, double height) 
    {
	final double maxX = x + width;
	final double maxY = y + height;
	
	double resX = 0, resY = 0;
	
	switch(popup.getToast().getAlignment()) {
	    case BOTTOM_LEFT:
		resX = x;
		resY = maxY - popup.getHeight(); break;
	    case BOTTOM_CENTER:
		resX = x + width / 2 - (popup.getWidth() / 2);
		resY = maxY - popup.getHeight(); break;
	    case BOTTOM_RIGHT:
		resX = maxX - popup.getWidth();
		resY = maxY - popup.getHeight(); break;
	    case BASELINE_LEFT:
	    case TOP_LEFT:
		resX = x;
		resY = y; break;
	    case BASELINE_CENTER:
	    case TOP_CENTER:
		resX = x + width / 2 - (popup.getWidth() / 2);
		resY = y; break;
	    case BASELINE_RIGHT:
	    case TOP_RIGHT:
		resX = maxX - popup.getWidth();
		resY = y; break;
	    case CENTER_LEFT:
		resX = x;
		resY = y + height / 2 - (popup.getHeight() / 2); break;
	    case CENTER_RIGHT:
		resX = maxX - popup.getWidth();
		resY = y + height / 2 - (popup.getHeight() / 2); break;
	    case CENTER:
		resX = x + width / 2 - (popup.getWidth() / 2);
		resY = y + height / 2 - (popup.getHeight() / 2); break;
	}
	
	//Add offset
	resX += popup.getToast().getOffset().getX();
	resY += popup.getToast().getOffset().getY();
	
	popup.setX(resX);
	popup.setY(resY);
    }

}
