package util;

import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import au.com.bytecode.opencsv.CSVReader;

public class QeAData {

	// TODO: Change these file paths to a file (outside from the git
	// repository)
	private static final String POST_TAGS_FILE = "/home/augusto/git/tutor-qEa/TutorQeA/data/PostTags.csv";
	private static final String QUESTIONS_DATA_FILE = "/home/augusto/git/tutor-qEa/TutorQeA/data/QuestionData.csv";
	private static final String QUESTION_ANSWERS_FILE = "/home/augusto/git/tutor-qEa/TutorQeA/data/QuestionAnswers.csv";

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
	private static Hashtable<Integer, CentroidData> centroidIdsToData = new Hashtable<Integer, CentroidData>();

	private static Hashtable<Integer, QuestionData> questionIdsToData = new Hashtable<Integer, QuestionData>();
	private static Hashtable<Integer, ArrayList<AnswerData>> questionIdsToAnswers = new Hashtable<Integer, ArrayList<AnswerData>>();

	@SuppressWarnings("unchecked")
	public static void setTagList(ArrayList<Integer> tagList,
			ArrayList<String> tagNameList) {

		if (tagList.size() == tagNameList.size() && !tagList.equals(chosenTags)) {
			// CLEAR the chosenTags, chosenQuestions and centroidIdsToData
			chosenTags.clear();
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
		CSVReader csvReader = new CSVReader(new FileReader(
				QUESTION_ANSWERS_FILE));

		// Unique features used... At this moment...
		int questionId;
		int answerId;
		int score;
		Date creationDate;

		DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		// Reads the file header
		String[] nextLine = csvReader.readNext();
		while ((nextLine = csvReader.readNext()) != null) {
			questionId = Integer.valueOf(nextLine[0]);
			answerId = Integer.valueOf(nextLine[1]);
			score = Integer.valueOf(nextLine[2]);
			creationDate = format.parse(nextLine[3]);

			if (!questionIdsToAnswers.containsKey(questionId)) {
				questionIdsToAnswers.put(questionId,
						new ArrayList<AnswerData>());
			}

			questionIdsToAnswers.get(questionId).add(
					new AnswerData(answerId, score, creationDate));
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

	public static Hashtable<Integer, ArrayList<AnswerData>> getQuestionIdsToAnswers() {
		return questionIdsToAnswers;
	}
}
