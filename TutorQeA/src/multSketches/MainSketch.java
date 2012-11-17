package multSketches;

import java.awt.GridLayout;
import java.io.IOException;
import java.text.ParseException;

import org.gicentre.utils.multisketch.SketchPanel;

import processing.core.PApplet;
import util.QeAData;

public class MainSketch extends PApplet {

	private static final long serialVersionUID = 1L;

	public static SketchTop SKETCH_TOP = new SketchTop(0, 0, 1000, 350);
	public static SketchBottom SKETCH_BOTTOM = new SketchBottom(0, 350, 1000, 350);

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
		
		size(1001, 701);
		setLayout(new GridLayout(2, 0));

		SketchPanel spTop = new SketchPanel(this, SKETCH_TOP);
		spTop.setBounds(0, 0, 1000, 350);
		add(spTop);
		SKETCH_TOP.setIsActive(true);
		SKETCH_TOP.setParentSketch(this);
		
		SketchPanel spBottom = new SketchPanel(this, SKETCH_BOTTOM);
		spBottom.setBounds(0, 350, 1000, 350);
		add(spBottom);
		SKETCH_BOTTOM.setIsActive(true);
		SKETCH_BOTTOM.setParentSketch(this);
	}
}
