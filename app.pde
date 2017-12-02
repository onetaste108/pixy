import controlP5.*;
ControlP5 cp5time;
ControlP5 cp5gen;
ControlP5 cp5main;

ControlP5 selectButtons;

//wtf


class App {
	Pop pop;
	int popRow;
	int popSize;
	int genSize;

	NodeDisplay nd = new NodeDisplay();

	float mutationRate = 50;

	float appTime = 0;
	float timeFreq = (float) 1/3;
	boolean timeRun = false;

	int appFrameRate = 60;

	boolean isRender;
	int renderFrameCount = 0;
	int renderID;

	// general

	String view = "GRID";
	String uiview = "GENERAL";

	int focusedId;
	boolean isFocused = false;

	int lastIdPressed = -1;
	
	// GridView

	PVector[] gridPos;
	PVector gridScale;

	// UI

	float generalMargin = 20;
	float gridMargin = 10;

	float separatorWidth = 10;
	float separator = height;
	boolean separatorIsMoving = false;
	float separatorLimitMin = width/4;
	float separatorLimitMax = width-width/4;

	PVector displayPos;
	PVector displaySize;
	PVector uiPos;
	PVector uiSize;

	int uiblock = 10;


	color mainColor = color(155,0,255);
	color mainColorOver = color(187,77,255);
	color mainColorDown = color(93,0,158);

	color againColor = color(201,34,117);
	color againColorOver = color(229,101,172);
	color againColorDown = color(158,24,98);

	color newColor = color(60,135,240);
	color newColorOver = color(123,175,239);
	color newColorDown = color(18,83,160);



	color grayNormal = color(170);
	color grayNormalOver = color(212);
	color grayNormalDown = color(117);
	color grayDarker = color(66);

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
	Textlabel tPopSize;
	Textlabel tGenNum;
	PVector[] genMutRect;
	Textlabel tMut;
	Slider sMut;

	PVector[] mainRect;
	Button bMainEvolve;
	Button bMainAgain;
	Button bMainNew;

	ArrayList<Button> selButs = new ArrayList<Button>();

	PFont font = createFont("font.ttf", uiblock+2);
	PFont fontbig = createFont("font.ttf", (uiblock*2.5+2)*width/1500);

	App() {
		pop = new Pop(this);
		controls();
		updateLayout();
		setPopSize(5);
		updateSelButs();
	}

	void run() {
		updateLayout();
		keyIsPressed();
		mouseIsPressed();
		displayPop();
		displayUI();
		runTime();
		if (isRender) render();
	}

	void render() {
		pop.arts.get(focusedId).render("renders/render"+renderID+"/frame"+renderFrameCount+".jpg");
		renderFrameCount++;
	}

	void beginRender() {
		appTime = 0;
		renderFrameCount = 0;
		renderID = (int) random(99999);
		isRender = true;
		actionTimePlay();
	}

	void runTime() {
		if (timeRun) {
			appTime += (float) 1/timeFreq / appFrameRate;
			if (appTime > 1) {
				appTime = 0;
				if (isRender) {
					isRender = false;
					actionTimeStop();

				}
			}
		}
	}

	void actionTimePlay() {
		timeRun = true;
		bTimePlay.setColorBackground(mainColor)
		.setColorActive(mainColorDown) 
		.setColorForeground(mainColorOver)
		.getCaptionLabel()
			.setColor(grayNormalOver);

	}

	void actionTimePause() {
		timeRun = false;
		bTimePlay.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
		.getCaptionLabel()
			.setColor(grayDarker);
	}

	void actionTimeStop() {
		timeRun = false;
		appTime = 0;
		bTimePlay.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
		.getCaptionLabel()
			.setColor(grayDarker);
	}

	void actionGenPlus() {
		increasePop();
	}

	void actionGenMinus() {
		decreasePop();
	}

	void actionMainEvolve() {
		if (isAnySelected()) {
			pop.evolve();
		}
	}

	void actionMainAgain() {
		pop.evolveAgain();
	}

	boolean isAnySelected() {
		for (Artwork a : pop.arts) {
			if (a.isSelected) {
				return true;
			}
		}
		return false;
	}

	// Population control

	void setPopSize(int n) {
		popRow = n;
		popSize = n*n;
		pop.setPopSize(n);
		countGrid();
		if (selButs.size() < popSize) {
			for (int i = selButs.size(); i < popSize; i++) {
				selButs.add(addBut(i));
			}
		} else if (selButs.size() > popSize) {
			for (int i = 0; i < selButs.size() - popSize; i++) {
				selButs.remove(selButs.size()-1);
			}
		}
	}

