import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.gicentre.utils.colour.ColourTable;
import org.gicentre.utils.stat.XYChart;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import au.com.bytecode.opencsv.CSVReader;

public class Sketch2 extends PApplet {

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
		xyChartYOrigin = 15;
		xyChartWidth = width - 130;
		xyChartHeight = height - 30;
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
		try {
			// TODO: Change these file paths to a file (outside from the git
			// repository)
			QeAData.readPostTags("/home/augusto/git/tutor-qEa/TutorQeA/data/PostTags.csv");
			QeAData.readQuestionFeatures("/home/augusto/git/tutor-qEa/TutorQeA/data/QuestionFeatures.csv");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

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
	}

	public void draw() {
		background(255);

		// Reset the data
		resetChartData();
		// Rebuild the chart
		rebuildChart();

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
			getLegendClusterSelected(mouseX, mouseY);
		}
	}

	private void drawXYChartLegend() {
		fill(255);
		// noStroke();
		rect(legendXOrigin, legendYOrigin, legendWidth, legendHeight, 4, 4, 4,
				4);

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

	private int getLegendClusterSelected(int x, int y) {
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

	private void resetChartData() {
		ChartData.removeAllData();

		Enumeration<CentroidData> centroidEnum = QeAData.getCentroidDataList();
		CentroidData centroidTmp;
		while (centroidEnum.hasMoreElements()) {
			centroidTmp = centroidEnum.nextElement();

			ChartData.addData(centroidTmp.getMeanAnswerCount(),
					centroidTmp.getMeanScore(), centroidTmp.getClusterSize());
		}
	}

	private void rebuildChart() {
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

			// System.out.println(ChartData.getColourTable());
			// System.out.println();
			// System.out.println(ChartData.getColourTable().findColour(1));
			// System.out.println();
			// System.out.println(new ColourRule(1, ChartData.getColourTable()
			// .findColour(1)));

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
			text(title, xyChartXOrigin + 55, xyChartYOrigin + 15);
			textSize(15);
			text(subtitle, xyChartXOrigin + 55, xyChartYOrigin + 35);
		}
	}
}

class QeAData {

	// ArrayList with the chosen tags, the order expresses the intern
	// relationship between the tags
	private static ArrayList<Integer> chosenTags = new ArrayList<Integer>();
	private static ArrayList<String> chosenTagNames = new ArrayList<String>();
	// ArrayList with the chosen questions filtered after chosing the tags
	private static ArrayList<Integer> chosenQuestions = new ArrayList<Integer>();

	// Data in Memory
	private static Hashtable<Integer, ArrayList<Integer>> tagToQuestions = new Hashtable<Integer, ArrayList<Integer>>(
			100);
	private static Hashtable<Integer, ArrayList<Integer>> questionToTags = new Hashtable<Integer, ArrayList<Integer>>(
			2000);
	private static Hashtable<Integer, QuestionData> questionIdsToData = new Hashtable<Integer, QuestionData>();
	private static Hashtable<Integer, CentroidData> centroidIdsToData = new Hashtable<Integer, CentroidData>();

