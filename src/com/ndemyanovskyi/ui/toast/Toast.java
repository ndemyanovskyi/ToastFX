/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;
import javafx.util.Duration;

public final class Toast<T> {
    
    private static final Map<Class<?>, Function<ToastQueue<?>, QueueThread<?>>> THREAD_FACTORIES = new HashMap<>();
    
    static {
	registerThreadFactory(Node.class, NodeQueueThread::new);
	registerThreadFactory(Window.class, WindowQueueThread::new);
    }
    
    private static final Map<Object, ToastQueue<?>> queues = Collections.synchronizedMap(new WeakHashMap<>());
    
    public static final Duration DURATION_SHORT = Duration.millis(2000);
    public static final Duration DURATION_LONG = Duration.millis(5000);

    private final T owner;
    private final Node node;
    private final Node content;
    private final Pos alignment;
    private final Duration duration;
    private final Point2D offset;

    private final ReadOnlyBooleanWrapper shown = new ReadOnlyBooleanWrapper(this, "shown");
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");
    private final ReadOnlyBooleanWrapper cancelled = new ReadOnlyBooleanWrapper(this, "cancelled");
    private final ReadOnlyObjectWrapper<ToastQueue<T>> queue = new ReadOnlyObjectWrapper(this, "queue");

    private Toast(Builder builder) {
	this.offset = builder.getOffset();
	this.owner = (T) builder.getOwner();
	this.content = builder.getContent();
	this.duration = builder.getDuration();
	this.alignment = builder.getAlignment();
	
	showing.addListener(p -> {
	    if(isShowing()) shown.set(true);
	});
	cancelled.addListener(p -> {
	    if(isCancelled()) showing.set(false);
	});
	
	BorderPane container = new BorderPane(content);
	container.getStyleClass().add("toast");
	container.getStylesheets().add(getClass().getResource("Toast.css").toExternalForm());
	container.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
	    if(e.getClickCount() == 2) {
		cancel();
	    }
	});
	this.node = container;
    }
    
    static final <T> void registerThreadFactory(Class<T> ownerType, Function<ToastQueue<T>, QueueThread<T>> factory) {
	Objects.requireNonNull(ownerType, "ownerType");
	Objects.requireNonNull(factory, "factory");
	THREAD_FACTORIES.put(ownerType, (Function) factory);
    }
    
    static final <T> Function<ToastQueue<T>, QueueThread<T>> getThreadFactoryOrThrow(Class<T> ownerType) {
	Objects.requireNonNull(ownerType, "ownerType");
	
	Class<? super T> type = ownerType;
	Function<ToastQueue<T>, QueueThread<T>> factory = null;
	
	while(type != null) {
	    factory = (Function) THREAD_FACTORIES.get(type);
	    if(factory != null) break;
	    type = type.getSuperclass();
	}
	if(factory == null) {
	    throw new IllegalArgumentException("Factory for type " + ownerType + " not defined.");
	}
	return factory;
    }
    
    public final Pos getAlignment() {
	return alignment;
    }

    public final T getOwner() {
	return owner;
    }

    public final Node getNode() {
	return node;
    }

    public final Duration getDuration() {
	return duration;
    }

    public final Point2D getOffset() {
	return offset;
    }

    public final Node getContent() {
	return content;
    }
    
    public final ReadOnlyBooleanProperty shownProperty() {
	return shown.getReadOnlyProperty();
    }
    
    public final ReadOnlyBooleanProperty showingProperty() {
	return showing.getReadOnlyProperty();
    }
    
    public final ReadOnlyObjectProperty<ToastQueue<T>> queueProperty() {
	return queue.getReadOnlyProperty();
    }
    
    public final ReadOnlyBooleanProperty cancelledProperty() {
	return cancelled.getReadOnlyProperty();
    }    

    public final boolean isShown() {
	return shown.get();
    }

    public final boolean isShowing() {
	return showing.get();
    }

    public final ToastQueue<T> getQueue() {
	return queue.get();
    }

    public final boolean isCancelled() {
	return cancelled.get();
    }  

    final void setShowing(boolean shown) {
	this.showing.set(shown);
    }

    final void setQueue(ToastQueue<T> queue) {
	this.queue.set(queue);
    }
    
    public Window getOwnerWindow() {
	if(getOwner() instanceof Node) {
	    Scene scene = ((Node) getOwner()).getScene();
	    return (scene != null) ? scene.getWindow() : null;
	}
	return (Window) getOwner();
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 89 * hash + Objects.hashCode(this.owner);
	hash = 89 * hash + Objects.hashCode(this.content);
	hash = 89 * hash + Objects.hashCode(this.alignment);
	hash = 89 * hash + Objects.hashCode(this.duration);
	hash = 89 * hash + Objects.hashCode(this.offset);
	hash = 89 * hash + Objects.hashCode(this.queue);
	hash = 89 * hash + (isShown() ? 1 : 0);
	hash = 89 * hash + (isShowing() ? 1 : 0);
	hash = 89 * hash + (isCancelled() ? 1 : 0);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	return super.equals(obj);
	/*if(obj == this) return true;
	if(obj == null) return false;
	if(!(obj instanceof Toast)) return false;
	
	final Toast<?> other = (Toast<?>) obj;
	
	return Objects.equals(getOwner(), other.getOwner()) 
		&& Objects.equals(getContent(), other.getContent())
		&& Objects.equals(getDuration(), other.getDuration())
		&& Objects.equals(getOffset(), other.getOffset())
		&& Objects.equals(getAlignment(), other.getAlignment())
		&& isCancelled() == other.isCancelled()
		&& isInQueue() == other.isInQueue()
		&& isShowing() == other.isShowing()
		&& isShown() == other.isShown();*/
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Toast [owner: ").append(owner).
		append(", content: ").append(content).
		append(", alignment: ").append(alignment).
		append(", duration: ").append(duration).
		append(", offset: ").append(offset);
	
	builder.append(", states: ");
	List<String> states = new ArrayList<>();
	if(isShown()) states.add("shown");
	if(isShowing()) states.add("showing");
	if(isCancelled()) states.add("cancelled");
	builder.append(states);
	
	return builder.toString();
    }

    public final void show() {
	if(isCancelled()) {
	    throw new IllegalStateException("Toast already cancelled.");
	}
	if(isShown()) {
	    throw new IllegalStateException("Toast already shown.");
	}
	
	if(getQueue() == null) {
	    getQueue(getOwner()).getToasts().add((Toast) this);
	}
    }

    public final void cancel() {
	cancelled.set(true);
	setQueue(null);
    }

    public static <T> Toast<T> of(T owner, String text) {
	return of(owner, text, Pos.CENTER);
    }

    public static <T> Toast<T> of(T owner, String text, Pos alignment) {
	return of(owner, text, DURATION_SHORT, alignment);
    }

    public static <T> Toast<T> of(T owner, String text, Duration duration) {
	return of(owner, text, duration, Pos.CENTER);
    }

    public static <T> Toast<T> of(T owner, String text, Duration duration, Pos alignment) {
	return builder(owner).
		setDuration(duration).
		setAlignment(alignment).
		setContent(createTextContent(text)).build();
    }

    public static <T> Toast<T> of(T owner, ObservableValue<String> text) {
	return of(owner, text, Pos.CENTER);
    }

    public static <T> Toast<T> of(T owner, ObservableValue<String> text, Pos alignment) {
	return of(owner, text, DURATION_SHORT, alignment);
    }

    public static <T> Toast<T> of(T owner, ObservableValue<String> text, Duration duration) {
	return of(owner, text, duration, Pos.CENTER);
    }

    public static <T> Toast<T> of(T owner, ObservableValue<String> text, Duration duration, Pos alignment) {
	return builder(owner).
		setDuration(duration).
		setAlignment(alignment).
		setContent(createTextContent(text)).build();
    }

    public static <T> Toast<T> of(T owner, Node content) {
	return of(owner, content, Pos.CENTER);
    }

    public static <T> Toast<T> of(T owner, Node content, Pos alignment) {
	return of(owner, content, DURATION_SHORT, alignment);
    }

    public static <T> Toast<T> of(T owner, Node content, Duration duration) {
	return of(owner, content, duration, Pos.CENTER);
    }

    public static <T> Toast<T> of(T owner, Node content, Duration duration, Pos alignment) {
	return builder(owner).
		setDuration(duration).
		setAlignment(alignment).
		setContent(content).build();
    }
    
    private static Label createLabel() {
	Label l = new Label();
	l.setTextFill(Color.GHOSTWHITE);
	l.setWrapText(true);
        l.setTextAlignment(TextAlignment.CENTER);
	return l;
    }

    public static Label createTextContent(String text) {
	Label l = createLabel();
	l.setText(text);
	return l;
    }

    public static Label createTextContent(ObservableValue<String> text) {
	Label l = createLabel();
        l.textProperty().bind(text);
	return l;
    }
    
    public static <T> ToastQueue<T> getQueue(T owner) {
	synchronized(queues) {
	    ToastQueue<T> tq = (ToastQueue<T>) queues.get(owner);
	    if(tq == null) {
		tq = new ToastQueue<>(owner, getThreadFactoryOrThrow((Class<T>) owner.getClass()));
		queues.put(owner, tq);
	    }
	    return tq;
	}
    }

    public static <T> Toast<T> getCurrent(T owner) {
	Objects.requireNonNull(owner, "owner");
	getThreadFactoryOrThrow((Class<T>) owner.getClass());
	ToastQueue<T> queue = (ToastQueue<T>) queues.get(owner);
	return queue != null ? queue.getCurrentToast() : null;
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    public static <T> Builder<T> builder(T owner) {
        return new Builder<T>().setOwner(owner);
    }
    
    public static class Builder<T> implements javafx.util.Builder<Toast<T>> {
        
        private T owner;
        private Node content;
        
        private Pos alignment = Pos.CENTER;
        private Duration duration = DURATION_SHORT;
        private Point2D offset = Point2D.ZERO;
        
        private Builder() {}

        public Builder setAlignment(Pos alignment) {
            this.alignment = Objects.requireNonNull(alignment, "alignment");
            return this;
        }

        public Builder setDuration(Duration duration) {
            this.duration = Objects.requireNonNull(duration, "duration");
            return this;
        }

        public Builder setText(String text) {
            return setContent(createTextContent(text));
        }

        public Builder setText(ObservableValue<String> textProperty) {
            return setContent(Toast.createTextContent(textProperty));
        }

        public Builder setContent(Node content) {
            this.content = Objects.requireNonNull(content, "content");
            return this;
        }

        public Builder setOffset(Point2D offset) {
            this.offset = Objects.requireNonNull(offset, "offset");
            return this;
        }

        public Builder setOffset(double x, double y) {
            return setOffset(new Point2D(x, y));
        }

        public Builder setOwner(T owner) {
            Objects.requireNonNull(owner, "owner");
            getThreadFactoryOrThrow(owner.getClass());
	    this.owner = owner;
            return this;
        }

        public Pos getAlignment() {
            return alignment;
        }

        public Duration getDuration() {
            return duration;
        }

        public Node getContent() {
            return content;
        }

        public Point2D getOffset() {
            return offset;
        }

        public Object getOwner() {
            return owner;
        }
        
        public Toast<T> show() {
            Toast<T> toast = build();
            toast.show();
            return toast;
        }
        
	@Override
        public Toast<T> build() {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(content, "content");            
            return new Toast<>(this);
        }
        
    }

}
