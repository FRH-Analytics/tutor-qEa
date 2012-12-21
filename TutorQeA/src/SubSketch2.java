import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import processing.core.PApplet;
import processing.core.PVector;
import util.CentroidData;
import util.QeAData;
import util.QuestionData;

public class SubSketch2 {

	private static final int X_AXIS = 0;
	private static final int Y_AXIS = 1;

	private float plotX1, plotY1;
	private float plotX2, plotY2;

	private float xMin, xMax, yMin, yMax;

	private int xAttributeIndex, yAttributeIndex;

	private float labelSize;
	private float xLabelYOrigin, yLabelXOrigin;

	private float legendX1, legendY1;
	private float legendX2, legendY2;
	private float notesX1, notesX2;
	private float legendPadding;
	private float legendPartitionSize;
	private float littleClusterSize;

	private float xInPixels, yInPixels, sizeInPixels;

	private static float valueDivisions = 5;
	private float valueSize;
	private float gridGrayColor;

	private int maxClusterNumber;
	private int mouseOverClusterId;
	private int clickedClusterId;

	private float maxPointSize;
	private float minPointSize;

	private String textNote;

	protected int myWidth;
	protected int myHeight;
	protected int myXOrigin;
	protected int myYOrigin;

	protected EmbeddedSketch mySketch;

	public SubSketch2(EmbeddedSketch parent, int xOrigin, int yOrigin,
			int width, int height) {
		mySketch = parent;
		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;
	}

	public void setup() {

		// X Corners of the plot
		plotX1 = myXOrigin + (myWidth * (float) 0.07);
		plotX2 = myXOrigin + (myWidth * (float) 0.54);

		// Legend
		legendPadding = myWidth * (float) 0.0075;
		legendX1 = myXOrigin + (myWidth * (float) 0.55);
		legendX2 = myXOrigin + (myWidth * (float) 0.69);

		// Notes
		notesX1 = myXOrigin + (myWidth * (float) 0.74);
		notesX2 = myXOrigin + (myWidth * (float) 0.99);

		textNote = "";

		// Plot
		plotY1 = myYOrigin + (myHeight * (float) 0.05);
		plotY2 = myYOrigin + (myHeight * (float) 0.83);

		// Label
		labelSize = myWidth / 57;
		xLabelYOrigin = myYOrigin + (myHeight * (float) 0.92);
		yLabelXOrigin = myXOrigin + (myWidth * (float) 0.00);

		// FIXED NUMBER OF CLUSTER
		maxClusterNumber = 6;

		// No Cluster selected
		mouseOverClusterId = -1;

		// No Cluster chosen
		clickedClusterId = -1;

		// Legend (starts in the top and goes to the bottom of the plot)
		legendY1 = plotY1;
		legendY2 = plotY2;
		legendPartitionSize = (legendY2 - legendY1) / maxClusterNumber;
		littleClusterSize = (legendX2 - legendX1) / 5;

		/*
		 * Values of the plot
		 */
		// Value Size
		valueSize = myWidth / 63;

		gridGrayColor = 235;

		/*
		 * Point Size
		 */
		maxPointSize = ((plotX2 - plotX1) + (plotY2 - plotY1) / 2)
				* (float) 0.15;
		minPointSize = ((plotX2 - plotX1) + (plotY2 - plotY1) / 2)
				* (float) 0.025;

	}

	public void draw() {

		if (ChartData.getSize() > 0) {
			drawAxisLabels();

			// Use thin, gray lines to draw the grid
			mySketch.stroke(gridGrayColor);
			mySketch.strokeWeight((float) 1);

			// Draw Values by axis
			drawXValues();
			drawYValues();

			// Legend
			drawLegend();

			// Points
			drawDataPoints();

			// Notes
			drawNotes();

			// Highlight Cluster (Hover query)
			clusterHighlights();

			// Draw Axis Label Tooltips
			drawTooltipAxis();

		} else {
			drawNoPlot();
		}
	}

	private void clusterHighlights() {
		if (clickedClusterId != -1) {
			highlightCluster(clickedClusterId);
		}
		if (mouseOverClusterId != -1) {
			highlightAndTooltipCluster(mouseOverClusterId);
		}
	}

