/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.toast;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;
import javafx.util.Duration;

public class Toast<T> {
    
    public static final Duration DURATION_SHORT = Duration.millis(2500);
    public static final Duration DURATION_LONG = Duration.millis(5500);

    private final T owner;
    private final BorderPane node;
    private final Node content;
    private final Pos alignment;
    private final Duration duration;
    private final Point2D offset;
    
    private ToastPopup<T> popup;
    private Timeline hidingTimeline;

    private final ReadOnlyBooleanWrapper shown = new ReadOnlyBooleanWrapper(this, "shown");
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");
    private final ReadOnlyBooleanWrapper cancelled = new ReadOnlyBooleanWrapper(this, "cancelled");
    private final ReadOnlyBooleanWrapper inQueue = new ReadOnlyBooleanWrapper(this, "inQueue");
    private final ReadOnlyObjectWrapper<ToastQueue> queue = new ReadOnlyObjectWrapper(this, "queue");
    private final ReadOnlyObjectWrapper<Instant> showingTime = new ReadOnlyObjectWrapper(this, "showingTime");
    private final ReadOnlyObjectWrapper<Window> ownerWindow = new ReadOnlyObjectWrapper<>(this, "ownerWindow");

    private ObjectProperty<EventHandler<? super ActionEvent>> onShowing;
    private ObjectProperty<EventHandler<? super ActionEvent>> onShown;
    private ObjectProperty<EventHandler<? super ActionEvent>> onHiding;
    private ObjectProperty<EventHandler<? super ActionEvent>> onHidden;
    
