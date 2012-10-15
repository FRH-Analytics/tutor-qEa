import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import processing.core.PApplet;
import util.QeAData;

public class MainSketch extends PApplet {

	private static final long serialVersionUID = 1L;

	NewSketch2 newSketch2;
	Sketch3 sketch3;

	public void setup() {
		size(1000, 700);
		smooth();

		newSketch2 = new NewSketch2(this, 500, 0, 500, 350);
		sketch3 = new Sketch3(this, 0, 400, width, 500);

		try {
			QeAData.readPostTagsFile();
			QeAData.readQuestionsDataFile();
			QeAData.readQuestionAnswersFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}

		newSketch2.setup();
		sketch3.setup();

		ArrayList<Integer> tagList = new ArrayList<Integer>();
//		tagList.add(41);
//		tagList.add(111);
//		tagList.add(264);
		// tagList.add(294);
		 tagList.add(528);
		ArrayList<String> tagNameList = new ArrayList<String>();
//		tagNameList.add("r");
//		tagNameList.add("regression");
//		tagNameList.add("logistic-regression");
		// tagNameList.add("roc");
		 tagNameList.add("hmm");
		QeAData.setTagList(tagList, tagNameList);

		// Reset the data
		newSketch2.updateChartData();

		// TODO: Delete this!!!!
		// getSketch3().updateQuestions(QeAData.getQuestionIdsByCluster(5));

	}

	public void draw() {
		background(255);
		newSketch2.draw();
		sketch3.draw();
	}

	@Override
	public void mousePressed() {
		newSketch2.mousePressed();
		sketch3.mousePressed();
	}

	public NewSketch2 getSketch2() {
		return newSketch2;
	}

	public Sketch3 getSketch3() {
		return sketch3;
	}
}
