import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class pixy extends PApplet {

PApplet sketchRef = this;

App app;
String[] vertexShader;
String[] fragmentShader;
CheckBox checkbox;

PImage logo;

PGraphics renderer;

boolean begin = true;


String textGreet = "Hello, my name is Pixy. And I am here to generate images with you!\nBegin with choosing an image you like. You can generate new images with *NEW* button, and adjust the grid with *+* and *-* buttons under *SIZE* label. By the way, *AA* parameter sets quality of my images!\nTo see an image in detail, press on it. You can navigate through the image by dragging, and zoom with keys *a* and *z*! (You can also use arrows to navigate)\nWhen you found one that you like, you can select it with small button on it, and then develop with *DEVELOP* button. If you don\u2019t like the results, you can click *REPEAT* to generate more!\nIf you find several images that you like, you can merge them by selecting all of them. But be careful! This is experimental feature and results may look more different than you expect!\nWhen you are satisfied with the image, you can save it by selecting or opening and hitting *EXPORT* button! Don\u2019t forget to set the size for your image! It will be saved in the folder \u2018export\u2019 with random name in the location of the app.\nYou can also animate images with controls in the bottom of the screen. Upper slider sets loop time! To save animation, press *RENDER*. It will be saved in the \u2018renders\u2019 folder as image sequence.\nGood luck!";
 
public void settings() {
	size(1280,720,P2D);
	PJOGL.setIcon("data/logo.png");
}

public void setup() {
	surface.setResizable(true);

	logo = loadImage("logo.png");


	pushStyle();
	background(17);

	imageMode(CENTER);
	image(logo, width/2, height/2, 200,200);
	popStyle();

	vertexShader = loadStrings("data/vertex.glsl");
	fragmentShader = loadStrings("data/fragment.glsl");

	renderer = createGraphics(800,800,P2D);

	app = new App();

}

public void draw() {
	background(17);

		app.run();

}

public void mousePressed() {
	app.mousePressed();
}

public void mouseReleased() {
	app.mouseReleased();
}

public void keyPressed() {
	app.keyPressed();
}

public void keyReleased() {
	app.keyReleased();
}

public void mouseMoved() {
	app.mouseMoved();
}

ControlP5 cp5time;
ControlP5 cp5gen;
ControlP5 cp5main;
ControlP5 cp5back;

ControlP5 selectButtons;

class App {
	Pop pop;
	int aa = 1;
	int expSize = 1000;

	int lastSel = -1;
	
	int popRow;
	int popSize;
	int genSize;

	boolean goSingle = false;

	NodeDisplay nd = new NodeDisplay();

	float mutationRate = 100;

	// TIME

	float appTime = 0;
	float timeFreq = 60;
	boolean timeRun = false;

	// RENDERING

	boolean isRender;
	int renderFrameCount = 0;
	int renderID;

	// general

	String view = "GRID";
	String uiview = "GENERAL";

	int focusedId;
	boolean isFocused = false;

	int lastIdPressed = -1;
	
	// // GridView

	PVector[] gridPos;
	PVector gridScale;

	// LAYOUT

	float generalMargin = 20;
	float gridMargin = 10;

	float separatorWidth = 10;
	float separator = (float) height/width;
	boolean separatorIsMoving = false;
	float separatorLimitMin = 0.2f;
	float separatorLimitMax = 0.8f;

	PVector displaySize;
	PVector uiPos;
	PVector uiSize;

	int uiblock = 10;


	int mainColor = color(155,0,255);
	int mainColorOver = color(187,77,255);
	int mainColorDown = color(93,0,158);

	int grayNormal = color(170);
	int grayNormalOver = color(212);
	int grayNormalDown = color(117);
	int grayDark = color(66);

	// BUTTONS UI

	PVector[] timeRect;
	Button bTimePlay;
	Button bTimePause;
	Button bTimeStop;
	Slider sTimeFreq;
	Slider sTimePos;
	Textlabel tTime;

	PVector[] genRect;
	Button bGenPlus;
	Button bGenMinus;
	Button bGenBack;
	Button bGenFov;
	Button bGenExp;
	Button bGenRen;
	Textlabel tPopSize;
	Textlabel tGenNum;
	PVector[] genMutRect;
	Textlabel tExpSize;
	Slider sExpSize;

	PVector[] mainRect;
	Button bMainEvolve;
	Button bMainAgain;
	Button bMainNew;
	Button bBack;

	ArrayList<Button> selButs = new ArrayList<Button>();

	PFont font = createFont("font.ttf", uiblock+2);
	PFont fontbig = createFont("font.ttf", (uiblock*2+2));

	App() {
		pop = new Pop(this);
		setPopSize(5);
		controls();
	}

	public void run() {
		keyIsPressed();
		mouseIsPressed();
		updateLayout();
		displayPop();
		runTime();
		if (isRender) render();
	}


	public void runTime() {
		if (timeRun) {
			appTime += (float) 1 / timeFreq / 60;
			sTimePos.setValue(appTime);
			if (appTime >= 1) {
				appTime = 0;
				sTimePos.setValue(appTime);
				if (isRender) {
					isRender = false;
					actionTimeStop();
				}
			}
		}
	}

	public void render() {
		pop.arts.get(lastSel).render("renders/render"+renderID+"/frame"+renderFrameCount+".jpg");
		renderFrameCount++;
	}

	public void beginRender() {
		
		renderer = createGraphics(expSize,expSize,P2D);

		appTime = 0;
		renderFrameCount = 0;
		renderID = (int) random(99999);
		isRender = true;
		actionTimePlay();
	}




	public void setPopSize(int n) {
		popRow = n;
		popSize = n*n;
		pop.setPopSize(n);
	}

	public void randomPop() {
		pop.randomPop();
	}

	// DISPLAY POP

	public void displayPop() {
		if (view == "SINGLE") displaySingleView();
		if (view == "GRID") displayGridView();
	}

	public void displaySingleView() {
		pop.display(focusedId, 0, 0, displaySize.x, displaySize.y);
	}

	public void displayGridView() {
		for (int i = 0; i < pop.arts.size(); i++) {
			pop.display(i, gridPos[i].x, gridPos[i].y, gridScale.x, gridScale.y);
		}
		displayStroke();
	}

	public void displayStroke() {
		pushStyle();
		noFill();
		for (int i = 0; i < popSize; i++) {
			if (pop.arts.get(i).isSelected) {
				if (i == focusedId) stroke(mainColorOver);
				else stroke(mainColor);
				strokeWeight(3);
			} else if (isFocused && i == focusedId) {
				stroke(150);
				strokeWeight(3);

			} else {
				strokeWeight(1);
				stroke(77);
			}
			rect(gridPos[i].x, gridPos[i].y, gridScale.x, gridScale.y);
		}
		popStyle();
	}

	// UPDATE LAYOUT

	public void updateLayout() {

		if (goSingle) {
			view = "GRID";
			goSingle = false;
		}


		cp5time.setGraphics(sketchRef,0,0);
		cp5gen.setGraphics(sketchRef,0,0);
		cp5main.setGraphics(sketchRef,0,0);
		cp5back.setGraphics(sketchRef,0,0);

		selectButtons.setGraphics(sketchRef,0,0);


		displaySize = new PVector(separator*width, height);
		uiPos = new PVector(separator*width + separatorWidth + generalMargin, generalMargin);
		uiSize = new PVector(width-uiPos.x-generalMargin, height-generalMargin*2);
		countGrid();
		updateControls();
		displayUI();
	}

	public void countGrid() {
		gridPos = new PVector[popSize];
		gridScale = new PVector((displaySize.x-generalMargin*2+gridMargin)/popRow - gridMargin, (displaySize.y-generalMargin*2+gridMargin)/popRow - gridMargin);
		int count = 0;
		for (int iy = 0; iy < popRow; iy++) {
			for (int ix = 0; ix < popRow; ix++) {

				gridPos[count] = new PVector( (generalMargin) + ix*(gridScale.x+gridMargin), (generalMargin) + iy*(gridScale.y+gridMargin));
				count++;

			}
		}
	}

	public void updateControls() {
		updateTimeBlock();
		updateGenBlock();
		updateMainBlock();
		updateSelButs();
	}

	public void displayUI() {
		displaySeparator();
		displayGeneral();

		if (view == "SINGLE") {
			cp5back.show();
		} else {
			cp5back.hide();
		}

		if (view == "SINGLE") {
			nd.display(pop.arts.get(focusedId).dna, uiPos.x, uiPos.y, uiSize.x, uiSize.x);
			pushStyle();
			stroke(grayNormal);
			noFill();
			rect(uiPos.x, uiPos.y, uiSize.x, uiPos.y+uiSize.y-uiblock*23);
			popStyle();
		} else if (isFocused) {
			pop.display(focusedId, uiPos.x, uiPos.y, uiSize.x, uiPos.y+uiSize.y-uiblock*23);
			pushStyle();
			stroke(grayNormal);
			noFill();
			rect(uiPos.x, uiPos.y, uiSize.x, uiPos.y+uiSize.y-uiblock*23);
			popStyle();
		} else if (lastSel != -1) {
			pop.display(lastSel, uiPos.x, uiPos.y, uiSize.x, uiPos.y+uiSize.y-uiblock*23);
			pushStyle();
			stroke(grayNormal);
			noFill();
			rect(uiPos.x, uiPos.y, uiSize.x, uiPos.y+uiSize.y-uiblock*23);
			popStyle();
		} else {
			pushStyle();
			fill(240);
			textFont(font);
			text(textGreet,uiPos.x, uiPos.y, uiSize.x, uiSize.x);
			popStyle();
		}
	}

	public void displaySeparator() {
		pushStyle();
		if (mouseOver(separator,0,separatorWidth,height)) fill(55);
		else fill(44);
		noStroke();
		rect(separator*width,0,separatorWidth,height);
		popStyle();
	}

	public void displayGeneral() {
		displayTime();
		displayGeneBlock();
		displayMainBlock();
		displayButtons();
	}

	public void displayTime() {
		pushStyle();
		noFill();
		stroke(grayNormal);
		rect(timeRect[0].x,timeRect[0].y,timeRect[1].x,timeRect[1].y);
		popStyle();

		sTimePos.setValue(appTime);
		tTime.setText("time "+(float) round(timeFreq*10)/10+"s");
	}

	public void displayMainBlock() {
		pushStyle();
		noFill();
		stroke(grayNormal);
		rect(mainRect[0].x,mainRect[0].y,mainRect[1].x,mainRect[1].y);
		popStyle();
	}

	public void displayGeneBlock() {
		pushStyle();
		noFill();
		stroke(grayNormal);
		rect(genRect[0].x,genRect[0].y,genRect[1].x,genRect[1].y);
		popStyle();

		tPopSize.setText("POP: "+popSize);
		tGenNum.setText("AA: "+aa);
		tExpSize.setText("SIZE: "+expSize+"px");
	}

	public void displayButtons() {

		if (selButs.size() < popSize) {
			for (int i = selButs.size(); i < popSize; i++) {
				selButs.add(addBut(i));
			}
		} else if (selButs.size() > popSize) {
			for (int i = 0; i < selButs.size() - popSize; i++) {
				selButs.remove(selButs.size()-1);
			}
		}



		for (int i = 0; i < popSize; i++) {
			if (isFocused && focusedId == i && view == "GRID") {
				selButs.get(i).show();
			} else {
				selButs.get(i).hide();	
			}
		}
	}








	public boolean isAnySelected() {
		for (int i = 0; i < pop.arts.size(); i++) {
			Artwork a = pop.arts.get(i);
			if (a.isSelected) {
				lastSel = i;
				return true;
			}
		}
		return false;
	}

	// Population control


	public Button addBut(int n) {
		Button temp = selectButtons.addButton("baton"+n);
		temp.setSize(uiblock*2,uiblock*2);

		temp.plugTo(this)
		.setId(n)
		.setLabelVisible(false)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver);
		return temp;
	}

	public void selButAction(int n) {
		if (!pop.arts.get(n).isSelected) {
			selButs.get(n).setColorBackground(mainColor)
			.setColorActive(mainColorDown) 
			.setColorForeground(mainColorOver);
			pop.arts.get(n).isSelected = true;

			lastSel = n;

		} else {
			selButs.get(n).setColorBackground(grayNormal)
			.setColorActive(grayNormalDown) 
			.setColorForeground(grayNormalOver);
			pop.arts.get(n).isSelected = false;

			lastSel = -1;
		}

		if (view == "SINGLE") {
			lastSel = focusedId;
		}

		if (isAnySelected()) {
			bMainEvolve
				.setColorBackground(mainColor)
				.setColorActive(mainColorOver) 
				.setColorForeground(mainColorDown)
					.getCaptionLabel()
					.setColor(color(255));
		} else {
			bMainEvolve
				.setColorBackground(grayDark)
				.setColorActive(grayDark) 
				.setColorForeground(grayDark)
					.getCaptionLabel()
					.setColor(grayNormalDown);
		}
	}



	public void increasePop() {
		popRow++;
		setPopSize(popRow);

		bGenMinus
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver);

	}

	public void decreasePop() {
		if (popRow > 1) {
			popRow--;
			setPopSize(popRow);		
		} 
		if (popRow <= 1){
			bGenMinus
			.setColorBackground(grayDark)
			.setColorActive(grayDark)
			.setColorForeground(grayDark);

		}
	}

	// Population Display







	// // Population Display Layout


	public void separatorMove() {
		separator = (mouseX-separatorWidth/2)/width;
		if (separator < separatorLimitMin) separator = separatorLimitMin;
		if (separator > separatorLimitMax) separator = separatorLimitMax;
	}



	// Population Grid UI

	public void checkArtsFocus() {
		isFocused = false;
		for (int i = 0; i < popSize; i++) {
			if (mouseOver(gridPos[i].x, gridPos[i].y, gridScale.x, gridScale.y)) {
				isFocused = true;
				focusedId = i;
			}
		}
	}

	// UI display






	// Global UI Events

	public void mousePressed() {
		if (mouseOver(separator*width,0, separatorWidth, height)) separatorIsMoving = true;
		if (isFocused && !selButs.get(focusedId).isMouseOver()) {
			lastIdPressed = focusedId;
			lastSel = focusedId;
		}
	}

	public void mouseIsPressed() {
		if (mousePressed) {
			if (view == "SINGLE" && mouseOver (0,0,displaySize.x,displaySize.y)) {
				pop.arts.get(focusedId).mouseMove();
			}

			if (separatorIsMoving) separatorMove();

			if (view == "GRID") {
				checkArtsFocus();
			}
		}
	}

	public void mouseReleased() {
		if (lastIdPressed == focusedId && view != "SINGLE") {
			view = "SINGLE";
		}
		lastIdPressed = -1;

		if (separatorIsMoving) separatorIsMoving = false;
	}

	public void mouseMoved() {
		if (view == "GRID") {
			checkArtsFocus();
		}
	}

	public void keyPressed() {
		if (key == ' ') {
			pop.randomPop();
		}

		if (key == BACKSPACE) {
			view = "GRID";
		}

		if (key == 's') {
			pop.arts.get(focusedId).export();
		}

		if (key == 'r') {

		}

		if (key == 'm') {
		}

		if (key == 'n') {
		}

		if (key == 'c') {
			pop.arts.get(focusedId).isSelected = !pop.arts.get(focusedId).isSelected;
		}

		if (key == 'x') {
			pop.evolve();
		}

		if (key == 't') {
			appTime = 0;
			timeRun = !timeRun;
		}

		if (key == '1') aa = 1;
		if (key == '2') aa = 2;
		if (key == '3') aa = 3;
		if (key == '4') aa = 4;
		if (key == '5') aa = 5;
		if (key == '6') aa = 6;

	}
		
	public void keyReleased() {

	}

	public void keyIsPressed() {
		if (keyPressed) {

		if (key == CODED && keyCode == UP) {
			pop.arts.get(focusedId).addOffset(0,0.05f);
		}
		
		if (key == CODED && keyCode == DOWN) {
			pop.arts.get(focusedId).addOffset(0,-0.05f);
		}
		
		if (key == CODED && keyCode == LEFT) {
			pop.arts.get(focusedId).addOffset(-0.05f,0);
		}
		
		if (key == CODED && keyCode == RIGHT) {
			pop.arts.get(focusedId).addOffset(0.05f,0);
		}

		if (keyPressed && key == 'z') {
			pop.arts.get(focusedId).addScale(1.05f);
		}

		if (keyPressed && key == 'a') {
			pop.arts.get(focusedId).addScale(0.95f);
		}

		}
	}










	public void updateTimeBlock() {
		timeRect = new PVector[] {
			new PVector((int) uiPos.x, (int) uiPos.y+uiSize.y-uiblock*6),
			new PVector((int) uiSize.x, (int) uiblock*6)
		};

		bTimePlay.setPosition((int) uiPos.x + uiblock*1, (int) uiPos.y+uiSize.y-uiblock-uiblock*2)
			.setSize(uiblock*2,uiblock*2);
		bTimePause.setPosition((int) uiPos.x + uiblock*4, (int) uiPos.y+uiSize.y-uiblock-uiblock*2)
			.setSize(uiblock*2,uiblock*2);
		bTimeStop.setPosition((int) uiPos.x + uiblock*7, (int) uiPos.y+uiSize.y-uiblock-uiblock*2)
			.setSize(uiblock*2,uiblock*2);

		sTimeFreq.setPosition((int) uiPos.x + uiblock*10, (int) uiPos.y+uiSize.y-uiblock-uiblock*4)
			.setSize((int) uiSize.x-uiblock-uiblock*10, (int) uiblock*1);
		sTimePos.setPosition((int) uiPos.x + uiblock*10, (int) uiPos.y+uiSize.y-uiblock-uiblock*2)
			.setSize((int) uiSize.x-uiblock-uiblock*10, (int) uiblock*2);


		tTime.setPosition((int) uiPos.x + uiblock*1 - 3, (int) uiPos.y+uiSize.y-uiblock-uiblock*4 - 4);
	}

	public void updateGenBlock() {
		genRect = new PVector[] {
			new PVector((int) uiPos.x, (int) uiPos.y+uiSize.y-uiblock*13),
			new PVector((int) uiSize.x, (int) uiblock*6)
		};

		bGenMinus.setPosition((int) uiPos.x + uiblock, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize(uiblock*3,uiblock*2);
		bGenPlus.setPosition((int) uiPos.x + uiblock*5, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize(uiblock*3,uiblock*2);
		bGenBack.setPosition((int) uiPos.x + uiblock*9, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize(uiblock*3,uiblock*2);
		bGenFov.setPosition((int) uiPos.x + uiblock*13, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize(uiblock*3,uiblock*2);
		bGenExp.setPosition((int) uiPos.x + uiblock*17, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize((int) abs(uiSize.x-(uiblock*18))/2-uiblock/2,uiblock*2);
		bGenRen.setPosition((int) uiPos.x + uiblock*17 + abs(uiSize.x-(uiblock*18))/2+uiblock/2, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize((int) abs(uiSize.x-( uiblock*18))/2-uiblock/2,uiblock*2);

		sExpSize.setPosition((int) uiPos.x + uiblock*27, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*1)
			.setSize((int) uiSize.x - uiblock*28, (int) uiblock*1);

		tPopSize.setPosition((int) uiPos.x + uiblock -3, (int) uiPos.y+uiSize.y-uiblock*13+uiblock-4);
		tGenNum.setPosition((int) uiPos.x + uiblock*9 -3, (int) uiPos.y+uiSize.y-uiblock*13+uiblock-4);
		tExpSize.setPosition((int) uiPos.x + uiblock*17 -3, (int) uiPos.y+uiSize.y-uiblock*13+uiblock-4);
	}

	public void updateMainBlock() {
		mainRect = new PVector[] {
			new PVector((int) uiPos.x, (int) uiPos.y+uiSize.y-uiblock*20),
			new PVector((int) uiSize.x, (int) uiblock*6)
		};

		int mid = (int) (uiSize.x - uiblock*4)/3;

		bMainEvolve.setPosition(PApplet.parseInt(uiPos.x + uiblock), (int) uiPos.y+uiSize.y-uiblock*19)
			.setSize(mid, uiblock*4);
		bMainAgain.setPosition(PApplet.parseInt(uiPos.x + uiSize.x/2 - mid/2), (int) uiPos.y+uiSize.y-uiblock*19)
			.setSize(mid, uiblock*4);
		bMainNew.setPosition(PApplet.parseInt(uiPos.x + uiSize.x - mid - uiblock), (int) uiPos.y+uiSize.y-uiblock*19)
			.setSize(mid, uiblock*4);

		bBack.setPosition(PApplet.parseInt(uiPos.x), (int) uiPos.y+uiSize.y-uiblock*25)
			.setSize((int) uiSize.x, uiblock*4);


		if (pop.lastPool.size() > 0) {
			bMainAgain
				.setColorBackground(grayNormal)
				.setColorActive(grayNormalDown) 
				.setColorForeground(grayNormalOver)
				.getCaptionLabel()
				.setColor(color(255))
				.setFont(fontbig);	
		} else {
			bMainAgain
				.setColorBackground(grayDark)
				.setColorActive(grayDark) 
				.setColorForeground(grayDark)
					.getCaptionLabel()
					.setColor(grayNormalDown);
		}
		
	}

	public void updateSelButs() {
		for (int i = 0; i < selButs.size(); i++) {
			if (i < popSize) {
				selButs.get(i).setPosition((int) gridPos[i].x + uiblock, (int) gridPos[i].y + gridScale.y - uiblock*3);
			}
		}
	}















	public void controls() {

		cp5time = new ControlP5(sketchRef);

		bTimePlay = cp5time.addButton("actionTimePlay");
		bTimePlay.setLabelVisible(true)
		.setLabel(">")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDark)
			.setFont(font);

		bTimePause = cp5time.addButton("actionTimePause");
		bTimePause.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
		.setLabel("||")
			.getCaptionLabel()
			.setColor(grayDark)
			.setFont(font);
		bTimePause.setLabelVisible(true);

		bTimeStop = cp5time.addButton("actionTimeStop");
		bTimeStop.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
		.setLabel("x")
			.getCaptionLabel()
			.setColor(grayDark)
			.setFont(font);
		bTimeStop.setLabelVisible(true);

		sTimeFreq = cp5time.addSlider("timeFreq")
		.plugTo(this)
		.setRange(0.5f,10)
		.setValue(5)
		.setLabelVisible(false)
		.setColorActive(grayNormalOver)
		.setColorBackground(grayDark)
		.setColorForeground(grayNormal);
		sTimePos = cp5time.addSlider("appTime")
		.plugTo(this)
		.setLabelVisible(false)
		.setRange(0,1)
		.setValue(0)
		.setColorActive(grayNormal)
		.setColorBackground(grayDark)
		.setColorForeground(grayNormalDown);

		tTime = cp5time.addTextlabel("timelabel").setFont(font).setColor(grayNormal);




		selectButtons = new ControlP5(sketchRef);

		cp5gen = new ControlP5(sketchRef);
		cp5back = new ControlP5(sketchRef);

		bGenPlus = cp5time.addButton("actionGenPlus");
		bGenPlus.setLabelVisible(true)
		.setLabel("+")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDark)
			.setFont(font);
		bGenMinus = cp5time.addButton("actionGenMinus");
		bGenMinus.setLabelVisible(true)
		.setLabel("-")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDark)
			.setFont(font);
		bGenBack = cp5time.addButton("actionAAm");
		bGenBack.setLabelVisible(true)
		.setLabel("-")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDark)
			.setFont(font);		
		bGenFov = cp5time.addButton("actionAAp");
		bGenFov.setLabelVisible(true)
		.setLabel("+")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDark)
			.setFont(font);
		bGenExp = cp5time.addButton("actionExp");
		bGenExp.setLabelVisible(true)
		.setLabel("EXPORT")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDark)
			.setFont(font);		
		bGenRen = cp5time.addButton("actionRen");
		bGenRen.setLabelVisible(true)
		.setLabel("RENDER")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDark)
			.setFont(font);

		sExpSize = cp5time.addSlider("expSize")
		.plugTo(this)
		.setRange(500,4000)
		.setValue(expSize)
		.setLabelVisible(false)
		.setColorActive(grayNormalOver)
		.setColorBackground(grayDark)
		.setColorForeground(grayNormal);

		tGenNum = cp5time.addTextlabel("genenum").setFont(font).setColor(grayNormal);
		tPopSize = cp5time.addTextlabel("genepopsize").setFont(font).setColor(grayNormal);
		tExpSize = cp5time.addTextlabel("genemutrate").setFont(font).setColor(grayNormal);



		cp5main = new ControlP5(sketchRef);

		bMainEvolve = cp5time.addButton("actionMainEvolve");
		bMainEvolve.setLabelVisible(true)
		.setLabel("develop")
		.plugTo(this)
		.setColorBackground(grayDark)
		.setColorActive(grayDark) 
		.setColorForeground(grayDark)
			.getCaptionLabel()
			.setColor(grayNormalDown)
			.setFont(fontbig);
		bMainAgain = cp5time.addButton("actionMainAgain");
		bMainAgain.setLabelVisible(true)
		.setLabel("repeat")
		.plugTo(this)
		.setColorBackground(grayDark)
		.setColorActive(grayDark) 
		.setColorForeground(grayDark)
			.getCaptionLabel()
			.setColor(grayNormalDown)
			.setFont(fontbig);
		bMainNew = cp5time.addButton("actionMainNew");
		bMainNew.setLabelVisible(true)
		.setLabel("new")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(color(255))
			.setFont(fontbig);		



		bBack = cp5back.addButton("actionBack");
		bBack.setLabelVisible(true)
		.setLabel("back")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(color(255))
			.setFont(fontbig);		

	}



	/////////// ACTIONS

	public void actionBack() {
		goSingle = true;
	}

	public void actionTimePlay() {
		timeRun = true;
		bTimePlay.setColorBackground(mainColor)
		.setColorActive(mainColorDown) 
		.setColorForeground(mainColorOver)
		.getCaptionLabel()
			.setColor(grayNormalOver);

	}

	public void actionTimePause() {
		timeRun = false;
		bTimePlay.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
		.getCaptionLabel()
			.setColor(grayDark);
	}

	public void actionTimeStop() {
		timeRun = false;
		appTime = 0;
		bTimePlay.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
		.getCaptionLabel()
			.setColor(grayDark);
	}

	public void actionGenPlus() {
		increasePop();
	}

	public void actionGenMinus() {
		decreasePop();
	}

	public void actionAAm() {
		if (aa > 1) aa--;
	}	

	public void actionAAp() {
		if (aa < 16) aa++;
	}

	public void actionExp() {
		if (lastSel >= 0) {
			pop.arts.get(lastSel).export();
		}
	}

	public void actionRen() {
			if (isRender || lastSel == -1) isRender = false;
			else beginRender();
	}

	public void actionMainNew() {
		randomPop();

		bMainEvolve
			.setColorBackground(grayDark)
			.setColorActive(grayDark) 
			.setColorForeground(grayDark)
				.getCaptionLabel()
				.setColor(grayNormalDown);



	}

	public void actionMainEvolve() {
		if (isAnySelected()) {
			pop.evolve();
		}
	}

	public void actionMainAgain() {
		pop.evolveAgain();
	}



}

