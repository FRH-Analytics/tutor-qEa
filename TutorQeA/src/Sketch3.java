import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import util.QeAData;
import controlP5.ControlP5;

public class Sketch3 extends PApplet {

	private static final long serialVersionUID = 1L;
	int defaultFontSize;

	int fullBoxXOrigin;
	int fullBoxYOrigin;

	int questionRectHeight;
	int questionRectPadding;

	ArrayList<Integer> questionIds;

	ControlP5 cp5;
	
	public void setup() {
		size(800, 500);
		smooth();

		// Font
		defaultFontSize = 15;
		PFont font = createFont("Helvetica", defaultFontSize);
		textFont(font, defaultFontSize);

		// Full box
		fullBoxXOrigin = 5;
		fullBoxYOrigin = 5;

		// Question Rectangles
		questionRectHeight = 75;
		questionRectPadding = fullBoxXOrigin + 5;

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
		questionIds.add(1);
		questionIds.add(2);
		questionIds.add(4);
	}

	public void draw() {
		background(255);

		fill(255);
		rect(fullBoxXOrigin, fullBoxYOrigin, width - fullBoxXOrigin, height
				- fullBoxYOrigin, 5, 5, 5, 5);

		for (int j = 0; j < questionIds.size(); j++) {
			drawQuestionRectangles(j);
		}

//		noLoop();
	}

	private void drawQuestionRectangles(int qIndex) {

		int xRectPadding = questionRectPadding;
		int yRectPadding = questionRectPadding
				+ ((questionRectHeight + questionRectPadding) * qIndex);
		int rectWidth = width - (2 * questionRectPadding) - 500;

		int xTitlePadding = 2 * questionRectPadding;
		int yTitlePadding = yRectPadding + questionRectPadding
				+ questionRectPadding;

		int qId = questionIds.get(qIndex);

		fill(255);
		rect(xRectPadding, yRectPadding, rectWidth, questionRectHeight);

		fill(0);
		text(QeAData.getQuestionIdsToData().get(qId).getTitle(), xTitlePadding,
				yTitlePadding, rectWidth - questionRectPadding, yRectPadding
						+ questionRectHeight - questionRectPadding);
	}

	@SuppressWarnings("unchecked")
	void updateQuestions(ArrayList<Integer> newQuestionIds) {
		questionIds = (ArrayList<Integer>) newQuestionIds.clone();
	}
}
