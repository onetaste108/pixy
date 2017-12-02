class NodeDisplay {
	PGraphics canvas = createGraphics(500,500,P2D);
	float xmar = 5;
	float ymar = 20;
	float nxsize = 50;
	float nysize = 20;

	NodeDisplay() {

	}

	void display(DNA dna, float x_, float y_, float w_, float h_) {
		process(dna);
		image(canvas, x_, y_, w_, h_);
	}

	void process(DNA d) {
		canvas.beginDraw();
		canvas.clear();

		canvas.rectMode(CENTER);
		canvas.textAlign(CENTER, CENTER);


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
						canvas.stroke(250);
						canvas.line(
							canvas.width/2 + ((nxsize+xmar) * layer.get(i).adress.get(layer.get(i).adress.size()-2)) - ((nxsize+xmar) * prevlayer.size())/2, (nysize+ymar) * (iter-1),
							canvas.width/2 + ((nxsize+xmar) * i) - ((nxsize+xmar) * layer.size())/2, (nysize+ymar) * iter
							);
					}

					drawNode(layer.get(i).type, canvas.width/2 + ((nxsize+xmar) * i) - ((nxsize+xmar) * layer.size())/2, (nysize+ymar) * iter);
				}
			} else {
				ok = false;
			}
			iter++;
			prevlayer = layer;
		}
		canvas.endDraw();
	}

	void drawNode(String type, float x_, float y_) {
		canvas.fill(#8477BC);
		if (type == "x" || type == "y") canvas.fill(#E1480E);
		if (type == "rndm" || type == "rndm3") canvas.fill(#E10E80);
		if (type == "add" || type == "sub" || type == "mult" || type == "div") canvas.fill(#4CA14A);

		canvas.rect(x_,y_,nxsize,nysize);
		canvas.fill(255);
		canvas.text(type, x_, y_);
	}

}