public abstract class OurSketch {

	protected MainSketch pApplet;
	protected int myWidth;
	protected int myHeight;
	protected int myXOrigin;
	protected int myYOrigin;

	public OurSketch(MainSketch parent, int xOrigin, int yOrigin, int width,
			int height) {
		pApplet = parent;

		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;
	}

	public abstract void setup();

	public abstract void draw();

	public abstract void mousePressed();

	public boolean mouseOverSketch() {
		return (pApplet.mouseX > myXOrigin
				&& pApplet.mouseX < (myXOrigin + myWidth)
				&& pApplet.mouseY > myYOrigin && pApplet.mouseY < (myYOrigin + myHeight));
	}
}