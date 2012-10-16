import java.io.IOException;
import java.text.ParseException;

import processing.core.PApplet;
import util.QeAData;
import controlP5.ControlEvent;

public class MainSketch extends PApplet {

	private static final long serialVersionUID = 1L;

	Sketch1 sketch1;
	Sketch2 sketch2;
	Sketch3 sketch3;

	public void setup() {
		size(1000, 700);
		smooth();

		sketch1 = new Sketch1(this, 0, 50, 500, 300);
		sketch2 = new Sketch2(this, 500, 50, 500, 300);
		sketch3 = new Sketch3(this, 0, 350, width, 350);

		try {
			QeAData.readTagLinksFile();
			QeAData.readTagDictionaryFile();
			QeAData.readPostTagsFile();
			QeAData.readQuestionsDataFile();
			QeAData.readQuestionAnswersFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		sketch1.setup();
		sketch2.setup();
		sketch3.setup();

		/*
		 * TODO: Testing... Delete allll this, then...
		 */
//		ArrayList<Integer> tagList = new ArrayList<Integer>();
		// tagList.add(41);
		// tagList.add(111);
		// tagList.add(264);
		// tagList.add(294);
//		tagList.add(528);
//		ArrayList<String> tagNameList = new ArrayList<String>();
		// tagNameList.add("r");
		// tagNameList.add("regression");
		// tagNameList.add("logistic-regression");
		// tagNameList.add("roc");
//		tagNameList.add("hmm");
//		QeAData.setTagList(tagList, tagNameList);

		// Reset the data
//		sketch2.updatePlot();

		// update the accordion
//		getSketch3().updateQuestionsByCluster(5);
		

	}

	public void draw() {
		background(255);

		// Draw Main Title
		fill(0);
		textAlign(PApplet.CENTER, PApplet.CENTER);
		textSize(30);
		text("Tutor Q&A", width / 2, 25);

		// Draw line between Sketch1 and Sketch2
		stroke(100);
		strokeWeight((float) 2);
		line(width / 2, 50, width / 2, 350);

		sketch1.draw();
		sketch2.draw();
		sketch3.draw();
	}

	@Override
	public void mousePressed() {
		sketch1.mousePressed();
		sketch2.mousePressed();
		sketch3.mousePressed();
	}

	public Sketch1 getSketch1() {
		return sketch1;
	}

	public Sketch2 getSketch2() {
		return sketch2;
	}

	public Sketch3 getSketch3() {
		return sketch3;
	}
	
	public void search(int value){
		sketch1.search(value);
	}
	
	public void input(String value){
		sketch1.input(value);
	}
	
	public void controlEvent(ControlEvent theEvent){
		sketch1.controlEvent(theEvent);
		sketch2.updatePlot();
	}
}
