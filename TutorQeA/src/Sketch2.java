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
	XYChart clustersChart;
	ChartData chartData;

	/**
	 * Initialises the sketch, loads data into the chart and customises its
	 * appearance.
	 */
	public void setup() {
		size(800, 500);
		smooth();
		// noLoop();

		PFont font = createFont("Helvetica", 11);
		textFont(font, 10);

		// Create CHART
		clustersChart = new XYChart(this);
		clustersChart.setYFormat("#####");
		clustersChart.setXFormat("#####");
		clustersChart.setXAxisLabel("Answer Count");
		clustersChart.setYAxisLabel("Question Score");

		// Create the ChartData container
		chartData = new ChartData();

		// Read files (raw file path)
		try {
			QeAData.readPostTags("/home/augusto/git/tutor-qEa/TutorQeA/data/PostTags.csv");
			QeAData.readQuestionFeatures("/home/augusto/git/tutor-qEa/TutorQeA/data/QuestionFeatures.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}

		ArrayList<Integer> tagList = new ArrayList<Integer>();
		tagList.add(41);
		QeAData.setTagList(tagList);
	}

	/**
	 * Draws the chart and a title.
	 */
	public void draw() {
		background(255);
		textSize(9);

		// Reset the data
		resetChartData();
		// Rebuild the chart
		rebuildChart();

		clustersChart.draw(15, 15, width - 30, height - 30);

		// Draw a title over the top of the chart.
		fill(120);
		textSize(20);
		text("Questions Clustered", 70, 30);
		textSize(11);
		text("Centroid visualization only", 70, 45);
	}

	public void resetChartData() {
		chartData.removeAllData();

		Enumeration<CentroidData> centroidEnum = QeAData.getCentroidDataList();
		CentroidData centroidTmp;
		while (centroidEnum.hasMoreElements()) {
			centroidTmp = centroidEnum.nextElement();

			chartData.addData(centroidTmp.getMeanAnswerCount(),
					centroidTmp.getMeanScore(), centroidTmp.getClusterSize());
		}
	}

	public void rebuildChart() {
		clustersChart.setData(chartData.getPoints());

		// Axis formatting and labels.
		clustersChart.showXAxis(true);
		clustersChart.showYAxis(true);

		// Symbol colours
		clustersChart.setPointSize(chartData.getSizeArray(),
				chartData.getMaxSize());
		// 12 different colors
		clustersChart.setPointColour(chartData.getColourData(),
				ColourTable.getPresetColourTable(ColourTable.SET3_12));
	}
}

class QeAData {

	// ArrayList with the chosen tags, the order expresses the intern
	// relationship between the tags
	private static ArrayList<Integer> chosenTags = new ArrayList<Integer>();
	// ArrayList with the chosen questions filtered after chosing the tags
	private static ArrayList<Integer> chosenQuestions = new ArrayList<Integer>();

	// Data in Memory
	private static Hashtable<Integer, ArrayList<Integer>> tagToQuestions = new Hashtable<Integer, ArrayList<Integer>>(
			100);
	private static Hashtable<Integer, ArrayList<Integer>> questionToTags = new Hashtable<Integer, ArrayList<Integer>>(
			2000);
	private static Hashtable<Integer, QuestionData> questionIdsToData = new Hashtable<Integer, QuestionData>();
	private static Hashtable<Integer, CentroidData> centroidIdsToData = new Hashtable<Integer, CentroidData>();

	public static void setTagList(ArrayList<Integer> tagList) {

		if (!tagList.equals(chosenTags)) {
			// CLEAR the chosenTags, chosenQuestions and centroidIdsToData
			chosenTags.clear();
			chosenQuestions.clear();
			centroidIdsToData.clear();

			QuestionData questionDataTmp;

			for (Integer tagId : tagList) {
				if (!tagToQuestions.containsKey(tagId)) {
					System.err.println("Unexistent Tag ID: " + tagList);
					System.exit(1);
				}

				chosenTags.add(tagId);
				int qId;
				// If there is any chosen question
				if (!chosenQuestions.isEmpty()) {
					for (int i = 0; i < chosenQuestions.size(); i++) {
						qId = chosenQuestions.get(i);
						// For each old question check if it contains the new
						// tag and remove it if doesn't
						if (!questionToTags.get(qId).contains(tagId)) {
							chosenQuestions.remove(i);
						}
					}
				} else {
					chosenQuestions = tagToQuestions.get(tagId);
				}
			}

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
	private ArrayList<PVector> points;
	private ArrayList<Float> sizes;

	public ChartData() {
		this.points = new ArrayList<PVector>();
		this.sizes = new ArrayList<Float>();
	}

	public void addData(float x, float y, float size) {
		points.add(new PVector(x, y));
		sizes.add(size);
	}

	public void removeAllData() {
		points.clear();
		sizes.clear();
	}

	public ArrayList<PVector> getPoints() {
		return this.points;
	}

	public ArrayList<Float> getSizeArrayList() {
		return this.sizes;
	}

	public float[] getSizeArray() {
		float[] floatArray = new float[this.sizes.size()];

		for (int i = 0; i < floatArray.length; i++) {
			Float f = this.sizes.get(i);
			floatArray[i] = (f != null ? f : Float.NaN);
		}
		return floatArray;
	}

	public float getMaxSize() {
		float max = 0;

		for (Float f : getSizeArrayList()) {
			max = (f > max) ? f : max;
		}
		return max;
	}

	public float[] getColourData() {
		float[] colourData = new float[getSizeArrayList().size()];
		for (int i = 0; i < colourData.length; i++) {
			colourData[i] = i + 1;
		}
		return colourData;
	}

	public float getMinY() {
		float minX = Float.MAX_VALUE;
		for (PVector p : points) {
			minX = (p.x < minX) ? p.x : minX;
		}
		return minX;
	}
}