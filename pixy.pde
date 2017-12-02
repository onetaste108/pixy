PApplet sketchRef = this;

App app;
String[] vertexShader;
String[] fragmentShader;
CheckBox checkbox;

PImage logo;
PImage icon;

PGraphics renderer;
 
void settings() {
	size(1280,720,P2D);
	 // fullScreen(P2D);
	// logo = loadImage("data/icon.png");

	// surface.setIcon(logo);
	PJOGL.setIcon("data/icon.png");
}

void setup() {
	 //size(640,480,P2D);
	surface.setResizable(true);


	// getSurface().setCursor(0);


	vertexShader = loadStrings("data/vertex.glsl");
	fragmentShader = loadStrings("data/fragment.glsl");
	renderer = createGraphics(800,800,P2D);


	app = new App();

}

void draw() {
	background(17);

	app.run();
	fill(0,255,0);
	textSize(32);
}

void mousePressed() {
	app.mousePressed();
}

void mouseReleased() {
	app.mouseReleased();
}

void keyPressed() {
	app.keyPressed();
}

void keyReleased() {
	app.keyReleased();
}

void mouseMoved() {
	app.mouseMoved();
}

// FUNCTIONS

// float randomCurve(int n) {
// 	float temp = 
// }