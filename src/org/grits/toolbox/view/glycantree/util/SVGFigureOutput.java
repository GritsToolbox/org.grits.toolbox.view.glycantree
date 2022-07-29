package org.grits.toolbox.view.glycantree.util;

import java.awt.Dimension;

public class SVGFigureOutput {

	String svg;
	Dimension dimension;
	
	public SVGFigureOutput (String svg, Dimension dimension) {
		this.svg = svg;
		this.dimension = dimension;
	}

	public String getSvg() {
		return svg;
	}

	public Dimension getDimension() {
		return dimension;
	}
	
	
}