    Toast(Builder<T> builder) {
	this.owner = builder.getOwner();
	this.offset = builder.getOffset();
	this.content = builder.getContent();
	this.duration = builder.getDuration();
	this.alignment = builder.getAlignment();
	
	inQueue.bind(queue.isNotNull());
	
	showing.addListener(p -> {
	    if(isShowing()) {
		shown.set(true);
		showingTime.set(Instant.now());
	    }
	});
	cancelled.addListener(p -> {
	    if(isCancelled()) showing.set(false);
	});
	
	BorderPane container = new BorderPane(content);
	container.setStyle(builder.getStyle());
	container.getStyleClass().addAll(builder.getStyleClass());
	container.getStylesheets().addAll(builder.getStylesheets());
	container.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
	    if(e.getClickCount() == 2) {
		cancel();
	    }
	});
	this.node = container;
    }
    
    //Getters
    
    public final Pos getAlignment() {
	return alignment;
    }

    public final T getOwner() {
	return owner;
    }

    public final Region getNode() {
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

    public final Instant getShowingTime() {
	return showingTime.get();
    }

    public final boolean isShown() {
	return shown.get();
    }

    public final boolean isInQueue() {
	return inQueue.get();
    }

    public final boolean isShowing() {
	return showing.get();
    }

    public final ToastQueue getQueue() {
	return queue.get();
    }

    public final boolean isCancelled() {
	return cancelled.get();
    }  
    
    public final Window getOwnerWindow() {
	return ownerWindowProperty().get();
    }
    
    public final EventHandler<? super ActionEvent> getOnShowing() {
	return onShowing != null ? onShowing.get() : null;
    }
    
    public final EventHandler<? super ActionEvent> getOnShown() {
	return onShown != null ? onShown.get() : null;
    }
    
    public final EventHandler<? super ActionEvent> getOnHiding() {
	return onHiding != null ? onHiding.get() : null;
    }
    
    public final EventHandler<? super ActionEvent> getOnHidden() {
	return onHidden != null ? onHidden.get() : null;
    }

    ToastPopup<T> getPopup() {
	return popup != null ? popup : (popup = ToastPopup.of(this));
    }
    
    //Setters

    final void setShowing(boolean shown) {
	this.showing.set(shown);
    }

    final void setQueue(ToastQueue queue) {
	this.queue.set(queue);
    }

    final void setOwnerWindow(Window ownerWindow) {
	this.ownerWindow.set(ownerWindow);
    }
    
    public final void setOnHidden(EventHandler<? super ActionEvent> onHidden) {
	if(this.onHidden != null || onHidden != null) {
	    onHiddenProperty().set(onHidden);
	}
    }
    
    public final void setOnHiding(EventHandler<? super ActionEvent> onHiding) {
	if(this.onHiding != null || onHiding != null) {
	    onHidingProperty().set(onHiding);
	}
    }
    
    public final void setOnShowing(EventHandler<? super ActionEvent> onShowing) {
	if(this.onShowing != null || onShowing != null) {
	    onShowingProperty().set(onShowing);
	}
    }
    
    public final void setOnShown(EventHandler<? super ActionEvent> onShown) {
	if(this.onShown != null || onShown != null) {
	    onShownProperty().set(onShown);
	}
    }
    
    //Property getters
    
    public final ReadOnlyBooleanProperty inQueueProperty() {
	return inQueue.getReadOnlyProperty();
    }
    
    public final ReadOnlyBooleanProperty shownProperty() {
	return shown.getReadOnlyProperty();
    }
    
    public final ReadOnlyBooleanProperty showingProperty() {
	return showing.getReadOnlyProperty();
    }
    
    public final ReadOnlyObjectProperty<ToastQueue> queueProperty() {
	return queue.getReadOnlyProperty();
    }
    
    public final ReadOnlyObjectProperty<Instant> showingTimeProperty() {
	return showingTime.getReadOnlyProperty();
    }
    
    public final ReadOnlyBooleanProperty cancelledProperty() {
	return cancelled.getReadOnlyProperty();
    }   
    
    public final ReadOnlyObjectProperty<Window> ownerWindowProperty() {
	return ownerWindow.getReadOnlyProperty();
    }
    
    public final ReadOnlyObjectWrapper<Window> ownerWindowPropertyImpl() {
	return ownerWindow;
    }
    
    public final ObjectProperty<EventHandler<? super ActionEvent>> onShowingProperty() {
	return onShowing != null ? onShowing 
		: (onShowing = new SimpleObjectProperty<>(this, "onShowing"));
    }
    
    public final ObjectProperty<EventHandler<? super ActionEvent>> onShownProperty() {
	return onShown != null ? onShown 
		: (onShown = new SimpleObjectProperty<>(this, "onShown"));
    }
    
    public final ObjectProperty<EventHandler<? super ActionEvent>> onHidingProperty() {
	return onHiding != null ? onHiding 
		: (onHiding = new SimpleObjectProperty<>(this, "onHiding"));
    }
    
    public final ObjectProperty<EventHandler<? super ActionEvent>> onHiddenProperty() {
	return onHidden != null ? onHidden 
		: (onHidden = new SimpleObjectProperty<>(this, "onHidden"));
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
	
	if(getQueue() != null) {
	    throw new IllegalStateException(
		    "This toast in queue. His can`t be showing manually.");
	} else {
	    if(getDuration().equals(Duration.ZERO)) {
		showing.set(true);
		showing.set(false);
	    } else {
		getPopup().show();
		if(getDuration().lessThan(Duration.INDEFINITE)) {
		    hidingTimeline = new Timeline(new KeyFrame(
			    getDuration(), e -> getPopup().hide()));
		    hidingTimeline.play();
		}
	    }
	}
    }

    public final void cancel() {
	if(!isCancelled()) {
	    cancelled.set(true);
	    ToastQueue localQueue = getQueue();
	    if(localQueue != null) {
		localQueue.getToasts().remove(this);
		queue.set(null);
	    }
	    if(popup != null) popup.hide();
	    if(hidingTimeline != null) {
		hidingTimeline.stop();
	    }
	}
    }
    
    //Static initializers

    public static <T> Toast<T> of(T owner, String text) {
	return of(owner, text, Pos.CENTER);
    }

    public static <T> Toast<T> of(T owner, String text, Pos alignment) {
	return of(owner, text, DURATION_SHORT, alignment);
    }

    public static <T> Toast<T> of(T owner, String text, Duration duration) {
	return of(owner, text, duration, Pos.CENTER);
    }

    public static <T> Toast<T> of(T owner, String text, Pos alignment, Point2D offset) {
	return of(owner, text, DURATION_SHORT, alignment, offset);
    }

    public static <T> Toast<T> of(T owner, String text, Pos alignment, double x, double y) {
	return of(owner, text, DURATION_SHORT, alignment, x, y);
    }

    public static <T> Toast<T> of(T owner, String text, Duration duration, Pos alignment) {
	return of(owner, text, duration, alignment, Point2D.ZERO);
    }

    public static <T> Toast<T> of(T owner, String text, Duration duration, Pos alignment, double x, double y) {
	return of(owner, text, duration, alignment, new Point2D(x, y));
    }

    public static <T> Toast<T> of(T owner, String text, Duration duration, Pos alignment, Point2D offset) {
	return of(owner, Toast.createText(text), duration, alignment, offset);
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

    public static <T> Toast<T> of(T owner, Node content, Pos alignment, Point2D offset) {
	return of(owner, content, DURATION_SHORT, alignment, offset);
    }

    public static <T> Toast<T> of(T owner, Node content, Pos alignment, double x, double y) {
	return of(owner, content, DURATION_SHORT, alignment, x, y);
    }

    public static <T> Toast<T> of(T owner, Node content, Duration duration, Pos alignment) {
	return of(owner, content, duration, alignment, Point2D.ZERO);
    }

    public static <T> Toast<T> of(T owner, Node content, Duration duration, Pos alignment, double x, double y) {
	return of(owner, content, duration, alignment, new Point2D(x, y));
    }

    public static <T> Toast<T> of(T owner, Node content, Duration duration, Pos alignment, Point2D offset) {
	return Toast.<T>builder().
		setOwner(owner).
		setOffset(offset).
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

    public static Label createText(String text) {
	return createText(text, Font.getDefault());
    }

    public static Label createText(String text, Font font) {
	Label l = createLabel();
	l.setText(text);
	l.setFont(font);
	return l;
    }

    public static Label createText(String text, String fontFamily) {
	return createText(text, Font.font(fontFamily));
    }

    public static Label createText(String text, int size) {
	return createText(text, Font.font(size));
    }

    public static Label createText(String text, String fontFamily, int size) {
	return createText(text, Font.font(fontFamily, size));
    }

    public static Label createText(ObservableValue<String> text) {
	return createText(text, Font.getDefault());
    }

    public static Label createText(ObservableValue<String> text, Font font) {
	Label l = createLabel();
	l.textProperty().bind(text);
	l.setFont(font);
	return l;
    }

    public static Label createText(ObservableValue<String> text, String fontFamily) {
	return createText(text, Font.font(fontFamily));
    }

    public static Label createText(ObservableValue<String> text, int size) {
	return createText(text, Font.font(size));
    }

    public static Label createText(ObservableValue<String> text, String fontFamily, int size) {
	return createText(text, Font.font(fontFamily, size));
    }

    /*public static <T extends Node> Toast<T> getCurrent(T owner) {
	return getCurrentImpl(owner);
    }

    public static <T extends Window> Toast<T> getCurrent(T owner) {
	return getCurrentImpl(owner);
    }

    private static <T> Toast<T> getCurrentImpl(T owner) {
	Objects.requireNonNull(owner, "owner");
	CheckedToastQueue<T> queue = (CheckedToastQueue<T>) CheckedToastQueue.getQueues().get(owner);
	return queue != null ? queue.getCurrentToast() : null;
    }*/
    
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    public static class Builder<T> implements javafx.util.Builder<Toast<T>> {
	
	private static String toastStylesheet = Toast.class.getResource("Toast.css").toExternalForm();
        
        private T owner;
        private Node content;
        
        private Pos alignment = Pos.CENTER;
        private Duration duration = DURATION_SHORT;
        private Point2D offset = Point2D.ZERO;
	
	private String style;
	private ObservableList<String> stylesheets = FXCollections.observableArrayList(toastStylesheet);
	private ObservableList<String> styleClass = FXCollections.observableArrayList("toast");
        
        private Builder() {}

        public Builder<T> setAlignment(Pos alignment) {
            this.alignment = Objects.requireNonNull(alignment, "alignment");
            return this;
        }

        public Builder<T> setDuration(Duration duration) {
            this.duration = Objects.requireNonNull(duration, "duration");
            return this;
        }

        public Builder<T> setText(String text) {
            return setContent(createText(text));
        }

        public Builder<T> setText(ObservableValue<String> textProperty) {
            return setContent(createText(textProperty));
        }

        public Builder<T> setContent(Node content) {
            this.content = Objects.requireNonNull(content, "content");
            return this;
        }

        public Builder<T> setOffset(Point2D offset) {
            this.offset = Objects.requireNonNull(offset, "offset");
            return this;
        }

        public Builder<T> setOffset(double x, double y) {
            return setOffset(new Point2D(x, y));
        }

        public Builder<T> setOwner(T owner) {
            Objects.requireNonNull(owner, "owner");
	    if(!ToastPopup.isSupported(owner)) {
		throw new IllegalArgumentException(
			"Owner [" + owner + "] not supported by Toast.");
	    }
	    this.owner = owner;
            return this;
        }

	public Builder<T> setStyle(String style) {
	    this.style = style;
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

        public T getOwner() {
            return owner;
        }

	public String getStyle() {
	    return style;
	}

	public Builder<T> setStylesheets(ObservableList<String> stylesheets) {
	    Objects.requireNonNull(stylesheets, "stylesheets");
	    this.stylesheets = stylesheets;
	    return this;
	}

	public Builder<T> setStylesheets(String... stylesheets) {
	    return setStylesheets(FXCollections.observableArrayList(stylesheets));
	}

	public Builder<T>  setStyleClass(ObservableList<String> styleClass) {
	    Objects.requireNonNull(styleClass, "styleClass");
	    this.styleClass = styleClass;
	    return this;
	}

	public Builder<T> setStyleClass(String... stylesheets) {
	    return setStyleClass(FXCollections.observableArrayList(stylesheets));
	}

	public ObservableList<String> getStylesheets() {
	    return stylesheets;
	}

	public ObservableList<String> getStyleClass() {
	    return styleClass;
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
	    
	    Toast<T> toast = new Toast<>(this);
	    this.content = null;
	    return toast;
        }
        
    }

}
