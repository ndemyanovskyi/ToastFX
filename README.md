# ToastFX
Toasts for JavaFX

<h3>Features:</h3> 
<ul>
	<li>Alignment to owner (supported owners: Window, Scene, Node);</li>
	<li>Additional offset to alignment;</li>
	<li>CSS support (class 'toast');</li>
	<li>Based on Java 8 and JavaFX 8;</li>
</ul>

<h3>How it use:</h3> 
Using static initializer 'of':
```Java
Duration duration = Duration.seconds(2000);
Point2D offset = new Point2D(10, 10);
Pos alignment = Pos.CENTER;
Stage owner = new Stage();
String text = "Some text";
	
Toast<Stage> toast = Toast.of(owner, text, duration, alignment, offset);
toast.show();
```
Using Builder:
```Java
Toast<Stage> toast = Toast.builder().
	setOwner(stage).
	setOffset(10, 10).
	setText("Some text").
	setAlignment(Pos.TOP_LEFT).
	setDuration(Duration.seconds(2000)).
        build();
toast.show();
```
<h3>Examples:</h3> 
```Java
Toast.of(scene, "Yeah! This is toast.", Pos.BOTTOM_CENTER).show();
```
![alt tag](https://github.com/ndemyanovskyi/ToastFX/blob/master/src/com/ndemyanovskyi/example/first_example.png)
```Java
Toast.builder().
	setOwner(scene).
	setContent(new Button("Yeah! This is toast.")).
	setAlignment(Pos.CENTER).
	show();
```
![alt tag](https://github.com/ndemyanovskyi/ToastFX/blob/master/src/com/ndemyanovskyi/example/second_example.png)
```Java
Label content = Toast.createContent(
        "Yeah! This is toast.", "Bodoni MT Condensed", 25);
Toast.builder().
	setOwner(scene).
	setContent(content).
	setAlignment(Pos.CENTER_RIGHT).
	show();
```
![alt tag](https://github.com/ndemyanovskyi/ToastFX/blob/master/src/com/ndemyanovskyi/example/third_example.png)
```Java
Toast.builder().
	setOwner(Screen.getPrimary()).
	setContent(Toast.createContent("Yeah! Is a screen toast.", 20)).
	setAlignment(Pos.BOTTOM_RIGHT).
	show();
```
![alt tag](https://github.com/ndemyanovskyi/ToastFX/blob/master/src/com/ndemyanovskyi/example/fourth_example.png)