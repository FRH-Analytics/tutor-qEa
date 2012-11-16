package util;

public class QuestionData implements Comparable<QuestionData>{

	private int id;
	private String title;
	private int score;
	private int answerCount;
	private float scope;
	private float dialogue;
	private float empathy;
	private int cluster;

	public QuestionData(int id, String title, int score, int answerCount,
			float scope, float dialogue, float empathy, int cluster) {
		this.id = id;
		this.title = title;
		this.score = score;
		this.answerCount = answerCount;
		this.scope = scope;
		this.dialogue = dialogue;
		this.empathy = empathy;
		this.cluster = cluster;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public int getScore() {
		return score;
	}

	public int getAnswerCount() {
		return answerCount;
	}

	public float getScope() {
		return scope;
	}
	
	public float getDialogue() {
		return dialogue;
	}
	
	public float getEmpathy() {
		return empathy;
	}

	public int getCluster() {
		return cluster;
	}

	@Override
	public int compareTo(QuestionData other) {
	 	int diffScore = other.getScore() - this.getScore();
	 	if (diffScore == 0){
	 		int diffTitle = other.getTitle().compareTo(this.getTitle());
	 		return diffTitle; 
	 	}else{
	 		return diffScore;
	 	}
	 }
	
	@Override
	public String toString() {
		return this.getId() + " - " + this.getScore();
	}

}
