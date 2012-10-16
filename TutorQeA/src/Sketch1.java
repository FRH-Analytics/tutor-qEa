import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

import processing.core.*;
import util.QeAData;
import controlP5.*;

public class Sketch1 extends PApplet {

	private static final long serialVersionUID = 1L;

	private String csvFilePath = "C:/Users/MATHEUS/workspace/rascunhoTutorQeA/TagsDictionary.csv";

	ArrayList<DropdownList> lists = new ArrayList<>();
	private ArrayList<Integer> selectedTags = new ArrayList<Integer>();
	private ArrayList<String> selectedTagsNames = new ArrayList<String>();
	private ArrayList<String> relatedTags = new ArrayList<String>();

	ControlP5 cp5;
	DropdownList d1;

	PApplet pApplet = this;
	int myWidth = 400;
	int myHeight = 400;
	int myXOrigin = 0;
	int myYOrigin = 0;

	int defaultFontSize;

	int defaultFontColor;
	PFont font;

	// public Sketch1(MainSketch parent, int xOrigin, int yOrigin, int width,
	// int height) {
	// pApplet = parent;
	//
	// myXOrigin = xOrigin;
	// myYOrigin = yOrigin;
	// myWidth = width;
	// myHeight = height;
	// }

	public void setup() {
		
		try {
			QeAData.readTagLinksFile();
			QeAData.readTagDictionaryFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pApplet.size(myWidth, myHeight);

		defaultFontSize = 11;
		defaultFontColor = 120;
		font = pApplet.createFont("Helvetica", defaultFontSize);
		pApplet.textFont(font);

		pApplet.noStroke();

		cp5 = new ControlP5(pApplet);

		cp5.addTextfield("input").setPosition(20, 20)
				.setSize(pApplet.width - 100, 20).setFont(font).setFocus(true)
				.setColor(pApplet.color(255)).setColorBackground(200).setColorForeground(200);

		cp5.addButton("search").setValue(0).setPosition(pApplet.width - 70, 20)
				.setSize(50, 20).setColorBackground(0);

	}

	public void draw() {
		background(255);
		noFill();
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
		} else {
			QeAData.setTagList(selectedTags, selectedTagsNames);
		}

	}

	public void search(int theValue) {
		System.out.println("2");
		clearList();
	}

	public void input(String theText) {
		
		System.out.println(3);
		
		clearList();
		
		ArrayList<Integer> keys = new ArrayList<>(QeAData.getTagDictionary().keySet());		
		ArrayList<String> values = new ArrayList<>(QeAData.getTagDictionary().values());
		
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
							new ArrayList<>(Arrays.asList(QeAData.getTagLinks()
									.get(tagID).split(",")))));
		} catch (Exception e) {
		}

	}

	private void addDropDownList(int tagID, ArrayList<String> newTags) {

		int x = 20 + 100 * (lists.size() % 3);
		int y = 80 + 20 * (PApplet.floor(lists.size() / 3));

		if (QeAData.getTagDictionary().containsKey(tagID)) {

			DropdownList newD = cp5.addDropdownList(
					"myList-d" + (lists.size() + 1)).setPosition(x, y).setColorBackground(0);

			for (String tag : newTags) {
				int id = Integer.valueOf(tag);
				newD.addItem(QeAData.getTagDictionary().get(id), id);
			}

			lists.add(newD);
			try {
				lists.get(lists.size() - 2).disableCollapse();
			} catch (Exception e) {
			}
		}
		relatedTags = newTags;
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

	public void readCSVFile(HashMap<Integer, String> table) throws IOException {

		CSVReader reader = new CSVReader(new FileReader(csvFilePath));

		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			int key = Integer.valueOf(nextLine[0]);
			if (table.containsKey(key)) {
				table.put(Integer.valueOf(nextLine[0]),
						table.get(Integer.valueOf(nextLine[0])) + ","
								+ nextLine[1]);
			} else {
				table.put(Integer.valueOf(nextLine[0]), nextLine[1]);
			}
		}
		reader.close();
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
