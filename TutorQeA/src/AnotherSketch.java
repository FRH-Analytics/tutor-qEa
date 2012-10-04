import org.gicentre.utils.multisketch.*;

import processing.core.PFont;

public class AnotherSketch extends EmbeddedSketch {
	// Simple embedded sketch that can be placed in its own window.
	// Version 1.2, 18th July, 2009.
	// Author Jo Wood.

	// ----------------------- Object variables -------------------------

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	float textScale;

	// ----------------------- Initialisation ---------------------------

	/**
	 * Initialises the sketch ready to display some animated text.
	 */
	public void setup() {
		size(300, 300);
		PFont font = createFont("SansSerif", 24);
		textFont(font, 24);
		smooth();
		textAlign(CENTER, CENTER);
		fill(20, 120, 20);
		textScale = 0;
	}

	// ----------------------- Processing draw --------------------------

	/**
	 * Displays some text and animates a change in size.
	 */
	public void draw() {
		super.draw(); // Should be the first line of draw().
		background(200, 255, 200);

		pushMatrix();
		translate(width / 2, height / 2);
		scale((float)0.1 + sin(textScale), 1);
		text("Hello again", 0, 0);
		popMatrix();

		textScale += 0.02;
	}
}
