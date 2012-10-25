package util;

import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;

import au.com.bytecode.opencsv.CSVReader;

public class QeAData {

	private static final String POST_TAGS_FILE = "../data/PostTags.csv";
	private static final String QUESTIONS_DATA_FILE = "../data/QuestionData.csv";
	private static final String QUESTION_ANSWERS_FILE = "../data/QuestionAnswers1.csv";
	private static final String QUESTION_ANSWERS_FILE_2 = "../data/QuestionAnswers2.csv";
	private static final String TAG_LINKS_FILE = "../data/TagLinks.csv";
	private static final String TAGS_FILE = "../data/TagsDictionary.csv";

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

			// Define the Centroids Data
			QuestionData questionDataTmp;
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
		CSVReader csvReader = new CSVReader(new FileReader(POST_TAGS_FILE));

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

	public static void readQuestionsDataFile() throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader(QUESTIONS_DATA_FILE));

		// Unique collumns used... At this moment...
		int questionId, score, answerCount, commentCount, cluster;
		String title;

		// Reads the file header
		String[] nextLine = csvReader.readNext();
		while ((nextLine = csvReader.readNext()) != null) {
			questionId = Integer.valueOf(nextLine[0]);
			title = nextLine[1];
			score = Integer.valueOf(nextLine[2]);
			answerCount = Integer.valueOf(nextLine[3]);
			commentCount = Integer.valueOf(nextLine[4]);
			cluster = Integer.valueOf(nextLine[5]);
			questionIdsToData.put(questionId, new QuestionData(questionId,
					title, score, answerCount, commentCount, cluster));
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

		/*
		 * FILE 1
		 */
		CSVReader csvReader = new CSVReader(new FileReader(
				QUESTION_ANSWERS_FILE));

		// Reads the file header
		String[] nextLine = csvReader.readNext();
		while ((nextLine = csvReader.readNext()) != null) {
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

		/*
		 * FILE 2
		 */
		csvReader = new CSVReader(new FileReader(QUESTION_ANSWERS_FILE_2));

		// Reads the file header
		nextLine = csvReader.readNext();
		while ((nextLine = csvReader.readNext()) != null) {
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

	public static void readTagLinksFile() throws IOException {

		CSVReader reader = new CSVReader(new FileReader(TAG_LINKS_FILE));

		String[] nextLine = reader.readNext();
		while ((nextLine = reader.readNext()) != null) {
			int key = Integer.valueOf(nextLine[0]);
			if (tagLinks.containsKey(key)) {
				tagLinks.put(Integer.valueOf(nextLine[0]),
						tagLinks.get(Integer.valueOf(nextLine[0])) + ","
								+ nextLine[1]);
			} else {
				tagLinks.put(Integer.valueOf(nextLine[0]), nextLine[1]);
			}
		}
		reader.close();

	}

	public static void readTagDictionaryFile() throws IOException {

		CSVReader reader = new CSVReader(new FileReader(TAGS_FILE));

		String[] nextLine = reader.readNext();
		while ((nextLine = reader.readNext()) != null) {
			tagDictionary.put(Integer.valueOf(nextLine[0]), nextLine[1]);
		}
		reader.close();
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
}
