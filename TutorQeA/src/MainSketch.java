import java.io.IOException;
import java.text.ParseException;

import processing.core.PApplet;
import util.QeAData;

public class MainSketch extends PApplet {

	private static final long serialVersionUID = 1L;

	Sketch2 sketch2;
	Sketch3 sketch3;

	public void setup() {
		size(1000, 700);
		smooth();

		sketch2 = new Sketch2(this, 0, 0, 400, 280);
		sketch3 = new Sketch3(this, 0, 300, width, 500);

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
		
		sketch2.setup();
		sketch3.setup();
		
		// TODO: Delete this!!!!
		getSketch3().updateQuestions(
				QeAData.getQuestionIdsByCluster(5));

	}

	public void draw() {
		background(255);
		sketch2.draw();
		sketch3.draw();
	}

	@Override
	public void mousePressed() {
		sketch2.mousePressed();
	}

	public Sketch2 getSketch2() {
		return sketch2;
	}

	public Sketch3 getSketch3() {
		return sketch3;
	}
}
