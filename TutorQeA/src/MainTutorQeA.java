

import java.awt.GridLayout;
import java.io.IOException;

import org.gicentre.utils.multisketch.SketchPanel;

import processing.core.PApplet;
import processing.core.PFont;
import util.QeAData;

public class MainTutorQeA extends PApplet {

	private static final long serialVersionUID = 1L;

	public static SketchTop SKETCH_TOP = new SketchTop(1200, 450);
	public static SketchBottom SKETCH_BOTTOM = new SketchBottom(1200, 350);

	public static PFont mainFont;
	
	public void setup() {
		try {
			QeAData.readTagLinksFile();
			QeAData.readTagDictionaryFile();
			QeAData.readPostTagsFile();
			// The QuestionData and QuestionAnswers are read by demand, but
			// after demanded the first time, they are completely stored in
			// memory as the others.
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		size(1200, 801);
		setLayout(new GridLayout(2, 0));

		mainFont = loadFont("Purisa-Bold-48.vlw");
		textFont(MainTutorQeA.mainFont);
		
		SketchPanel spTop = new SketchPanel(this, SKETCH_TOP);
		spTop.setBounds(0, 0, 1250, 450);
		add(spTop);
		SKETCH_TOP.setIsActive(true);
		SKETCH_TOP.setParentSketch(this);

		SketchPanel spBottom = new SketchPanel(this, SKETCH_BOTTOM);
		spBottom.setBounds(0, 450, 1250, 400);
		add(spBottom);
		SKETCH_BOTTOM.setIsActive(true);
		SKETCH_BOTTOM.setParentSketch(this);
	}
}
