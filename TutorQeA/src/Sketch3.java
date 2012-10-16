import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import processing.core.PApplet;
import processing.core.PFont;
import util.AnswerData;
import util.CompositeSketch;
import util.QeAData;
import util.QuestionData;
import controlP5.Accordion;
import controlP5.Canvas;
import controlP5.ControlP5;
import controlP5.Group;

public class Sketch3 implements CompositeSketch {

	private int answerRectHeight;
	private int questionRectHeight;
	private int questionRectPadding;

	private float titlePadding;

	private TreeSet<QuestionData> sortedQuestions;

	private ControlP5 cp5;
	private Accordion accordion;

	private PApplet pApplet;
	private int myWidth;
	private int myHeight;
	private int myXOrigin;
	private int myYOrigin;

	private int clusterId;

	public Sketch3(PApplet parent, int xOrigin, int yOrigin, int width,
			int height) {
		pApplet = parent;

		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;
	}

	public void setup() {
		cp5 = new ControlP5(pApplet);

		// Question Rectangles
		questionRectHeight = myHeight / 25;
		questionRectPadding = myWidth / 50;
		answerRectHeight = myHeight / 7;

		// Instantiates the objects
		sortedQuestions = new TreeSet<QuestionData>(
				new QuestionDescComparator());

		// Title
		titlePadding = myHeight * (float) 0.05;

		// Cluster
		clusterId = -1;
	}

	public void draw() {
		// Draw title
		drawTitle();
	}

	@Override
	public void mousePressed() {
	}

	private void drawTitle() {
		pApplet.fill(0);
		pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
		pApplet.textSize(myHeight / 25);
		pApplet.text(getTitle(), myXOrigin + (myWidth / 2), myYOrigin
				+ titlePadding);
		pApplet.textAlign(PApplet.LEFT);

		// Draw line
		float titleLength = pApplet.textWidth(getTitle());
		float fixedY = myYOrigin + titlePadding;
		float xBeforeTitle = myXOrigin + (myWidth / 2) - (titleLength / 2);
		float xAfterTitle = xBeforeTitle + titleLength;

		pApplet.stroke(100);
		pApplet.strokeWeight((float) 2);
		pApplet.line(myXOrigin + titlePadding, fixedY, xBeforeTitle
				- titlePadding, fixedY);
		pApplet.line(myXOrigin + myWidth - titlePadding, fixedY, xAfterTitle
				+ titlePadding, fixedY);
	}

	private void drawQuestionsAccordion() {
		if (sortedQuestions.size() > 0) {

			accordion = cp5
					.addAccordion("QuestionsAccordion")
					.setPosition(myXOrigin + questionRectPadding,
							myYOrigin + (2 * titlePadding))
					.setWidth(myWidth - 2 * questionRectPadding)
					.setMinItemHeight(20);

			Group g1;
			int scoreValue, questionFontSize;
			for (QuestionData qData : sortedQuestions) {

				pApplet.colorMode(PApplet.HSB);
				scoreValue = (int) PApplet.map(qData.getCommentCount(),
						sortedQuestions.last().getCommentCount(),
						sortedQuestions.first().getCommentCount(), 50, 200);

				// ADD the QUESTION SCORE, TITLE and SATURATION+BRIGHTNESS
				g1 = cp5.addGroup(String.valueOf(qData.getId()))
						.setLabel(
								qData.getScore() + " votes - "
										+ qData.getTitle())
						.setBarHeight(questionRectHeight)
						.setBackgroundHeight(answerRectHeight)
						.setColorBackground(
								pApplet.color(100, scoreValue, scoreValue));
				pApplet.colorMode(PApplet.RGB);

				// FONT
				questionFontSize = questionRectHeight / 2;
				PFont pfont = pApplet.createFont("Helvetica", questionFontSize,
						true);
				controlP5.ControlFont font = new controlP5.ControlFont(pfont,
						questionFontSize);
				g1.getCaptionLabel().toUpperCase(false).setLetterSpacing(3)
						.setFont(font).getStyle().marginLeft = 10;

				// ADD ANSWER CANVAS
				g1.addCanvas(new AnswerCanvas(qData.getId(), accordion
						.getWidth(), answerRectHeight));

				accordion.addItem(g1);
			}

			accordion.open();
			accordion.setCollapseMode(Accordion.MULTI);
		}
	}

	public void updateQuestionsByCluster(int clusterId) {

		// Set the cluster id to the title
		this.clusterId = clusterId;

		// Removes the old Accordion
		cp5.remove("QuestionsAccordion");

		sortedQuestions.clear();

		// Sort the new question id based on the question data
		for (Integer id : QeAData.getQuestionIdsByCluster(clusterId)) {
			sortedQuestions.add(QeAData.getQuestionIdsToData().get(id));
		}

		// Redraw the Accordion
		drawQuestionsAccordion();
	}

	private String getTitle() {
		return (clusterId == -1) ? "Questions and Answers by Cluster"
				: "Questions and Answers - Cluster " + clusterId;
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
			p.fill(200, 100);
			p.rect(0, 0, canvasWidth, canvasHeight);

			// There are questions without answers!!!
			if (QeAData.getQuestionIdsToAnswers().get(questionId) != null) {
				// DRAW timeline

				// DRAW answer balls
				int maxScore = 0;
				int maxCommentsCount = 0;
				for (AnswerData ans : QeAData.getQuestionIdsToAnswers().get(
						questionId)) {
					maxScore = (ans.getScore() > maxScore) ? ans.getScore()
							: maxScore;
					maxCommentsCount = (ans.getCommentsCount() > maxCommentsCount) ? ans.getCommentsCount()
							: maxCommentsCount;
				}

				p.ellipseMode(PApplet.RADIUS);

				float ballPadding = 10;
				float ballRadius;
				float ballBrightness;
				float maxBallRadius = PApplet.map(maxScore, 0, maxScore,
						canvasHeight / 15, canvasHeight / 3);

				float ballYCenter = (canvasHeight / 2) + maxBallRadius;
				float ballXCenter = ballPadding;

				for (AnswerData ans : QeAData.getQuestionIdsToAnswers().get(
						questionId)) {
					
					ballBrightness = PApplet.map(ans.getCommentsCount(), 0, maxCommentsCount, 100, 250);
					
					if(!ans.isAccepted()){
						p.fill(pApplet.color(50, 200, 150, ballBrightness));
					}else{
						p.fill(pApplet.color(0, 200, 0, 150));
					}

					ballRadius = PApplet.map(ans.getScore(), 0, maxScore,
							canvasHeight / 15, canvasHeight / 3);

					// Ball shift
					ballXCenter += ballRadius;

					pApplet.ellipse(ballXCenter, ballYCenter - ballRadius,
							ballRadius, ballRadius);

					// Ball shift
					ballXCenter += ballRadius + ballPadding;
				}
			}
		}
	}
}
