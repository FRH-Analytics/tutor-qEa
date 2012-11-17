package multSketches;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.ConcurrentSkipListSet;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import processing.core.PApplet;
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

	private float newYOrigin = 0, offsetY = 0;

	private float qX1;
	private float qX2;
	private float qScoreX1;
	private float qScoreX2;
	private float qTitleX1;
	private float qTitleX2;
	private float qAnswerX1;
	private float qAnswerX2;

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
		size(myWidth, myHeight);

		smooth();
		resetNewYOrigin();

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

		// Mouse Wheel
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent mwe) {
//				updateYOrigin(newYOrigin - (mwe.getWheelRotation() * 12));
				newYOrigin -= (mwe.getWheelRotation() * 12);
			}
		});
	}

	public void draw() {
		super.draw();

		if (mousePressed) {
			cursor(MOVE);
			updateYOrigin(mouseY - offsetY);
		} else {
			cursor(ARROW);
		}

		background(255);
		translate(0, newYOrigin);

		if (sortedQuestions.size() > 0) {
			drawQuestions();
		} else {
			drawNoCluster();
		}
	}

	private void resetNewYOrigin(){
		// TODO: workaround ^^...
		newYOrigin = -350;
	}
	
	private void updateYOrigin(float nextNewYOrigin) {
		if (nextNewYOrigin < 0) {
			float questionsY1Distance = qHeight + questionRectYPadding;
			if ((sortedQuestions.size() * questionsY1Distance)
					- abs(newYOrigin) <= myHeight - 50) {
				if (nextNewYOrigin > newYOrigin) {
					newYOrigin = nextNewYOrigin;
				}
			} else {
				newYOrigin = nextNewYOrigin;
			}
		} else {
			resetNewYOrigin();
		}
	}

	public void mouseMoved() {
		selectedQuestionIndex = getSelectedQuestionIndex(mouseX, mouseY,
				myXOrigin, myXOrigin + myWidth);
	}

	public void mousePressed() {

		// Update the Offset
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
		resetNewYOrigin();

		// Sort the new question id based on the question data
		for (Integer id : QeAData.getQuestionIdsByCluster(clusterId)) {
			sortedQuestions.add(QeAData.getQuestionIdsToData().get(id));
		}

		// Clean the garbage...
		System.gc();
	}

	public void removeQuestionsAndCluster() {
		sortedQuestions.clear();
		clusterTitleId = -1;
		selectedQuestionIndex = -1;
	}

	private void drawNoCluster() {
		String noTag = "No cluster selected...";
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

		// First Rectangle boundaries
		float qY1 = myYOrigin + questionRectYPadding;
		float qY2 = qY1 + qHeight;

		strokeWeight((float) 1.5);

		maxAnswerScoreOfAll = getMaxAnswerScore();

		int i = 0;
		boolean highlightQuestion;
		for (QuestionData qData : sortedQuestions) {

			highlightQuestion = (selectedQuestionIndex == i++);

			// DRAW score rectangle
			drawQuestionScore(qData, qScoreX1, qY1, qScoreX2, qY2,
					highlightQuestion);

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
			float x2, float y2, boolean highlightQuestion) {
		// Highlight or not the Stroke
		stroke(150, (highlightQuestion) ? 255 : 100);

		rectMode(PApplet.CORNERS);
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

		// Highlight or not the Stroke
		stroke(150, (highlightQuestion) ? 255 : 100);

		rectMode(PApplet.CORNERS);
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

			ChartItem clusterItem = ChartData.getItemById(qData.getCluster());

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

				// Highlight or not the Stroke
				stroke(150, (highlightQuestion) ? 255 : 100);

				// Ellipse Radius mapped from the maxAnswerScoreEver!!!
				ballRadius = map(ans.getScore(), 0, maxAnswerScoreOfAll,
						(y2 - y1) / 15, (y2 - y1) / (float) 2.75);

				// Ball shift
				ballXCenter += ballRadius;

				ellipse(ballXCenter, ballYCenter - ballRadius, ballRadius,
						ballRadius);

				drawAnswerTooltip(ans, ballXCenter, ballYCenter - ballRadius,
						ballRadius, y2 - y1);

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

	private void drawAnswerTooltip(AnswerData ans, float ballXCenter,
			float ballYCenter, float ballRadius, float rectHeight) {

		// Distance
		double dist = Math.sqrt(Math.pow(mouseX - ballXCenter, 2)
				+ Math.pow(mouseY - ballYCenter + abs(newYOrigin), 2));
		float tooltipX, tooltipY;

		if (dist <= ballRadius) {
			String tooltip = "Score: " + ans.getScore() + " - Comments: "
					+ ans.getCommentsCount() + "\n" + ans.getCreationDate();

			tooltipX = ballXCenter - textWidth(tooltip) / 2;
			tooltipY = ballYCenter - rectHeight / 3;

			fill(50, 100);
			strokeWeight((float) 1);
			rectMode(PApplet.CENTER);
			rect(tooltipX, tooltipY, textWidth(tooltip) - 5, 40, 5, 5);
			fill(255);
			textSize(12);
			textAlign(PApplet.CENTER, PApplet.CENTER);
			text(tooltip, tooltipX, tooltipY);
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