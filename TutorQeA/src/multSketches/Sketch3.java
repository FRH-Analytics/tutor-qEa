package multSketches;

import java.util.concurrent.ConcurrentSkipListSet;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import util.AnswerData;
import util.QeAData;
import util.QuestionData;

public class Sketch3 extends EmbeddedSketch {

	private static final long serialVersionUID = 1L;
	private final int NOT_DRAWING = 0, DRAWING = 1;

	private int qHeight;
	private int qCornerRadius;
	private float questionRectXPadding, questionRectYPadding;

	private float maxAnswerScoreOfAll;

	private ConcurrentSkipListSet<QuestionData> sortedQuestions;
	private int clusterTitleId;

	protected int myWidth;
	protected int myHeight;
	protected int myXOrigin;
	protected int myYOrigin;

	private int centerY = 0, offsetY = 0;

	private int drawState;

	public Sketch3(int xOrigin, int yOrigin, int width, int height) {
		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;

		sortedQuestions = new ConcurrentSkipListSet<QuestionData>();
	}

	public int getClusterTitleId() {
		return clusterTitleId;
	}

	public void setup() {
		size(myXOrigin + myWidth, myYOrigin + myHeight);

		centerY = 0;
		cursor(MOVE);
		smooth();

		// Question Rectangles
		questionRectXPadding = myWidth / 50;
		questionRectYPadding = myHeight * (float) 0.01;

		qHeight = myHeight / 6;
		qCornerRadius = 5;

		// Set no cluster
		clusterTitleId = -1;

		drawState = DRAWING;
	}

	public void draw() {
		super.draw();

		switch (drawState) {
		case (DRAWING):
			if (mousePressed == true) {
				centerY = mouseY - offsetY;
			}

			translate(0, centerY);
			
			System.out.println("Drawing");
			background(255);
			if (sortedQuestions.size() == 0) {
				drawNoCluster();
			} else {
				drawQuestions();
			}
			drawState = NOT_DRAWING;
			break;
		default:
		}
	}

	public void mouseMoved() {
		// System.out.println("Event: Mouse Moved");
		drawState = DRAWING;
	}

	public void mousePressed() {
		// System.out.println("Event: Mouse Pressed");
		offsetY = mouseY - centerY;
	}

	@Override
	public void mouseDragged() {
		// System.out.println("Event: Mouse Dragged");
		drawState = DRAWING;
	}

	public void updateQuestionsByCluster(int clusterId) {

		// Remove everything...
		removeQuestionsAndCluster();

		// Set the cluster id to the title
		clusterTitleId = clusterId;

		// Sort the new question id based on the question data
		for (Integer id : QeAData.getQuestionIdsByCluster(clusterId)) {
			sortedQuestions.add(QeAData.getQuestionIdsToData().get(id));
		}
		System.out.println("Event: Update Questions");
		drawState = DRAWING;
	}

	public void removeQuestionsAndCluster() {
		sortedQuestions.clear();
		clusterTitleId = -1;
	}

	private void drawNoCluster() {
		String noTag = "No cluster...";
		fill(150, 100);
		strokeWeight((float) 2);
		rectMode(CENTER);
		rect(myXOrigin + myWidth / 2, myYOrigin + myHeight / 2,
				textWidth(noTag) + 30, 30, 5, 5);
		fill(0);
		textSize(myHeight / 25);
		textAlign(CENTER, CENTER);
		text(noTag, myXOrigin + myWidth / 2, myYOrigin + myHeight / 2);
		textAlign(LEFT);
	}

	private void drawQuestions() {

		// First Rectangle boudaries
		float qX1 = myXOrigin + questionRectXPadding;
		float qY1 = myYOrigin + questionRectYPadding;
		float qX2 = qX1 + myWidth - 2 * questionRectXPadding;
		float qY2 = qY1 + qHeight;

		float qScoreX1 = qX1;
		float qScoreX2 = qScoreX1 + ((qX2 - qX1) * (float) 0.05);

		float qTitleX1 = qX1 + ((qX2 - qX1) * (float) 0.06);
		float qTitleX2 = qX1 + ((qX2 - qX1) * (float) 0.5);

		float qAnswerX1 = qX1 + ((qX2 - qX1) * (float) 0.51);
		float qAnswerX2 = qX1 + ((qX2 - qX1) * (float) 1);

		rectMode(CORNERS);
		stroke(150, 100);
		strokeWeight((float) 1.5);

		maxAnswerScoreOfAll = getMaxAnswerScore();

		for (QuestionData qData : sortedQuestions) {

			// DRAW score rectangle
			drawQuestionScore(qData, qScoreX1, qY1, qScoreX2, qY2);

			// DRAW title rectangle
			drawQuestionTitle(qData, qTitleX1, qY1, qTitleX2, qY2);

			// DRAW answers rectangle
			drawQuestionAnswers(qData, qAnswerX1, qY1, qAnswerX2, qY2);

			// Update the qYOrigin
			qY1 = qY2 + questionRectXPadding / 3;
			qY2 = qY1 + qHeight;
		}

		// TODO: Remove this after add it in the Sketch1
		textAlign(LEFT);
	}