	public int getClickedClusterId() {
		return clickedClusterId;
	}

	public void mousePressed() {
		if (isMouseOver() && mySketch.mouseButton == PApplet.LEFT) {
			changeLabel();
			clickedClusterId = mouseOverClusterId;

			if (mouseOverClusterId != -1) {
				MainTutorQeA.SKETCH_BOTTOM
						.updateQuestionsByCluster(mouseOverClusterId);
			}
		}
	}

	public void mouseMoved() {
		if (isMouseOver()) {
			mouseOverClusterId = getClusterInLegend(mySketch.mouseX,
					mySketch.mouseY);
			if (mouseOverClusterId == -1) {
				mouseOverClusterId = getClusterInPlot(mySketch.mouseX,
						mySketch.mouseY);
			}
		}
	}

	private boolean isMouseOver() {
		return (mySketch.mouseX >= myXOrigin
				&& mySketch.mouseX <= myXOrigin + myWidth
				&& mySketch.mouseY >= myYOrigin && mySketch.mouseY <= myYOrigin
				+ myHeight);
	}

	private void highlightAndTooltipCluster(int clusterId) {
		String tooltip;
		ChartItem clusterItem = ChartData.getItemById(clusterId);

		highlightCluster(clusterId);

		// Draw ToolTip
		mySketch.textSize(12); // Sufficiently far from the text() call so that
								// processing has time to set it before drawing

		tooltip = String.valueOf((int) clusterItem.getSize()) + " question(s)";

		mySketch.fill(50, 100);
		mySketch.noStroke();
		mySketch.rectMode(PApplet.CENTER);
		mySketch.rect(xInPixels + sizeInPixels / (float) 1.2, yInPixels
				- sizeInPixels / (float) 1.4, mySketch.textWidth(tooltip)
				* (float) 1.2, 20, 5, 5);
		mySketch.fill(255);
		mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		mySketch.text(tooltip, xInPixels + sizeInPixels / (float) 1.2,
				yInPixels - sizeInPixels / (float) 1.4);

		textNote = getClusterTextNote(clusterId);
	}

	private void highlightCluster(int clusterId) {
		ChartItem clusterItem = ChartData.getItemById(clusterId);
		mySketch.fill(clusterItem.getColor(ChartItem.RED),
				clusterItem.getColor(ChartItem.GREEN),
				clusterItem.getColor(ChartItem.BLUE));
		mySketch.stroke(gridGrayColor / (float) 1.5);
		mySketch.strokeWeight((float) 1.75);
		mySketch.ellipseMode(PApplet.CENTER);

		// Highlight LittleCluster (LEGEND)
		xInPixels = getLegendLittleClusterX();
		yInPixels = getLegendLittleClusterY(ChartData.getIndexById(clusterId));
		sizeInPixels = littleClusterSize;

		mySketch.ellipse(xInPixels, yInPixels, sizeInPixels, sizeInPixels);

		// Highlight Cluster (PLOT)
		xInPixels = getPointXInPixels(clusterItem.getPoint().x);
		yInPixels = getPointYInPixels(clusterItem.getPoint().y);
		sizeInPixels = getPointSizeInPixels(clusterItem.getSize());

		mySketch.ellipse(xInPixels, yInPixels, sizeInPixels, sizeInPixels);

		mySketch.noStroke();
	}

	private void drawTooltipAxis() {
		int isOverLabel = isOverWhichLabel(mySketch.mouseX, mySketch.mouseY);
		if (isOverLabel != -1) {
			String tooltip = "Click to change the\naxis attribute";

			mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
			mySketch.textSize(12); // Sufficiently far from the text() call so
									// that processing has time to set it before
									// drawing

			float tooltipX = mySketch.mouseX + 35;
			float tooltipY = mySketch.mouseY - 35;

			mySketch.fill(35, 100);
			mySketch.strokeWeight((float) 1);
			mySketch.rectMode(PApplet.CENTER);
			mySketch.rect(tooltipX, tooltipY, mySketch.textWidth(tooltip)
					* (float) 1.2, 40, 5, 5);

			mySketch.textLeading(15);
			mySketch.fill(255);
			mySketch.text(tooltip, tooltipX, tooltipY);

			// Draw note about the attribute
			if (isOverLabel == X_AXIS) {
				textNote = getAttributeTextNote(xAttributeIndex);
			} else {
				textNote = getAttributeTextNote(yAttributeIndex);
			}
		}
	}

