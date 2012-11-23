package util;

import java.util.ArrayList;

public class CentroidData {

	private int clusterId;
	private int score;
	private float answerCount;
	private float debate;	
	private float hotness;
	private ArrayList<Integer> questionIds;

	public CentroidData(int clusterId) {
		this.clusterId = clusterId;
		this.score = 0;
		this.answerCount = 0;
		this.debate = 0;
		this.hotness = 0;

		questionIds = new ArrayList<Integer>();
	}

	public void addQuestion(int id, float score, float answerCount, float debate, float hotness) {
		questionIds.add(id);
		this.score += score;
		this.answerCount += answerCount;
		this.debate += debate;
		this.hotness += hotness;
	}

	public int getClusterSize() {
		return questionIds.size();
	}

	public float getMeanScore() {
		return (getClusterSize() > 0) ? this.score / getClusterSize() : 0;
	}

	public float getMeanAnswerCount() {
		return (getClusterSize() > 0) ? this.answerCount / getClusterSize() : 0;
	}
	
	public float getMeanDebate() {
		return (getClusterSize() > 0) ? this.debate / getClusterSize() : 0;
	}
	
	public float getMeanHotness() {
		return (getClusterSize() > 0) ? this.hotness / getClusterSize() : 0;
	}

	public int getClusterId() {
		return clusterId;
	}

	public ArrayList<Integer> getQuestionIds() {
		return questionIds;
	}
}
