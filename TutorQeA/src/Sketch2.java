import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import org.gicentre.utils.colour.ColourTable;

import processing.core.PApplet;
import processing.core.PVector;
import util.CentroidData;
import util.CompositeSketch;
import util.QeAData;

public class Sketch2 implements CompositeSketch {

	private float plotX1, plotY1;
	private float plotX2, plotY2;

	private float xMin, xMax, yMin, yMax;

	private float labelSize, yLabelXOrigin, xLabelYOrigin;

	private float titleSize, subtitleSize, titleYOrigin, subtitleYOrigin;

	private float legendX1, legendY1;
	private float legendX2, legendY2;
	private float legendPadding;
	private float legendPartitionSize;
	private float littleClusterSize;

	private float valueDivisions;
	private float valueSize;
	private float gridGrayColor;

	private int maxClusterNumber;
	private int selectedCluster;

	private float maxPointSize;
	private float minPointSize;

	private MainSketch pApplet;
	private int myWidth;
	private int myHeight;
	private int myXOrigin;
	private int myYOrigin;

	public Sketch2(MainSketch parent, int xOrigin, int yOrigin, int width,
			int height) {
		pApplet = parent;

		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;
	}

	public void setup() {
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
		labelSize = myWidth / 40;
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
		titleSize = Math.min(myWidth / 27, myHeight / 17);
		subtitleSize = Math.min(myWidth / 35, myHeight / 23);
		titleYOrigin = myYOrigin + (myHeight * (float) 0.075); // The bottom
		subtitleYOrigin = myYOrigin + (myHeight * (float) 0.125);

		// Plot
		plotY1 = myYOrigin + (myHeight * (float) 0.15);
		plotY2 = myYOrigin + (myHeight * (float) 0.88);

		// Label
		xLabelYOrigin = myYOrigin + (myHeight * (float) 0.99);// The bottom

		// FIXED NUMBER OF CLUSTER
		maxClusterNumber = 8;

		// No Cluster selected
		selectedCluster = -1;

		// Legend (starts in the top and goes to the bottom of the plot)
		legendY1 = plotY1;
		legendY2 = plotY2;
		legendPartitionSize = (legendY2 - legendY1) / maxClusterNumber;
		littleClusterSize = (legendX2 - legendX1) / 5;

		/*
		 * Values of the plot
		 */
		// FIXED number of divisions
		valueDivisions = 7;
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

		if (ChartData.getPoints().size() > 0) {
			drawSubtitle();
			drawAxisLabels();

			// Use thin, gray lines to draw the grid
			pApplet.stroke(gridGrayColor);
			pApplet.strokeWeight((float) 1);
			drawXValues();
			drawYValues();

			// Legend
			drawLegend();

			// Points
			drawDataPoints();

			// Highlight Cluster (Hover query)
			if (selectedCluster != -1) {
				highlightClusterAndTooltip(selectedCluster);
			}
		} else {
			drawNoPlot();
		}
	}

	public void mousePressed() {
		if (pApplet.mouseButton == PApplet.LEFT && mouseOverSketch()) {
			if (selectedCluster != -1) {
				pApplet.getSketch3().updateQuestionsByCluster(
						selectedCluster + 1);
			}
		}
	}

	public void mouseMoved() {
		if (mouseOverSketch()) {
			selectedCluster = getClusterInLegend(pApplet.mouseX, pApplet.mouseY);
			if (selectedCluster == -1) {
				selectedCluster = getClusterInPlot(pApplet.mouseX,
						pApplet.mouseY);
			}
		}
	}

