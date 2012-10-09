import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import util.QeAData;
import controlP5.Accordion;
import controlP5.ControlP5;
import controlP5.Group;

public class Sketch3 extends PApplet {

	private static final long serialVersionUID = 1L;
	int defaultFontSize;

	// int fullBoxXOrigin;
	// int fullBoxYOrigin;

	int questionRectHeight;
	int questionRectPadding;

	ArrayList<Integer> questionIds;

	ControlP5 cp5;
	Accordion accordion;

	public void setup() {
		size(800, 500);
		smooth();

		// Font
		defaultFontSize = 15;
		PFont font = createFont("Helvetica", defaultFontSize);
		textFont(font, defaultFontSize);

		// Full box
		// fullBoxXOrigin = 5;
		// fullBoxYOrigin = 5;

		cp5 = new ControlP5(this);

		// Question Rectangles
		questionRectHeight = 75;
		questionRectPadding = 5;

		// Instantiates the objects
		questionIds = new ArrayList<Integer>();

		// TODO: After all, move this calls to Main
		try {
			QeAData.readQuestionsDataFile();
			QeAData.readQuestionAnswersFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Testing...
		ArrayList<Integer> newIds = new ArrayList<Integer>();
		newIds.add(1);
		newIds.add(2);
		newIds.add(4);

		updateQuestions(newIds);
	}

	public void draw() {
		background(255);
	}

	private void drawQuestionsAccordion() {
		int qId;
		if (questionIds.size() > 0) {

			accordion = cp5.addAccordion("QuestionsAccordion")
					.setPosition(20, 20).setWidth(width - 50);

			Group g1;
			for (int j = 0; j < questionIds.size(); j++) {
				qId = questionIds.get(j);

				g1 = cp5.addGroup(String.valueOf(qId))
						.setLabel(
								QeAData.getQuestionIdsToData().get(qId)
										.getTitle())
						.setBackgroundColor(color(225))
						.setBackgroundHeight(100).setBarHeight(30);

				PFont pfont = createFont("Arial", 20, true); // use true/false
																// for
				// smooth/no-smooth
				controlP5.ControlFont font = new controlP5.ControlFont(pfont,
						20);

				g1.getCaptionLabel().toUpperCase(false).setLetterSpacing(3).setFont(font);

				accordion.addItem(g1);
			}

			accordion.open();

			// use Accordion.MULTI to allow multiple group
			// to be open at a time.
			accordion.setCollapseMode(Accordion.MULTI);
		}
	}

	// private void drawQuestionRectangles(int qIndex) {
	//
	// int xRectPadding = questionRectPadding;
	// int yRectPadding = questionRectPadding
	// + ((questionRectHeight + questionRectPadding) * qIndex);
	// int rectWidth = width - (2 * questionRectPadding) - 500;
	//
	// int xTitlePadding = 2 * questionRectPadding;
	// int yTitlePadding = yRectPadding + questionRectPadding
	// + questionRectPadding;
	//
	// int qId = questionIds.get(qIndex);
	//
	// fill(255);
	// rect(xRectPadding, yRectPadding, rectWidth, questionRectHeight);
	//
	// fill(0);
	// text(QeAData.getQuestionIdsToData().get(qId).getTitle(), xTitlePadding,
	// yTitlePadding, rectWidth - questionRectPadding, yRectPadding
	// + questionRectHeight - questionRectPadding);
	// }

	@SuppressWarnings("unchecked")
	public void updateQuestions(ArrayList<Integer> newQuestionIds) {
		if (!newQuestionIds.equals(questionIds)) {
			cp5.remove("QuestionsAccordion");
			questionIds = (ArrayList<Integer>) newQuestionIds.clone();
			drawQuestionsAccordion();
		}
	}
}
