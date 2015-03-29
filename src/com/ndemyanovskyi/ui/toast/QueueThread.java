/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import java.util.Objects;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

final class QueueThread<T> extends Thread {
    
    private final ReadOnlyObjectWrapper<Toast<?>> currentToast
	    = new ReadOnlyObjectWrapper<>(this, "currentToast");
    
    private final ToastQueue queue;
    private boolean finished;

    public QueueThread(ToastQueue queue) {
	this.queue = Objects.requireNonNull(queue, "queue");
	setDaemon(true);
    }

    public ToastQueue getQueue() {
	return queue;
    }

    public boolean isFinished() {
	return finished;
    }

    public Toast<?> getCurrentToast() {
	return currentToast.get();
    }

    public final ReadOnlyObjectProperty<Toast<?>> currentToastProperty() {
	return currentToast.getReadOnlyProperty();
    }

    @Override
    public final void run() {
	try {
	    while(true) {
		Toast<?> toast;
		synchronized(queue) {
		    try {
			if(queue.getToasts().isEmpty()) break;
			toast = queue.getToasts().get(0);
		    } catch(IndexOutOfBoundsException ex) {
			break;
		    }
		}
		if(!toast.isCancelled()) {
		    ToastPopup<?> popup = toast.getPopup();

		    popup.show();
		    currentToast.set(toast);
		    long time = Math.round(toast.getDuration().toMillis());
		    while(time > 0) {
			if(toast.isCancelled() || toast.getQueue() == null) {
			    break;
			}
			try {
			    Thread.sleep(5);
			} catch(InterruptedException ignored) {
			}
			time -= 5;
		    }
		    popup.hide();
		}
		getQueue().getToasts().remove(toast);
	    }
	    currentToast.set(null);
	} finally {
	    finished = true;
	}
    }

}
