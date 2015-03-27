/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import com.sun.javafx.collections.ObservableListWrapper;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public final class ToastQueue<T> {
    
    private final T owner;
    private QueueThread<T> thread;
    
    private final ReadOnlyObjectWrapper<Toast<T>> currentToast
	    = new ReadOnlyObjectWrapper<>(this, "currentToast");
    
    private final ObservableList<Toast<T>> toasts = FXCollections.
	    synchronizedObservableList(new ObservableListWrapper<Toast<T>>(new ArrayList<>()) {

		{
		    addListener((Change<? extends Toast<T>> c) -> {
			synchronized(this) {
			    while(c.next()) {
				if(c.wasReplaced()) {
				    break;
				}
				if(c.wasAdded()) {
				    for(Toast<T> toast : c.getAddedSubList()) {
					toast.setQueue(ToastQueue.this);
				    }
				} else if(c.wasRemoved()) {
				    for(Toast<T> toast : c.getRemoved()) {
					if(toast.isShowing() || !toast.isShown()) {
					    toast.cancel();
					} else {
					    toast.setQueue(null);
					}
				    }
				}
			    }
			}
		    });
		}
		
		@Override
		public void add(int index, Toast<T> toast) {
		    Objects.requireNonNull(toast, "element");
		    if(contains(toast)) {
			throw new IllegalArgumentException(
				"list already contains this toast [" + toast + "].");
		    }
		    if(toast.isCancelled())  {
			throw new IllegalArgumentException(
				"toast [" + toast + "] already cancelled.");
		    }
		    if(toast.getQueue() != null)  {
			throw new IllegalArgumentException(
				"toast [" + toast + "] already in queue.");
		    }
		    if(toast.isShown())  {
			throw new IllegalArgumentException(
				"toast [" + toast + "] already shown.");
		    }
		    super.add(index, toast);
		}

	    });

    private final Function<ToastQueue<T>, QueueThread<T>> threadFactory;
    
    ToastQueue(T owner, Function<ToastQueue<T>, QueueThread<T>> threadFactory) {
	this.owner = Objects.requireNonNull(owner);
	this.threadFactory = Objects.requireNonNull(threadFactory, "threadFactory");
	
	toasts.addListener((ListChangeListener.Change<? extends Toast<T>> c) -> {
	    while(c.next()) {
		if(c.wasAdded()) {
		    if(thread == null || thread.isFinished()) {
			thread = threadFactory.apply(this);
			currentToast.bind(thread.currentToastProperty());
			thread.start();
		    }
		    break;
		}
	    }
	});
    }
    
    public final ObservableList<Toast<T>> getToasts() {
	return toasts;
    }

    public final T getOwner() {
	return owner;
    }

    public Toast<T> getCurrentToast() {
	return currentToast.get();
    }
    
    public final ReadOnlyObjectProperty<Toast<T>> currentToastProperty() {
	return currentToast.getReadOnlyProperty();
    }
    
}
