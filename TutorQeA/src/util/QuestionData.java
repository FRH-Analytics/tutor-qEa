package util;

public class QuestionData {

	private int id;
	private String title;
	private int score;

	public QuestionData(int id, String title, int score) {
		this.id = id;
		this.title = title;
		this.score = score;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuestionData other = (QuestionData) obj;
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
