import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;
import util.AnswerData;
import util.QeAData;
import util.QuestionData;

public class Sketch3 extends ComposableSketch {

	private int qHeight;
	private int qCornerRadius;
	private int questionRectPadding;

	private float titlePadding;
	private float maxAnswerScoreOfAll;

	private ArrayList<QuestionData> sortedQuestions;
	private int clusterId;

	public Sketch3(MainSketch parent, int xOrigin, int yOrigin, int width,
			int height) {
		super(parent, xOrigin, yOrigin, width, height);
	}

	public void setup() {
		// Question Rectangles
		questionRectPadding = myWidth / 50;
		qHeight = myHeight / 6;
		qCornerRadius = 5;

		// Instantiates the objects
		sortedQuestions = new ArrayList<QuestionData>();

		// Title
		titlePadding = myHeight * (float) 0.05;

		// Cluster
		clusterId = -1;
	}

	public void draw() {
		// Draw title
		drawTitle();

		if (sortedQuestions.size() == 0) {
			drawNoCluster();
		} else {
			drawQuestions();
		}
	}

	@Override
	public void mousePressed() {
	}

	public void updateQuestionsByCluster(int clusterId) {

		// Remove everything...
		removeQuestionsAndCluster();

		// Set the cluster id to the title
		this.clusterId = clusterId;

		// Sort the new question id based on the question data
		for (Integer id : QeAData.getQuestionIdsByCluster(clusterId)) {
			sortedQuestions.add(QeAData.getQuestionIdsToData().get(id));
		}
		Collections.sort(sortedQuestions);

	}

	public void removeQuestionsAndCluster() {
		sortedQuestions.clear();
		clusterId = -1;
	}

	private void drawNoCluster() {
		String noTag = "No cluster...";
		pApplet.fill(150, 100);
		pApplet.strokeWeight((float) 2);
		pApplet.rectMode(PApplet.CENTER);
		pApplet.rect(myXOrigin + myWidth / 2, myYOrigin + myHeight / 2,
				pApplet.textWidth(noTag) + 30, 30, 5, 5);
		pApplet.fill(0);
		pApplet.textSize(myHeight / 25);
		pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
		pApplet.text(noTag, myXOrigin + myWidth / 2, myYOrigin + myHeight / 2);
		pApplet.textAlign(PApplet.LEFT);
	}

	private void drawTitle() {
		pApplet.fill(0);
		pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
		pApplet.textSize(myHeight / 20);
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

	private String getTitle() {
		return (clusterId == -1) ? "Questions and Answers by Cluster"
				: "Questions and Answers - Cluster " + clusterId;
	}

	private void drawQuestions() {

		// First Rectangle boudaries
		float qX1 = myXOrigin + questionRectPadding;
		float qY1 = myYOrigin + (2 * titlePadding);
		float qX2 = qX1 + myWidth - 2 * questionRectPadding;
		float qY2 = qY1 + qHeight;

		float qScoreX1 = qX1;
		float qScoreX2 = qScoreX1 + ((qX2 - qX1) * (float) 0.05);

		float qTitleX1 = qX1 + ((qX2 - qX1) * (float) 0.06);
		float qTitleX2 = qX1 + ((qX2 - qX1) * (float) 0.5);

		float qAnswerX1 = qX1 + ((qX2 - qX1) * (float) 0.51);
		float qAnswerX2 = qX1 + ((qX2 - qX1) * (float) 1);

		pApplet.rectMode(PApplet.CORNERS);
		pApplet.stroke(150, 100);
		pApplet.strokeWeight((float) 1.5);

		maxAnswerScoreOfAll = getMaxAnswerScore();

		for (QuestionData qData : sortedQuestions) {

			// DRAW score rectangle
			drawQuestionScore(qData, qScoreX1, qY1, qScoreX2, qY2);

			// DRAW title rectangle
			drawQuestionTitle(qData, qTitleX1, qY1, qTitleX2, qY2);

			// DRAW answers rectangle
			drawQuestionAnswers(qData, qAnswerX1, qY1, qAnswerX2, qY2);

			// Update the qYOrigin
			qY1 = qY2 + questionRectPadding / 3;
			qY2 = qY1 + qHeight;
		}

		// TODO: Remove this after add it in the Sketch1
		pApplet.textAlign(PApplet.LEFT);
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
		pApplet.fill(255);
		pApplet.rect(x1, y1, x2, y2, qCornerRadius, qCornerRadius);

		pApplet.fill(0);
		pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);

		// Draw votes
		String voteString = (qData.getScore() == 1) ? "vote" : "votes";
		pApplet.textSize((y2 - y1) / 4);
		float y1Votes = y2 - (float) 2 * pApplet.textAscent();
		pApplet.text(voteString, x1, y1Votes, x2, y2);

		// Draw number
		pApplet.textSize((y2 - y1) / (float) 3);
		pApplet.text(String.valueOf(qData.getScore()), x1, y1, x2, y1Votes);
	}

