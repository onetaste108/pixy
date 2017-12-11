class NodeDisplay {
	PGraphics canvas = createGraphics(600,600,P2D);
	float xmar = 5;
	float ymar = 20;
	float nxsize = 50;
	float nysize = 30;

	NodeDisplay() {
	}

	void display(DNA dna, float x_, float y_, float w_, float h_) {
		process(dna);
		image(canvas, x_, y_, w_, h_);
	}

	void process(DNA d) {
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

	void drawNode(String type, float x_, float y_) {
		canvas.fill(#8477BC);

		canvas.rect(x_,y_,nxsize,nysize);
		canvas.fill(255);
		canvas.text(type, x_, y_);
	}

}