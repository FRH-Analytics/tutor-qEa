import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.gicentre.utils.colour.ColourTable;
import org.gicentre.utils.stat.BarChart;

import processing.core.PApplet;
import processing.core.PFont;
import au.com.bytecode.opencsv.CSVReader;

public class TagSelection extends PApplet {

	private static final long serialVersionUID = 1L;

	BarChart barChart; 
	float[] values = { 76, 24, 39, 18, 20 };
	ColourTable barColours = ColourTable.getPresetColourTable(ColourTable.PAIRED_12);

	String csvFilePath = "/home/augusto/git/tutor-qEa/TutorQeA/data/QuestionFeatures.csv";
	
	HashMap<Integer, Integer> questionClusterMap; 
	
	/**
	 * Initialises the sketch and loads data into the chart.
	 */
	public void setup() {
		size(600, 500);
		smooth();

		barChart = new BarChart(this);
		barChart.setData(values);

		// Scaling
		barChart.setMinValue(0);
		barChart.setMaxValue(100);

		// Axis appearance
		PFont font = createFont("Serif", 10);
		textFont(font, 10);

		barChart.showValueAxis(true);
//		barChart.setValueFormat("#%");
		barChart.showCategoryAxis(true);
		
		// Bar colours and appearance
		barChart.setBarColour(values, barColours);
		barChart.setBarGap(4);

		// Bar layout
		barChart.transposeAxes(false);
		
		try {
			 questionClusterMap = new HashMap<Integer, Integer>();
			readCSVFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readCSVFile() throws IOException {

		CSVReader reader = new CSVReader(new FileReader(csvFilePath));

		String[] nextLine = reader.readNext();
		while ((nextLine = reader.readNext()) != null) {
			questionClusterMap.put(Integer.valueOf(nextLine[0]), Integer.valueOf(nextLine[nextLine.length-1]));
		}
//		System.out.println(clusterMap);
	}
	
	/**
	 * Draws the chart in the sketch
	 */
	public void draw() {

		background(255);
		barChart.draw(15, 15, width - 30, height - 30);
	}
}