	private float getMaxAnswerScore() {
		float maxScore = Float.MIN_VALUE;
		for (QuestionData qData : sortedQuestions) {
			if (QeAData.getQuestionIdsToAnswers().get(qData.getId()) != null)
				for (AnswerData ans : QeAData.getQuestionIdsToAnswers().get(
						qData.getId())) {
					maxScore = (ans.getScore() > maxScore) ? ans.getScore()
							: maxScore;
				}
		}
		return maxScore;
	}

	private void drawQuestionScore(QuestionData qData, float x1, float y1,
			float x2, float y2) {
		fill(255);
		rect(x1, y1, x2, y2, qCornerRadius, qCornerRadius);

		fill(0);
		textAlign(CENTER, CENTER);

		// Draw votes
		String voteString = (qData.getScore() == 1) ? "vote" : "votes";
		textSize((y2 - y1) / 4);
		float y1Votes = y2 - (float) 2 * textAscent();
		text(voteString, x1, y1Votes, x2, y2);

		// Draw number
		textSize((y2 - y1) / (float) 3);
		text(String.valueOf(qData.getScore()), x1, y1, x2, y1Votes);
	}

	private void drawQuestionTitle(QuestionData qData, float x1, float y1,
			float x2, float y2) {

		ChartItem clusterItem = ChartData.getItemById(qData.getCluster());

		fill(clusterItem.getColor(ChartItem.RED),
				clusterItem.getColor(ChartItem.GREEN),
				clusterItem.getColor(ChartItem.BLUE));
		rect(x1, y1, x2, y2, qCornerRadius, qCornerRadius);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize((y2 - y1) / (float) 3.2);
		text(qData.getTitle(), x1, y1, x2, y2);
	}

	private void drawQuestionAnswers(QuestionData qData, float x1, float y1,
			float x2, float y2) {
		fill(255);
		rect(x1, y1, x2, y2, qCornerRadius, qCornerRadius);

		// DRAW CIRCLES with answers
		if (QeAData.getQuestionIdsToAnswers().get(qData.getId()) != null) {

			int maxScore = 0;
			for (AnswerData ans : QeAData.getQuestionIdsToAnswers().get(
					qData.getId())) {
				maxScore = (ans.getScore() > maxScore) ? ans.getScore()
						: maxScore;
			}

			ellipseMode(RADIUS);

			float ballPadding = 10;
			float ballRadius;
			float maxBallRadius = map(maxScore, 0, maxScore, (y2 - y1) / 15,
					(y2 - y1) / 3);

			float ballYCenter = y1 + ((y2 - y1) / 2) + maxBallRadius;
			float ballXCenter = x1 + ballPadding;

			for (AnswerData ans : QeAData.getQuestionIdsToAnswers().get(
					qData.getId())) {

				// The MaxScore answers has a different color...
				if (ans.getScore() == maxScore) {
					// Green Color
					fill(59, 217, 127);
				} else {
					// The cluster color
					ChartItem clusterItem = ChartData.getItemById(qData
							.getCluster());
					fill(clusterItem.getColor(ChartItem.RED),
							clusterItem.getColor(ChartItem.GREEN),
							clusterItem.getColor(ChartItem.BLUE));
				}

				// Ellipse Radius mapped from the maxAnswerScoreEver!!!
				ballRadius = map(ans.getScore(), 0, maxAnswerScoreOfAll,
						(y2 - y1) / 15, (y2 - y1) / (float) 2.75);

				// Ball shift
				ballXCenter += ballRadius;

				ellipse(ballXCenter, ballYCenter - ballRadius, ballRadius,
						ballRadius);

				// If the answer is accepted put a star on it!
				if (ans.isAccepted()) {
					star(5, ballXCenter, ballYCenter - ballRadius,
							2 * ballRadius, 2 * ballRadius, (float) 55,
							(float) 0.50);
				}

				// Ball shift
				ballXCenter += ballRadius + ballPadding;
			}
		}
	}

	private void star(int n, float cx, float cy, float w, float h,
			float startAngle, float proportion) {
		if (n > 2) {
			colorMode(RGB);
			fill(255, 255, 0);
			float angle = TWO_PI / (2 * n); // twice as many sides
			float dw; // draw width
			float dh; // draw height

			w = (float) (w / 2.0);
			h = (float) (h / 2.0);

			stroke(150, 100);
			beginShape();
			for (int i = 0; i < 2 * n; i++) {
				dw = w;
				dh = h;
				if (i % 2 == 1) // for odd vertices, use short radius
				{
					dw = w * proportion;
					dh = h * proportion;
				}
				vertex(cx + dw * cos(startAngle + angle * i), cy + dh
						* sin(startAngle + angle * i));
			}
			endShape(CLOSE);
		}
	}

}