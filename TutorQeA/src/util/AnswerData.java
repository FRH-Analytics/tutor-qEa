package util;

import java.util.Date;

public class AnswerData {

	private int id;
	private int score;
	private Date creationDate;
	private int commentsCount;
	private boolean isAccepted;

	public AnswerData(int id, int score, Date creationDate) {
		this.id = id;
		this.score = score;
		this.creationDate = creationDate;
	}
	
	public AnswerData(int id, int score, Date creationDate, int commentsCount, boolean isAccepted) {
		this.id = id;
		this.score = score;
		this.creationDate = creationDate;
		this.commentsCount = commentsCount;
		this.isAccepted = isAccepted;
	}

	public int getCommentsCount() {
		return commentsCount;
	}

	public boolean isAccepted() {
		return isAccepted;
	}

	public int getId() {
		return id;
	}

	public Date getCreationDate() {
		return creationDate;
	}
	
	public int getScore() {
		return score;
	}
}
