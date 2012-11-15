package multSketches;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import processing.core.PApplet;
import processing.core.PVector;
import util.CentroidData;
import util.QeAData;

public class SubSketch2 {

	private float plotX1, plotY1;
	private float plotX2, plotY2;

	private float xMin, xMax, yMin, yMax;

	private float labelSize, yLabelXOrigin, xLabelYOrigin;

	private float subtitleSize, subtitleYOrigin, titleYOrigin;

	private float legendX1, legendY1;
	private float legendX2, legendY2;
	private float legendPadding;
	private float legendPartitionSize;
	private float littleClusterSize;

	private static float valueDivisions = 7;
	private float valueSize;
	private float gridGrayColor;

	private int maxClusterNumber;
	private int selectedClusterId;

	private float maxPointSize;
	private float minPointSize;

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
		// size(myXOrigin + myWidth, myYOrigin + myHeight);
		mySketch.smooth();

		/*
		 * WIDTHs (based on myWidth)
		 * 
		 * 12% to the y label + y values
		 * 
		 * 70% to the plot
		 * 
		 * 18% to the legend
		 */

		// Y label
		labelSize = myWidth / 35;
		yLabelXOrigin = myXOrigin + (myWidth * (float) 0.02); // LEFT

		// X Corners of the plot
		plotX1 = myXOrigin + (myWidth * (float) 0.12);
		plotX2 = myXOrigin + (myWidth * (float) 0.81);

		// Legend
		legendPadding = myWidth * (float) 0.0075;
		legendX1 = myXOrigin + (myWidth * (float) 0.82);
		legendX2 = myXOrigin + (myWidth * (float) 0.99);

		/*
		 * HEIGHT
		 * 
		 * 7.5% -> title
		 * 
		 * 5% -> subtitle
		 * 
		 * 70% -> plot
		 * 
		 * 15% to the x label
		 */

		// Title
		subtitleSize = Math.min(myWidth / 35, myHeight / 23);
		titleYOrigin = myYOrigin + (myHeight * (float) 0.055); // The bottom
		subtitleYOrigin = myYOrigin + (myHeight * (float) 0.1);

		// Plot
		plotY1 = myYOrigin + (myHeight * (float) 0.12);
		plotY2 = myYOrigin + (myHeight * (float) 0.85);

		// Label
		xLabelYOrigin = myYOrigin + (myHeight * (float) 0.99);// The bottom

		// FIXED NUMBER OF CLUSTER
		maxClusterNumber = 8;

		// No Cluster selected
		selectedClusterId = -1;

		// Legend (starts in the top and goes to the bottom of the plot)
		legendY1 = plotY1;
		legendY2 = plotY2;
		legendPartitionSize = (legendY2 - legendY1) / maxClusterNumber;
		littleClusterSize = (legendX2 - legendX1) / 5;

		/*
		 * Values of the plot
		 */
		// Value Size
		valueSize = myWidth / 40;

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

		drawTitle();

