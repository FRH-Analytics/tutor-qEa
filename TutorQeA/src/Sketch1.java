import java.util.ArrayList;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.DropdownList;
import processing.core.PApplet;
import processing.core.PFont;

// Simple example to show how two sketches can be created in separate windows. 
// Version 1.2, 18th July, 2009. 
// Author Jo Wood. 


public class Sketch1 extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ControlP5 cp5;

	ArrayList<DropdownList> lists = new ArrayList<>();

	PFont font = createFont("arial", 12);

	int actual_x = 20, actual_y = 80;

	DropdownList d1;

	public void setup() {
		size(400, 400);

		noStroke();
		cp5 = new ControlP5(this);

		cp5.addTextfield("input").setPosition(20, 20).setSize(width - 100, 20)
				.setFont(font).setFocus(true).setColor(color(255));

		cp5.addButton("search").setValue(0).setPosition(width - 70, 20)
				.setSize(50, 20);

	}

	public void draw() {
		background(0);
		fill(255);
	}

	public void controlEvent(ControlEvent theEvent) {

		if (theEvent.isGroup()) {
			addDropDownList();
		}

	}

	public void search(int theValue) {
		clearList();
	}

	public void input(String theText) {
		clearList();
		addDropDownList();
	}

	private void addDropDownList() {
		int x = 20+100*(lists.size()%3);
		int y = 80+20*(floor(lists.size()/3));
		DropdownList newD = cp5.addDropdownList("myList-d" + (lists.size()+1)).setPosition(x,y);
		lists.add(newD);
		try {
			lists.get(lists.size() - 2).disableCollapse();
		} catch (Exception e) {			
		}
	}
	public void clearList() {
		for (DropdownList list : lists) {
			list.remove();
		}
		lists = new ArrayList<>();
	}
}