public void controlEvent(ControlEvent theEvent) {
  if (theEvent.isController()) {
    if (theEvent.controller().getName().startsWith("baton")) {
      int id = theEvent.controller().getId();

        app.selButAction(id);
 
    }
  }
}

public boolean mouseOver(float x, float y, float w, float h) {
	return(mouseX > x && mouseX < x+w && mouseY > y && mouseY < y+h);
}
class Artwork {
	Pop p;
	int id;

	float x;
	float y;
	float w;
	float h;

	float resolution;
	float g_scale;
	PVector g_offset;

	PShader shader;
	String[] shaderCode = fragmentShader.clone();

	ArrayList<PVector> animatedArgs;

	boolean isSelected = false;
	
	DNA dna;


	Artwork(Pop p_) {
		p = p_;
		id = p.arts.size()-1;

		x = 0;
		y = 0;
		w = 0;
		h = 0;

		shader = loadShader("data/fragment.glsl");
	}

	// GENERAL

	public void randomDNA() {
		dna = new DNA("RANDOM");
		compileShader();
	}

	public void assignDNA(DNA d) {
		dna = d;
		compileShader();
	}

	// RENDERING

	public void display(float x_, float y_, float w_, float h_) {
		update(x_,y_,w_,h_);
		setShader();
		shader(shader);
		rect(x_,y_,w,h);
		resetShader();
	}

