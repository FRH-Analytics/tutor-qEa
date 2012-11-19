package multSketches;

import java.util.ArrayList;
import java.util.Hashtable;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import processing.core.PApplet;
import processing.core.PFont;
import util.QeAData;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.DropdownList;
import controlP5.Textfield;

public class SubSketch1 {

	private ArrayList<Integer> selectedTags = new ArrayList<Integer>();
	private ArrayList<String> selectedTagsNames = new ArrayList<String>();
	private ArrayList<Integer> relatedTags = new ArrayList<Integer>();
	private Hashtable<Integer, ArrayList<Integer>> usefulQuestions = new Hashtable<Integer, ArrayList<Integer>>();

	ControlP5 cp5;
	ArrayList<DropdownList> lists = new ArrayList<DropdownList>();
	Textfield tagSearchField;

	float searchX, searchY, tagsOfInterestX, tagsOfInterestY;
	int textFieldX, textFieldY, textFieldWidth, textFieldHeight;
	int ddlX, ddlY, ddlWidth, ddlHeight;

	int defaultFontSize;
	int defaultFontColor;
	PFont font;

	private float highlightColor;

	protected int myWidth;
	protected int myHeight;
	protected int myXOrigin;
	protected int myYOrigin;

	protected EmbeddedSketch mySketch;

	public SubSketch1(EmbeddedSketch parent, int xOrigin, int yOrigin,
			int width, int height) {
		mySketch = parent;

		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;
	}

	public void setup() {

		defaultFontSize = 12;
		font = mySketch.createFont("Helvetica", defaultFontSize);
		mySketch.textFont(font);

		searchX = myXOrigin + 20;
		searchY = myYOrigin + 30;

		tagsOfInterestX = searchX;
		tagsOfInterestY = searchY + 30;

		textFieldX = (int) (searchX + 75);
		textFieldY = (int) searchY - 15;
		textFieldWidth = myWidth - 250;
		textFieldHeight = (int) (defaultFontSize * 1.75);

		ddlX = myXOrigin + 20;
		ddlY = (int) tagsOfInterestY + 30;
		ddlWidth = 150;
		ddlHeight = 30;

		highlightColor = 200;

		cp5 = new ControlP5(mySketch);

		tagSearchField = cp5.addTextfield("input")
				.setPosition(textFieldX, textFieldY)
				.setSize(textFieldWidth, textFieldHeight).setFont(font)
				.setFocus(true).setColor(mySketch.color(0, 0, 0))
				.setColorActive(mySketch.color(highlightColor))
				.setColorCursor(mySketch.color(highlightColor))
				.setColorBackground(mySketch.color(255));

	}

	public void draw() {
		drawTexts();
	}

	public void keyPressed() {
		if (tagSearchField.isActive()) {
			// Re-Draw...
			mySketch.loop();
		}
	}

	private void drawTexts() {

		mySketch.textSize(15);
		mySketch.textAlign(PApplet.LEFT);

		String text = "Search:";
		mySketch.text(text, searchX, searchY);

		text = "Tags of Interest:";
		mySketch.text(text, tagsOfInterestX, tagsOfInterestY);
	}

	public void input(String theText) {
		theText = theText.toLowerCase();
		usefulQuestions = QeAData.getQuestionToTags();
		if (lists.size() != 0) {
			clearList();
		}

		ArrayList<Integer> keys = new ArrayList<Integer>(QeAData
				.getTagDictionary().keySet());
		ArrayList<String> values = new ArrayList<String>(QeAData
				.getTagDictionary().values());

		int index = values.indexOf(theText);
		if (index >= 0) {
			int tagID = keys.get(index);
			selectedTags.add(tagID);
			selectedTagsNames.add(QeAData.getTagDictionary().get(tagID));
			updateUsefulQuestions(tagID);
			addDropDownList(tagID, relatedTags);
			createRelatedTags();
			addDropDownList(tagID, relatedTags);
		}
	}

	public void controlEvent(ControlEvent theEvent) {

		if (theEvent.isGroup()) {
			int tagId = (int) theEvent.getValue();
			selectedTags.add(tagId);
			selectedTagsNames.add(QeAData.getTagDictionary().get(tagId));
			updateUsefulQuestions(tagId);
			createRelatedTags();
			if (relatedTags.size() > 0) {
				// Add a new list
				addDropDownList(tagId, relatedTags);
			} else {
				// Disable the last list
				lists.get(lists.size() - 1).disableCollapse();
			}
		}

		QeAData.setTagList(selectedTags, selectedTagsNames);
	}

	private void addDropDownList(int tagId, ArrayList<Integer> newTags) {

		float x = ddlX + ddlWidth * (lists.size() % 3);
		float y = ddlY + ddlHeight * (PApplet.floor(lists.size() / 3));

		// Create the new DropdownList
		PFont font2 = mySketch.createFont("Helvetica", defaultFontSize);
		DropdownList newD = cp5
				.addDropdownList("Select Tag " + (lists.size() + 2))
				.setPosition(x, y).setBarHeight(20).setWidth(ddlWidth - 10)
				.setHeight(150).setColorBackground(mySketch.color(235))
				.setColorForeground(mySketch.color(highlightColor))
				.setColorLabel(0);
		newD.getCaptionLabel().toUpperCase(false).setLetterSpacing(3)
				.setFont(font2).setColor(0);

		// Add the new tags
		for (int tag : newTags) {
			newD.addItem(QeAData.getTagDictionary().get(tag), tag);
		}

		// Disable the previous or this list
		if (newTags.size() > 0) {
			lists.get(lists.size() - 1).disableCollapse();
		} else {
			newD.disableCollapse();
			newD.setLabel(QeAData.getTagDictionary().get(tagId));
		}

		// Add the list
		lists.add(newD);
	}

	public void clearList() {
		for (DropdownList list : lists) {
			list.remove();
		}
		lists.clear();
		relatedTags.clear();
		selectedTags.clear();
		selectedTagsNames.clear();
	}

	private void updateUsefulQuestions(Integer newTag) {
		Hashtable<Integer, ArrayList<Integer>> result = new Hashtable<Integer, ArrayList<Integer>>();
		for (int question : QeAData.getTagToQuestions().get(newTag)) {
			if (usefulQuestions.keySet().contains(question)) {
				result.put(question, usefulQuestions.get(question));
			}
		}

		usefulQuestions = result;
	}

	private void createRelatedTags() {
		relatedTags = new ArrayList<Integer>();
		for (int question : usefulQuestions.keySet()) {
			addRelatedTag(usefulQuestions.get(question));
		}

	}

	private void addRelatedTag(ArrayList<Integer> tagList) {
		for (int tag : tagList) {
			if (!selectedTags.contains(tag) && !relatedTags.contains(tag)) {
				relatedTags.add(tag);
			}
		}
	}
}