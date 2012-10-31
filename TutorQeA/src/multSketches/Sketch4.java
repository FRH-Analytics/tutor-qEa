package multSketches;

import org.gicentre.utils.multisketch.EmbeddedSketch;

public class Sketch4 extends EmbeddedSketch {

	private static final long serialVersionUID = 1L;
	protected int myWidth;
	protected int myHeight;
	protected int myXOrigin;
	protected int myYOrigin;

	public Sketch4(int xOrigin, int yOrigin, int width, int height) {
		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;
	}

	@Override
	public void setup() {
		size(myXOrigin + myWidth, myYOrigin + myHeight);
	}

	@Override
	public void draw() {
		super.draw();
		
		background(255);
		
		drawMiddleDivision();
	}

	private void drawMiddleDivision() {
		float titlePadding = 10;
		int clusterId = MainSketch2.SKETCH_3.getClusterTitleId();
		String titleSkecth3 = (clusterId == -1) ? "Questions and Answers by Cluster"
				: "Questions and Answers - Cluster " + clusterId;

		fill(0);
		textAlign(CENTER, CENTER);
		textSize(myHeight/(float)1.5);
		text(titleSkecth3, myXOrigin + (myWidth / 2), myYOrigin + titlePadding);
		textAlign(LEFT);

		// Draw line
		float titleLength = textWidth(titleSkecth3);
		float fixedY = myYOrigin + titlePadding;
		float xBeforeTitle = myXOrigin + (myWidth / 2) - (titleLength / 2);
		float xAfterTitle = xBeforeTitle + titleLength;

		stroke(100);
		strokeWeight((float) 2);
		line(myXOrigin + titlePadding, fixedY, xBeforeTitle - titlePadding,
				fixedY);
		line(myXOrigin + myWidth - titlePadding, fixedY, xAfterTitle
				+ titlePadding, fixedY);
	}
}