	public void compileShader() {
		shaderCode[shaderCode.length - 14] = dna.code;
		saveStrings("data/temp/shader"+id, shaderCode);
		shader = loadShader("data/temp/shader"+id);

	}

	public void setShader() {
		shader.set("u_g_off", g_offset.x, g_offset.y);
		shader.set("u_g_scale", g_scale);
		shader.set("u_off", dna.offset.x, dna.offset.y);
		shader.set("u_scale", dna.scale);
		shader.set("u_hoff", dna.hueOffset);
		shader.set("u_args", argsToFloat(animateArgs()));

		shader.set("u_aa",app.aa);
	}

	public void update(float x_, float y_, float w_, float h_) {
		update(x_, y_, w_, h_, height);
	}

	public void update(float x_, float y_, float w_, float h_, float dh_) {
		if (x_!=x || y_!=y || w_!=w || h_!=h || dh_ != height) {
			w = w_;
			h = h_;
			x = x_;
			y =  y_;

			g_scale = 1/w * 4;
			g_offset = new PVector(-x/w - 0.5f, -(dh_ - y - h - (w-h)/2)/w - 0.5f);
			g_offset.mult(4);
		}
	}

	public ArrayList<PVector> animateArgs() {
		animatedArgs = new ArrayList<PVector>();
		for (PVector a : dna.args) {
			float addTime = sin((app.appTime) * 2 * PI);
			PVector addTimeV = new PVector(addTime,addTime,addTime);
			animatedArgs.add(PVector.add(a,addTimeV));
		}
		return animatedArgs;
	}

