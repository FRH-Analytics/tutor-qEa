import java.util.ArrayList;
import java.util.Enumeration;

import org.gicentre.utils.colour.ColourTable;
import org.gicentre.utils.stat.XYChart;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import util.CentroidData;
import util.QeAData;

public class Sketch2 {

	XYChart xyChart;

	int xyChartXOrigin;
	int xyChartYOrigin;
	int xyChartWidth;
	int xyChartHeight;
	int xyChartMaxPointSize;

	int legendXOrigin;
	int legendYOrigin;
	int legendWidth;
	int legendHeight;
	int legendPadding;
	int ellipseWidth;
	int ellipseHeight;

	int defaultFontSize;
	int defaultFontColor;

	int maxClusterNumber;

	MainSketch pApplet;
	int myWidth;
	int myHeight;
	int myXOrigin;
	int myYOrigin;

	public Sketch2(MainSketch parent, int xOrigin, int yOrigin, int width,
			int height) {
		pApplet = parent;

		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;
	}

	/**
	 * Initialises the sketch, loads data into the chart and customises its
	 * appearance.
	 */
	public void setup() {
		// Font
		defaultFontSize = 11;
		defaultFontColor = 120;
		PFont font = pApplet.createFont("Helvetica", defaultFontSize);
		pApplet.textFont(font);

		// Create CHART and SET attributes
		xyChart = new XYChart(pApplet);
		xyChart.setYFormat("####.###");
		xyChart.setXFormat("####.###");
		xyChart.setXAxisLabel("Answer Count");
		xyChart.setYAxisLabel("Question Score");

		xyChartXOrigin = myXOrigin + (myWidth / 40);
		xyChartYOrigin = myYOrigin + 50;
		xyChartWidth = myWidth - ((myWidth / 6) + 2 * xyChartXOrigin);
		xyChartHeight = myHeight - xyChartYOrigin;
		xyChartMaxPointSize = myWidth / 10;

		// Legend size
		legendXOrigin = xyChartXOrigin + (xyChartWidth) + (myWidth / 20);
		legendYOrigin = xyChartYOrigin + (xyChartHeight / 4);
		legendWidth = (myWidth / 8);
		legendHeight = (myWidth / 3);

		legendPadding = (myWidth / 100);
		ellipseWidth = (myWidth / 50);
		ellipseHeight = ellipseWidth;

		// Cluster fields
		maxClusterNumber = 8;

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
	}

	public void draw() {
		pApplet.background(255);

		// Rebuild the chart
		prepareXYChart();

		pApplet.textSize(defaultFontSize);
		xyChart.draw(xyChartXOrigin, xyChartYOrigin, xyChartWidth,
				xyChartHeight);

		// Draw chart legend
		drawXYChartLegend();

		// TODO: enable the noLoop() before deploy the application. This noLoop
		// will improve the app performance.
		// pApplet.noLoop();
	}

	public void mousePressed() {
		if (pApplet.mouseButton == PApplet.LEFT) {
			int selectedCluster = getClusterSelectedInLegend(pApplet.mouseX,
					pApplet.mouseY);
			if (selectedCluster != -1) {
				pApplet.getSketch3().updateQuestions(
						QeAData.getQuestionIdsByCluster(selectedCluster));
//				System.out.println("Cluster selected: " + selectedCluster);
			}

		}
	}

	private void prepareXYChart() {
		if (!ChartData.getPoints().isEmpty()) {
			xyChart.setData(ChartData.getPoints());

			// Axis formatting and labels.
			xyChart.showXAxis(true);
			xyChart.showYAxis(true);

			// Symbol colours
			xyChart.setPointSize(ChartData.getSizeArray(), xyChartMaxPointSize);
			// 12 different colors
			xyChart.setPointColour(ChartData.getArrayDataIndex(),
					ChartData.getColourTable());

			// Draw a title and a subtitle over the top of the chart.
			// Subtitle with the tag names
			StringBuilder tagTitle = new StringBuilder("Tags: ");
			for (String tag : QeAData.getChosenTagNames()) {
				tagTitle.append(tag);
				tagTitle.append(", ");
			}
			String title = "Question Clustering - Centroid Visualization";
			String subtitle = tagTitle.toString().substring(0,
					tagTitle.length() - 2);

			// Draw a title over the top of the chart.
			pApplet.fill(defaultFontColor);

			// Cool and adaptable sizes
			int titleSize = myWidth / 40;
			int subtitleSize = myWidth / 50;
			int titlePadding = (myWidth > 600) ? myWidth / 20 : 30;

			pApplet.textSize(titleSize);
			pApplet.text(title, xyChartXOrigin + titlePadding, xyChartYOrigin
					- titleSize);
			pApplet.textSize(subtitleSize);
			pApplet.text(subtitle, xyChartXOrigin + titlePadding,
					xyChartYOrigin);
		}
	}

	private void drawXYChartLegend() {
		pApplet.fill(255);
		pApplet.noStroke();
		pApplet.rect(legendXOrigin, legendYOrigin, legendWidth, legendHeight);

		int clusterIndex;
		int clusterNum = ChartData.getArrayDataIndex().length;
		int legendPartitionSize = legendHeight / maxClusterNumber;
		pApplet.ellipseMode(PApplet.CORNER);

		for (int i = 0; i < clusterNum; i++) {
			clusterIndex = Math.round(ChartData.getArrayDataIndex()[i]);

			pApplet.fill(ChartData.RGBA_COLOURS[i][0],
					ChartData.RGBA_COLOURS[i][1], ChartData.RGBA_COLOURS[i][2],
					ChartData.RGBA_COLOURS[i][3]);
			pApplet.ellipse(legendXOrigin + legendPadding, legendYOrigin
					+ legendPadding
					+ (legendHeight / 2 - clusterNum / 2 * legendPartitionSize)
					+ legendPartitionSize * i, ellipseWidth, ellipseHeight);

			int legendSize = myWidth / 60;

			pApplet.textSize(legendSize);
			pApplet.fill(defaultFontColor);
			pApplet.text("Cluster " + clusterIndex, legendXOrigin
					+ ellipseWidth + 2 * legendPadding, legendYOrigin
					+ legendPadding
					+ (legendHeight / 2 - clusterNum / 2 * legendPartitionSize)
					+ legendSize + legendPartitionSize * i);
		}
	}

	private int getClusterSelectedInLegend(int x, int y) {
		int clusterNum = ChartData.getArrayDataIndex().length;
		int legendPartitionSize = legendHeight / maxClusterNumber;

		int clusterIndex = -1;
		int xInside = x - (legendXOrigin + legendPadding);
		int yInside = y
				- (legendYOrigin + legendPadding + (legendHeight / 2 - clusterNum
						/ 2 * legendPartitionSize));

		if (xInside > 0 && yInside > 0
				&& yInside <= (clusterNum * legendPartitionSize)) {
			clusterIndex = (int) Math.ceil(yInside
					/ (double) legendPartitionSize);
		}

		return (clusterIndex);
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
		pApplet.loop();
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

	public static float[] getSizeArray() {
		float[] floatArray = new float[sizes.size()];

		for (int i = 0; i < floatArray.length; i++) {
			Float f = sizes.get(i);
			floatArray[i] = (f != null ? f : Float.NaN);
		}
		return floatArray;
	}

	public static float[] getArrayDataIndex() {
		float[] colourData = new float[getSizeArrayList().size()];
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
}