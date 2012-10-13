import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import processing.core.PApplet;
import processing.core.PFont;
import util.AnswerData;
import util.QeAData;
import util.QuestionData;
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

	TreeSet<QuestionData> sortedQuestions;

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
		sortedQuestions = new TreeSet<QuestionData>(
				new QuestionDescComparator());

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
		if (sortedQuestions.size() > 0) {

			accordion = cp5.addAccordion("QuestionsAccordion")
					.setPosition(questionRectPadding, questionRectPadding)
					.setWidth(width - questionRectPadding);

			Group g1;
			int scoreMapValue;
			for (QuestionData qData : sortedQuestions) {

				colorMode(HSB);
				scoreMapValue = (int) map(qData.getCommentCount(), sortedQuestions
						.last().getCommentCount(), sortedQuestions.first().getCommentCount(),
						0, 255);
				
				// ADD the QUESTION SCORE, TITLE and SATURATION+BRIGHTNESS
				g1 = cp5.addGroup(String.valueOf(qData.getId()))
						.setLabel(
								qData.getScore() + " votes - "
										+ qData.getTitle())
						.setBarHeight(questionRectHeight)
						.setBackgroundHeight(answerRectHeight)
						.setColorBackground(
								color(160, scoreMapValue, scoreMapValue));
				colorMode(RGB);

				PFont pfont = createFont("Helvetica", defaultFontSize, true);
				controlP5.ControlFont font = new controlP5.ControlFont(pfont,
						defaultFontSize);

				g1.getCaptionLabel().toUpperCase(false).setLetterSpacing(3)
						.setFont(font).getStyle().marginLeft = 10;

				// ADD ANSWER CANVAS
				g1.addCanvas(new AnswerCanvas(qData.getId(), width
						- questionRectPadding, answerRectHeight - 5));

				accordion.addItem(g1);
			}

			accordion.open();
			accordion.setCollapseMode(Accordion.MULTI);
		}
	}

	public void updateQuestions(ArrayList<Integer> newQuestionIds) {
		// Remove the old Accordion
		cp5.remove("QuestionsAccordion");

		// Sort the new question id based on the question data
		for (Integer id : newQuestionIds) {
			sortedQuestions.add(QeAData.getQuestionIdsToData().get(id));
		}

		// Redraw the Accordion
		drawQuestionsAccordion();
	}
	
	class QuestionDescComparator implements Comparator<QuestionData> {

		@Override
		public int compare(QuestionData q1, QuestionData q2) {
			return q2.getScore() - q1.getScore();
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

				ballRadius = map(ans.getScore(), 0, maxScore, 0,
						canvasHeight / 2);

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