	// CONTROLS

	public void addScale(float amount) {
		dna.scale *= amount;
		dna.offset.div(amount);
	}

	public void addOffset(float x, float y) {
		dna.offset.add(x,y);
	}

	public void mouseMove() {
		PVector relMouse = new PVector((PApplet.parseFloat(mouseX)-x)*g_scale,1-(PApplet.parseFloat(mouseY)-y)*g_scale);
		PVector relPMouse = new PVector((PApplet.parseFloat(pmouseX)-x)*g_scale,1-(PApplet.parseFloat(pmouseY)-y)*g_scale);
		dna.offset.add(PVector.sub(relPMouse,relMouse));
	}

	// SERVICE

	public float[] argsToFloat(ArrayList<PVector> args) {
		float[] temp = new float[args.size()*3];
		for (int i = 0; i < args.size(); i++) {
			temp[i*3] = args.get(i).x;
			temp[i*3+1] = args.get(i).y;
			temp[i*3+2] = args.get(i).z;
		}
		return temp;
	}

	// EXPORTING

	public void render(String path) {
		update(0,0,app.expSize,app.expSize,app.expSize);

		setShader();

		renderer.beginDraw();
		renderer.shader(shader);
		renderer.rect(0,0,app.expSize,app.expSize);
		renderer.endDraw();
		renderer.save(path);
		resetShader();
	}

