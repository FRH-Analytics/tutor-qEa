package util;

import java.util.ArrayList;

public class CentroidData {

	private int clusterId;
	private ArrayList<Integer> questionIds;
	private ArrayList<Float> sumAttributeValues;

	public CentroidData(int clusterId, int numFeatures) {
		this.clusterId = clusterId;

		questionIds = new ArrayList<Integer>();
		sumAttributeValues = new ArrayList<Float>();
		for (int i = 0; i < numFeatures; i++) {
			sumAttributeValues.add((float) 0.0);
		}
	}

	public void addQuestion(QuestionData qData) {
		questionIds.add(qData.getId());

		for (int i = 0; i < qData.getFeatureValues().size(); i++) {
			sumAttributeValues.set(i, sumAttributeValues.get(i)
					+ qData.getFeatureValues().get(i));
		}
	}

	// public void addQuestion(int id, float score, float answerCount,
	// float debate, float hotness) {
	// questionIds.add(id);
	// this.score += score;
	// this.answerCount += answerCount;
	// this.debate += debate;
	// this.hotness += hotness;
	// }

	public int getClusterId() {
		return clusterId;
	}

	public ArrayList<Integer> getQuestionIds() {
		return questionIds;
	}

	public int getClusterSize() {
		return questionIds.size();
	}

	public float getMeanByIndex(int index) {
		return (getClusterSize() > 0) ? this.sumAttributeValues.get(index)
				/ getClusterSize() : 0;
	}

	// public float getMeanScore() {
	//
	// }
	//
	// public float getMeanAnswerCount() {
	// return (getClusterSize() > 0) ? this.answerCount / getClusterSize() : 0;
	// }
	//
	// public float getMeanDebate() {
	// return (getClusterSize() > 0) ? this.debate / getClusterSize() : 0;
	// }
	//
	// public float getMeanHotness() {
	// return (getClusterSize() > 0) ? this.hotness / getClusterSize() : 0;
	// }
}
