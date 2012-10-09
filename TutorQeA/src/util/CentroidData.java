package util;

import java.util.ArrayList;

public class CentroidData {

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
