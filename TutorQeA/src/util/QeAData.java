package util;

import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;

import processing.core.PApplet;

public class QeAData {
	
	private static PApplet pApplet;

	private static final String POST_TAGS_FILE = "data/PostTags.csv";
	private static final String QUESTIONS_DATA_FILE = "data/QuestionData.csv";
	private static final String QUESTION_ANSWERS_FILE = "data/QuestionAnswers.csv";
	private static final String TAG_LINKS_FILE = "data/TagLinks.csv";
	private static final String TAGS_FILE = "data/TagsDictionary.csv";

	// ArrayList with the chosen tags, the order expresses the intern
	// relationship between the tags
	private static ArrayList<Integer> chosenTagIds = new ArrayList<Integer>();
	private static ArrayList<String> chosenTagNames = new ArrayList<String>();
	// ArrayList with the chosen questions filtered after choosing the tags
	private static ArrayList<Integer> chosenQuestions = new ArrayList<Integer>();
	// Map with the centroids
	private static TreeMap<Integer, CentroidData> centroidIdsToData = new TreeMap<Integer, CentroidData>();

	// Static Data in Memory
	private static Hashtable<Integer, ArrayList<Integer>> tagToQuestions = new Hashtable<Integer, ArrayList<Integer>>(
			100);
	private static Hashtable<Integer, ArrayList<Integer>> questionToTags = new Hashtable<Integer, ArrayList<Integer>>(
			2000);
	private static Hashtable<Integer, QuestionData> questionIdsToData = new Hashtable<Integer, QuestionData>();
	private static Hashtable<Integer, ArrayList<AnswerData>> questionIdsToAnswers = new Hashtable<Integer, ArrayList<AnswerData>>();

	private static HashMap<Integer, String> tagLinks = new HashMap<Integer, String>();
	private static HashMap<Integer, String> tagDictionary = new HashMap<Integer, String>();

	@SuppressWarnings("unchecked")
	public static void setTagList(ArrayList<Integer> tagList,
			ArrayList<String> tagNameList) {

		if (tagList.size() == tagNameList.size()
				&& !tagList.equals(chosenTagIds)) {
			// CLEAR everything
			chosenTagIds.clear();
			chosenTagNames.clear();
			chosenQuestions.clear();
			centroidIdsToData.clear();

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

				chosenTagIds.add(tagId);
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
					chosenQuestions = (ArrayList<Integer>) tagToQuestions.get(
							tagId).clone();
				}
			}

			// READ THE QUESTION_DATA file if it was not read yet...
			if (questionIdsToData.size() == 0) {
				try {
					readQuestionsDataFile();
				} catch (IOException e) {
					System.err.println("Error reading the QUESTION_DATA_FILE!");
				}
			}

