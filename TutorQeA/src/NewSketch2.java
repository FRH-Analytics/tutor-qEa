import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import org.gicentre.utils.colour.ColourTable;

import processing.core.PApplet;
import processing.core.PVector;
import util.CentroidData;
import util.QeAData;

public class NewSketch2 implements CompositeSketch {

	private float plotX1, plotY1;
	private float plotX2, plotY2;

	private float xMin, xMax, yMin, yMax;

	private float labelSize, yLabelXOrigin, xLabelYOrigin;

	private float titleSize, subtitleSize, titleYOrigin, subtitleYOrigin;

	private float legendX1, legendY1;
	private float legendX2, legendY2;
	private float legendPadding;

	private float valueDivisions;
	private float valueSize;

	private int maxClusterNumber;

	private MainSketch pApplet;
	private int myWidth;
	private int myHeight;
	private int myXOrigin;
	private int myYOrigin;

	public NewSketch2(MainSketch parent, int xOrigin, int yOrigin, int width,
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
		plotX2 = myXOrigin + (myWidth * (float) 0.82);

		// Legend
		legendPadding = myWidth * (float) 0.02;
		legendX1 = myXOrigin + (myWidth * (float) 0.85);
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
		titleSize = myWidth / 26;
		subtitleSize = myWidth / 35;
		titleYOrigin = myYOrigin + (myHeight * (float) 0.075); // The bottom
		subtitleYOrigin = myYOrigin + (myHeight * (float) 0.125);

		// Plot
		plotY1 = myYOrigin + (myHeight * (float) 0.15);
		plotY2 = myYOrigin + (myHeight * (float) 0.88);

		// Label
		xLabelYOrigin = myYOrigin + (myHeight * (float) 0.99);// The bottom

		// Legend
		float meanPlotY = ((plotY1 + plotY2) / 2);
		legendY1 = (plotY1 + meanPlotY) / 2;
		legendY2 = (plotY2 + meanPlotY) / 2;

		// FIXED NUMBER OF CLUSTER
		maxClusterNumber = 8;

		/*
		 * Values of the plot
		 */
		// FIXED number of divisions
		valueDivisions = 7;
		// Value Size
		valueSize = myWidth / 40;
	}

	public void draw() {
		// TODO: delete this...
		pApplet.fill(255);
		pApplet.stroke(100);
		pApplet.rect(myXOrigin, myYOrigin, myWidth, myHeight);

		// Show the plot area as a white box
		// pApplet.fill(255);
		// pApplet.rectMode(PApplet.CORNERS);
		// noStroke();
		// pApplet.rect(plotX1, plotY1, plotX2, plotY2);

		drawTitle();
		drawAxisLabels();

		// Use thin, gray lines to draw the grid
		pApplet.stroke(235);
		pApplet.strokeWeight((float) 1.5);
		drawXValues();
		drawYValues();

		// Legend
		drawLegend();

		// Draw points
		drawDataPoints();

		// Stop looping
		pApplet.noLoop();
	}

	public void mousePressed() {
		if (pApplet.mouseButton == PApplet.LEFT) {
			int selectedCluster = getClusterSelectedInLegend(pApplet.mouseX,
					pApplet.mouseY);
			if (selectedCluster != -1) {
				// pApplet.getSketch3().updateQuestions(
				// QeAData.getQuestionIdsByCluster(selectedCluster));
				System.out.println("Cluster selected: " + selectedCluster);
			}

		}
	}

	public void updateChartData() {
		ChartData.removeAllData();

		Enumeration<CentroidData> centroidEnum = QeAData.getCentroidDataList();
		CentroidData centroidTmp;
		while (centroidEnum.hasMoreElements()) {
			centroidTmp = centroidEnum.nextElement();

			ChartData.addData(centroidTmp.getMeanAnswerCount(),
					centroidTmp.getMeanScore(), centroidTmp.getClusterSize());
		}

		updateMaxMinXYPlot();

		// Reenable loops
		pApplet.loop();
	}

