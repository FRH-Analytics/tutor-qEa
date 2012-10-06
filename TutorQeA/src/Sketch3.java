import org.gicentre.utils.multisketch.EmbeddedSketch;
import org.gicentre.utils.multisketch.PopupWindow;

import processing.core.PFont;

// Simple example to show how two sketches can be created in separate windows. 
// Version 1.2, 18th July, 2009. 
// Author Jo Wood. 


public class Sketch3 extends EmbeddedSketch {

	// ----------------------- Object variables -------------------------

	float rotationAngle;

	// ----------------------- Initialisation ---------------------------

	/**
	 * Sets up this sketch and adds another sketch in a separate window.
	 */
	public void setup() {
		size(300, 300);
		PFont font = createFont("Serif", 32);
		textFont(font, 32);
		smooth();
		textAlign(CENTER, CENTER);
		fill(120, 20, 20);
		rotationAngle = 0;
	}

	// ----------------------- Processing draw --------------------------

	/**
	 * Displays some text and animates its rotation.
	 */
	public void draw() {
		background(255, 200, 200);

		pushMatrix();
		translate(width / 2, height / 2);
		rotate(rotationAngle);
		text("Hello world", 0, 0);
		popMatrix();

		rotationAngle += 0.01;
	}
}