	private void changeLabel() {
		int label = isOverWhichLabel(mySketch.mouseX, mySketch.mouseY);
		if (label != -1) {
			if (label == X_AXIS) {
				xAttributeIndex++;
				xAttributeIndex %= QuestionData.getFeatureNames().size();
			}
			if (label == Y_AXIS) {
				yAttributeIndex++;
				yAttributeIndex %= QuestionData.getFeatureNames().size();
			}

			updatePlot();
		}
	}

	private int isOverWhichLabel(int x, int y) {
		int over;

		String xAttributeName = QuestionData.getFeatureNames().get(
				xAttributeIndex);
		String yAttributeName = QuestionData.getFeatureNames().get(
				yAttributeIndex);

		if (x > (plotX1 + plotX2) / 2 - mySketch.textWidth(xAttributeName) / 2
				&& x < (plotX1 + plotX2) / 2
						+ mySketch.textWidth(xAttributeName) / 2
				&& y > (xLabelYOrigin - labelSize / 2)
				&& y < (xLabelYOrigin + labelSize / 2)) {
			over = X_AXIS;

		} else if (x > (yLabelXOrigin - labelSize / 2)
				&& x < (yLabelXOrigin + labelSize / 2)
				&& y > (plotY1 + plotY2) / 2
						- mySketch.textWidth(yAttributeName) / 2
				&& y < (plotY1 + plotY2) / 2
						+ mySketch.textWidth(yAttributeName) / 2) {
			over = Y_AXIS;

		} else {
			over = -1;
		}

		return over;
	}

	public void updatePlot() {
		// Removes the plot data
		ChartData.removeAllData();

		// Remove the clusters selection and click
		clickedClusterId = -1;
		mouseOverClusterId = -1;

		// Update the plot data
		Collection<CentroidData> centroids = QeAData.getCentroidDataList();
		for (CentroidData centroidData : centroids) {
			ChartData.addData(centroidData.getMeanByIndex(xAttributeIndex),
					centroidData.getMeanByIndex(yAttributeIndex),
					centroidData.getClusterSize(), centroidData.getClusterId());
		}

		// Update the size of the Axis
		if (ChartData.getSize() > 0) {
			// Update Max and Min plot
			float precisionDiff = (float) 0.0001;
			float diffX = (ChartData.maxX - ChartData.minX > precisionDiff) ? (ChartData.maxX - ChartData.minX)
					* (float) 0.1
					: 1;
			float diffY = (ChartData.maxY - ChartData.minY > precisionDiff) ? (ChartData.maxY - ChartData.minY)
					* (float) 0.1
					: 1;

			xMin = ChartData.minX;
			xMax = ChartData.maxX + diffX;
			yMin = ChartData.minY;
			yMax = ChartData.maxY + diffY;
		}
		// Re-Draw...
		mySketch.loop();
	}

	private int getClusterInPlot(int x, int y) {

		int clusterId = -1;
		float xInPixels, yInPixels, radiusInPixels;
		double dist, smallerRadius = Double.MAX_VALUE;
		ChartItem clusterItem;
		for (Integer id : ChartData.getAllIds()) {
			clusterItem = ChartData.getItemById(id);
			xInPixels = getPointXInPixels(clusterItem.getPoint().x);
			yInPixels = getPointYInPixels(clusterItem.getPoint().y);
			radiusInPixels = getPointSizeInPixels(clusterItem.getSize()) / 2;

			// Distance
			dist = Math.sqrt(Math.pow(x - xInPixels, 2)
					+ Math.pow(y - yInPixels, 2));

			if (dist <= radiusInPixels) {
				// Select the cluster with smaller radius that the mouse is over
				if (radiusInPixels < smallerRadius) {
					clusterId = id;
					smallerRadius = radiusInPixels;
				}
			}
		}

		return (clusterId);
	}