	public void export(String path) {
		PGraphics export = createGraphics(app.expSize,app.expSize,P2D);
		update(0,0,app.expSize,app.expSize,app.expSize);

		setShader();

		export.beginDraw();
		export.shader(shader);
		export.rect(0,0,app.expSize,app.expSize);
		export.endDraw();
		export.save(path);
		resetShader();
	}

	public void export() {
		export("export/image_"+PApplet.parseInt(random(999999))+".jpg");
	}
}
class DNA {
	ArrayList<Gene> genes;
	float scale = 4;
	PVector offset = new PVector();
	float hueOffset;
	ArrayList<PVector> args = new ArrayList<PVector>();

	String code;

	float complexity = 6;
	float mutationRate = 1;

	DNA() {
	}	

	DNA(String s) {
		if (s == "RANDOM") {
			randomDNA();
		}
	}

	// MAIN METHODS

	public void construct() {
		code = "vec3 col = " + genes.get(0).get() + ";";
	}

	public void randomDNA() {
		args = new ArrayList<PVector>();
		// scale = random(4,16);
		complexity = random(3,8);
		// complexity = random(4,16);
		hueOffset = random(1);
		genes = new ArrayList<Gene>();
		addGene();
		construct();
	}

	// SEX

	public DNA sex(DNA d1, DNA d2) {
		DNA p1 = d1.copy();
		DNA p2 = d2.copy();

		for (int i = 0; i < 5+app.mutationRate/10; i++) {
			sSwap(p1, p1.genes.get( (int) random(p1.genes.size()) ), p2, p2.genes.get( (int) random(p2.genes.size()) ) );
		}
		p1.mutate();
		p1.construct();
		return p1;
	}

	public void sSwap(DNA p1, Gene g1, DNA p2, Gene g2) {
		ArrayList<Gene> b1 = p1.grabBranch(p2, g1);
		ArrayList<Gene> b2 = p2.grabBranch(p1, g2);

		for (Gene g : b2) {
			if (g.type == "rndm" || g.type == "rndm3") {
				p1.args.add(p2.args.get(g.argsBinder).get());
				g.argsBinder = p1.args.size()-1;
			}
		}

		for (Gene g : b1) {
			if (g.type == "rndm" || g.type == "rndm3") {
				p2.args.add(p1.args.get(g.argsBinder).get());
				g.argsBinder = p2.args.size()-1;
			}
		}

		int ind = p1.geneIndex(g1);
		p1.deleteBranch(g1);
		p1.injectBranch(ind, b2);
		p1.genes.get(ind).setAdress(g1.adress);
		p1.updAd(p1.genes.get(ind));

		p1.sortArgs();

		ind = p2.geneIndex(g2);
		p2.deleteBranch(g2);
		p2.injectBranch(ind, b1);
		p2.genes.get(ind).setAdress(g2.adress);
		p2.updAd(p2.genes.get(ind));

		p2.sortArgs();
	}

	// MUTATION

	public void mutate() {
		mutateArgs();
		mutateParameters();
		for (int i = 0; i < genes.size()*(app.mutationRate/100*0.05f); i++) {
			Gene g = genes.get((int) random(genes.size()));
			Gene g2 = genes.get((int) random(genes.size()));
			int act = (int) random(5);
			if (act == 0) mRemoveNode(g);
			if (act == 1) mInsert(g);
			if (act == 2) changeGene(g);
			if (act == 3) mSwap(g,g2);
			if (act == 4) mCopy(g,g2);

		}
		sortArgs();

		construct();
	}

	public void sortArgs() {
		ArrayList<PVector> sorted = new ArrayList<PVector>();
		for (Gene g : genes) {
			if (g.type == "rndm" || g.type == "rndm3") {
				if (sorted.size() < 512) {
					sorted.add( args.get(g.argsBinder) );
					g.argsBinder = sorted.size()-1;
				} else {
					g.argsBinder = 511;
				}
			}
		}
		args = sorted;
	}

	public void mCopy(Gene g1, Gene g2) {
		ArrayList<Gene> b1 = grabBranch(g1);

		int ind = geneIndex(g2);
		deleteBranch(g2);
		injectBranch(ind, b1);
		genes.get(ind).setAdress(g2.adress);
		updAd(genes.get(ind));


	}