	private void drawQuestionTitle(QuestionData qData, float x1, float y1,
			float x2, float y2) {

		ChartItem clusterItem = ChartData.getItemById(qData.getCluster());

		pApplet.fill(clusterItem.getColor(ChartItem.RED),
				clusterItem.getColor(ChartItem.GREEN),
				clusterItem.getColor(ChartItem.BLUE));
		pApplet.rect(x1, y1, x2, y2, qCornerRadius, qCornerRadius);

		pApplet.fill(0);
		pApplet.textAlign(PApplet.LEFT, PApplet.CENTER);
		pApplet.textSize((y2 - y1) / (float) 3.2);
		pApplet.text(qData.getTitle(), x1, y1, x2, y2);
	}

	private void drawQuestionAnswers(QuestionData qData, float x1, float y1,
			float x2, float y2) {
		pApplet.fill(255);
		pApplet.rect(x1, y1, x2, y2, qCornerRadius, qCornerRadius);

		// DRAW CIRCLES with answers
		if (QeAData.getQuestionIdsToAnswers().get(qData.getId()) != null) {

			int maxScore = 0;
			for (AnswerData ans : QeAData.getQuestionIdsToAnswers().get(
					qData.getId())) {
				maxScore = (ans.getScore() > maxScore) ? ans.getScore()
						: maxScore;
			}

			pApplet.ellipseMode(PApplet.RADIUS);

			float ballPadding = 10;
			float ballRadius;
			float maxBallRadius = PApplet.map(maxScore, 0, maxScore,
					(y2 - y1) / 15, (y2 - y1) / 3);

			float ballYCenter = y1 + ((y2 - y1) / 2) + maxBallRadius;
			float ballXCenter = x1 + ballPadding;

			for (AnswerData ans : QeAData.getQuestionIdsToAnswers().get(
					qData.getId())) {

				// The MaxScore answers has a different color...
				if (ans.getScore() == maxScore) {
					// Green Color
					pApplet.fill(59, 217, 127);
				} else {
					// The cluster color
					ChartItem clusterItem = ChartData.getItemById(qData
							.getCluster());
					pApplet.fill(clusterItem.getColor(ChartItem.RED),
							clusterItem.getColor(ChartItem.GREEN),
							clusterItem.getColor(ChartItem.BLUE));
				}

				// Ellipse Radius mapped from the maxAnswerScoreEver!!!
				ballRadius = PApplet.map(ans.getScore(), 0,
						maxAnswerScoreOfAll, (y2 - y1) / 15, (y2 - y1)
								/ (float) 2.75);

				// Ball shift
				ballXCenter += ballRadius;

				pApplet.ellipse(ballXCenter, ballYCenter - ballRadius,
						ballRadius, ballRadius);

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
			pApplet.colorMode(PApplet.RGB);
			pApplet.fill(255, 255, 0);
			float angle = PApplet.TWO_PI / (2 * n); // twice as many sides
			float dw; // draw width
			float dh; // draw height

			w = (float) (w / 2.0);
			h = (float) (h / 2.0);

			pApplet.stroke(150, 100);
			pApplet.beginShape();
			for (int i = 0; i < 2 * n; i++) {
				dw = w;
				dh = h;
				if (i % 2 == 1) // for odd vertices, use short radius
				{
					dw = w * proportion;
					dh = h * proportion;
				}
				pApplet.vertex(cx + dw * PApplet.cos(startAngle + angle * i),
						cy + dh * PApplet.sin(startAngle + angle * i));
			}
			pApplet.endShape(PApplet.CLOSE);
		}
	}
}
