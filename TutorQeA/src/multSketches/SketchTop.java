package multSketches;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import processing.core.PApplet;
import processing.core.PImage;
import util.QuestionData;
import controlP5.ControlEvent;

public class SketchTop extends EmbeddedSketch {

	private static final long serialVersionUID = 1L;
	private int myWidth;
	private int myHeight;

	PImage statsImage;
	
	private float middleHeaderY;
	private float middleSubHeaderY;

	private SubSketch1 SKETCH_1;
	private SubSketch2 SKETCH_2;

	public SketchTop(int width, int height) {
		myWidth = width;
		myHeight = height;

		SKETCH_1 = new SubSketch1(this, 0, 50, 350, myHeight - 110);
		SKETCH_2 = new SubSketch2(this, 350, 50, myWidth - 350, myHeight - 110);
	}

	@Override
	public void setup() {
		size(myWidth, myHeight);
		smooth();

		statsImage = loadImage("stats.stackexchange.com.png");
		
		middleHeaderY = myHeight - 45;
		middleSubHeaderY = myHeight - 15;

		SKETCH_1.setup();
		SKETCH_2.setup();

		// Mouse Wheel
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent mwe) {
				// Re-Draw...
				loop();
			}
		});
	}

	@Override
	public void draw() {
		super.draw();

		// Draw Background
		background(255);
		textFont(MainSketch.mainFont);

		SKETCH_1.draw();
		SKETCH_2.draw();

		strokeWeight(1);

		drawMainTitle();
		drawMiddleHeader();

		textAlign(PApplet.LEFT);

		noLoop();
	}

	@Override
	public void mousePressed() {
		SKETCH_2.mousePressed();

		if (MainSketch.SKETCH_TOP.SKETCH_2.getClickedClusterId() != -1
				&& isMouseOverFeature()) {
			changeQuestionOrdering();
			// Re-draw...
			loop();
		}
	}

	public void mouseMoved() {
		SKETCH_2.mouseMoved();

		// Re-Draw...
		loop();
	}

	@Override
	public void keyPressed() {
		SKETCH_1.keyPressed();
	}

	private void changeQuestionOrdering() {
		// Change the Question Order
		int newIndex = -1;
		if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
			newIndex = (QuestionData.getSortByIndex() + 1)
					% QuestionData.getFeatureNames().size();
		} else {
			if (QuestionData.getSortByIndex() != 0) {
				newIndex = (QuestionData.getSortByIndex() - 1)
						% QuestionData.getFeatureNames().size();
			} else {
				newIndex = QuestionData.getFeatureNames().size() - 1;
			}
		}

		QuestionData.setSortByIndex(newIndex);
		MainSketch.SKETCH_BOTTOM
				.updateQuestionsByCluster(MainSketch.SKETCH_TOP.SKETCH_2
						.getClickedClusterId());
	}

	private boolean isMouseOverFeature() {
		String featureName = QuestionData.getFeatureNameOfSortIndex();
		float featureCenterX = (MainSketch.SKETCH_BOTTOM.getqFeatureX1() + MainSketch.SKETCH_BOTTOM
				.getqFeatureX2()) / 2;
		float featureXOrigin = featureCenterX - textWidth(featureName) / 2;

		return (mouseX >= featureXOrigin
				&& mouseX <= featureXOrigin + textWidth(featureName)
				&& mouseY >= middleSubHeaderY - 10 && mouseY <= middleSubHeaderY + 10);
	}

	/*
	 * USED BY THE SKETCH 1 (ControlP5)
	 */
	public void input(String value) {
		SKETCH_1.input(value);
	}

	public void controlEvent(ControlEvent theEvent) {
		SKETCH_1.controlEvent(theEvent);
		MainSketch.SKETCH_BOTTOM.removeQuestionsAndCluster();
		SKETCH_2.updatePlot();
	}

	/*
	 * Draw Titles and Divisions
	 */
	private void drawMainTitle() {
		fill(0);
		textAlign(PApplet.CENTER, PApplet.CENTER);
		textSize(35);
		text("Tutor Q&A - Cross Validated", width / 2, 25);
	}

	private void drawMiddleHeader() {
		float headerPadding = 10;
		int clusterId = MainSketch.SKETCH_TOP.SKETCH_2.getClickedClusterId();

		// Header
		String middleTitle = (clusterId == -1) ? "Questions and Answers by Group"
				: SKETCH_2.getClusterName(clusterId)
						+ " - Questions and Answers";

		fill(0);
		textAlign(PApplet.CENTER, PApplet.CENTER);
		textSize(18);
		text(middleTitle, (myWidth / 2), middleHeaderY);

		// Draw line
		float titleLength = textWidth(middleTitle);
		float xBeforeTitle = (myWidth / 2) - (titleLength / 2);
		float xAfterTitle = xBeforeTitle + titleLength;

		stroke(100);
		line(2 * headerPadding, middleHeaderY + 3,
				xBeforeTitle - headerPadding, middleHeaderY + 3);
		line(myWidth - 2 * headerPadding, middleHeaderY + 3, xAfterTitle
				+ headerPadding, middleHeaderY + 3);

		// Subheader
		String featureString;
		if (clusterId != -1) {
			featureString = QuestionData.getFeatureNameOfSortIndex();
		} else {
			featureString = "Attribute...";
		}
		// TODO: Change to the real cluster name
		String questionsString = "Question's Title";
		String answersString = "Answer's Timeline";

		fill(0);
		textAlign(PApplet.CENTER, PApplet.CENTER);
		textSize(16);

		float featureCenterX, questionCenterX, answersCenterX;

		// Draw texts and lines
		featureCenterX = (MainSketch.SKETCH_BOTTOM.getqFeatureX2() + MainSketch.SKETCH_BOTTOM
				.getqFeatureX1()) / 2;
		questionCenterX = (MainSketch.SKETCH_BOTTOM.getqTitleX2() + MainSketch.SKETCH_BOTTOM
				.getqTitleX1()) / 2;
		answersCenterX = (MainSketch.SKETCH_BOTTOM.getqAnswerX2() + MainSketch.SKETCH_BOTTOM
				.getqAnswerX1()) / 2;

		text(featureString, featureCenterX, middleSubHeaderY);
		text(questionsString, questionCenterX, middleSubHeaderY);
		text(answersString, answersCenterX, middleSubHeaderY);

		if (clusterId != -1 && isMouseOverFeature()) {
			drawTooltipFeatureChange();
		}
	}

	private void drawTooltipFeatureChange() {
		String tooltip = "Click to change the\nordering criteria";

		float tooltipX = mouseX + 15;
		float tooltipY = mouseY - 35;

		fill(50, 100);
		strokeWeight((float) 1);
		rectMode(PApplet.CENTER);
		rect(tooltipX, tooltipY, textWidth(tooltip), 40, 5, 5);

		textSize(12);
		textLeading(15);
		fill(255);
		textAlign(PApplet.CENTER, PApplet.CENTER);
		text(tooltip, tooltipX, tooltipY);
	}
}