	private void highlightClusterAndTooltip(int clusterIndex) {
		if (clusterIndex >= 0 && clusterIndex < maxClusterNumber) {
			float xInPixels, yInPixels, sizeInPixels, realSize;
String tooltip;

			pApplet.fill(ChartData.RGBA_COLOURS[clusterIndex][0],
					ChartData.RGBA_COLOURS[clusterIndex][1],
					ChartData.RGBA_COLOURS[clusterIndex][2]);
			pApplet.stroke(gridGrayColor / (float) 1.5);
			pApplet.strokeWeight((float) 1.75);
			pApplet.ellipseMode(PApplet.CENTER);

			// Highlight LittleCluster (LEGEND)
			xInPixels = getLegendLittleClusterX();
			yInPixels = getLegendLittleClusterY(clusterIndex);
			sizeInPixels = littleClusterSize;

			pApplet.ellipse(xInPixels, yInPixels, sizeInPixels, sizeInPixels);

			// Highlight Cluster (PLOT)
			xInPixels = getPointXInPixels(clusterIndex);
			yInPixels = getPointYInPixels(clusterIndex);
			sizeInPixels = getPointSizeInPixels(clusterIndex);

			pApplet.ellipse(xInPixels, yInPixels, sizeInPixels, sizeInPixels);

			// Draw ToolTip
			
			realSize = ChartData.getSizeArrayList().get(clusterIndex);
			tooltip = String.valueOf((int) realSize) + " question(s)";
			
			pApplet.fill(50, 100);
			pApplet.strokeWeight((float) 1);
			pApplet.rectMode(PApplet.CENTER);
			pApplet.rect(xInPixels + sizeInPixels / 2, yInPixels - sizeInPixels
					/ 2, pApplet.textWidth(tooltip) + 20, 20, 5, 5);
			pApplet.fill(255);
			pApplet.textSize(12);
			pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
			pApplet.text(tooltip,
					xInPixels + sizeInPixels / 2, yInPixels - sizeInPixels / 2);
		}
	}

	public void updatePlot() {
		ChartData.removeAllData();

		Enumeration<CentroidData> centroidEnum = QeAData.getCentroidDataList();
		CentroidData centroidTmp;
		while (centroidEnum.hasMoreElements()) {
			centroidTmp = centroidEnum.nextElement();

			ChartData.addData(centroidTmp.getMeanAnswerCount(),
					centroidTmp.getMeanScore(), centroidTmp.getClusterSize());
		}

		// Update the size of the Axis
		if (ChartData.getPoints().size() > 0) {
			// Update Max and Min plot
			xMin = ChartData.getMinX();
			xMax = ChartData.getMaxX();

			yMin = ChartData.getMinY();
			yMax = ChartData.getMaxY();

			// Normalize the axis
			xMin = xMin - xMin % 10;
			yMin = yMin - yMin % 10;

			while ((xMax - xMin) % valueDivisions != 0) {
				xMax++;
			}
			while ((yMax - yMin) % valueDivisions != 0) {
				yMax++;
			}
		}
	}

	// TODO: Create an AbstractClass with this method and the attributes that
	// are common to all sketches
	public boolean mouseOverSketch() {
		return (pApplet.mouseX > myXOrigin
				&& pApplet.mouseX < (myXOrigin + myWidth)
				&& pApplet.mouseY > myYOrigin && pApplet.mouseY < (myYOrigin + myHeight));
	}

	private int getClusterInPlot(int x, int y) {
		int clusterIndex = -1;

		float xInPixels, yInPixels, radiusInPixels;
		double dist, finalDist = Double.MAX_VALUE;

		for (int i = 0; i < ChartData.getPoints().size(); i++) {
			xInPixels = getPointXInPixels(i);
			yInPixels = getPointYInPixels(i);
			radiusInPixels = getPointSizeInPixels(i) / 2;

			// Distance
			dist = Math.sqrt(Math.pow(x - xInPixels, 2)
					+ Math.pow(y - yInPixels, 2));

			if (dist <= radiusInPixels) {
				// Select the smaller cluster that the mouse is over
				clusterIndex = (dist < finalDist) ? i : clusterIndex;
				break;
			}
		}

		return (clusterIndex);
	}

	private int getClusterInLegend(int x, int y) {
		int clusterNum = ChartData.getArrayDataIndex().length;
		int clusterIndex = -1;

		if (x > legendX1 && x < legendX2 && y > legendY1 && y < legendY2
				&& (y - legendY1) <= (clusterNum * legendPartitionSize)) {
			clusterIndex = (int) Math.ceil((y - legendY1)
					/ (double) legendPartitionSize);
			clusterIndex--;
		}

		return (clusterIndex);
	}

	private void drawNoPlot() {
		String noTag = "No tag selected...";
		pApplet.fill(150, 100);
		pApplet.strokeWeight((float) 2);
		pApplet.rectMode(PApplet.CENTER);
		pApplet.rect(myXOrigin + myWidth / 2, myYOrigin + myHeight / 2,
				pApplet.textWidth(noTag) + 30, 30, 5, 5);
		pApplet.fill(0);
		pApplet.textSize(titleSize - 3);
		pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
		pApplet.text(noTag, myXOrigin + myWidth / 2, myYOrigin + myHeight / 2);
		pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
	}