	Button addBut(int n) {
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

	void selButAction(int n) {
		if (!pop.arts.get(n).isSelected) {
			selButs.get(n).setColorBackground(mainColor)
			.setColorActive(mainColorDown) 
			.setColorForeground(mainColorOver);
			pop.arts.get(n).isSelected = true;
		} else {
			selButs.get(n).setColorBackground(grayNormal)
			.setColorActive(grayNormalDown) 
			.setColorForeground(grayNormalOver);
			pop.arts.get(n).isSelected = false;
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
				.setColorBackground(grayDarker)
				.setColorActive(grayDarker) 
				.setColorForeground(grayDarker)
					.getCaptionLabel()
					.setColor(grayNormalDown);
		}
	}

	void increasePop() {
		popRow++;
		setPopSize(popRow);

		bGenMinus
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver);

	}

	void decreasePop() {
		if (popRow > 1) {
			popRow--;
			setPopSize(popRow);		
		} 
		if (popRow <= 1){
			bGenMinus
			.setColorBackground(grayDarker)
			.setColorActive(grayDarker)
			.setColorForeground(grayDarker);

		}
	}

	// Population Display

	void displayPop() {
		if (view == "SINGLE") displaySingleView();
		if (view == "GRID") displayGridView();
	}

	void displaySingleView() {
		pop.display(focusedId, 0, 0, displaySize.x, displaySize.y);
	}

	void displayGridView() {
		for (int i = 0; i < pop.arts.size(); i++) {
			pop.display(i, gridPos[i].x, gridPos[i].y, gridScale.x, gridScale.y);
		}
		displayStroke();
	}