	public void mSwap(Gene g1, Gene g2) {
		ArrayList<Gene> b1 = grabBranch(g1);
		ArrayList<Gene> b2 = grabBranch(g2);

		int ind = geneIndex(g1);
		deleteBranch(g1);
		injectBranch(ind, b2);
		genes.get(ind).setAdress(g1.adress);
		updAd(genes.get(ind));

		ind = geneIndex(g2);
		if (ind >= 0) {
			deleteBranch(g2);
			injectBranch(ind, b1);
			genes.get(ind).setAdress(g2.adress);
			updAd(genes.get(ind));
		} else println("Its okay");

	}

	public void mChange(Gene g) {
		int index = geneIndex(g);
		boolean newIsValue;
		if (isValue(g)) newIsValue = random(1) > 0.2f;
		else newIsValue = random(1) < 0.2f;
		Gene newGene = getGene(newIsValue);
		int toadd = newGene.nodes-g.nodes;
		newGene.setAdress(g.adress);
		genes.set(index,newGene);
		if (toadd > 0) {
			fillNode(newGene,toadd);
			updAd(newGene);
		} else if (toadd < 0) {
			clearNode(newGene, abs(toadd));
			updAd(newGene);
		}
	}

	public void mRemoveNode(Gene g) {
		if (!isValue(g)) {
			int index = geneIndex(g);
			clearNode(g, g.nodes-1);
			genes.remove(index);
			genes.get(index).setAdress(g.adress);
			updAd(genes.get(index));
		}
	}

	public void mInsert(Gene g) {
		int index = geneIndex(g);
		Gene newGene = getGene(false);
		newGene.setAdress(g.adress);
		genes.add(index,newGene);
		int toAdd = newGene.nodes - 1;
		updNode(newGene, toAdd);
	}

	public void updNode(Gene g, int n) {
		if (n > 0) {
			fillNode(g, n);
		} else if (n < 0) {
			clearNode(g, abs(n));
		}
		updAd(g);

	}

	public void fillNode(Gene g, int n) {
		int index = geneIndex(g);
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				Gene newGene = getGene(true);
				genes.add(index+1,newGene);
			}
		}
	}	

	public void clearNode(Gene g, int n) {
		int index = geneIndex(g);
		ArrayList<Integer> deleted = new ArrayList<Integer>();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				ArrayList<Integer> newAdress = new ArrayList<Integer>(g.adress);
				boolean setdelnode = false;
				int delnode = 0;
				while (!setdelnode) {
					delnode = (int) random(g.nodes);
					setdelnode = true;
					for (Integer nd : deleted) {
						if (delnode == nd) setdelnode = false;
					}
				}
				newAdress.add(delnode);
				deleted.add(delnode);
				Gene delGene = getGeneByAdress(newAdress);
				deleteBranch(delGene);
			}
		}
	}

	int updAdInd;
	public void updAd(Gene g) {
		updAdInd = geneIndex(g) + 1;
		if (updAdInd < genes.size()) {
			for (int i = 0; i < g.nodes; i++) {
				updAd(updAdInd,i,g.adress);
			}
		}
	}

	public void updAd(int index, int n, ArrayList<Integer> a) {
		Gene g = genes.get(updAdInd);
		ArrayList<Integer> newAd = new ArrayList<Integer>(a);
		newAd.add(n);
		g.setAdress(newAd);
		updAdInd++;
		for (int i = 0; i < g.nodes; i++) {
			if (updAdInd < genes.size()) updAd(updAdInd, i, newAd);
		}
	}

	// PICK GENES

	public String getVal() {
		for (int i = 0; i < 100; i++) {
			int rtest = PApplet.parseInt(random(genesValues.length));
			if (random(1) < genesValuesRate[rtest]) return genesValues[rtest];
		}

		println("Hard to find value");
		return "rndm";
	}

	public String getMethod() {

		String[] methodGroup = new String[0];
		float[] methodGroupRate = new float[0];

		for (int i = 0; i < 100; i++) {
			int rtest = PApplet.parseInt(random(genesMethods.length));
			if (random(1) < genesMethodsGroupRate[rtest]) {
				methodGroup = genesMethods[rtest];
				methodGroupRate = genesMethodsRate[rtest];
				break;
			}
			if (i == 99) {
				println("Hard to find method");
				return "rndm";
			}
		}

		for (int i = 0; i < 100; i++) {
			int rtest = PApplet.parseInt(random(methodGroup.length));
			if (random(1) < methodGroupRate[rtest]) return methodGroup[rtest];
		}

		println("Hard to find method");
		return "rndm";
	}

	public Gene getGene(boolean isVal) {
		if (isVal) {
			return new Gene(this, getVal());
		}
		return new Gene(this, getMethod());
	}

	// COMPLEXITY FORMULA

	public boolean isValue(float depth) {
		float test = 1-((depth-2)/complexity);
		test = pow(test, 2);
		if (random(1) > test) return true;
		return false;
	}

	// GENERAL METHODS

	public Gene getGeneByAdress(ArrayList<Integer> a) {
		for (Gene g : genes) {
			if (g.adress.equals(a)) return g;
		}

		println("Wrong gene adress:");
		println(a);
		println("All adresses:");
		for (Gene g : genes) {
			println("g.adress: "+g.adress);
		}
		return getGene(true);
	}

	public int geneIndex(Gene g) {
		for (int i = 0; i < genes.size(); i++) {
			if (genes.get(i) == g) return i;
		}
		println("geneIndex Error");
		return -1;
	}

	public int[] branchIndex(Gene g) {
		int first = geneIndex(g);
		for (int i = first+1; i < genes.size(); i++) {
			Gene test = genes.get(i);
			if (test.depth <= g.depth) {
				return new int[] {first, i-1};
			}
		}
		return new int[] {first, genes.size()-1};
	}

	public boolean isValue(Gene g) {
		if (g.type == "x" || g.type == "y" || g.type == "rndm" || g.type == "rndm3") return true;
		return false;
	}


	// CONSTRUCTION METHODS

	public void addGene() {
		addGene(0, new ArrayList<Integer>());
	}

	public void addGene(int n, ArrayList<Integer> a) {

		genes.add(getGene(isValue(a.size()+1)));
		Gene lastGene = genes.get(genes.size()-1);
		ArrayList<Integer> newAdress = new ArrayList<Integer>(a);
		newAdress.add(n);
		lastGene.setAdress(newAdress);

		for (int i = 0; i < lastGene.nodes; i++) addGene(i,lastGene.adress);
	}

	// GENE MANIPULATION

	public void mutateParameters() {
		hueOffset += randomGaussian()*0.1f;
	}

	public void mutateArgs() {
		for (int i = 0; i < random(args.size()); i++) {
			int num = (int) random(args.size());
			PVector a = args.get(num);
			if (a.x == a.y && a.y == a.z) {
				float temp = randomGaussian()*0.1f;
				a.add(temp,temp,temp);
			} else {
				a.add(randomGaussian()*0.1f,randomGaussian()*0.1f,randomGaussian()*0.1f);
			}
		}		
	}

	public void changeGene(Gene g) {
		int index = geneIndex(g);

		if (isValue(g)) {
			genes.set(index, getGene(true));
		} else {
			genes.set(index, getGene(false));
		}

		Gene newGene = genes.get(index);
		newGene.setAdress(new ArrayList<Integer>(g.adress));

		if (newGene.nodes < g.nodes) {
			int todelete = g.nodes - newGene.nodes;
			for (int i = todelete; i > 0; i--) {
				ArrayList<Integer> deladress = new ArrayList<Integer>(newGene.adress);
				deladress.add(g.nodes-i);
				deleteBranch(getGeneByAdress(deladress));
			}
		} else if (newGene.nodes > g.nodes) {
			int toadd = newGene.nodes - g.nodes;
			for (int i = toadd; i >= 1; i--) {
					genes.add(index+1,getGene(true));
					ArrayList<Integer> parentAdress = new ArrayList<Integer>(newGene.adress);
					parentAdress.add(g.nodes+i-1);
					genes.get(index+1).setAdress(parentAdress);
			}
		}
	}

	public void changeValToMeth(Gene g) {
		if (isValue(g)) {
			int index = geneIndex(g);
			genes.set(index, getGene(false));
			Gene newGene = genes.get(index);
			newGene.setAdress(new ArrayList<Integer>(g.adress));

			for (int i = newGene.nodes; i >= 1; i--) {
					if (i == 1) {
						genes.add(index+1,g.copy(this));
					} else {
						genes.add(index+1,getGene(true));
					}
					ArrayList<Integer> parentAdress = new ArrayList<Integer>(newGene.adress);
					parentAdress.add(g.nodes+i-1);
					genes.get(index+1).setAdress(parentAdress);
			}

		} else {
			println("hey!((");
		}
	}

	public void deleteBranch(Gene g) {
		int[] branch = branchIndex(g);
		for (int i = branch[0]; i <= branch[1]; i++) {
			genes.remove(branch[0]);
		}
	}

	public ArrayList<Gene> grabBranch(Gene g) {
		return grabBranch(this, g);
	}

	public ArrayList<Gene> grabBranch(DNA p, Gene g) {
		ArrayList<Gene> branch = new ArrayList<Gene>();
		int[] bInd = branchIndex(g);
		for (int i = bInd[0]; i <= bInd[1]; i++) {
			branch.add(genes.get(i).copy(p));
		}
		return branch;
	}

	public void injectBranch(int index, ArrayList<Gene> branch) {
		for (int i = branch.size()-1; i >= 0; i--) {
			genes.add(index, branch.get(i));
		}
	}

	//	COPY

	public ArrayList<Gene> copyGenes(DNA p) {
		ArrayList<Gene> temp = new ArrayList<Gene>();
		for (Gene g : genes) {
			temp.add(g.copy(p));
		}
		return temp;
	}

	public ArrayList<PVector> copyArgs() {
		ArrayList<PVector> copy = new ArrayList<PVector>();
		for (PVector p : args) {
			copy.add(p.get());
		}
		return copy;
	}

	public DNA copy() {

		DNA temp = new DNA();
		temp.genes = copyGenes(temp);
		temp.code = code;
		temp.scale = scale;
		temp.offset = offset.get();
		temp.hueOffset = hueOffset;
		temp.args = copyArgs();
		return temp;
	}

}
String[] genesValues = new String[] {
	"x", 
	"y",
	"rndm",
	"rndm3"
};
float[] genesValuesRate = new float[] {1, 1, 0.5f, 0.5f, 0.5f};


