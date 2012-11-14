package multSketches;

import java.util.concurrent.ConcurrentSkipListSet;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import util.AnswerData;
import util.QeAData;
import util.QuestionData;

public class SketchBottom extends EmbeddedSketch {

	private static final long serialVersionUID = 1L;

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

	private int newYOrigin = 0, offsetY = 0;

	float qX1;
	float qX2;
	float qScoreX1;
	float qScoreX2;
	float qTitleX1;
	float qTitleX2;
	float qAnswerX1;
	float qAnswerX2;

	private int selectedQuestionIndex;

	public SketchBottom(int xOrigin, int yOrigin, int width, int height) {
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

		newYOrigin = 0;
		cursor(MOVE);
		smooth();

		// Question Rectangles
		questionRectXPadding = myWidth / 50;
		questionRectYPadding = myHeight * (float) 0.01;

		qHeight = myHeight / 6;
		qCornerRadius = 5;

		// Fixed X Values (Question Rectangles)
		qX1 = myXOrigin + questionRectXPadding;
		qX2 = qX1 + myWidth - 2 * questionRectXPadding;

		qScoreX1 = qX1;
		qScoreX2 = qScoreX1 + ((qX2 - qX1) * (float) 0.05);

		qTitleX1 = qX1 + ((qX2 - qX1) * (float) 0.06);
		qTitleX2 = qX1 + ((qX2 - qX1) * (float) 0.5);

		qAnswerX1 = qX1 + ((qX2 - qX1) * (float) 0.51);
		qAnswerX2 = qX1 + ((qX2 - qX1) * (float) 1);

		// Set no cluster
		clusterTitleId = -1;

		selectedQuestionIndex = -1;
	}

	public void draw() {
		super.draw();

		if (mousePressed) {
			newYOrigin = mouseY - offsetY;
		}

		translate(0, newYOrigin);

		background(255);
		if (sortedQuestions.size() == 0) {
			drawNoCluster();
		} else {
			drawQuestions();
		}
	}

	public void mouseMoved() {
		selectedQuestionIndex = getSelectedQuestionIndex(mouseX, mouseY,
				myXOrigin, myXOrigin + myWidth);
	}

	public void mousePressed() {
		// Update the Scroll
		offsetY = mouseY - newYOrigin;

		// Call the Link of the STATS from StackExchange!!!
		if (mouseEvent.getClickCount() == 2) {
			int selectedQuestionIndex = getSelectedQuestionIndex(mouseX,
					mouseY, qTitleX1, qTitleX2);
			if (selectedQuestionIndex != -1) {
				QuestionData q = getQuestionByIndex(selectedQuestionIndex);
				if (q != null) {
					link("http://stats.stackexchange.com/questions/"
							+ q.getId());
				} else {
					System.err.println("Unexistent Question Id!");
				}
			}
		}
	}

	private QuestionData getQuestionByIndex(int questionIndex) {
		int i = 0;
		QuestionData qFinal = null;
		for (QuestionData q : sortedQuestions) {
			if (i == questionIndex) {
				qFinal = q;
				break;
			}
			i++;
		}
		return qFinal;
	}

	private void highlightQuestion() {
		if (selectedQuestionIndex != -1) {

			// float xInPixels, yInPixels, sizeInPixels;
			// String tooltip;

			float firstQuestionY1 = myYOrigin + questionRectYPadding;
			float newFirstQuestionY1 = firstQuestionY1 + newYOrigin;
			float questionsY1Distance = qHeight + questionRectYPadding;

			float qTitleY1 = newFirstQuestionY1
					+ (questionsY1Distance * selectedQuestionIndex);

			drawQuestionTitle(getQuestionByIndex(selectedQuestionIndex),
					qTitleX1, qTitleY1, qTitleX2, qTitleY1 + qHeight, false);
			// fill(clusterItem.getColor(ChartItem.RED),
			// clusterItem.getColor(ChartItem.GREEN),
			// clusterItem.getColor(ChartItem.BLUE));
			// stroke(gridGrayColor / (float) 1.5);
			// strokeWeight((float) 1.75);
			// ellipseMode(PApplet.CENTER);
			//
			// // Highlight Cluster (PLOT)
			// xInPixels = getPointXInPixels(clusterItem.getPoint().x);
			// yInPixels = getPointYInPixels(clusterItem.getPoint().y);
			// sizeInPixels = getPointSizeInPixels(clusterItem.getSize());
			//
			// ellipse(xInPixels, yInPixels, sizeInPixels, sizeInPixels);

			// Draw ToolTip
			// tooltip = String.valueOf((int) clusterItem.getSize())
			// + " question(s)";
			//
			// fill(50, 100);
			// strokeWeight((float) 1);
			// rectMode(PApplet.CENTER);
			// rect(xInPixels + sizeInPixels / 2, yInPixels
			// - sizeInPixels / 2, textWidth(tooltip) + 20, 20,
			// 5, 5);
			// fill(255);
			// textSize(12);
			// textAlign(PApplet.CENTER, PApplet.CENTER);
			// text(tooltip, xInPixels + sizeInPixels / 2, yInPixels
			// - sizeInPixels / 2);
		}
	}