	void displayStroke() {
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

	void displayButtons() {
		for (int i = 0; i < popSize; i++) {
			if (isFocused && focusedId == i && view == "GRID") {
				selButs.get(i).show();
			} else {
				selButs.get(i).hide();	
			}
		}
	}

	// // Population Display Layout

	void updateLayout() {
		displayPos = new PVector();
		displaySize = new PVector(separator, height);
		uiPos = new PVector(separator + separatorWidth + generalMargin, generalMargin);
		uiSize = new PVector(width-uiPos.x-generalMargin, height-generalMargin*2);
		countGrid();
		updateControls();
	}

	void separatorMove() {
		separator = mouseX-separatorWidth/2;
		if (separator < separatorLimitMin) separator = separatorLimitMin;
		if (separator > separatorLimitMax) separator = separatorLimitMax;
		updateLayout();
	}

	void countGrid() {
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

	// Population Grid UI

	void checkArtsFocus() {
		isFocused = false;
		for (int i = 0; i < popSize; i++) {
			if (mouseOver(gridPos[i].x, gridPos[i].y, gridScale.x, gridScale.y)) {
				isFocused = true;
				focusedId = i;
			}
		}
	}

	// UI display

	void displayUI() {
		displaySeparator();
		displayGeneral();

		if (keyPressed && key == 'q') {
			nd.display(pop.arts.get(focusedId).dna, uiPos.x, uiPos.y, uiSize.x, uiSize.x);
			pushStyle();
			stroke(grayNormal);
			noFill();
			rect(uiPos.x, uiPos.y, uiSize.x, uiPos.y+uiSize.y-uiblock*23);
			popStyle();
		} else {
			pop.display(focusedId, uiPos.x, uiPos.y, uiSize.x, uiPos.y+uiSize.y-uiblock*23);
			pushStyle();
			stroke(grayNormal);
			noFill();
			rect(uiPos.x, uiPos.y, uiSize.x, uiPos.y+uiSize.y-uiblock*23);
			popStyle();
		}
	}

	void displaySeparator() {
		pushStyle();
		if (mouseOver(separator,0,separatorWidth,height)) fill(55);
		else fill(44);
		noStroke();
		rect(separator,0,separatorWidth,height);
		popStyle();
	}


	// Global UI Events

	void mousePressed() {
		if (mouseOver(separator,0, separatorWidth, height)) separatorIsMoving = true;
		if (isFocused && !selButs.get(focusedId).isMouseOver()) {
			lastIdPressed = focusedId;
		}
	}

	void mouseIsPressed() {
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

	void mouseReleased() {
		if (lastIdPressed == focusedId) {
			view = "SINGLE";
		}
		lastIdPressed = -1;

		if (separatorIsMoving) separatorIsMoving = false;
	}

	void mouseMoved() {
		if (view == "GRID") {
			checkArtsFocus();
		}
	}

	void keyPressed() {
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
			if (isRender) isRender = false;
			else beginRender();
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
	}
		
	void keyReleased() {

	}

	void keyIsPressed() {
		if (keyPressed) {

		if (key == CODED && keyCode == UP) {
			pop.arts.get(focusedId).addOffset(0,0.02);
		}
		
		if (key == CODED && keyCode == DOWN) {
			pop.arts.get(focusedId).addOffset(0,-0.02);
		}
		
		if (key == CODED && keyCode == LEFT) {
			pop.arts.get(focusedId).addOffset(-0.02,0);
		}
		
		if (key == CODED && keyCode == RIGHT) {
			pop.arts.get(focusedId).addOffset(0.03,0);
		}

		if (keyPressed && key == 'z') {
			pop.arts.get(focusedId).addScale(1.05);
		}

		if (keyPressed && key == 'a') {
			pop.arts.get(focusedId).addScale(0.95);
		}

		}
	}

	void updateControls() {
		updateTimeBlock();
		updateGenBlock();
		updateMainBlock();
		updateSelButs();
	}

	void displayGeneral() {
		displayTime();
		displayGeneBlock();
		displayMainBlock();
		displayButtons();
	}

	void displayTime() {
		pushStyle();
		noFill();
		stroke(grayNormal);
		rect(timeRect[0].x,timeRect[0].y,timeRect[1].x,timeRect[1].y);
		popStyle();

		sTimePos.setValue(appTime);
		tTime.setText("time "+(float) round(timeFreq*10)/10+"s");
	}

	void displayGeneBlock() {
		pushStyle();
		noFill();
		stroke(grayNormal);
		rect(genRect[0].x,genRect[0].y,genRect[1].x,genRect[1].y);
		rect(genMutRect[0].x,genMutRect[0].y,genMutRect[1].x,genMutRect[1].y);
		popStyle();

		tPopSize.setText("POP: "+popSize);
		tGenNum.setText("GEN: "+genSize);
		tMut.setText("MUT. RATE: "+round(mutationRate)+"%");
	}

	void displayMainBlock() {
		pushStyle();
		noFill();
		stroke(grayNormal);
		rect(mainRect[0].x,mainRect[0].y,mainRect[1].x,mainRect[1].y);
		popStyle();
	}

	void updateTimeBlock() {
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

	void updateGenBlock() {
		genRect = new PVector[] {
			new PVector((int) uiPos.x, (int) uiPos.y+uiSize.y-uiblock*13),
			new PVector((int) uiblock*17, (int) uiblock*6)
		};
		genMutRect = new PVector[] {
			new PVector((int) uiPos.x + uiblock*18, (int) uiPos.y+uiSize.y-uiblock*13),
			new PVector((int) uiSize.x - uiblock*18, (int) uiblock*6)
		};

		bGenMinus.setPosition((int) uiPos.x + uiblock, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize(uiblock*3,uiblock*2);
		bGenPlus.setPosition((int) uiPos.x + uiblock*5, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize(uiblock*3,uiblock*2);
		bGenBack.setPosition((int) uiPos.x + uiblock*9, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize(uiblock*3,uiblock*2);
		bGenFov.setPosition((int) uiPos.x + uiblock*13, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize(uiblock*3,uiblock*2);

		sMut.setPosition((int) uiPos.x + uiblock*19, (int) uiPos.y+uiSize.y-uiblock*13+uiblock*3)
			.setSize((int) uiSize.x - uiblock*20, (int) uiblock*2);

		tPopSize.setPosition((int) uiPos.x + uiblock -3, (int) uiPos.y+uiSize.y-uiblock*13+uiblock-4);
		tGenNum.setPosition((int) uiPos.x + uiblock*9 -3, (int) uiPos.y+uiSize.y-uiblock*13+uiblock-4);
		tMut.setPosition((int) uiPos.x + uiblock*19 -3, (int) uiPos.y+uiSize.y-uiblock*13+uiblock-4);
	}

	void updateMainBlock() {
		mainRect = new PVector[] {
			new PVector((int) uiPos.x, (int) uiPos.y+uiSize.y-uiblock*20),
			new PVector((int) uiSize.x, (int) uiblock*6)
		};

		int mid = (int) (uiSize.x - uiblock*4)/3;

		bMainEvolve.setPosition(int(uiPos.x + uiblock), (int) uiPos.y+uiSize.y-uiblock*19)
			.setSize(mid, uiblock*4);
		bMainAgain.setPosition(int(uiPos.x + uiSize.x/2 - mid/2), (int) uiPos.y+uiSize.y-uiblock*19)
			.setSize(mid, uiblock*4);
		bMainNew.setPosition(int(uiPos.x + uiSize.x - mid - uiblock), (int) uiPos.y+uiSize.y-uiblock*19)
			.setSize(mid, uiblock*4);
	}

	void updateSelButs() {
		for (int i = 0; i < popSize; i++) {
			selButs.get(i).setPosition((int) gridPos[i].x + uiblock, (int) gridPos[i].y + gridScale.y - uiblock*3);
		}
	}

	void controls() {

		cp5time = new ControlP5(sketchRef);

		bTimePlay = cp5time.addButton("actionTimePlay");
		bTimePlay.setLabelVisible(true)
		.setLabel(">")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDarker)
			.setFont(font);
		bTimePause = cp5time.addButton("actionTimePause");
		bTimePause.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
		.setLabel("||")
			.getCaptionLabel()
			.setColor(grayDarker)
			.setFont(font);
		bTimePause.setLabelVisible(true);
		bTimeStop = cp5time.addButton("actionTimeStop");
		bTimeStop.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
		.setLabel("x")
			.getCaptionLabel()
			.setColor(grayDarker)
			.setFont(font);
		bTimeStop.setLabelVisible(true);

		sTimeFreq = cp5time.addSlider("timeFreq")
		.plugTo(this)
		.setRange(0.5,10)
		.setValue(5)
		.setLabelVisible(false)
		.setColorActive(grayNormalOver)
		.setColorBackground(grayDarker)
		.setColorForeground(grayNormal);
		sTimePos = cp5time.addSlider("appTime")
		.plugTo(this)
		.setLabelVisible(false)
		.setRange(0,1)
		.setValue(0)
		.setColorActive(grayNormal)
		.setColorBackground(grayDarker)
		.setColorForeground(grayNormalDown);

		tTime = cp5time.addTextlabel("timelabel").setFont(font).setColor(grayNormal);




		selectButtons = new ControlP5(sketchRef);

		cp5gen = new ControlP5(sketchRef);

		bGenPlus = cp5time.addButton("actionGenPlus");
		bGenPlus.setLabelVisible(true)
		.setLabel("+")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDarker)
			.setFont(font);
		bGenMinus = cp5time.addButton("actionGenMinus");
		bGenMinus.setLabelVisible(true)
		.setLabel("-")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDarker)
			.setFont(font);
		bGenBack = cp5time.addButton("actionGenBack");
		bGenBack.setLabelVisible(true)
		.setLabel("<<")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDarker)
			.setFont(font);		
		bGenFov = cp5time.addButton("actionGenFov");
		bGenFov.setLabelVisible(true)
		.setLabel("<<")
		.plugTo(this)
		.setColorBackground(grayNormal)
		.setColorActive(grayNormalDown) 
		.setColorForeground(grayNormalOver)
			.getCaptionLabel()
			.setColor(grayDarker)
			.setFont(font);

		sMut = cp5time.addSlider("mutationRate")
		.plugTo(this)
		.setRange(0,100)
		.setValue(50)
		.setLabelVisible(false)
		.setColorActive(grayNormalOver)
		.setColorBackground(grayDarker)
		.setColorForeground(grayNormal);

		tGenNum = cp5time.addTextlabel("genenum").setFont(font).setColor(grayNormal);
		tPopSize = cp5time.addTextlabel("genepopsize").setFont(font).setColor(grayNormal);
		tMut = cp5time.addTextlabel("genemutrate").setFont(font).setColor(grayNormal);



		cp5main = new ControlP5(sketchRef);

		bMainEvolve = cp5time.addButton("actionMainEvolve");
		bMainEvolve.setLabelVisible(true)
		.setLabel("evolve!")
		.plugTo(this)
		.setColorBackground(grayDarker)
		.setColorActive(grayDarker) 
		.setColorForeground(grayDarker)
			.getCaptionLabel()
			.setColor(grayNormalDown)
			.setFont(fontbig);
		bMainAgain = cp5time.addButton("actionMainAgain");
		bMainAgain.setLabelVisible(true)
		.setLabel("again")
		.plugTo(this)
		.setColorBackground(againColor)
		.setColorActive(againColorOver) 
		.setColorForeground(againColorDown)
			.getCaptionLabel()
			.setColor(color(255))
			.setFont(fontbig);
		bMainNew = cp5time.addButton("actionMainNew");
		bMainNew.setLabelVisible(true)
		.setLabel("new")
		.plugTo(this)
		.setColorBackground(newColor)
		.setColorActive(newColorDown) 
		.setColorForeground(newColorOver)
			.getCaptionLabel()
			.setColor(color(255))
			.setFont(fontbig);		

	}


}

void controlEvent(ControlEvent theEvent) {
  if (theEvent.isController()) {
    if (theEvent.controller().getName().startsWith("baton")) {
      int id = theEvent.controller().getId();

        app.selButAction(id);
 
    }
  }
}

boolean mouseOver(float x, float y, float w, float h) {
	return(mouseX > x && mouseX < x+w && mouseY > y && mouseY < y+h);
}