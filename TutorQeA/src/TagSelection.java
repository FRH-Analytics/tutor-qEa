import processing.core.PApplet;


public class TagSelection extends PApplet{

	private static final long serialVersionUID = 1L;

	public void setup() {
		size(1380, 900);

		cursor(ARROW);
		rectMode(CORNERS);
		smooth();
		noStroke();
		frameRate(30);

		
		
	}

	public void draw() {
		background(100, 100);
	}
}