	private int getSelectedQuestionIndex(float mouseX, float mouseY, float X1,
			float X2) {
		int questionIndex = -1;

		float firstQuestionY1 = myYOrigin + questionRectYPadding;
		float newFirstQuestionY1 = firstQuestionY1 + newYOrigin;
		float questionsY1Distance = qHeight + questionRectYPadding;

		if (mouseX >= X1
				&& mouseX <= X2
				&& (mouseY - newFirstQuestionY1) < (sortedQuestions.size() * questionsY1Distance)) {
			questionIndex = ((int) Math.ceil((mouseY - newFirstQuestionY1)
					/ (double) questionsY1Distance)) - 1;
		}
		return questionIndex;
	}

	public void updateQuestionsByCluster(int clusterId) {

		// Remove everything...
		removeQuestionsAndCluster();

		// Set the cluster id to the title
		clusterTitleId = clusterId;

		// Reset the newYOrigin
		newYOrigin = 0;

		// Sort the new question id based on the question data
		for (Integer id : QeAData.getQuestionIdsByCluster(clusterId)) {
			sortedQuestions.add(QeAData.getQuestionIdsToData().get(id));
		}
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
		float qY1 = myYOrigin + questionRectYPadding;
		float qY2 = qY1 + qHeight;

		rectMode(CORNERS);
		strokeWeight((float) 1.5);

		maxAnswerScoreOfAll = getMaxAnswerScore();

		int i = 0;
		boolean highlightQuestion;
		for (QuestionData qData : sortedQuestions) {

			highlightQuestion = (selectedQuestionIndex == i++);

			// Highlight or not the Stroke
			stroke(150, (highlightQuestion) ? 255 : 100);

			// DRAW score rectangle
			drawQuestionScore(qData, qScoreX1, qY1, qScoreX2, qY2);

			// DRAW title rectangle
			drawQuestionTitle(qData, qTitleX1, qY1, qTitleX2, qY2,
					highlightQuestion);

			// DRAW answers rectangle
			drawQuestionAnswers(qData, qAnswerX1, qY1, qAnswerX2, qY2,
					highlightQuestion);

			// Update the qYOrigin
			qY1 = qY2 + questionRectYPadding;
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
			float x2, float y2, boolean highlightQuestion) {

		ChartItem clusterItem = ChartData.getItemById(qData.getCluster());

		if (highlightQuestion) {
			fill(clusterItem.getColor(ChartItem.RED),
					clusterItem.getColor(ChartItem.GREEN),
					clusterItem.getColor(ChartItem.BLUE));
		} else {
			fill(clusterItem.getColor(ChartItem.RED),
					clusterItem.getColor(ChartItem.GREEN),
					clusterItem.getColor(ChartItem.BLUE),
					clusterItem.getColor(ChartItem.ALPHA));
		}

		rect(x1, y1, x2, y2, qCornerRadius, qCornerRadius);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize((y2 - y1) / (float) 3.2);
		text(qData.getTitle(), x1, y1, x2, y2);
	}

	private void drawQuestionAnswers(QuestionData qData, float x1, float y1,
			float x2, float y2, boolean highlightQuestion) {
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
					if (highlightQuestion) {
						// Green Color
						fill(59, 217, 127);
					} else {
						fill(59, 217, 127, 160);
					}
				} else {
					// The cluster color
					ChartItem clusterItem = ChartData.getItemById(qData
							.getCluster());

					if (highlightQuestion) {
						fill(clusterItem.getColor(ChartItem.RED),
								clusterItem.getColor(ChartItem.GREEN),
								clusterItem.getColor(ChartItem.BLUE));
					} else {
						fill(clusterItem.getColor(ChartItem.RED),
								clusterItem.getColor(ChartItem.GREEN),
								clusterItem.getColor(ChartItem.BLUE),
								clusterItem.getColor(ChartItem.ALPHA));
					}
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