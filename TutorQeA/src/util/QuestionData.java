package util;

import java.util.ArrayList;

public class QuestionData implements Comparable<QuestionData> {

	private int id;
	private String title;
	private int cluster;
	private int sortByIndex;

	private ArrayList<Float> featureValues;
	private ArrayList<String> featureNames;

	@SuppressWarnings("unchecked")
	public QuestionData(int id, String title, ArrayList<Float> values,
			ArrayList<String> names, int cluster) {

		if (values.size() <= 0 || values.size() != names.size()) {
			throw new RuntimeException(
					"The Values and Names of the features are not of the same size or are empty!");
		}

		this.id = id;
		this.title = title;
		this.cluster = cluster;
		featureValues = (ArrayList<Float>) values.clone();
		featureNames = (ArrayList<String>) names.clone();

		sortByIndex = 0;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public int getCluster() {
		return cluster;
	}

	public ArrayList<String> getFeatureNames() {
		return featureNames;
	}

	public ArrayList<Float> getFeatureValues() {
		return featureValues;
	}

	public int getSortByIndex() {
		return sortByIndex;
	}

	public Float getFeatureValueByName(String name) {
		Float result = null;
		for (int i = 0; i < featureNames.size(); i++) {
			if (featureNames.get(i).equals(name)) {
				result = featureValues.get(i);
				break;
			}
		}
		if (result == null) {
			throw new RuntimeException("Unexistent FeatureName: " + name + ".");
		}
		return result;
	}

	public Float getFeatureValueOfSortIndex() {
		return featureValues.get(sortByIndex);
	}

	public void setSortByIndex(int sortByIndex) {
		if (sortByIndex < featureValues.size()) {
			this.sortByIndex = sortByIndex;
		} else {
			throw new RuntimeException(
					"The sortByIndex is larger than the amount of features!");
		}
	}

	@Override
	public int compareTo(QuestionData other) {
		float diff = other.getFeatureValueOfSortIndex()
				- this.getFeatureValueOfSortIndex();
		if (diff == 0) {
			int diffTitle = other.getTitle().compareTo(this.getTitle());
			return diffTitle;
		} else {
			return (int) diff;
		}
	}

	@Override
	public String toString() {
		return this.getId() + " - " + this.getFeatureValueOfSortIndex();
	}

}