String[] genesBasicMath = new String[] {
	"add",
	"sub",
	"mult",
	"div"
};
float[] genesBasicMathRate = new float[] {1, 1, 1, 1};

String[] genesExponential = new String[] {
	"pow2",
	"sqrt",
	"powOf",
	"logOf",
	"2pow",
	"2log"
};
float[] genesExponentialRate = new float[] {1, 1, 0.3f, 0.3f, 0.3f, 0.3f};


String[] genesRound = new String[] {
	"mod",
	"fract",
	"floor",
	"ceil",
	"round"
};
float[] genesRoundRate = new float[] {0.5f, 1, 1, 1, 1, 1};

String[] genesTrig = new String[] {
	"sin",
	"cos",
	"tan",
	"asin",
	"acos",
	"atan"
};
float[] genesTrigRate = new float[] {1, 1, 0.1f, 0.1f, 0.1f, 0.5f};


String[] genesConstrain = new String[] {
	"min",
	"max",
	"clamp",
	"abs"
};
float[] genesConstrainRate = new float[] {1, 1, 0.5f, 1};


String[] genesMix = new String[] {
	"mix"
};
float[] genesMixRate = new float[] {1};

String[] genesLogic = new String[] {
	"if",
	"and",
	"or",
	"xor"
};
float[] genesLogicRate = new float[] {1,1,1,1};


String[] genesElse = new String[] {
	"hsb2rgb",
	"combine",
	"setH",
	"setS",
	"setV",
	"noise2"
};
float[] genesElseRate = new float[] {0, 0, 0, 0, 0, 1, 1};


String[][] genesMethods = new String[][] {
	genesBasicMath,
	genesExponential,
	genesRound,
	genesTrig,
	genesConstrain,
	genesMix,
	genesLogic,
	genesElse
};
float[] genesMethodsGroupRate = new float[] {1, 0.02f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f};

float[][] genesMethodsRate = new float[][] {
	genesBasicMathRate,
	genesExponentialRate,
	genesRoundRate,
	genesTrigRate,
	genesConstrainRate,
	genesMixRate,
	genesLogicRate,
	genesElseRate
};

public String getMethodGroupName(int n) {
	if (n == 0) return "Basic Math";
	if (n == 1) return "Exponential";
	if (n == 2) return "Round";
	if (n == 3) return "Trigonometry";
	if (n == 4) return "Constrain";
	if (n == 5) return "Mix";
	if (n == 6) return "Logic";
	if (n == 7) return "Else";
	return "Oops";
}

class Gene {
	DNA p;
	String type;

	ArrayList<Integer> adress = new ArrayList<Integer>();
	int depth;
	int nodes = 0;

	int argsBinder;

	Gene(DNA p_, String type_) {
		p = p_;
		type = type_;

		if (type == "x") nodes = 0;
		if (type == "y") nodes = 0;

		if (type == "add") nodes = 2;
		if (type == "sub") nodes = 2;
		if (type == "mult")	nodes = 2;
		if (type == "div") nodes = 2;

		if (type == "pow2") nodes = 1;
		if (type == "sqrt") nodes = 1;
		if (type == "powOf") nodes = 2;
		if (type == "logOf") nodes = 2;
		if (type == "2pow") nodes = 1;
		if (type == "2log") nodes = 1;

		if (type == "mod") nodes = 2;
		if (type == "fract") nodes = 1;
		if (type == "floor") nodes = 1;
		if (type == "ceil") nodes = 1;
		if (type == "round") nodes = 1;

		if (type == "min") nodes = 2;
		if (type == "max") nodes = 2;
		if (type == "clamp") nodes = 3;
		if (type == "abs") nodes = 1;

		if (type == "sin") nodes = 1;
		if (type == "cos") nodes = 1;
		if (type == "tan") nodes = 1;
		if (type == "asin") nodes = 1;
		if (type == "acos") nodes = 1;
		if (type == "atan") nodes = 2;

		if (type == "mix") nodes = 3;

		if (type == "if") nodes = 4;
		if (type == "and") nodes = 6;
		if (type == "or") nodes = 6;
		if (type == "xor") nodes = 6;

		if (type == "hsb2rgb") nodes = 1;
		if (type == "combine") nodes = 3;
		if (type == "setH") nodes = 2;
		if (type == "setS") nodes = 2;
		if (type == "setV") nodes = 2;
		if (type == "noise2") nodes = 2;

		if (type == "rndm") {
			nodes = 0;
			if (p.args.size() >= 511) {
				argsBinder = 511;
			} else {
				argsBinder = p.args.size();
				float temp = random(1);
				p.args.add(new PVector(temp,temp,temp));
			}
		}

		if (type == "rndm3") {
			nodes = 0;
			if (p.args.size() >= 511) {
				argsBinder = 511;
			} else {
				argsBinder = p.args.size();
				float temp = random(1);
				p.args.add(new PVector(temp+randomGaussian()*0.2f,temp+randomGaussian()*0.2f,temp+randomGaussian()*0.2f));
			}
			
		}
	}