	private int getClusterInLegend(int x, int y) {
		int clusterId = -1, clusterIndex;

		if (x > legendX1
				&& x < legendX2
				&& y > legendY1
				&& y < legendY2
				&& (y - legendY1) <= (ChartData.getSize() * legendPartitionSize)) {
			clusterIndex = ((int) Math.ceil((y - legendY1)
					/ (double) legendPartitionSize)) - 1;
			clusterId = ChartData.getIdByIndex(clusterIndex);
		}

		return (clusterId);
	}

	private void drawNotes() {

		mySketch.fill(220);
		mySketch.rectMode(PApplet.CORNER);
		mySketch.rect(notesX1, myYOrigin + 15, (notesX2 - notesX1), myHeight
				- myYOrigin, 20, 20);

		String notesString = "NOTES";
		mySketch.fill(0);
		mySketch.textSize(myHeight / 15);
		mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		mySketch.text(notesString, (notesX1 + notesX2) / 2, myYOrigin + 25);

		// Draw the textNote string, that is changed elsewhere
		mySketch.fill(0);
		mySketch.textSize(myHeight / 20);
		mySketch.textLeading(myHeight / 12);
		mySketch.textAlign(PApplet.LEFT);
		mySketch.text(textNote, notesX1 + 8, myYOrigin + 45, notesX2 - notesX1
				- 10, myHeight - myYOrigin);
	}

	private void drawNoPlot() {
		mySketch.noStroke();
		mySketch.fill(220);
		mySketch.rectMode(PApplet.CORNER);
		mySketch.rect(myXOrigin + 15, myYOrigin + 15, myWidth - 30,
				myHeight - 30, 50, 50);
		mySketch.fill(255);

		String noTag = "No tag selected...";
		mySketch.textSize(myHeight / 15);
		mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		mySketch.text(noTag, myXOrigin + myWidth / 2, myYOrigin + myHeight / 2);
		mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
	}

	private void drawAxisLabels() {
		mySketch.fill(0);

		// X Label
		mySketch.textSize(labelSize);
		mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		mySketch.text(QuestionData.getFeatureNames().get(xAttributeIndex),
				(plotX1 + plotX2) / 2, xLabelYOrigin);

		// Y Label (with rotation)
		mySketch.textSize(labelSize);
		mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		mySketch.pushMatrix();
		mySketch.translate(yLabelXOrigin, (plotY1 + plotY2) / 2);
		mySketch.rotate(-PApplet.PI / 2);
		mySketch.text(QuestionData.getFeatureNames().get(yAttributeIndex), 0, 0);
		mySketch.popMatrix();
	}

	private void drawXValues() {
		mySketch.fill(0);
		mySketch.textSize(valueSize);
		mySketch.textAlign(PApplet.CENTER);

		float xSize = plotX2 - plotX1;
		float fixedYPlace = plotY2 + mySketch.textAscent();
		float nextXPlace = plotX1;

		DecimalFormat decimalForm = new DecimalFormat("#.##");
		float xValue = xMin;

		float xInterval = (xMax - xMin) / valueDivisions;

		for (int i = 0; i < valueDivisions + 1; i++) {
			mySketch.text(decimalForm.format(xValue), nextXPlace, fixedYPlace);
			mySketch.line(nextXPlace, plotY1, nextXPlace, plotY2);

			xValue += xInterval;
			nextXPlace += xSize / valueDivisions;
		}
	}

	private void drawYValues() {
		mySketch.fill(0);
		mySketch.textSize(valueSize);
		mySketch.textAlign(PApplet.RIGHT);

		float ySize = plotY2 - plotY1;
		float nextYPlace = plotY1;
		float fixedXPlace = plotX1 - 5;

		float yValue = yMax;
		float yInterval = (yMax - yMin) / valueDivisions;

		DecimalFormat decimalForm = new DecimalFormat("#.#");

		for (int i = 0; i < valueDivisions + 1; i++) {

			float textOffset = mySketch.textAscent() / 2; // PApplet.CENTER
															// vertically
			if (i == valueDivisions) {
				textOffset = 0; // Align by the bottom
			} else if (i == 0) {
				textOffset = mySketch.textAscent(); // Align by the top
			}
			mySketch.text(decimalForm.format(yValue), fixedXPlace, nextYPlace
					+ textOffset);
			mySketch.line(plotX1, nextYPlace, plotX2, nextYPlace);

			yValue -= yInterval;
			nextYPlace += ySize / valueDivisions;
		}
	}

