package multSketches;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.concurrent.ConcurrentSkipListSet;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import processing.core.PApplet;
import util.AnswerData;
import util.QeAData;
import util.QuestionData;

public class SketchBottom extends EmbeddedSketch {

	private static final long serialVersionUID = 1L;

	private float qHeight;
	private float qCornerRadius;
	private float questionRectXPadding, questionRectYPadding;

	private float maxAnswerScoreOfAll;

	private ConcurrentSkipListSet<QuestionData> sortedQuestions;

	protected float myWidth;
	protected float myHeight;

	private float newYOrigin;

	private float qX1, qX2;
	private float qFeatureX1, qFeatureX2;
	private float qTitleX1, qTitleX2;
	private float qAnswerX1, qAnswerX2;
	private float scrollBarX1, scrollBarX2;

	private int selectedQuestionIndex;

	public SketchBottom(int width, int height) {
		myWidth = width;
		myHeight = height;

		sortedQuestions = new ConcurrentSkipListSet<QuestionData>();
	}

	public void setup() {
		size((int) myWidth, (int) myHeight);

		smooth();
		resetNewYOrigin();

		// Question Rectangles
		questionRectXPadding = myWidth / 50;
		questionRectYPadding = myHeight * (float) 0.01;

		qHeight = myHeight / 6;
		qCornerRadius = 5;

		// Fixed X Values (Question Rectangles)
		qX1 = questionRectXPadding;
		qX2 = qX1 + myWidth - 3 * questionRectXPadding;

		qFeatureX1 = qX1;
		qFeatureX2 = qFeatureX1 + ((qX2 - qX1) * (float) 0.07);

		qTitleX1 = qX1 + ((qX2 - qX1) * (float) 0.08);
		qTitleX2 = qX1 + ((qX2 - qX1) * (float) 0.52);

		qAnswerX1 = qX1 + ((qX2 - qX1) * (float) 0.53);
		qAnswerX2 = qX1 + ((qX2 - qX1) * (float) 1);

		// Scroll Bar
		scrollBarX1 = qX2 + questionRectXPadding;
		scrollBarX2 = myWidth;

		selectedQuestionIndex = -1;

		// Mouse Wheel
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent mwe) {

				float qTotalHeight = sortedQuestions.size() * qHeight
						+ (sortedQuestions.size() - 1) * questionRectYPadding;

				float multRotation = 9;
				// Does NOT permit that the questions disappear!!!
				if (mwe.getWheelRotation() > 0) {
					if ((newYOrigin + qTotalHeight + qHeight / 2) > myHeight) {
						newYOrigin -= (mwe.getWheelRotation() * multRotation);
					}
				} else {
					if (newYOrigin < 0) {
						newYOrigin -= (mwe.getWheelRotation() * multRotation);
					}
				}

				// Re-Draw...
				loop();
			}
		});
	}

	public void draw() {

		super.draw();
		
		textFont(MainSketch.mainFont);
		background(255);

		if (sortedQuestions.size() > 0) {
			translate(0, newYOrigin);
			drawQuestions();
			drawScrollBar();
		} else {
			drawNoCluster();
		}

		noLoop();
	}

	public void mouseMoved() {
		selectedQuestionIndex = getSelectedQuestionIndex(mouseX, mouseY, 0,
				myWidth);

		// Re-Draw...
		loop();
	}

	public void mousePressed() {
		if (mouseEvent.getClickCount() == 2) {
			// Call the Link of the STATS from StackExchange!!!
			int selectedQuestion = getSelectedQuestionIndex(mouseX, mouseY,
					qTitleX1, qTitleX2);
			if (selectedQuestion != -1) {
				QuestionData q = getQuestionByIndex(selectedQuestion);
				if (q != null) {
					link("http://stats.stackexchange.com/questions/"
							+ q.getId());
				} else {
					System.err.println("Unexistent Question Id!");
				}
			}
		}
	}

	public float getqFeatureX1() {
		return qFeatureX1;
	}

	public float getqFeatureX2() {
		return qFeatureX2;
	}

	public float getqTitleX1() {
		return qTitleX1;
	}

	public float getqTitleX2() {
		return qTitleX2;
	}

	public float getqAnswerX1() {
		return qAnswerX1;
	}

	public float getqAnswerX2() {
		return qAnswerX2;
	}

	private void resetNewYOrigin() {
		newYOrigin = 0;
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

		float firstQuestionY1 = questionRectYPadding;
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

		// READ THE QUESTION_ANSWER_FILE if it wasn't read yet...
		if (QeAData.getQuestionIdsToAnswers().size() == 0) {
			try {
				QeAData.readQuestionAnswersFile();
			} catch (IOException e) {
				System.err.println("Error reading the QUESTION_ANSWERS_FILE!");
			} catch (ParseException e) {
				System.err
						.println("Parsing error reading the QUESTION_ANSWERS_FILE!");
			}
		}

		// Remove everything...
		removeQuestionsAndCluster();

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
		selectedQuestionIndex = -1;

		// Re-Draw...
		loop();
	}

	private void drawScrollBar() {
		rectMode(PApplet.CORNER);
		fill(245);
		noStroke();
		rect(scrollBarX1, -newYOrigin, scrollBarX2 - scrollBarX1, -newYOrigin
				+ myHeight);

		float qTotalHeight = sortedQuestions.size() * qHeight
				+ (sortedQuestions.size() - 1) * questionRectYPadding;

		float barHeight = 40;
		float barY1 = Math.min(1, -newYOrigin
				/ ((qTotalHeight + qHeight / 2) - myHeight));

		if (qTotalHeight > myHeight) {
			fill(225);
			rect(scrollBarX1, -newYOrigin + barY1 * (myHeight - barHeight),
					scrollBarX2 - scrollBarX1 - 1, barHeight);
		}
	}

	private void drawNoCluster() {

		String noCluster = "No cluster selected...";
		noStroke();
		fill(220);
		rectMode(PApplet.CORNER);
		rect(10, 0, myWidth - 20, myHeight - 10, 50, 50);
		fill(255);
		textSize(myHeight / 20);
		textAlign(PApplet.CENTER, PApplet.CENTER);
		text(noCluster, myWidth / 2, myHeight / 2);

	}

	private void drawQuestions() {

		// First Rectangle boundaries (not 0 to avoid hidding the tooltips)
		float qY1 = 0;
		float qY2 = 0;

		strokeWeight((float) 1.5);

		maxAnswerScoreOfAll = getMaxAnswerScore();

		int i = -1;
		boolean highlightQuestion;
		for (QuestionData qData : sortedQuestions) {

			// Update the qY's
			qY1 = qY2 + questionRectYPadding;
			qY2 = qY1 + qHeight;
			i++;

			// Check if the Question is outside (before the draw area)
			if (qY1 + newYOrigin < 0 && qY2 + newYOrigin < 0) {
				// Performance improvement...
				continue;
			}
			// Check if the Question is outside (after the draw area)
			if (qY1 + newYOrigin > myHeight && qY2 + newYOrigin > myHeight) {
				// This is a HUGE performance improvement!!
				break;
			}

			highlightQuestion = (selectedQuestionIndex == i);

			// DRAW score rectangle
			drawQuestionFeatureToSort(qData, qFeatureX1, qY1, qFeatureX2, qY2,
					highlightQuestion);

			// DRAW title rectangle
			drawQuestionTitle(qData, qTitleX1, qY1, qTitleX2, qY2,
					highlightQuestion);

			// DRAW answers rectangle
			drawQuestionAnswers(qData, qAnswerX1, qY1, qAnswerX2, qY2,
					highlightQuestion);
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

	private void drawQuestionFeatureToSort(QuestionData qData, float x1,
			float y1, float x2, float y2, boolean highlightQuestion) {
		// Highlight or not the Stroke
		stroke(150, (highlightQuestion) ? 255 : 100);

		// Draw Rectangle
		fill(255);
		rectMode(PApplet.CORNERS);
		rect(x1, y1, x2, y2, qCornerRadius, qCornerRadius);

		textSize((y2 - y1) / (float) 5);

		// Draw value and name of feature
		DecimalFormat decimalForm = new DecimalFormat("#.##");
		double value = qData.getFeatureValueOfSortIndex();
		String postName = QuestionData.getFeaturePostNameOfSortIndex();
		if (value == 1) {
			postName = postName.substring(0, postName.length() - 2);
		}
		float y1Feature = y2 - (float) 2 * textAscent();

		fill(0);

		textAlign(PApplet.CENTER, PApplet.CENTER);
		text(postName, x1, y1Feature, x2, y2);

		// Draw number
		textSize(27);
		textLeading(1);
		// Change this... If the font changes..
		text(decimalForm.format(value), x1, y1 - 7, x2, y1Feature + 10);
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
		textAlign(PApplet.LEFT, PApplet.CENTER);

		// Adapt the text size to the width of the title
		float qTextSize;
		float textRectRatio = (textWidth(qData.getTitle()) * (float) 1.05)
				/ (x2 - x1 - 6);

		if (textRectRatio > 2) {
			qTextSize = (y2 - y1) / (float) (3.6 + textRectRatio - 1.5);
			textSize(qTextSize);
		} else {
			qTextSize = (y2 - y1) / (float) (3.6);
			textSize(qTextSize);
		}
		
		textLeading(qTextSize + 5);
		text(qData.getTitle(), x1 + 3, y1, x2 - 3, y2);

		if (highlightQuestion && mouseX >= qTitleX1 && mouseX <= qTitleX2) {
			// Tooltip with link
			drawToolTipQuestion(qData);
		}
	}

	private void drawToolTipQuestion(QuestionData qData) {

		String tooltip = "http://stats.stackexchange.com/questions/"
				+ qData.getId();

		float tooltipX = mouseX - textWidth(tooltip) / (float) 2.5;
		float tooltipY = mouseY - 15 - newYOrigin;

		fill(50, 100);
		strokeWeight((float) 1);
		rectMode(PApplet.CENTER);
		rect(tooltipX, tooltipY, textWidth(tooltip), 20, 5, 5);

		textSize(12);
		fill(255);
		textAlign(PApplet.CENTER, PApplet.CENTER);
		text(tooltip, tooltipX, tooltipY);
	}

	private void drawQuestionAnswers(QuestionData qData, float x1, float y1,
			float x2, float y2, boolean highlightQuestion) {
		fill(255);
		rectMode(PApplet.LEFT);
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
				+ Math.pow(mouseY - ballYCenter - newYOrigin, 2));
		float tooltipX, tooltipY, tooltipHeight = 35;
		String tooltip = "";

		if (dist <= ballRadius) {
			if (ans.isAccepted()) {
				tooltip += "ACCEPTED answer!\n";
				tooltipHeight += tooltipHeight / 2;
			}

			tooltip += "Score: " + ans.getScore() + ", Comments: "
					+ ans.getCommentsCount() + "\n" + ans.getCreationDate();

			tooltipX = ballXCenter - textWidth(tooltip) / 2;
			tooltipY = ballYCenter - rectHeight / 3;

			fill(50, 100);
			strokeWeight((float) 1);
			rectMode(PApplet.CENTER);
			rect(tooltipX, tooltipY, textWidth(tooltip) - 10, tooltipHeight, 5,
					5);
			fill(255);
			textSize(12);
			textLeading(15);
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