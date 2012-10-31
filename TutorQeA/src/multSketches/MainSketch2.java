package multSketches;

import java.awt.GridLayout;
import java.io.IOException;
import java.text.ParseException;

import org.gicentre.utils.multisketch.SketchPanel;

import processing.core.PApplet;
import util.QeAData;

public class MainSketch2 extends PApplet {

	private static final long serialVersionUID = 1L;

	public static Sketch1 SKETCH_1 = new Sketch1(0, 0, 450, 350);
	public static Sketch2 SKETCH_2 = new Sketch2(450, 0, 550, 350);
	public static Sketch3 SKETCH_3 = new Sketch3(0, 380, 1000, 319);
	public static Sketch4 SKETCH_4 = new Sketch4(0, 350, 1000, 30);

	public void setup() {
		try {
			QeAData.readTagLinksFile();
			QeAData.readTagDictionaryFile();
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

		size(1001, 700);
		setLayout(new GridLayout(2, 2));
		noLoop();

		SketchPanel sp1 = new SketchPanel(this, SKETCH_1);
		add(sp1);
		SKETCH_1.setIsActive(true);

		SketchPanel sp2 = new SketchPanel(this, SKETCH_2);
		add(sp2);
		SKETCH_2.setIsActive(true);
		
		SketchPanel sp3 = new SketchPanel(this, SKETCH_4);
		add(sp3);
		SKETCH_4.setIsActive(true);
		
		SketchPanel sp4 = new SketchPanel(this, SKETCH_3);
		add(sp4);
		SKETCH_3.setIsActive(true);
	}
}