	private void drawDataPoints() {
		mySketch.noStroke();

		mySketch.ellipseMode(PApplet.CENTER);

		float size, x, y;
		ChartItem clusterItem;
		ArrayList<Integer> clusterIds = ChartData.getAllIds();

		for (int i = 0; i < ChartData.getSize(); i++) {

			clusterItem = ChartData.getItemById(clusterIds.get(i));
			size = getPointSizeInPixels(clusterItem.getSize());
			x = getPointXInPixels(clusterItem.getPoint().x);
			y = getPointYInPixels(clusterItem.getPoint().y);

			mySketch.fill(clusterItem.getColor(ChartItem.RED),
					clusterItem.getColor(ChartItem.GREEN),
					clusterItem.getColor(ChartItem.BLUE),
					clusterItem.getColor(ChartItem.ALPHA));
			mySketch.ellipse(x, y, size, size);

		}
	}

	private float getPointXInPixels(float x) {
		return (PApplet.map(x, xMin, xMax, plotX1, plotX2));
	}

	private float getPointYInPixels(float y) {
		return (PApplet.map(y, yMin, yMax, plotY2, plotY1));
	}

	private float getPointSizeInPixels(float size) {
		float finalSize;
		if (ChartData.minSize == ChartData.maxSize) {
			// Exceptional case. The PApplet.map would return NaN...
			finalSize = PApplet.map(size, ChartData.minSize - 5,
					ChartData.maxSize + 5, minPointSize, maxPointSize);
		} else {
			finalSize = PApplet.map(size, ChartData.minSize, ChartData.maxSize,
					minPointSize, maxPointSize);
		}
		return finalSize;
	}

	private void drawLegend() {
		mySketch.noStroke();

		// Names
		mySketch.textAlign(PApplet.LEFT);
		mySketch.textSize(valueSize - 1);

		// Little Cluster
		mySketch.ellipseMode(PApplet.CENTER);
		float littleClusterX, littleClusterY;
		float textX, textY;

		// Helpful variables
		ArrayList<Integer> clusterIds = ChartData.getAllIds();
		ChartItem clusterItem;

		for (int i = 0; i < ChartData.getSize(); i++) {
			littleClusterX = getLegendLittleClusterX();
			littleClusterY = getLegendLittleClusterY(i);

			// mySketch.ellipse
			clusterItem = ChartData.getItemById(clusterIds.get(i));
			mySketch.fill(clusterItem.getColor(ChartItem.RED),
					clusterItem.getColor(ChartItem.GREEN),
					clusterItem.getColor(ChartItem.BLUE),
					clusterItem.getColor(ChartItem.ALPHA));
			mySketch.ellipse(littleClusterX, littleClusterY, littleClusterSize,
					littleClusterSize);

			// Name
			textX = legendX1 + littleClusterSize + (2 * legendPadding);
			textY = littleClusterY + (littleClusterSize / 4);
			mySketch.fill(0);
			mySketch.text(getClusterName(clusterIds.get(i)), textX, textY);

		}
	}

	public String getClusterName(int index) {
		String name = "...";
		switch (index) {
		case (1):
			name = "Advanced";
			break;
		case (2):
			name = "FAQ";
			break;
		case (3):
			name = "3 Shots";
			break;
		case (4):
			name = "Big 'n Fast";
			break;
		case (5):
			name = "High Debated";
			break;
		case (6):
			name = "Uninteresting";
			break;
		default:

		}
		return (name);
	}

	private String getClusterTextNote(int index) {
		String note = "...";
		switch (index) {
		case (1):
			note = "Advanced: Questions with few answers appearing to be difficult.";
			break;
		case (2):
			note = "FAQ: Frequently Asked Question!";
			break;
		case (3):
			note = "3 Shots: Questions with low score, little debated and no more than 3 answers!";
			break;
		case (4):
			note = "Big 'n Fast: Well voted question with big and quick answers.";
			break;
		case (5):
			note = "High Debated: The question started an interesting debate over the questioner and the answerers.";
			break;
		case (6):
			note = "Uninteresting: Badly voted question without answers on average.";
			break;
		default:

		}
		return (note);
	}

