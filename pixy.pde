PApplet sketchRef = this;

App app;
String[] vertexShader;
String[] fragmentShader;
CheckBox checkbox;

PFont font;
PImage logo;
PImage icon;

PGraphics renderer;


void setup() {
	 //size(640,480,P2D);
	size(1280,720,P2D);
	 // fullScreen(P2D);

	logo = loadImage("data/logo.png");

	vertexShader = loadStrings("data/vertex.glsl");
	fragmentShader = loadStrings("data/fragment.glsl");
	renderer = createGraphics(800,800,P2D);

	font = createFont("font.otf", 32);
	textFont(font);

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