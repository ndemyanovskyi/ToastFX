/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.toast;

import com.sun.javafx.collections.ObservableListWrapper;
import java.util.ArrayList;
import java.util.Objects;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ToastQueue {

    private QueueThread thread;
        
    private final ReadOnlyObjectWrapper<Toast<?>> currentToast
	    = new ReadOnlyObjectWrapper<>(this, "currentToast");
    
    private final ObservableList<Toast<?>> toasts = FXCollections.synchronizedObservableList(
		    new ObservableListWrapper<Toast<?>>(new ArrayList<>()) {
		
		@Override
		public void add(int index, Toast<?> toast) {
		    Objects.requireNonNull(toast, "element");
		    if(contains(toast)) {
			throw new IllegalArgumentException("Queue already contains this toast.");
		    }
		    if(toast.getDuration().isIndefinite()) {
			throw new IllegalArgumentException("Toast in queue can`t be have duration of indefinite.");
		    }
		    if(toast.isCancelled()) {
			throw new IllegalArgumentException("Toast already cancelled.");
		    }
		    if(toast.getQueue() != null) {
			throw new IllegalArgumentException("Toast already in queue.");
		    }
		    if(toast.isShown()) {
			throw new IllegalArgumentException("Toast already shown.");
		    }
		    if(toast.getPopup().isShowingRequsted()) {
			throw new IllegalArgumentException("Toast already request showing.");
		    }
		    super.add(index, toast);
		}
	    });

    public ToastQueue() {
	toasts.addListener((ListChangeListener.Change<? extends Toast> c) -> {
	    synchronized(toasts) {
		while(c.next()) {
		    if(c.wasReplaced()) {
			break;
		    }
		    if(c.wasAdded()) {
			for(Toast toast : c.getAddedSubList()) {
			    toast.setQueue(ToastQueue.this);
			}
			if(thread == null || thread.isFinished()) {
			    thread = new QueueThread(this);
			    currentToast.bind(thread.currentToastProperty());
			    thread.start();
			}
		    } else if(c.wasRemoved()) {
			for(Toast toast : c.getRemoved()) {
			    if(toast.isShowing()) {
				toast.cancel();
			    } 
			    toast.setQueue(null);
			}
		    }
		}
	    }
	});
    }

    public final ObservableList<Toast<?>> getToasts() {
	return toasts;
    }

    final QueueThread getThread() {
	return thread;
    }

    public final Toast<?> getCurrentToast() {
	return currentToast.get();
    }

    public final ReadOnlyObjectProperty<Toast<?>> currentToastProperty() {
	return currentToast.getReadOnlyProperty();
    }
    
}
