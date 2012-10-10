import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import util.AnswerData;
import util.QeAData;
import controlP5.Accordion;
import controlP5.Canvas;
import controlP5.ControlP5;
import controlP5.Group;

public class Sketch3 extends PApplet {

	private static final long serialVersionUID = 1L;
	int defaultFontSize;

	int answerRectHeight;
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

		cp5 = new ControlP5(this);

		// Question Rectangles
		questionRectHeight = 25;
		answerRectHeight = 75;
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
					.setPosition(questionRectPadding, questionRectPadding)
					.setWidth(width - questionRectPadding);

			Group g1;
			for (int j = 0; j < questionIds.size(); j++) {
				qId = questionIds.get(j);

				// ADD QUESTION TITLE
				g1 = cp5.addGroup(String.valueOf(qId))
						.setLabel(
								QeAData.getQuestionIdsToData().get(qId)
										.getTitle()).setBackgroundColor(150)
						.setBarHeight(questionRectHeight)
						.setBackgroundHeight(answerRectHeight);

				PFont pfont = createFont("Helvetica", defaultFontSize, true);
				controlP5.ControlFont font = new controlP5.ControlFont(pfont,
						defaultFontSize);

				g1.getCaptionLabel().toUpperCase(false).setLetterSpacing(3)
						.setFont(font).getStyle().marginLeft = 10;

				// ADD ANSWER CANVAS
				g1.addCanvas(new AnswerCanvas(qId, width - questionRectPadding,
						answerRectHeight - 5));

				accordion.addItem(g1);
			}

			accordion.open();
			accordion.setCollapseMode(Accordion.MULTI);
		}
	}

	@SuppressWarnings("unchecked")
	public void updateQuestions(ArrayList<Integer> newQuestionIds) {
		if (!newQuestionIds.equals(questionIds)) {
			cp5.remove("QuestionsAccordion");
			questionIds = (ArrayList<Integer>) newQuestionIds.clone();
			drawQuestionsAccordion();
		}
	}

	class AnswerCanvas extends Canvas {

		private int questionId;
		private int canvasHeight;
		private int canvasWidth;

		public AnswerCanvas(int questionId, int canvasWidth, int canvasHeight) {
			this.questionId = questionId;
			this.canvasHeight = canvasHeight;
			this.canvasWidth = canvasWidth;
		}

		public void setup(PApplet p) {
		}

		public void draw(PApplet p) {
			p.fill(225);
			p.rect(0, 0, canvasWidth, (float) 1.5 * canvasHeight);

			// DRAW timeline

			// DRAW answer balls
			int maxScore = 0;
			for (AnswerData ans : QeAData.getQuestionIdsToAnswers().get(
					questionId)) {
				maxScore = (ans.getScore() > maxScore) ? ans.getScore()
						: maxScore;
			}

			p.ellipseMode(RADIUS);
			
			float ballPadding = 10;
			float ballCenter;
			float ballRadius;
			float ballShift = ballPadding;
			for (AnswerData ans : QeAData.getQuestionIdsToAnswers().get(
					questionId)) {
				p.fill(color(50, 100, 150));

				ballRadius = map(ans.getScore(), 0, maxScore, 0, canvasHeight/2);
				
				// Ball shift
				ballShift += ballRadius;
				// Ball center
				ballCenter = 70 + maxScore - ballRadius;
				
				ellipse(ballShift, ballCenter, ballRadius, ballRadius);

				// Ball shift
				ballShift += ballRadius + ballPadding;
			}
		}
	}
}
