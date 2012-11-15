package multSketches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.gicentre.utils.multisketch.EmbeddedSketch;

import processing.core.PApplet;
import processing.core.PFont;
import util.QeAData;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.DropdownList;

public class SubSketch1 {

	ArrayList<DropdownList> lists = new ArrayList<DropdownList>();
	private ArrayList<Integer> selectedTags = new ArrayList<Integer>();
	private ArrayList<String> selectedTagsNames = new ArrayList<String>();
	private ArrayList<Integer> relatedTags = new ArrayList<Integer>();
	private Hashtable<Integer, ArrayList<Integer>> usefulQuestions = new Hashtable<Integer, ArrayList<Integer>>();

	ControlP5 cp5;
	DropdownList d1;

	int textFieldX, textFieldY, textFieldWidth, textFieldHeight;

	int buttonX, buttonY, buttonWidth, buttonHeight;

	int ddlX, ddlY, ddlWidth, ddlHeight;

	int defaultFontSize;
	int defaultFontColor;
	PFont font;

	protected int myWidth;
	protected int myHeight;
	protected int myXOrigin;
	protected int myYOrigin;

	protected EmbeddedSketch mySketch;

	public SubSketch1(EmbeddedSketch parent, int xOrigin, int yOrigin, int width,
			int height) {
		mySketch = parent;

		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;

		textFieldX = myXOrigin + 20;
		textFieldY = myYOrigin + 20;
		textFieldWidth = myWidth - 50;
		textFieldHeight = 20;

		// buttonX = myXOrigin + myWidth - 70;
		// buttonY = myYOrigin + 20;
		// buttonWidth = 50;
		// buttonHeight = 20;

		ddlX = textFieldX;
		ddlY = textFieldY + 100;
		ddlWidth = 150;
		ddlHeight = 30;
	}

	public void setup() {
		// size(myXOrigin + myWidth, myYOrigin + myHeight);
		mySketch.smooth();

		defaultFontSize = 12;
		defaultFontColor = 120;
		font = mySketch.createFont("Helvetica", defaultFontSize - 1);
		mySketch.textFont(font);

		mySketch.noStroke();

		cp5 = new ControlP5(mySketch);

		cp5.addTextfield("input").setPosition(textFieldX, textFieldY)
				.setSize(textFieldWidth, textFieldHeight).setFont(font)
				.setFocus(true).setColor(mySketch.color(0, 0, 0))
				.setColorBackground(mySketch.color(255, 255, 255));

		cp5.addTextlabel("label1").setText("Initial Tag: ")
				.setPosition(textFieldX, textFieldY + 20).setColor(0)
				.setFont(mySketch.createFont("Helvetica", 20));

		cp5.addTextlabel("label3").setText("Tags of interest: ")
				.setPosition(textFieldX, textFieldY + 50).setColor(0)
				.setFont(mySketch.createFont("Helvetica", 20));
		// cp5.addButton("search").setValue(0).setPosition(buttonX, buttonY)
		// .setSize(buttonWidth, buttonHeight).setColorBackground(0);

	}

	public void draw() {
	}

	public void mousePressed() {
		// TODO Auto-generated method stub
	}

	public void controlEvent(ControlEvent theEvent) {

		if (theEvent.isGroup()) {
			int tagId = (int) theEvent.getValue();
			selectedTags.add(tagId);
			selectedTagsNames.add(QeAData.getTagDictionary().get(tagId));
			updateUsefulQuestions(tagId);
			createRelatedTags();
			addDropDownList(tagId, relatedTags);
		}

		QeAData.setTagList(selectedTags, selectedTagsNames);

		// SketchTop.SKETCH_2.updatePlot();
	}

	// public void search(int theValue) {
	// clearList();
	// }

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

		try {
			int tagID = keys.get(values.indexOf(theText));
			selectedTags.add(tagID);
			selectedTagsNames.add(QeAData.getTagDictionary().get(tagID));
			updateUsefulQuestions(tagID);
			createRelatedTags();
			addDropDownList(tagID, relatedTags);
			cp5.addTextlabel("label2").setText(theText.toUpperCase()).setPosition(textFieldX + 100,textFieldY + 20)
					.setColor(0).setFont(mySketch.createFont("Helvetica", 20));
		} catch (Exception e) {
		}

	}

	private void addDropDownList(int tagID, ArrayList<Integer> newTags) {

		int x = ddlX + ddlWidth * (lists.size() % 3);
		int y = ddlY + ddlHeight * (PApplet.floor(lists.size() / 3));

		if (newTags.size() > 0) {

			if (QeAData.getTagDictionary().containsKey(tagID)) {

				PFont font2 = mySketch.createFont("Helvetica", defaultFontSize);
				DropdownList newD = cp5
						.addDropdownList("Select Tag " + (lists.size() + 2))
						.setPosition(x, y).setBarHeight(20)
						.setWidth(ddlWidth - 10).setHeight(150);
				newD.getCaptionLabel().toUpperCase(false).setLetterSpacing(3)
						.setFont(font2).setColor(0);
				newD.setColorBackground(mySketch.color(200)).setColorActive(
						mySketch.color(255, 128));

				for (int tag : newTags) {
					newD.addItem(QeAData.getTagDictionary().get(tag), tag)
							.setColorLabel(0);
				}

				lists.add(newD);
				try {
					lists.get(lists.size() - 2).disableCollapse();
				} catch (Exception e) {
				}

				relatedTags = newTags;
			}
		} else {
			try {
				lists.get(lists.size() - 1).disableCollapse();
			} catch (Exception e) {
			}
		}
		mySketch.textFont(font);
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
	
	private void updateUsefulQuestions(Integer newTag){
		Hashtable<Integer, ArrayList<Integer>> result = new Hashtable<Integer, ArrayList<Integer>>();
		for(int question:QeAData.getTagToQuestions().get(newTag)){
			if(usefulQuestions.keySet().contains(question)){
				result.put(question, usefulQuestions.get(question));
			}
		}
		
		usefulQuestions = result;		
	}
	
	private void createRelatedTags(){
		relatedTags = new ArrayList<Integer>();
		for(int question:usefulQuestions.keySet()){
			addRelatedTag(usefulQuestions.get(question));
		}
		
	}
	
	private void addRelatedTag(ArrayList<Integer> tagList){
		for(int tag:tagList){
			if(!selectedTags.contains(tag) && !relatedTags.contains(tag)){
				relatedTags.add(tag);
			}
		}
	}

}