			// Define the Centroids Data (based on the QuestionData)
			QuestionData questionDataTmp;
			for (Integer qId : chosenQuestions) {
				questionDataTmp = questionIdsToData.get(qId);

				if (!centroidIdsToData
						.containsKey(questionDataTmp.getCluster())) {
					centroidIdsToData.put(questionDataTmp.getCluster(),
							new CentroidData(questionDataTmp.getCluster(),
									QuestionData.getFeatureNames().size()));
				}

				// TODO: Change this! The addQuestion will receive the
				// questionDataTmp and will iterate over the values summing up
				// The names are going to be the same of QuestionData and all
				// the other classes that uses the features, should do it
				// anonymously
				centroidIdsToData.get(questionDataTmp.getCluster())
						.addQuestion(questionDataTmp);
			}
		}
	}

	public static ArrayList<Integer> getQuestionIdsByCluster(int cluster) {
		ArrayList<Integer> clusterQuestions = new ArrayList<Integer>();
		for (Integer qId : chosenQuestions) {
			if (questionIdsToData.get(qId).getCluster() == cluster) {
				clusterQuestions.add(qId);
			}
		}
		return (clusterQuestions);
	}

	@SuppressWarnings("unchecked")
	public static void readPostTagsFile() throws IOException {
		String[] reader = pApplet.loadStrings(POST_TAGS_FILE);

		int question, tag;
		ArrayList<Integer> tmpList;

		// Reads the file header
		for (int i = 1;i<reader.length;i++) {
			String[] nextLine = reader[i].replace("\"",	"").split(",");
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

	public static void readTagLinksFile() throws IOException {

		String[] reader = pApplet.loadStrings(TAG_LINKS_FILE);

		for (int i = 1;i<reader.length;i++) {
			String[] nextLine = reader[i].replace("\"",	"").split(",");
			int key = Integer.valueOf(nextLine[0]);
			if (tagLinks.containsKey(key)) {
				tagLinks.put(Integer.valueOf(nextLine[0]),
						tagLinks.get(Integer.valueOf(nextLine[0])) + ","
								+ nextLine[1]);
			} else {
				tagLinks.put(Integer.valueOf(nextLine[0]), nextLine[1]);
			}
		}
	}

	public static void readTagDictionaryFile() throws IOException {

		String[] reader = pApplet.loadStrings(TAGS_FILE);

		for (int i = 1;i<reader.length;i++) {
			String[] nextLine = reader[i].replace("\"",	"").split(",");
			tagDictionary.put(Integer.valueOf(nextLine[0]), nextLine[1]);
		}
	}

	public static void readQuestionsDataFile() throws IOException {
		String[] reader = pApplet.loadStrings(QUESTIONS_DATA_FILE);

		// Unique columns used... At this moment...
		int questionId, cluster;
		String title;

		ArrayList<Float> values = new ArrayList<Float>();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> postNames = new ArrayList<String>();

		// Reads the file header and set the names
		String[] nextLine = parseQuestion(reader[0].replace("\"",""));
		names.add(nextLine[2]);
		names.add(nextLine[3]);
		names.add(nextLine[4]);
		names.add(nextLine[5]);
		QuestionData.setFeatureNames(names);
		// Set the postNames (HARDCODED...)
		postNames.add("votes");
		postNames.add("answers");
		postNames.add("points");
		postNames.add("points");
		QuestionData.setFeaturePostNames(postNames);
		// Set the initial index of the feature to sort by
		QuestionData.setSortByIndex(0);

		// Read the data
		for (int i = 1;i<reader.length;i++) {
			nextLine = parseQuestion(reader[i].replace("\"",""));
			questionId = Integer.valueOf(nextLine[0]);
			title = nextLine[1];
			values.add(Float.valueOf(nextLine[2]));
			values.add(Float.valueOf(nextLine[3]));
			values.add(Float.valueOf(nextLine[4]));
			values.add(Float.valueOf(nextLine[5]));
			cluster = Integer.valueOf(nextLine[6]);
			questionIdsToData.put(questionId, new QuestionData(questionId,
					title, values, cluster));
			values.clear();
		}
	}

	public static void readQuestionAnswersFile() throws IOException,
			ParseException {

		// Unique features used... At this moment...
		int questionId;
		int answerId;
		int score;
		Date creationDate;
		int answerCommentsCount;
		boolean isAccepted;

		DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		String[] reader = pApplet.loadStrings(QUESTION_ANSWERS_FILE);

		// Reads the file header
		for (int i = 1;i<reader.length;i++) {
			String[] nextLine = reader[i].replace("\"",	"").split(",");
			questionId = Integer.valueOf(nextLine[0]);
			answerId = Integer.valueOf(nextLine[1]);
			score = Integer.valueOf(nextLine[2]);
			creationDate = format.parse(nextLine[3]);
			answerCommentsCount = Integer.valueOf(nextLine[4]);
			isAccepted = Boolean.parseBoolean(nextLine[5].toLowerCase());

			if (!questionIdsToAnswers.containsKey(questionId)) {
				questionIdsToAnswers.put(questionId,
						new ArrayList<AnswerData>());
			}

			questionIdsToAnswers.get(questionId).add(
					new AnswerData(answerId, score, creationDate,
							answerCommentsCount, isAccepted));
		}
	}
	
	private static String[] parseQuestion(String line){
		String[] result = new String[7];
		String workingLine = line;
		int index;
		for (int i = 6; i>1;i--){
			index = workingLine.lastIndexOf(",");
			result[i] =  workingLine.substring(index+1, workingLine.length());
			workingLine = workingLine.substring(0, index);
		}
		index = workingLine.indexOf(",");
		result[0] = workingLine.substring(0, index);
		workingLine = workingLine.substring(index+1, workingLine.length());
		result[1] = workingLine;
		
		
		return result;
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

	public static TreeMap<Integer, CentroidData> getCentroidIdsToData() {
		return centroidIdsToData;
	}

	public static Collection<CentroidData> getCentroidDataList() {
		return centroidIdsToData.values();
	}

	public static ArrayList<Integer> getChosenTags() {
		return chosenTagIds;
	}

	public static ArrayList<String> getChosenTagNames() {
		return chosenTagNames;
	}

	public static ArrayList<Integer> getChosenQuestions() {
		return chosenQuestions;
	}

	public static Hashtable<Integer, ArrayList<AnswerData>> getQuestionIdsToAnswers() {
		return questionIdsToAnswers;
	}

	public static HashMap<Integer, String> getTagLinks() {
		return tagLinks;
	}

	public static HashMap<Integer, String> getTagDictionary() {
		return tagDictionary;
	}

	public static void setPApplet(PApplet applet) {
		pApplet = applet;
		
	}
}
