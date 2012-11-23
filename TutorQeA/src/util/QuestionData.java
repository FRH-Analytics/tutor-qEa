package util;

import java.util.ArrayList;

public class QuestionData implements Comparable<QuestionData> {

	private static ArrayList<String> featureNames;
	private static int sortByIndex;
	private static ArrayList<String> featurePostNames;

	private int id;
	private String title;
	private int cluster;
	private ArrayList<Float> featureValues;

	@SuppressWarnings("unchecked")
	public QuestionData(int id, String title, ArrayList<Float> values,
			int cluster) {

		if (values.size() <= 0
				|| values.size() != QuestionData.featureNames.size()) {
			throw new RuntimeException(
					"The Values and Names of the features are not of the same size or are empty!");
		}

		this.id = id;
		this.title = title;
		this.cluster = cluster;
		featureValues = (ArrayList<Float>) values.clone();
	}

	@SuppressWarnings("unchecked")
	public static void setFeatureNames(ArrayList<String> featureNames) {
		QuestionData.featureNames = (ArrayList<String>) featureNames.clone();
	}

	@SuppressWarnings("unchecked")
	public static void setFeaturePostNames(ArrayList<String> featurePostNames) {
		QuestionData.featurePostNames = (ArrayList<String>) featurePostNames
				.clone();
	}

	public static void setSortByIndex(int sortByIndex) {
		if (sortByIndex < QuestionData.featureNames.size()) {
			QuestionData.sortByIndex = sortByIndex;
		} else {
			throw new RuntimeException(
					"The sortByIndex is larger than the amount of features!");
		}
	}

	public static String getFeatureNameOfSortIndex() {
		return QuestionData.featureNames.get(sortByIndex);
	}
	
	public static String getFeaturePostNameOfSortIndex() {
		return QuestionData.featurePostNames.get(sortByIndex);
	}

	public static ArrayList<String> getFeatureNames() {
		return QuestionData.featureNames;
	}

	public static ArrayList<String> getFeaturePostNames() {
		return featurePostNames;
	}
	
	public static int getSortByIndex() {
		return sortByIndex;
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

	public ArrayList<Float> getFeatureValues() {
		return featureValues;
	}

	public Float getFeatureValueByName(String name) {
		Float result = null;
		for (int i = 0; i < QuestionData.featureNames.size(); i++) {
			if (QuestionData.featureNames.get(i).equals(name)) {
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

	@Override
	public int compareTo(QuestionData other) {
		float diff = other.getFeatureValueOfSortIndex()
				- this.getFeatureValueOfSortIndex();
		if (diff == 0) {
			return other.getTitle().compareTo(this.getTitle());
		} else {
			return (int) (diff * 1000000);// Decimal precision
		}
	}

	@Override
	public String toString() {
		return this.getId() + " - " + this.getFeatureValueOfSortIndex();
	}

}
