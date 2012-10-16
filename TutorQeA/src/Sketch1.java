import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PFont;
import util.CompositeSketch;
import util.QeAData;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.DropdownList;

public class Sketch1 implements CompositeSketch {

	ArrayList<DropdownList> lists = new ArrayList<DropdownList>();
	private ArrayList<Integer> selectedTags = new ArrayList<Integer>();
	private ArrayList<String> selectedTagsNames = new ArrayList<String>();
	private ArrayList<String> relatedTags = new ArrayList<String>();

	ControlP5 cp5;
	DropdownList d1;

	PApplet pApplet;
	int myWidth;
	int myHeight;
	int myXOrigin;
	int myYOrigin;

	int textFieldX, textFieldY, textFieldWidth, textFieldHeight;

	int buttonX, buttonY, buttonWidth, buttonHeight;

	int ddlX, ddlY, ddlWidth, ddlHeight;

	int defaultFontSize;
	int defaultFontColor;
	PFont font;

	public Sketch1(MainSketch parent, int xOrigin, int yOrigin, int width,
			int height) {
		pApplet = parent;

		myXOrigin = xOrigin;
		myYOrigin = yOrigin;
		myWidth = width;
		myHeight = height;

		textFieldX = myXOrigin + 20;
		textFieldY = myYOrigin + 20;
		textFieldWidth = myWidth - 100;
		textFieldHeight = 20;

		buttonX = myXOrigin + myWidth - 70;
		buttonY = myYOrigin + 20;
		buttonWidth = 50;
		buttonHeight = 20;

		ddlX = 20;
		ddlY = textFieldY + textFieldHeight + 40;
		ddlWidth = 100;
		ddlHeight = 20;
	}

	public void setup() {

		defaultFontSize = 11;
		defaultFontColor = 120;
		font = pApplet.createFont("Helvetica", defaultFontSize);
		pApplet.textFont(font);

		pApplet.noStroke();

		cp5 = new ControlP5(pApplet);

		cp5.addTextfield("input").setPosition(textFieldX, textFieldY)
				.setSize(textFieldWidth, textFieldHeight).setFont(font)
				.setFocus(true).setColor(pApplet.color(255))
				.setColorBackground(200).setColorForeground(200);

		cp5.addButton("search").setValue(0).setPosition(buttonX, buttonY)
				.setSize(buttonWidth, buttonHeight).setColorBackground(0);

	}

	public void draw() {
	}

	@Override
	public void mousePressed() {

	}

	public void controlEvent(ControlEvent theEvent) {

		if (theEvent.isGroup()) {
			selectedTags.add((int) theEvent.getValue());
			selectedTagsNames.add(QeAData.getTagDictionary().get(
					(int) theEvent.getValue()));
			addDropDownList(
					(int) theEvent.getValue(),
					intersct(
							relatedTags,
							new ArrayList<String>(Arrays.asList(QeAData
									.getTagLinks()
									.get((int) theEvent.getValue()).split(",")))));
		}
		QeAData.setTagList(selectedTags, selectedTagsNames);
	}

	public void search(int theValue) {
		clearList();
	}

	public void input(String theText) {
		clearList();

		ArrayList<Integer> keys = new ArrayList<Integer>(QeAData
				.getTagDictionary().keySet());
		ArrayList<String> values = new ArrayList<String>(QeAData
				.getTagDictionary().values());

		try {
			int tagID = keys.get(values.indexOf(theText));
			System.out.println(QeAData.getTagDictionary().get(tagID));
			selectedTags.add(tagID);
			selectedTagsNames.add(QeAData.getTagDictionary().get(tagID));
			relatedTags = new ArrayList<String>(Arrays.asList(QeAData
					.getTagLinks().get(tagID).split(",")));

			addDropDownList(
					tagID,
					intersct(
							relatedTags,
							new ArrayList<String>(Arrays.asList(QeAData
									.getTagLinks().get(tagID).split(",")))));
		} catch (Exception e) {
		}

	}

	private void addDropDownList(int tagID, ArrayList<String> newTags) {

		int x = ddlX + ddlWidth * (lists.size() % 3);
		int y = ddlY + ddlHeight * (PApplet.floor(lists.size() / 3));

		if (newTags.size() > 0) {

			if (QeAData.getTagDictionary().containsKey(tagID)) {

				DropdownList newD = cp5
						.addDropdownList("myList-d" + (lists.size() + 1))
						.setPosition(x, y).setColorBackground(0);

				for (String tag : newTags) {
					int id = Integer.valueOf(tag);
					newD.addItem(QeAData.getTagDictionary().get(id), id);
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

	private ArrayList<String> intersct(ArrayList<String> l1,
			ArrayList<String> l2) {
		ArrayList<String> newList = new ArrayList<String>();

		for (String s : l2) {
			if (!selectedTags.contains(Integer.valueOf(s))) {
				if (l1.contains(s)) {
					newList.add(s);
				}
			}
		}
		return newList;
	}
}
