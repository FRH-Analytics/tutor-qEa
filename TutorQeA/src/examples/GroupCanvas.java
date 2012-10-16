package examples;

import processing.core.PApplet;
import controlP5.Accordion;
import controlP5.Canvas;
import controlP5.ControlP5;
import controlP5.Group;

public class GroupCanvas extends PApplet {

	ControlP5 cp5;

	public void setup() {
		size(400, 600);
		smooth();

		cp5 = new ControlP5(this);
		ScrollCanvas c = new ScrollCanvas();
		cp5.addCanvas(c);

	}

	public void draw() {
		background(255);
		
		fill(150, 10, 100);
		rect(10, 10, 100, 100);

	}

	class TestCanvas extends Canvas {

		float n;
		float a;

		public void setup(PApplet p) {
			println("starting a test canvas.");
			n = 1;
		}

		public void draw(PApplet p) {
			n += 0.01;
			p.ellipseMode(CENTER);
			p.fill(lerpColor(color(0, 100, 200), color(0, 200, 100),
					map(sin(n), -1, 1, 0, 1)));
			p.rect(0, 0, 200, 200);
			p.fill(255, 150);
			a += 0.01;
			ellipse(100, 100, abs(sin(a) * 150), abs(sin(a) * 150));
			ellipse(40, 40, abs(sin(a + (float) 0.5) * 50), abs(sin(a
					+ (float) 0.5) * 50));
			ellipse(60, 140, abs(cos(a) * 80), abs(cos(a) * 80));
		}
	}

	class ScrollCanvas extends Canvas {

		int centerY, offsetY;
		
		public void setup(PApplet p) {
			println("starting scroll canvas.");

			centerY = 0;
			offsetY = 0;
			cursor(PApplet.MOVE);
			smooth();
			
			Group g1 = cp5.addGroup("myGroup").setLabel("Testing Canvas")
					.setWidth(50).setBackgroundHeight(210);

			Group g2 = cp5.addGroup("myGroup").setLabel("Testing Canvas")
					.setWidth(50).setBackgroundHeight(100);

			g1.addCanvas(new TestCanvas());
			g2.addCanvas(new TestCanvas());

			new Accordion(cp5, "newAcc").addItem(g1).addItem(g2).open()
					.setCollapseMode(Accordion.MULTI).setPosition(100, 100);

		}

		public void draw(PApplet p) {
			if (mousePressed == true) {
				centerY = mouseY - offsetY;
			}

			translate(0, centerY);
			
			fill(188);
			rect(10, 10, 100, 100);

		}
	}

}
