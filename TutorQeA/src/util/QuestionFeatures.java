package util;

public class QuestionFeatures {

	private int id, answerCount, score, cluster;

	public QuestionFeatures(int id, int answerCount, int score, int cluster) {
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
