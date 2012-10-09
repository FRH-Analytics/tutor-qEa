import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.gicentre.utils.colour.ColourTable;
import org.gicentre.utils.multisketch.EmbeddedSketch;
import org.gicentre.utils.stat.XYChart;

import processing.core.PFont;
import processing.core.PVector;
import util.CentroidData;
import util.QeAData;

public class Sketch2 extends EmbeddedSketch {

	private static final long serialVersionUID = 1L;
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

	/**
	 * Initialises the sketch, loads data into the chart and customises its
	 * appearance.
	 */
	public void setup() {
		size(800, 500);
		smooth();

		// Font
		defaultFontSize = 11;
		defaultFontColor = 120;
		PFont font = createFont("Helvetica", defaultFontSize);
		textFont(font);

		// Create CHART and SET attributes
		xyChart = new XYChart(this);
		xyChart.setYFormat("#####");
		xyChart.setXFormat("#####");
		xyChart.setXAxisLabel("Answer Count");
		xyChart.setYAxisLabel("Question Score");

		xyChartXOrigin = 15;
		xyChartYOrigin = 50;
		xyChartWidth = width - (100 + 2 * xyChartXOrigin);
		xyChartHeight = height - xyChartYOrigin;
		xyChartMaxPointSize = 80;

		// Legend size
		legendXOrigin = xyChartXOrigin + (xyChartWidth);
		legendYOrigin = xyChartYOrigin + (xyChartHeight / 4);
		legendWidth = 100;
		legendHeight = 240;

		legendPadding = 8;
		ellipseWidth = 15;
		ellipseHeight = ellipseWidth;

		// Cluster fields
		maxClusterNumber = 8;

		// Read files with the Important DATA (raw file path)
		// TODO: After all, put this call in the Main
		try {
			QeAData.readPostTagsFile();
			QeAData.readQuestionFeaturesFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// TODO: Delete all this!
		// Testing....
		ArrayList<Integer> tagList = new ArrayList<Integer>();
		tagList.add(41);
		tagList.add(111);
		tagList.add(264);
		// tagList.add(294);
		// tagList.add(528);
		ArrayList<String> tagNameList = new ArrayList<String>();
		tagNameList.add("r");
		tagNameList.add("regression");
		tagNameList.add("logistic-regression");
		// tagNameList.add("roc");
		// tagNameList.add("hmm");
		QeAData.setTagList(tagList, tagNameList);

		// Reset the data
		updateChartData();
	}

	public void draw() {
		background(255);

		// Rebuild the chart
		drawXYChart();

		textSize(defaultFontSize);
		xyChart.draw(xyChartXOrigin, xyChartYOrigin, xyChartWidth,
				xyChartHeight);

		// Draw chart legend
		drawXYChartLegend();

		// TODO: enable the noLoop() before deploy the application. This noLoop
		// will improve the app performance.
		 noLoop();
	}

	public void mousePressed() {
		if (mouseButton == LEFT) {
			// TODO: call the Sketch 3 event!
			getClusterSelectedInLegend(mouseX, mouseY);
		}
	}

	private void drawXYChart() {
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
				tagTitle.append(" > ");
			}
			String title = "Question Clustering - Centroid Visualization";
			String subtitle = tagTitle.toString().substring(0,
					tagTitle.length() - 3);

			// Draw a title over the top of the chart.
			fill(defaultFontColor);
			textSize(20);
			text(title, xyChartXOrigin + 55, xyChartYOrigin - 20);
			textSize(15);
			text(subtitle, xyChartXOrigin + 55, xyChartYOrigin);
		}
	}
	
	private void drawXYChartLegend() {
		fill(255);
		noStroke();
		rect(legendXOrigin, legendYOrigin, legendWidth, legendHeight);

		int clusterIndex;
		int clusterNum = ChartData.getArrayDataIndex().length;
		int legendPartitionSize = legendHeight / maxClusterNumber;
		ellipseMode(CORNER);

		for (int i = 0; i < clusterNum; i++) {
			clusterIndex = Math.round(ChartData.getArrayDataIndex()[i]);

			fill(ChartData.RGBA_COLOURS[i][0], ChartData.RGBA_COLOURS[i][1],
					ChartData.RGBA_COLOURS[i][2], ChartData.RGBA_COLOURS[i][3]);
			ellipse(legendXOrigin + legendPadding, legendYOrigin
					+ legendPadding
					+ (legendHeight / 2 - clusterNum / 2 * legendPartitionSize)
					+ legendPartitionSize * i, ellipseWidth, ellipseHeight);

			fill(defaultFontColor);
			text("Cluster " + clusterIndex, legendXOrigin + ellipseWidth + 2
					* legendPadding, legendYOrigin + legendPadding
					+ (legendHeight / 2 - clusterNum / 2 * legendPartitionSize)
					+ defaultFontSize + legendPartitionSize * i);
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

	private void updateChartData() {
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