	private void drawTitle() {
		pApplet.fill(0);
		pApplet.textAlign(PApplet.CENTER);
		String title = "Question Clustering by Tag";
		pApplet.textSize(titleSize);
		pApplet.text(title, myXOrigin + myWidth / 2, titleYOrigin);
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

		pApplet.fill(0);
		pApplet.textAlign(PApplet.LEFT);
		pApplet.textSize(subtitleSize);
		pApplet.text(subtitle, plotX1, subtitleYOrigin);
	}

	private void drawAxisLabels() {
		pApplet.fill(0);
		pApplet.textSize(labelSize);

		// Space in pixels between lines (depends on the size of the text)
		pApplet.textLeading(labelSize * (float) 1.25);

		// X Label
		pApplet.textAlign(PApplet.CENTER, PApplet.BOTTOM);
		pApplet.text("Answer Count", (plotX1 + plotX2) / 2, xLabelYOrigin);

		// Y Label (with rotation)
		pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
		pApplet.pushMatrix();
		pApplet.translate(yLabelXOrigin, (plotY1 + plotY2) / 2);
		pApplet.rotate(-PApplet.PI / 2);
		pApplet.text("Question Score", 0, 0);
		pApplet.popMatrix();
	}

	private void drawXValues() {
		pApplet.fill(0);
		pApplet.textSize(valueSize);
		pApplet.textAlign(PApplet.CENTER);

		float xSize = plotX2 - plotX1;
		float fixedYPlace = plotY2 + pApplet.textAscent() + 5;
		float nextXPlace = plotX1;

		DecimalFormat decimalForm = new DecimalFormat("###.#");
		float xValue = xMin;

		float xInterval = (xMax - xMin) / valueDivisions;

		while (xValue <= xMax) {
			pApplet.text(decimalForm.format(xValue), nextXPlace, fixedYPlace);
			pApplet.line(nextXPlace, plotY1, nextXPlace, plotY2);

			xValue += xInterval;
			nextXPlace += xSize / valueDivisions;
		}
	}

	private void drawYValues() {
		pApplet.fill(0);
		pApplet.textSize(valueSize);
		pApplet.textAlign(PApplet.RIGHT);

		float ySize = plotY2 - plotY1;
		float nextYPlace = plotY1;
		float fixedXPlace = plotX1 - 10;

		float yValue = yMax;
		float yInterval = (yMax - yMin) / valueDivisions;

		DecimalFormat decimalForm = new DecimalFormat("###.#");

		while (yValue >= yMin) {

			float textOffset = pApplet.textAscent() / 2; // Center vertically
			if (yValue == yMin) {
				textOffset = 0; // Align by the bottom
			} else if (yValue == yMax) {
				textOffset = pApplet.textAscent(); // Align by the top
			}

			pApplet.text(decimalForm.format(yValue), fixedXPlace, nextYPlace
					+ textOffset);
			pApplet.line(plotX1, nextYPlace, plotX2, nextYPlace);

			yValue -= yInterval;
			nextYPlace += ySize / valueDivisions;
		}
	}

	private void drawDataPoints() {
		pApplet.noStroke();

		pApplet.ellipseMode(PApplet.CENTER);

		float size, x, y;
		for (int i = 0; i < ChartData.getPoints().size(); i++) {

			size = getPointSizeInPixels(i);
			x = getPointXInPixels(i);
			y = getPointYInPixels(i);

			pApplet.fill(ChartData.RGBA_COLOURS[i][0],
					ChartData.RGBA_COLOURS[i][1], ChartData.RGBA_COLOURS[i][2],
					ChartData.RGBA_COLOURS[i][3]);
			pApplet.ellipse(x, y, size, size);
		}
	}

	private float getPointXInPixels(int index) {
		return (PApplet.map(ChartData.getPoints().get(index).x, xMin, xMax,
				plotX1, plotX2));
	}

	private float getPointYInPixels(int index) {
		return (PApplet.map(ChartData.getPoints().get(index).y, yMin, yMax,
				plotY1, plotY2));
	}

	private float getPointSizeInPixels(int index) {
		float size = -1;
		if (ChartData.getMinSize() == ChartData.getMaxSize()) {
			// Exceptional case. The map would return NaN...
			size = PApplet.map(ChartData.getSizeArrayList().get(index),
					ChartData.getMinSize() - 5, ChartData.getMaxSize() + 5,
					minPointSize, maxPointSize);
		} else {
			size = PApplet.map(ChartData.getSizeArrayList().get(index),
					ChartData.getMinSize(), ChartData.getMaxSize(),
					minPointSize, maxPointSize);
		}
		return size;
	}

