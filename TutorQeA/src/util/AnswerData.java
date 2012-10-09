package util;

import java.util.Date;

public class AnswerData {

	private int id;
	private int score;
	private Date creationDate;

	public AnswerData(int id, int score, Date creationDate) {
		this.id = id;
		this.score = score;
		this.creationDate = creationDate;
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
