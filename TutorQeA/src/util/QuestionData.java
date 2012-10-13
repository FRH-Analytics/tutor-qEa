package util;

public class QuestionData {

	private int id;
	private String title;
	private int score;
	private int answerCount;
	private int commentCount;
	private int cluster;

	public QuestionData(int id, String title, int score, int answerCount,
			int commentCount, int cluster) {
		this.id = id;
		this.title = title;
		this.score = score;
		this.answerCount = answerCount;
		this.commentCount = commentCount;
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

	public int getCommentCount() {
		return commentCount;
	}

	public int getCluster() {
		return cluster;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuestionData other = (QuestionData) obj;
		if (answerCount != other.answerCount)
			return false;
		if (cluster != other.cluster)
			return false;
		if (commentCount != other.commentCount)
			return false;
		if (id != other.id)
			return false;
		if (score != other.score)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

}
