/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

abstract class QueueThread<T> extends Thread {
    
    private final ReadOnlyObjectWrapper<Toast<T>> currentToast
	    = new ReadOnlyObjectWrapper<>(this, "currentToast");
    
    private final ToastQueue<T> queue;
    private final Map<Toast<T>, PopupController<T>> popupControllers = Collections.synchronizedMap(new HashMap<>());
    private boolean finished;
    
    private final InvalidationListener listener = e -> {
	for(PopupController<T> pc : popupControllers.values()) {
	    updatePopupPosition(pc);
	}
    };

    public QueueThread(ToastQueue<T> queue) {
	this.queue = Objects.requireNonNull(queue, "queue");
	setDaemon(true);
	setUncaughtExceptionHandler((Thread t, Throwable e) -> {
	    finished = true;
	    Logger.getGlobal().log(Level.SEVERE, "Exception in Thread [" + getName() + "]", e);
	});
    }

    public ToastQueue<T> getQueue() {
	return queue;
    }

    public boolean isFinished() {
	return finished;
    }

    public Toast<T> getCurrentToast() {
	return currentToast.get();
    }
    
    public final ReadOnlyObjectProperty<Toast<T>> currentToastProperty() {
	return currentToast.getReadOnlyProperty();
    }

    protected void onStart() {
    }

    protected void onFinish() {
    }

    protected void onToastShow(Toast<T> toast) {
    }

    protected void onToastHide(Toast<T> toast) {
    }

    protected abstract void updatePopupPosition(PopupController<T> pc);

    @Override
    public final void run() {
	onStart();
	List<Toast<T>> toasts = getQueue().getToasts();
	while(true) {
	    Toast<T> toast;
	    synchronized(toasts) {
		try {
		    if(toasts.isEmpty()) break;
		    toast = toasts.get(0);
		} catch(IndexOutOfBoundsException ex) {
		    break;
		}
	    }
	    if(!toast.isCancelled()) {
		PopupController<T> pc = new PopupController<>(this, toast);
		toast.getContent().layoutXProperty().addListener(listener);
		toast.getContent().layoutYProperty().addListener(listener);
		toast.getContent().boundsInLocalProperty().addListener(listener);
		toast.getContent().layoutBoundsProperty().addListener(listener);
		toast.getContent().boundsInParentProperty().addListener(listener);
		
		pc.show();
		
		toast.setShowing(true);
		currentToast.set(toast);
		long time = Math.round(toast.getDuration().toMillis());
		while(time > 0) {
		    if(toast.isCancelled() || toast.getQueue() == null) {
			break;
		    }
		    try { Thread.sleep(5); } 
		    catch(InterruptedException ignored) {}
		    time -= 5;
		}
		
		pc.hide();
		toast.getContent().layoutXProperty().removeListener(listener);
		toast.getContent().layoutYProperty().removeListener(listener);
		toast.getContent().boundsInLocalProperty().removeListener(listener);
		toast.getContent().layoutBoundsProperty().removeListener(listener);
		toast.getContent().boundsInParentProperty().removeListener(listener);
	    }
	    getQueue().getToasts().remove(toast);
	}
	currentToast.set(null);
	finished = true;
	onFinish();
    }

    public Map<Toast<T>, PopupController<T>> getPopups() {
	return popupControllers;
    }

}
