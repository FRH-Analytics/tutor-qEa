package util;

import java.util.ArrayList;

public class CentroidData {

	private int clusterId;
	private float scope;
	private float dialogue;
	private int score;
	private float empathy;
	private ArrayList<Integer> questionIds;

	public CentroidData(int clusterId) {
		this.clusterId = clusterId;
		this.scope = 0;
		this.dialogue = 0;
		this.empathy = 0;
		this.score = 0;

		questionIds = new ArrayList<Integer>();
	}

	public void addQuestion(int id, float scope, float dialogue, float empathy, float score) {
		questionIds.add(id);
		this.scope += scope;
		this.dialogue += dialogue;
		this.score += score;
		this.empathy += empathy;
	}

	public int getClusterSize() {
		return questionIds.size();
	}

	public float getMeanScope() {
		return (getClusterSize() > 0) ? this.scope / getClusterSize() : 0;
	}

	public float getMeanDialogue() {
		return (getClusterSize() > 0) ? this.dialogue / getClusterSize() : 0;
	}
	
	public float getMeanEmpathy() {
		return (getClusterSize() > 0) ? this.empathy / getClusterSize() : 0;
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
