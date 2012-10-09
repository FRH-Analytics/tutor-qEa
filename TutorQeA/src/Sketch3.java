import java.io.IOException;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import util.QeAData;

public class Sketch3 extends PApplet {

	private static final long serialVersionUID = 1L;
	int defaultFontSize;
	int questionRectPadding;

	ArrayList<Integer> questionIds;

	public void setup() {
		size(800, 500);
		smooth();

		// Font
		defaultFontSize = 32;
		PFont font = createFont("Helvetica", defaultFontSize);
		textFont(font, defaultFontSize);

		// Question Rectangles
		questionRectPadding = 8;

		// Instantiates the objects
		questionIds = new ArrayList<Integer>();

		// TODO: After all, put this call in the Main
		try {
			QeAData.readQuestionsDataFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Testing...
		questionIds.add(1);
		questionIds.add(50);
		questionIds.add(100);
	}

	public void draw() {
		background(255);

		for (int j = 0; j < questionIds.size(); j++) {
			drawQuestionRectangles(j);
		}

		noLoop();
	}

	private void drawQuestionRectangles(int qIndex) {

		int xRectPadding = questionRectPadding;
		int yRectPadding = questionRectPadding
				+ ((100 + questionRectPadding) * qIndex);
		int titlePadding = yRectPadding + questionRectPadding;
		fill(255);
		rect(questionRectPadding, yRectPadding, width
				- (2 * questionRectPadding), 100);
		fill(0);
		textSize(10);
		text(QeAData.getQuestionIdsToData().get(questionIds.get(qIndex))
				.getTitle(), xRectPadding + titlePadding, yRectPadding
				+ titlePadding);
		System.out.println(questionIds.get(qIndex));
		System.out.println();
	}

	@SuppressWarnings("unchecked")
	void updateQuestions(ArrayList<Integer> newQuestionIds) {
		questionIds = (ArrayList<Integer>) newQuestionIds.clone();
	}
}
