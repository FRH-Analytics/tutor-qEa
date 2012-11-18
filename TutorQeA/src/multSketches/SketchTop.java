package multSketches;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import processing.core.PApplet;
import controlP5.ControlEvent;

public class SketchTop extends EmbeddedSketch {

	private static final long serialVersionUID = 1L;
	protected int myWidth;
	protected int myHeight;

	private SubSketch1 SKETCH_1;
	private SubSketch2 SKETCH_2;

	public SketchTop(int width, int height) {
		myWidth = width;
		myHeight = height;

		SKETCH_1 = new SubSketch1(this, 0, 50, myWidth / 2, myHeight - 100);
		SKETCH_2 = new SubSketch2(this, myWidth / 2, 50, myWidth / 2,
				myHeight - 100);
	}

	@Override
	public void setup() {
		size(myWidth, myHeight);
		smooth();
		SKETCH_1.setup();
		SKETCH_2.setup();
	}

	@Override
	public void draw() {
		super.draw();

		// Draw Background
		background(255);

		SKETCH_2.draw();

		textAlign(PApplet.LEFT);

		drawMainTitle();
		drawMiddleTitle();
		drawMiddleDivision();

		noLoop();
	}

	@Override
	public void mousePressed() {
		SKETCH_2.mousePressed();
	}

	public void mouseMoved() {
		SKETCH_2.mouseMoved();
	}

	/*
	 * USED BY THE SKETCH 1 (ControlP5)
	 */
	public void input(String value) {
		SKETCH_1.input(value);
	}

	public void controlEvent(ControlEvent theEvent) {
		SKETCH_1.controlEvent(theEvent);
		SKETCH_2.updatePlot();
	}

	/*
	 * Draw Titles and Divisions
	 */
	private void drawMainTitle() {
		fill(0);
		textAlign(PApplet.CENTER, PApplet.CENTER);
		textSize(30);
		text("Tutor Q&A", width / 2, 25);
		textAlign(PApplet.LEFT);
	}

	private void drawMiddleTitle() {
		float titlePadding = 10;
		int clusterId = MainSketch.SKETCH_BOTTOM.getClusterTitleId();
		String middleTitle = (clusterId == -1) ? "Questions and Answers by Cluster"
				: "Questions and Answers - Cluster " + clusterId;

		fill(0);
		textAlign(PApplet.CENTER, PApplet.CENTER);
		textSize(18);
		float fixedY = myHeight - 25;
		text(middleTitle, (myWidth / 2), fixedY);
		textAlign(PApplet.LEFT);

		// Draw line
		float titleLength = textWidth(middleTitle);
		float xBeforeTitle = (myWidth / 2) - (titleLength / 2);
		float xAfterTitle = xBeforeTitle + titleLength;

		stroke(100);
		strokeWeight((float) 2);
		line(2 * titlePadding, fixedY, xBeforeTitle - titlePadding, fixedY);
		line(myWidth - 2 * titlePadding, fixedY, xAfterTitle + titlePadding,
				fixedY);
	}

	private void drawMiddleDivision() {
		stroke(100);
		strokeWeight((float) 2);
		line(myWidth / 2, 55, myWidth / 2, myHeight - 50);
	}
}