	private void drawLegend() {
		pApplet.noStroke();

		// Names
		pApplet.textAlign(PApplet.LEFT);
		pApplet.textSize(valueSize - 1);

		// Little Cluster
		pApplet.ellipseMode(PApplet.CENTER);
		float littleClusterX, littleClusterY;
		float textX, textY;

		// Helpful variables
		int clusterNum = ChartData.getArrayDataIndex().length;
		int[] clusterIndexes = ChartData.getArrayDataIndex();
		int clusterIndex;

		for (int i = 0; i < clusterNum; i++) {
			clusterIndex = Math.round(clusterIndexes[i]);

			littleClusterX = getLegendLittleClusterX();
			littleClusterY = getLegendLittleClusterY(i);

			// Ellipse
			pApplet.fill(ChartData.RGBA_COLOURS[i][0],
					ChartData.RGBA_COLOURS[i][1], ChartData.RGBA_COLOURS[i][2],
					ChartData.RGBA_COLOURS[i][3]);
			pApplet.ellipse(littleClusterX, littleClusterY, littleClusterSize,
					littleClusterSize);

			// Name
			textX = legendX1 + littleClusterSize + (2 * legendPadding);
			textY = littleClusterY + (littleClusterSize / 4);
			pApplet.fill(0);
			pApplet.text("Cluster " + clusterIndex, textX, textY);
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
	private static ArrayList<PVector> points = new ArrayList<PVector>();
	private static ArrayList<Float> sizes = new ArrayList<Float>();
	private static int alpha = 160;

	public static final int[][] RGBA_COLOURS = { { 141, 211, 199, alpha },
			{ 255, 255, 179, alpha }, { 190, 186, 218, alpha },
			{ 251, 128, 114, alpha }, { 128, 177, 211, alpha },
			{ 253, 180, 98, alpha }, { 179, 222, 105, alpha },
			{ 252, 205, 229, alpha }, { 217, 217, 217, alpha },
			{ 188, 128, 189, alpha }, { 204, 235, 197, alpha },
			{ 255, 237, 111, alpha } };

	/*
	 * Synchronized to avoid data corruption!
	 */
	public static void addData(float x, float y, float size) {
		points.add(new PVector(x, y));
		sizes.add(size);
	}

	/*
	 * Synchronized to avoid data corruption!
	 */
	public static void removeAllData() {
		points.clear();
		sizes.clear();
	}

	public static ArrayList<PVector> getPoints() {
		return points;
	}

	public static ArrayList<Float> getSizeArrayList() {
		return sizes;
	}

	public static float[] getArraySize() {
		float[] floatArray = new float[sizes.size()];

		for (int i = 0; i < floatArray.length; i++) {
			Float f = sizes.get(i);
			floatArray[i] = (f != null ? f : Float.NaN);
		}
		return floatArray;
	}

	public static int[] getArrayDataIndex() {
		int[] colourData = new int[getSizeArrayList().size()];
		for (int i = 0; i < colourData.length; i++) {
			colourData[i] = i + 1;
		}
		return colourData;
	}

	public static ColourTable getColourTable() {
		ColourTable c = new ColourTable();
		// Based on ColourTable.SET3_12
		for (int i = 0; i < RGBA_COLOURS.length; i++) {
			c.addDiscreteColourRule(i + 1, RGBA_COLOURS[i][0],
					RGBA_COLOURS[i][1], RGBA_COLOURS[i][2], RGBA_COLOURS[i][3]);
		}
		return (c);
	}

	// MAX and MIN values
	public static float getMinX() {
		float minX = Float.MAX_VALUE;
		for (PVector point : points) {
			minX = (point.x < minX) ? point.x : minX;
		}
		return minX;
	}

	public static float getMinY() {
		float minY = Float.MAX_VALUE;
		for (PVector point : points) {
			minY = (point.y < minY) ? point.y : minY;
		}
		return minY;
	}

	public static float getMaxX() {
		float maxX = Float.MIN_VALUE;
		for (PVector point : points) {
			maxX = (point.x > maxX) ? point.x : maxX;
		}
		return maxX;
	}

	public static float getMaxY() {
		float maxY = 0;
		for (PVector point : points) {
			maxY = (point.y > maxY) ? point.y : maxY;
		}
		return maxY;
	}

	public static float getMaxSize() {
		float maxSize = Float.MIN_VALUE;
		for (float s : sizes) {
			maxSize = (s > maxSize) ? s : maxSize;
		}
		return maxSize;
	}

	public static float getMinSize() {
		float minSize = Float.MAX_VALUE;
		for (float s : sizes) {
			minSize = (s < minSize) ? s : minSize;
		}
		return minSize;
	}
}