	private void updateMaxMinXYPlot() {
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

	private int getClusterSelectedInLegend(int x, int y) {
		int clusterNum = ChartData.getArrayDataIndex().length;
		float legendPartitionSize = legendY2 / maxClusterNumber;

		int clusterIndex = -1;
		float xInside = x - (legendX1 + legendPadding);
		float yInside = y
				- (legendY1 + legendPadding + (legendY2 / 2 - clusterNum / 2
						* legendPartitionSize));

		if (xInside > 0 && yInside > 0
				&& yInside <= (clusterNum * legendPartitionSize)) {
			clusterIndex = (int) Math.ceil(yInside
					/ (double) legendPartitionSize);
		}

		return (clusterIndex);
	}

	private void drawTitle() {

		StringBuilder tagTitle = new StringBuilder("Tags: ");
		for (String tag : QeAData.getChosenTagNames()) {
			tagTitle.append(tag);
			tagTitle.append(", ");
		}
		String title = "Question Clustering - centroid visualization";
		String subtitle = tagTitle.toString().substring(0,
				tagTitle.length() - 2);

		pApplet.fill(0);
		pApplet.textAlign(PApplet.LEFT);

		pApplet.textSize(titleSize);
		pApplet.text(title, plotX1, titleYOrigin);

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
		float fixedYPlace = plotY2 + pApplet.textAscent() + 10;
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
		PVector point;

		pApplet.ellipseMode(PApplet.CENTER);

		float maxPointSizeEver = ((plotX2 - plotX1) + (plotY2 - plotY1) / 2)
				* (float) 0.15;
		float minPointSizeEver = ((plotX2 - plotX1) + (plotY2 - plotY1) / 2)
				* (float) 0.025;

		float size, x, y;
		for (int i = 0; i < ChartData.getPoints().size(); i++) {

			point = ChartData.getPoints().get(i);

			size = PApplet.map(ChartData.getSizeArrayList().get(i),
					ChartData.getMinSize(), ChartData.getMaxSize(),
					minPointSizeEver, maxPointSizeEver);
			x = PApplet.map(point.x, xMin, xMax, plotX1, plotX2);
			y = PApplet.map(point.y, yMin, yMax, plotY2, plotY1);

			pApplet.fill(ChartData.RGBA_COLOURS[i][0],
					ChartData.RGBA_COLOURS[i][1], ChartData.RGBA_COLOURS[i][2],
					ChartData.RGBA_COLOURS[i][3]);
			pApplet.ellipse(x, y, size, size);
		}
	}

	private void drawLegend() {

		// TODO: Finish this method!
		pApplet.noStroke();

		// Names
		pApplet.textAlign(PApplet.LEFT);
		pApplet.textSize(valueSize);

		int clusterNum = ChartData.getArrayDataIndex().length;
		float legendPartitionSize = (legendY2 - legendY1) / maxClusterNumber;
		int[] clusterIndexes = ChartData.getArrayDataIndex();
		int clusterIndex;

		// Ellipses
		pApplet.ellipseMode(PApplet.CENTER);
		float ellipseSize = (legendX2 - legendX1) / 5;
		float ellipseY;

		for (int i = 0; i < clusterNum; i++) {
			clusterIndex = Math.round(clusterIndexes[i]);

			ellipseY = legendY1 + (legendY2 - legendY1) / 2
					- (clusterNum / (2 * legendPartitionSize))
					+ legendPartitionSize * i;

			// Ellipse
			pApplet.fill(ChartData.RGBA_COLOURS[i][0],
					ChartData.RGBA_COLOURS[i][1], ChartData.RGBA_COLOURS[i][2],
					ChartData.RGBA_COLOURS[i][3]);
			pApplet.ellipse(legendX1, ellipseY, ellipseSize, ellipseSize);

			// Name
			pApplet.fill(0);
			pApplet.text("Cluster " + clusterIndex, legendX1 + ellipseSize
					+ (2 * legendPadding), ellipseY);
		}
	}
}

class ChartData {
	private static ArrayList<PVector> points = new ArrayList<PVector>();
	private static ArrayList<Float> sizes = new ArrayList<Float>();
	private static int alpha = 150;

	public static final int[][] RGBA_COLOURS = { { 141, 211, 199, alpha },
			{ 255, 255, 179, alpha }, { 190, 186, 218, alpha },
			{ 251, 128, 114, alpha }, { 128, 177, 211, alpha },
			{ 253, 180, 98, alpha }, { 179, 222, 105, alpha },
			{ 252, 205, 229, alpha }, { 217, 217, 217, alpha },
			{ 188, 128, 189, alpha }, { 204, 235, 197, alpha },
			{ 255, 237, 111, alpha } };

	public static void addData(float x, float y, float size) {
		points.add(new PVector(x, y));
		sizes.add(size);
	}

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