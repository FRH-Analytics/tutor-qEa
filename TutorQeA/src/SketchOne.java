import java.awt.GridLayout;

import org.gicentre.utils.multisketch.SketchPanel;

import processing.core.PApplet;

public class SketchOne extends PApplet{

	public void setup() 
	{ 
	  size(600,300); 
	  setLayout(new GridLayout(0,2)); 
	  noLoop(); 
	     
	  ASketch       sketch1 = new ASketch(); 
	  AnotherSketch sketch2 = new AnotherSketch(); 
	   
	  SketchPanel sp1 = new SketchPanel(this,sketch1); 
	  add(sp1); 
	  sketch1.setIsActive(true); 
	   
	  SketchPanel sp2 = new SketchPanel(this,sketch2); 
	  add(sp2); 
	  sketch2.setIsActive(true); 
	} 
	
}
