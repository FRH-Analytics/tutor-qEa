import java.awt.GridLayout;

import processing.core.PApplet;

// Simple example to show how two sketches can be embedded in a single window. 
// Version 1.2, 18th July, 2009. 
// Author Jo Wood. 

public class MainSketch extends PApplet {

	// ----------------------- Initialisation ---------------------------

	/**
	 * Places two embedded sketches inside this one.
	 */
	public void setup() {
		size(1100, 410);
		setLayout(new GridLayout(0, 2));
		noLoop();

//		Sketch1 sketch1 = new Sketch1();
//		Sketch2 sketch2 = new Sketch2();
//		Sketch3 sketch3 = new Sketch3();
//		Sketch4 sketch4 = new Sketch4();

//		SketchPanel sp1 = new SketchPanel(this, sketch1);
//		add(sp1);
//		sketch1.setIsActive(true);
//
//		SketchPanel sp2 = new SketchPanel(this, sketch2);
//		add(sp2);
//		sketch2.setIsActive(true);

//		SketchPanel sp3 = new SketchPanel(this, sketch3);
//		add(sp3);
//		sketch3.setIsActive(true);
//		
//		SketchPanel sp4 = new SketchPanel(this, sketch4);
//		add(sp4);
//		sketch4.setIsActive(true);
	}
}