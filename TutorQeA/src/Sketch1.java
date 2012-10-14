import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

import processing.core.*;
import controlP5.*;

public class Sketch1 extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String csvFilePath = "C:/Users/MATHEUS/workspace/rascunhoTutorQeA/TagsDictionary.csv";
	private static HashMap<Integer, String> tagDictionary = new HashMap<Integer, String>();
	private static HashMap<Integer, String> tagLinks = new HashMap<Integer, String>();

	private ArrayList<Integer> selectedTags = new ArrayList<>();
	private ArrayList<String> relatedTags = new ArrayList<>();

	ControlP5 cp5;

	ArrayList<DropdownList> lists = new ArrayList<>();

	PFont font = createFont("arial", 12);

	DropdownList d1;

	public void setup() {
		size(400, 400);

		try {
			readCSVFile(tagDictionary);
			csvFilePath = "C:/Users/MATHEUS/workspace/rascunhoTutorQeA/TagLinks.csv";
			readCSVFile(tagLinks);
		} catch (IOException e) {
			e.printStackTrace();
		}

		noStroke();
		cp5 = new ControlP5(this);

		cp5.addTextfield("input").setPosition(20, 20).setSize(width - 100, 20)
				.setFont(font).setFocus(true).setColor(color(255));

		cp5.addButton("search").setValue(0).setPosition(width - 70, 20)
				.setSize(50, 20);

	}

	public void draw() {
		background(0);
		fill(255);
	}

	public void controlEvent(ControlEvent theEvent) {

		if (theEvent.isGroup()) {
			selectedTags.add((int) theEvent.getValue());
			addDropDownList(
					(int) theEvent.getValue(),
					intersct(
							relatedTags,
							new ArrayList<String>(Arrays.asList(tagLinks.get(
									(int) theEvent.getValue()).split(",")))));
		} else {
		}

	}

	public void search(int theValue) {
		clearList();
	}

	public void input(String theText) {
		
		clearList();

		ArrayList<String> values = new ArrayList<>(tagDictionary.values());
		
		try {
			int tagID = values.indexOf(theText) + 1;
			selectedTags.add(tagID);
			relatedTags = new ArrayList<String>(Arrays.asList(tagLinks.get(tagID).split(",")));
			
			addDropDownList(tagID, intersct(relatedTags, new ArrayList<>(Arrays.asList(tagLinks.get(tagID).split(",")))));
		} catch (Exception e) {
		}
		

		
	}

	private void addDropDownList(int tagID, ArrayList<String> newTags) {

		int x = 20 + 100 * (lists.size() % 3);
		int y = 80 + 20 * (floor(lists.size() / 3));

		if (tagDictionary.containsKey(tagID)) {

			DropdownList newD = cp5.addDropdownList(
					"myList-d" + (lists.size() + 1)).setPosition(x, y);

			for (String tag : newTags) {
				int id = Integer.valueOf(tag);
				newD.addItem(tagDictionary.get(id), id);
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
		lists = new ArrayList<>();
		relatedTags = new ArrayList<>();
		selectedTags = new ArrayList<>();
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

	private ArrayList<String> intersct(ArrayList<String> l1, ArrayList<String> l2) {
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