		if (ChartData.getSize() > 0) {
			drawSubtitle();
			drawAxisLabels();

			// Use thin, gray lines to draw the grid
			mySketch.stroke(gridGrayColor);
			mySketch.strokeWeight((float) 1);
			drawXValues();
			drawYValues();

			// Legend
			drawLegend();

			// Points
			drawDataPoints();

			// Highlight Cluster (Hover query)
			highlightClusterAndTooltip();
		} else {
			drawNoPlot();
		}
	}

	public int getSelectedClusterId() {
		return selectedClusterId;
	}

	public boolean isMouseOver() {
		return (mySketch.mouseX >= myXOrigin
				&& mySketch.mouseX <= myXOrigin + myWidth
				&& mySketch.mouseY >= myYOrigin && mySketch.mouseY <= myYOrigin
				+ myHeight);
	}

	public void mousePressed() {
		if (isMouseOver() && mySketch.mouseButton == PApplet.LEFT) {
			// System.out.println("Event: Mouse LEFT Pressed");
			if (selectedClusterId != -1) {
				MainSketch.SKETCH_BOTTOM
						.updateQuestionsByCluster(selectedClusterId);
			}
		}
	}

	public void mouseMoved() {
		if (isMouseOver()) {
			// System.out.println("Event: Mouse Moved");
			selectedClusterId = getClusterInLegend(mySketch.mouseX,
					mySketch.mouseY);
			if (selectedClusterId == -1) {
				selectedClusterId = getClusterInPlot(mySketch.mouseX,
						mySketch.mouseY);
			}
		}
	}

	private void highlightClusterAndTooltip() {
		if (selectedClusterId != -1) {

			float xInPixels, yInPixels, sizeInPixels;
			String tooltip;
			int clusterIndex = ChartData.getIndexById(selectedClusterId);
			ChartItem clusterItem = ChartData.getItemById(selectedClusterId);

			mySketch.fill(clusterItem.getColor(ChartItem.RED),
					clusterItem.getColor(ChartItem.GREEN),
					clusterItem.getColor(ChartItem.BLUE));
			mySketch.stroke(gridGrayColor / (float) 1.5);
			mySketch.strokeWeight((float) 1.75);
			mySketch.ellipseMode(PApplet.CENTER);

			// Highlight LittleCluster (LEGEND)
			xInPixels = getLegendLittleClusterX();
			yInPixels = getLegendLittleClusterY(clusterIndex);
			sizeInPixels = littleClusterSize;

			mySketch.ellipse(xInPixels, yInPixels, sizeInPixels, sizeInPixels);

			// Highlight Cluster (PLOT)
			xInPixels = getPointXInPixels(clusterItem.getPoint().x);
			yInPixels = getPointYInPixels(clusterItem.getPoint().y);
			sizeInPixels = getPointSizeInPixels(clusterItem.getSize());

			mySketch.ellipse(xInPixels, yInPixels, sizeInPixels, sizeInPixels);

			// Draw ToolTip
			tooltip = String.valueOf((int) clusterItem.getSize())
					+ " question(s)";

			mySketch.fill(50, 100);
			mySketch.strokeWeight((float) 1);
			mySketch.rectMode(PApplet.CENTER);
			mySketch.rect(xInPixels + sizeInPixels / 2, yInPixels
					- sizeInPixels / 2, mySketch.textWidth(tooltip) + 20, 20,
					5, 5);
			mySketch.fill(255);
			mySketch.textSize(12);
			mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
			mySketch.text(tooltip, xInPixels + sizeInPixels / 2, yInPixels
					- sizeInPixels / 2);
		}
	}

	public void updatePlot() {
		// Removes the questions and cluster of the Sketch 3
		MainSketch.SKETCH_BOTTOM.removeQuestionsAndCluster();

		// Removes the plot data
		ChartData.removeAllData();

		// Update the plot data
		Collection<CentroidData> centroids = QeAData.getCentroidDataList();
		for (CentroidData centroidData : centroids) {
			ChartData.addData(centroidData.getMeanAnswerCount(),
					centroidData.getMeanScore(), centroidData.getClusterSize(),
					centroidData.getClusterId());
		}

		// Update the size of the Axis
		if (ChartData.getSize() > 0) {
			// Update Max and Min plot
			xMin = ChartData.minX;
			xMax = ChartData.maxX;

			yMin = ChartData.minY;
			yMax = ChartData.maxY;

			// Normalize the axis
			xMin = xMin - xMin % 10;
			yMin = yMin - yMin % 10;

			while ((xMax - xMin) % valueDivisions != 0) {
				xMax++;
			}
			while ((yMax - yMin) % valueDivisions != 0) {
				yMax++;
			}
		} else {
		}
		// System.out.println("Event: Plot Update!");
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

	private void drawNoPlot() {
		String noTag = "No question...";
		mySketch.fill(150, 100);
		mySketch.strokeWeight((float) 2);
		mySketch.rectMode(PApplet.CENTER);
		mySketch.rect(myXOrigin + myWidth / 2, myYOrigin + myHeight / 2,
				mySketch.textWidth(noTag) + 30, 30, 5, 5);
		mySketch.fill(0);
		mySketch.textSize(myHeight / 22);
		mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		mySketch.text(noTag, myXOrigin + myWidth / 2, myYOrigin + myHeight / 2);
		mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
	}

	private void drawTitle() {
		mySketch.fill(0);
		mySketch.textAlign(PApplet.CENTER);
		String title = "Question Clusters";
		mySketch.textSize(myHeight / 20);
		mySketch.text(title, myXOrigin + myWidth / 2, titleYOrigin);
	}

	private void drawSubtitle() {

		StringBuilder tagTitle = new StringBuilder("Tags: ");
		for (String tag : QeAData.getChosenTagNames()) {
			tagTitle.append(tag);
			tagTitle.append(", ");
		}
		String subtitle = tagTitle.toString();

		if (!tagTitle.toString().equals("Tags: ")) {
			subtitle = subtitle.substring(0, tagTitle.length() - 2);
		}

		mySketch.fill(0);
		mySketch.textAlign(PApplet.LEFT);
		mySketch.textSize(subtitleSize);
		mySketch.text(subtitle, plotX1, subtitleYOrigin);
	}

	private void drawAxisLabels() {
		mySketch.fill(0);
		mySketch.textSize(labelSize);

		// Space in pixels between lines (depends on the size of the
		// mySketch.text)
		mySketch.textLeading(labelSize * (float) 1.25);

		// X Label
		mySketch.textAlign(PApplet.CENTER, PApplet.BOTTOM);
		mySketch.text("Answer Count", (plotX1 + plotX2) / 2, xLabelYOrigin);

		// Y Label (with rotation)
		mySketch.textAlign(PApplet.CENTER, PApplet.CENTER);
		mySketch.pushMatrix();
		mySketch.translate(yLabelXOrigin, (plotY1 + plotY2) / 2);
		mySketch.rotate(-PApplet.PI / 2);
		mySketch.text("Question Score", 0, 0);
		mySketch.popMatrix();
	}

	private void drawXValues() {
		mySketch.fill(0);
		mySketch.textSize(valueSize);
		mySketch.textAlign(PApplet.CENTER);

		float xSize = plotX2 - plotX1;
		float fixedYPlace = plotY2 + mySketch.textAscent() + 5;
		float nextXPlace = plotX1;

		DecimalFormat decimalForm = new DecimalFormat("###.#");
		float xValue = xMin;

		float xInterval = (xMax - xMin) / valueDivisions;

		while (xValue <= xMax) {
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
		float fixedXPlace = plotX1 - 10;

		float yValue = yMax;
		float yInterval = (yMax - yMin) / valueDivisions;

		DecimalFormat decimalForm = new DecimalFormat("###.#");

		while (yValue >= yMin) {

			float textOffset = mySketch.textAscent() / 2; // PApplet.CENTER
															// vertically
			if (yValue == yMin) {
				textOffset = 0; // Align by the bottom
			} else if (yValue == yMax) {
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
		return (PApplet.map(y, yMin, yMax, plotY1, plotY2));
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
			mySketch.text("Cluster " + clusterIds.get(i), textX, textY);
		}
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

	private static final int alpha = 160;
	private static final int[][] RGBA_COLOURS = { { 141, 211, 199, alpha },
			{ 255, 255, 179, alpha }, { 190, 186, 218, alpha },
			{ 251, 128, 114, alpha }, { 128, 177, 211, alpha },
			{ 253, 180, 98, alpha }, { 179, 222, 105, alpha },
			{ 252, 205, 229, alpha }, { 217, 217, 217, alpha },
			{ 188, 128, 189, alpha }, { 204, 235, 197, alpha },
			{ 255, 237, 111, alpha } };

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
		return (RGBA_COLOURS[id - 1][component]);
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
}