	@SuppressWarnings("unchecked")
	public static void setTagList(ArrayList<Integer> tagList,
			ArrayList<String> tagNameList) {

		if (tagList.size() == tagNameList.size() && !tagList.equals(chosenTags)) {
			// CLEAR the chosenTags, chosenQuestions and centroidIdsToData
			chosenTags.clear();
			chosenQuestions.clear();
			centroidIdsToData.clear();

			QuestionData questionDataTmp;
			ArrayList<Integer> nextChosenQuestions;
			int tagId;
			String tagName;

			for (int i = 0; i < tagList.size(); i++) {
				tagId = tagList.get(i);
				tagName = tagNameList.get(i);

				if (!tagToQuestions.containsKey(tagId)) {
					System.err.println("Unexistent Tag ID: " + tagList);
					System.exit(1);
				}

				chosenTags.add(tagId);
				chosenTagNames.add(tagName);
				int qId;
				// If there is any chosen question
				if (!chosenQuestions.isEmpty()) {

					nextChosenQuestions = new ArrayList<Integer>();
					for (int j = 0; j < chosenQuestions.size(); j++) {
						qId = chosenQuestions.get(j);
						// For each old question check if it contains the new
						// tag and remove it if doesn't
						if (questionToTags.get(qId).contains(tagId)) {
							nextChosenQuestions.add(qId);
						}
					}

					chosenQuestions = (ArrayList<Integer>) nextChosenQuestions
							.clone();

					if (chosenQuestions.isEmpty()) {
						// Avoiding the repetition of the emptiness initial
						// condition below...
						break;
					}
				} else {
					chosenQuestions = tagToQuestions.get(tagId);
				}
			}

			// Define the Centroids Data
			for (Integer qId : chosenQuestions) {
				questionDataTmp = questionIdsToData.get(qId);

				if (!centroidIdsToData
						.containsKey(questionDataTmp.getCluster())) {
					centroidIdsToData.put(questionDataTmp.getCluster(),
							new CentroidData(questionDataTmp.getCluster()));
				}
				centroidIdsToData.get(questionDataTmp.getCluster())
						.addQuestion(questionDataTmp.getId(),
								questionDataTmp.getAnswerCount(),
								questionDataTmp.getScore());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void readPostTags(String csvAbsolutePath) throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader(csvAbsolutePath));

		int question, tag;
		ArrayList<Integer> tmpList;

		// Reads the file header
		String[] nextLine = csvReader.readNext();
		while ((nextLine = csvReader.readNext()) != null) {
			question = Integer.valueOf(nextLine[0]);
			tag = Integer.valueOf(nextLine[1]);

			// TAG -> QUESTIONS
			if (tagToQuestions.containsKey(tag)) {
				tmpList = tagToQuestions.get(tag);

				if (tmpList == null) {
					tmpList = new ArrayList<Integer>();
				}
			} else {
				tmpList = new ArrayList<Integer>();
			}

			tmpList.add(question);
			tagToQuestions.put(tag, ((ArrayList<Integer>) tmpList.clone()));

			// QUESTIONS -> TAG
			if (questionToTags.containsKey(question)) {
				tmpList = questionToTags.get(question);

				if (tmpList == null) {
					tmpList = new ArrayList<Integer>();
				}
			} else {
				tmpList = new ArrayList<Integer>();
			}

			tmpList.add(tag);
			questionToTags
					.put(question, ((ArrayList<Integer>) tmpList.clone()));
		}

	}

	public static void readQuestionFeatures(String csvAbsolutePath)
			throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader(csvAbsolutePath));

		// Unique features used... At this moment...
		int questionId, clusterId, score, answerCount;

		// Reads the file header
		String[] nextLine = csvReader.readNext();
		while ((nextLine = csvReader.readNext()) != null) {

			questionId = Integer.valueOf(nextLine[0]);
			score = Math.round(Float.valueOf(nextLine[1]));
			answerCount = Math.round(Float.valueOf(nextLine[3]));
			clusterId = Integer.valueOf(nextLine[nextLine.length - 1]);

			questionIdsToData.put(questionId, new QuestionData(questionId,
					answerCount, score, clusterId));
		}
	}

	public static Hashtable<Integer, ArrayList<Integer>> getTagToQuestions() {
		return tagToQuestions;
	}

	public static Hashtable<Integer, ArrayList<Integer>> getQuestionToTags() {
		return questionToTags;
	}

	public static Hashtable<Integer, QuestionData> getQuestionIdsToData() {
		return questionIdsToData;
	}

	public static Hashtable<Integer, CentroidData> getCentroidIdsToData() {
		return centroidIdsToData;
	}

	public static Enumeration<CentroidData> getCentroidDataList() {
		return centroidIdsToData.elements();
	}

	public static ArrayList<Integer> getChosenTags() {
		return chosenTags;
	}

	public static ArrayList<String> getChosenTagNames() {
		return chosenTagNames;
	}

	public static ArrayList<Integer> getChosenQuestions() {
		return chosenQuestions;
	}

}

class CentroidData {

	private int clusterId;
	private int answerCount;
	private int score;
	private ArrayList<Integer> questionIds;

	public CentroidData(int clusterId) {
		this.clusterId = clusterId;
		this.answerCount = 0;
		this.score = 0;

		questionIds = new ArrayList<Integer>();
	}

	public void addQuestion(int id, int answerCount, int score) {
		questionIds.add(id);
		this.answerCount += answerCount;
		this.score += score;
	}

	public int getClusterSize() {
		return questionIds.size();
	}

	public float getMeanAnswerCount() {
		return (getClusterSize() > 0) ? this.answerCount / getClusterSize() : 0;
	}

	public float getMeanScore() {
		return (getClusterSize() > 0) ? this.score / getClusterSize() : 0;
	}

	public int getClusterId() {
		return clusterId;
	}

	public ArrayList<Integer> getQuestionIds() {
		return questionIds;
	}
}

class QuestionData {

	private int id, answerCount, score, cluster;

	public QuestionData(int id, int answerCount, int score, int cluster) {
		this.id = id;
		this.answerCount = answerCount;
		this.score = score;
		this.cluster = cluster;
	}

	public int getId() {
		return id;
	}

	public int getAnswerCount() {
		return answerCount;
	}

	public int getScore() {
		return score;
	}

	public int getCluster() {
		return cluster;
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