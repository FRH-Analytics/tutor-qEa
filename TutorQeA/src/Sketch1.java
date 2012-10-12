import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.DropdownList;
import processing.core.PApplet;
import processing.core.PFont;

// Simple example to show how two sketches can be created in separate windows. 
// Version 1.2, 18th July, 2009. 
// Author Jo Wood. 


public class Sketch1 extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String csvFilePath = "C:/Users/MATHEUS/workspace/rascunhoTutorQeA/TagsDictionary.csv";
	private static HashMap<Integer, String> tagDictionary = new HashMap<Integer, String>();
	private static HashMap<Integer, String> tagLinks = new HashMap<Integer, String>();

	private ArrayList<Integer> selectedTags = new ArrayList<>();
	private List<String> relatedTags = new ArrayList<>();

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

		System.out.println(tagDictionary.get(1));
		System.out.println(tagLinks.get(1));

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
			addDropDownList((int) theEvent.getValue());
		} else {
			System.out.println("fez algo");
		}

	}

	public void search(int theValue) {
		clearList();
	}

	public void input(String theText) {
		clearList();
		relatedTags = Arrays.asList(tagLinks.get(1).split(","));
		System.out.println(tagDictionary.get(1));
		addDropDownList(1);
	}

	private void addDropDownList(int tagID) {

		System.out.println("entrou com tagID: " + tagID);
		int x = 20 + 100 * (lists.size() % 3);
		int y = 80 + 20 * (floor(lists.size() / 3));

		if (tagDictionary.containsKey(tagID)) {

			DropdownList newD = cp5.addDropdownList(
					"myList-d" + (lists.size() + 1)).setPosition(x, y);
			for (String tag : relatedTags){
				int id = Integer.valueOf(tag);
				newD.addItem(tagDictionary.get(id), id);
			}

			lists.add(newD);
			selectedTags.add(tagID);
			try {
				lists.get(lists.size() - 2).disableCollapse();
			} catch (Exception e) {
			}

		}
	}

	public void clearList() {
		for (DropdownList list : lists) {
			list.remove();
		}
		lists = new ArrayList<>();
		relatedTags = new ArrayList<>();
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
	}
}