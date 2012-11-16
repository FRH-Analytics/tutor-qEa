package util;

import java.util.ArrayList;

public class CentroidData {

	private int clusterId;
	private float scope;
	private float dialogue;
	private ArrayList<Integer> questionIds;

	public CentroidData(int clusterId) {
		this.clusterId = clusterId;
		this.scope = 0;
		this.dialogue = 0;

		questionIds = new ArrayList<Integer>();
	}

	public void addQuestion(int id, float scope, float dialogue) {
		questionIds.add(id);
		this.scope += scope;
		this.dialogue += dialogue;
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

	public int getClusterId() {
		return clusterId;
	}

	public ArrayList<Integer> getQuestionIds() {
		return questionIds;
	}
}