	// TODO: Refactor it. Hardcode in wrong place, It should go to the
	// QuestionData creation at the QeAData class
	private String getAttributeTextNote(int index) {
		String note = "...";
		switch (index) {
		case (0):
			note = "Score: The amount of votes a question received.";
			break;
		case (1):
			note = "Answer Count: The quantity of answers a question received.";
			break;
		case (2):
			note = "Debate: The amount of \"talk\" between the questioner and the answerers observing a question, its answers and all comments as a sequenced dialogue.";
			break;
		case (3):
			note = "Hotness: The ratio between the Score and the Hours elapsed until the first answer to appear. Or -1 if there isn't an answer.";
			break;
		default:

		}
		return (note);
	}

	private float getLegendLittleClusterX() {
		return (legendX1 + legendPadding + (littleClusterSize / 2));
	}

	private float getLegendLittleClusterY(int index) {
		return (legendY1 + legendPartitionSize * index + (littleClusterSize / 2));
	}
}

class ChartData {
	private static TreeMap<Integer, ChartItem> data = new TreeMap<Integer, ChartItem>();
	public static float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE,
			minSize = Float.MAX_VALUE, maxX = Float.MIN_VALUE,
			maxY = Float.MIN_VALUE, maxSize = Float.MIN_VALUE;

	public static void addData(float x, float y, float size, int id) {
		data.put(id, new ChartItem(new PVector(x, y), size, id));

		// Update MAX and MIN values
		minX = (x < minX) ? x : minX;
		minY = (y < minY) ? y : minY;
		minSize = (size < minSize) ? size : minSize;
		maxX = (x > maxX) ? x : maxX;
		maxY = (y > maxY) ? y : maxY;
		maxSize = (size > maxSize) ? size : maxSize;
	}

	public static void removeAllData() {
		data.clear();
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minSize = Float.MAX_VALUE;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxSize = Float.MIN_VALUE;
	}

	public static int getSize() {
		return data.size();
	}

	public static ChartItem getItemById(int id) {
		return data.get(new Integer(id));
	}

	public static int getIndexById(int id) {
		int index = -1;
		for (Integer itemId : data.keySet()) {
			index++;
			if (itemId == id)
				break;
		}
		return index;
	}

	public static int getIdByIndex(int index) {
		int id = -1;
		if (index >= 0 && index < ChartData.getSize()) {
			for (Integer itemId : data.keySet()) {
				if (index == 0) {
					id = itemId;
					break;
				}
				index--;
			}
		}
		return id;
	}

	public static ArrayList<Integer> getAllIds() {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (Integer itemId : data.keySet()) {
			ids.add(itemId);
		}
		return ids;
	}

}

class ChartItem implements Comparable<ChartItem> {
	public static final int RED = 0, GREEN = 1, BLUE = 2, ALPHA = 3;

	private static int alpha = 100;
	private static final int[][] RGBA_CATHEGORICAL_COLOURS = {
			{ 141, 211, 199, alpha }, { 190, 186, 218, alpha },
			{ 251, 128, 114, alpha }, { 128, 177, 211, alpha },
			{ 253, 180, 98, alpha }, { 179, 222, 105, alpha },
			{ 252, 205, 229, alpha }, { 217, 217, 217, alpha },
			{ 188, 128, 189, alpha }, { 204, 235, 197, alpha },
			{ 255, 255, 179, alpha }, { 255, 237, 111, alpha } };

	private PVector point;
	private float size;
	private int id;

	public ChartItem(PVector point, float size, int id) {
		this.point = point;
		this.size = size;
		this.id = id;
	}

	public PVector getPoint() {
		return point;
	}

	public float getSize() {
		return size;
	}

	public int getId() {
		return id;
	}

	public int getColor(int component) {
		return (RGBA_CATHEGORICAL_COLOURS[id - 1][component]);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChartItem other = (ChartItem) obj;
		if (id != other.id)
			return false;
		if (point == null) {
			if (other.point != null)
				return false;
		} else if (!point.equals(other.point))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	@Override
	public int compareTo(ChartItem o) {
		return this.getId() - o.getId();
	}

	@Override
	public String toString() {
		return "Cluster: " + this.point.x + " - " + this.point.y;
	}
}