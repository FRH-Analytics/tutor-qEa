

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.gicentre.utils.colour.ColourTable;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import util.CentroidData;
import util.QeAData;

public class NewSketch2 extends PApplet {
	private static final long serialVersionUID = 1L;

	float yMin, yMax;

	float plotX1, plotY1;
	float plotX2, plotY2;
	float labelX, labelY;

	float xMin, xMax;
	float[] xValues;

	float xInterval = 1;
	float yInterval = 1;

	PFont plotFont;

	float legendXOrigin;
	float legendYOrigin;
	float legendWidth;
	float legendHeight;
	float legendPadding;
	float ellipseSize;

	int maxClusterNumber;

	public void setup() {
		size(720, 405);

		// Corners of the plotted time series
		plotX1 = 120;
		plotX2 = width - 120;
		labelX = 50;
		plotY1 = 60;
		plotY2 = height - 50;
		labelY = height - 10;

		plotFont = createFont("SansSerif", 20);
		textFont(plotFont);

		legendXOrigin = plotX2 + 10;
		legendYOrigin = plotY1 + 20;
		legendPadding = (width / 100);

		legendWidth = width - legendXOrigin - legendPadding;
		legendHeight = (width / 3);

		ellipseSize = (width / 50);

		maxClusterNumber = 8;

		smooth();

		// / TESTING...
		try {
			QeAData.readPostTagsFile();
			QeAData.readQuestionsDataFile();
			QeAData.readQuestionAnswersFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// TODO: Delete all this!
		// Testing....
		ArrayList<Integer> tagList = new ArrayList<Integer>();
		// tagList.add(41);
		// tagList.add(111);
		// tagList.add(264);
		// tagList.add(294);
		tagList.add(528);
		ArrayList<String> tagNameList = new ArrayList<String>();
		// tagNameList.add("r");
		// tagNameList.add("regression");
		// tagNameList.add("logistic-regression");
		// tagNameList.add("roc");
		tagNameList.add("hmm");
		QeAData.setTagList(tagList, tagNameList);

		// Reset the data
		updateChartData();

		// years = int(data.getRowNames());
		xMin = ChartData.getMinX();
		xMax = ChartData.getMaxX();

		yMin = ChartData.getMinY();
		yMax = ChartData.getMaxX();
	}

	public void draw() {
		background(224);

		// Show the plot area as a white box
		fill(255);
		rectMode(CORNERS);
		// noStroke();
		rect(plotX1, plotY1, plotX2, plotY2);

		drawTitle();
		drawAxisLabels();

		// Use thin, gray lines to draw the grid
		stroke(235);
		strokeWeight((float) 1.5);
		drawXValues();
		drawYValues();

		// Legend
		drawLegend();

		// Draw points
		drawDataPoints();
	}

	public void mousePressed() {
		if (mouseButton == PApplet.LEFT) {
			int selectedCluster = getClusterSelectedInLegend(mouseX,
					mouseY);
			if (selectedCluster != -1) {
//				pApplet.getSketch3().updateQuestions(
//						QeAData.getQuestionIdsByCluster(selectedCluster));
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
		loop();
	}
	
	private int getClusterSelectedInLegend(int x, int y) {
		int clusterNum = ChartData.getArrayDataIndex().length;
		float legendPartitionSize = legendHeight / maxClusterNumber;

		int clusterIndex = -1;
		float xInside = x - (legendXOrigin + legendPadding);
		float yInside = y
				- (legendYOrigin + legendPadding + (legendHeight / 2 - clusterNum
						/ 2 * legendPartitionSize));

		if (xInside > 0 && yInside > 0
				&& yInside <= (clusterNum * legendPartitionSize)) {
			clusterIndex = (int) Math.ceil(yInside
					/ (double) legendPartitionSize);
		}

		return (clusterIndex);
	}


	void drawTitle() {
		fill(0);
		textSize(20);
		textAlign(LEFT);

		StringBuilder tagTitle = new StringBuilder("Tags: ");
		for (String tag : QeAData.getChosenTagNames()) {
			tagTitle.append(tag);
			tagTitle.append(", ");
		}
		String title = "Question Clustering - Centroid Visualization";
		String subtitle = tagTitle.toString().substring(0,
				tagTitle.length() - 2);

		text(title, plotX1, plotY1 - 30);

		textSize(15);
		text(subtitle, plotX1, plotY1 - 10);
	}

	void drawAxisLabels() {
		fill(0);
		textSize(13);
		textLeading(15);

		textAlign(CENTER, CENTER);
		text("Question\nScore", labelX, (plotY1 + plotY2) / 2);
		textAlign(CENTER);
		text("Answer Count", (plotX1 + plotX2) / 2, labelY);
	}

	void drawXValues() {
		fill(0);
		textSize(12);
		textAlign(CENTER);

		float xSize = plotX2 - plotX1;
		float xValue = xMin;
		float nextXPlace = plotX1;
		float fixedYPlace = plotY2 + textAscent() + 10;
		while (xValue <= xMax) {
			text(String.valueOf(xValue), nextXPlace, fixedYPlace);
			line(nextXPlace, plotY1, nextXPlace, plotY2); // Draw major tick

			xValue += xInterval;
			nextXPlace += Math.round(xSize / (xMax - xMin));
		}
	}

	void drawYValues() {
		fill(0);
		textSize(12);
		textAlign(RIGHT);

		float ySize = plotY2 - plotY1;
		float yValue = yMax;
		float nextYPlace = plotY1;
		float fixedXPlace = plotX1 - 10;
		while (yValue >= xMin) {

			float textOffset = textAscent() / 2; // Center vertically
			if (yValue == yMin) {
				textOffset = 0; // Align by the bottom
			} else if (yValue == yMax) {
				textOffset = textAscent(); // Align by the top
			}

			text(String.valueOf(yValue), fixedXPlace, nextYPlace + textOffset);
			line(plotX1, nextYPlace, plotX2, nextYPlace);

			yValue -= yInterval;
			nextYPlace += Math.round(ySize / (yMax - yMin));
		}
	}

	void drawDataPoints() {
		noStroke();
		PVector point;

		ellipseMode(PApplet.CENTER);

		float maxPointSizeEver = 80;
		float size;
		for (int i = 0; i < ChartData.getPoints().size(); i++) {
			point = ChartData.getPoints().get(i);
			size = map(ChartData.getSizeArrayList().get(i),
					ChartData.getMinSize(), ChartData.getMaxSize(), 1,
					maxPointSizeEver);

			float x = map(point.x, xMin, xMax, plotX1, plotX2);
			float y = map(point.y, yMin, yMax, plotY2, plotY1);
			fill(ChartData.RGBA_COLOURS[i][0], ChartData.RGBA_COLOURS[i][1],
					ChartData.RGBA_COLOURS[i][2], ChartData.RGBA_COLOURS[i][3]);
			ellipse(x, y, size, size);
		}
	}

	private void drawLegend() {
		fill(255);
		rectMode(PApplet.CORNER);
		rect(legendXOrigin, legendYOrigin, legendWidth, legendHeight);
		
		noStroke();
		int clusterIndex;
		int clusterNum = ChartData.getArrayDataIndex().length;
		float legendPartitionSize = legendHeight / maxClusterNumber;
		int[] clusterIndexes = ChartData.getArrayDataIndex();

		ellipseMode(PApplet.CORNER);
		for (int i = 0; i < clusterNum; i++) {
			clusterIndex = Math.round(clusterIndexes[i]);

			fill(ChartData.RGBA_COLOURS[i][0], ChartData.RGBA_COLOURS[i][1],
					ChartData.RGBA_COLOURS[i][2], ChartData.RGBA_COLOURS[i][3]);
			ellipse(legendXOrigin + legendPadding, legendYOrigin
					+ legendPadding
					+ (legendHeight / 2 - clusterNum / 2 * legendPartitionSize)
					+ legendPartitionSize * i, ellipseSize, ellipseSize);

			float legendSize = width / 60;

			textSize(legendSize);
			fill(0);
			text("Cluster " + clusterIndex, legendXOrigin + ellipseSize + (2
					* legendPadding) + 60, legendYOrigin + legendPadding
					+ (legendHeight / 2 - clusterNum / 2 * legendPartitionSize)
					+ legendSize + legendPartitionSize * i);
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