	public String get() {

		String temp = "";

		if (type == "rndm" || type == "rndm3") {
			temp = "g_arg(";
			temp += argsBinder;
			
		} else {

			temp = "g_" + type + "(";


			if (nodes > 0) {
				ArrayList<Gene> children = getChildren();
				for (int i = 0; i < children.size(); i++) {
					if (i > 0) temp += ",";
					
					temp += children.get(i).get();

				}
			}


		}
		temp += ")";

		return temp;

	}

	public Gene copy(DNA p_) {
		Gene temp = new Gene(p_, type);
		temp.adress = new ArrayList<Integer>(adress);
		temp.depth = depth;
		temp.nodes = nodes;
		temp.argsBinder = argsBinder;
		return temp;
	}

	public void setAdress(ArrayList<Integer> a) {
		adress = a;
		depth = a.size();
	}

	public ArrayList<Gene> getChildren() {
		ArrayList<Gene> children = new ArrayList<Gene>();
		for (int i = 0; i < nodes; i++) {
			ArrayList<Integer> con = new ArrayList<Integer>(adress);
			con.add(i);
			Gene child = p.getGeneByAdress(con);
			children.add(child);
		}
		return children;
	}
}
class NodeDisplay {
	PGraphics canvas = createGraphics(600,600,P2D);
	float xmar = 5;
	float ymar = 20;
	float nxsize = 50;
	float nysize = 30;

	NodeDisplay() {
	}

	public void display(DNA dna, float x_, float y_, float w_, float h_) {
		process(dna);
		image(canvas, x_, y_, w_, h_);
	}

	public void process(DNA d) {
		canvas.beginDraw();
		canvas.textFont(app.font);
		canvas.textSize(15);
		canvas.clear();
		canvas.rectMode(CENTER);
		canvas.textAlign(CENTER, CENTER);

		canvas.translate(nxsize/2+1,0);


		boolean ok = true;
		int iter = 1;
		ArrayList<Gene> prevlayer = new ArrayList<Gene>();;
		while(ok) {
			ArrayList<Gene> layer = new ArrayList<Gene>();
			for (Gene g : d.genes) {
				if (g.depth == iter) layer.add(g);
			}
			if (layer.size() > 0) {
				for (int i = 0; i < layer.size(); i++) {
					if (iter > 1) {

						ArrayList<Integer> paradr = new ArrayList<Integer>(layer.get(i).adress);
						paradr.remove(paradr.size()-1);
						int parent = -10;

						for (int j = 0; j < prevlayer.size(); j++) {
							if (paradr.equals(prevlayer.get(j).adress)) parent = j;
						}

						canvas.stroke(250);
						canvas.strokeWeight(2);
						canvas.line(
							canvas.width/2 + (nxsize+xmar) * parent - ((nxsize+xmar) * prevlayer.size())/2, (nysize+ymar) * (iter-1) + nysize/2,
							canvas.width/2 + ((nxsize+xmar) * (i)) - ((nxsize+xmar) * layer.size())/2, (nysize+ymar) * iter - nysize/2
							);
					}
					canvas.noStroke();

					drawNode(layer.get(i).type, canvas.width/2 + ((nxsize+xmar) * (i)) - ((nxsize+xmar) * layer.size())/2, (nysize+ymar) * iter);
				}
			} else {
				ok = false;
			}
			iter++;
			prevlayer = layer;
		}

		canvas.endDraw();
	}

	public void drawNode(String type, float x_, float y_) {
		canvas.fill(0xff8477BC);

		canvas.rect(x_,y_,nxsize,nysize);
		canvas.fill(255);
		canvas.text(type, x_, y_);
	}

}
class Pop {
	App p;

	ArrayList<Artwork> arts = new ArrayList<Artwork>();
	ArrayList<DNA> lastPool = new ArrayList<DNA>();

	Pop(App p_) {
		p = p_;
	}

	public void setPopSize(int row) {
		int popSize = row*row;
		int prevPop = arts.size();
		if (popSize > prevPop) {
			for (int i = 0; i < popSize - prevPop; i++) {
				Artwork a = new Artwork(this);
				a.randomDNA();
				arts.add(a);
			}
		} else if (prevPop > popSize) {
			for (int i = 0; i < prevPop - popSize; i++) {
				arts.remove(arts.size()-1);
			}
		}
		println("row: "+row);
		println("arts.size(): "+arts.size());
	}

	public void evolve() {
		ArrayList<DNA> pool = new ArrayList<DNA>();
		for (Artwork a : arts) {
			if (a.isSelected) pool.add(a.dna);
			a.isSelected = false;
		}
		if (pool.size() == 1) {
			arts.get(0).assignDNA(pool.get(0).copy());
			for (int i = 1; i < arts.size(); i++) {
				DNA newDNA = pool.get(0).copy();
				newDNA.mutate();
				arts.get(i).assignDNA(newDNA);
			}
		}
		if (pool.size() > 1) {
			for (Artwork a : arts) {
				DNA newDNA = pool.get((int)random(pool.size()));
				newDNA = newDNA.sex(newDNA, pool.get((int)random(pool.size())));
				a.assignDNA(newDNA);
			}
		}
		lastPool = pool;
	}

	public void evolveAgain() {
		ArrayList<DNA> pool = lastPool;
		for (Artwork a : arts) {
			if (a.isSelected) pool.add(a.dna);
			a.isSelected = false;
		}
		if (pool.size() == 1) {
			arts.get(0).assignDNA(pool.get(0).copy());
			for (int i = 1; i < arts.size(); i++) {
				DNA newDNA = pool.get(0).copy();
				newDNA.mutate();
				arts.get(i).assignDNA(newDNA);
			}
		}
		if (pool.size() > 1) {
			for (Artwork a : arts) {
				DNA newDNA = pool.get((int)random(pool.size()));
				newDNA = newDNA.sex(newDNA, pool.get((int)random(pool.size())));
				a.assignDNA(newDNA);
			}
		}
	}

	public void randomPop() {
		for (Artwork a : arts) {
			a.randomDNA();
			a.isSelected = false;
		}	
		lastPool = new ArrayList<DNA>();	
	}

	public void display(int num, float x, float y, float w, float h) {
		arts.get(num).display(x,y,w,h);
	}
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "pixy" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
