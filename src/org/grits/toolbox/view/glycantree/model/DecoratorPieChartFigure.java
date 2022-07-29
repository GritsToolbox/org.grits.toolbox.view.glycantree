package org.grits.toolbox.view.glycantree.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.graphics.Color;

public class DecoratorPieChartFigure extends Shape {
	public static Color color = new Color(null,102,178,255);
	
	int height;
	int width;
	int begin;
	int angle;
	double intensity;
	
	public DecoratorPieChartFigure (int h, int w, int beginAngle, int angle, double intensity, double highestIntensity) {
		this.height = h;
		this.width = w;
		this.begin = beginAngle;
		this.intensity= intensity;
		// convert the percentage to the angle
		this.angle = (int) Math.round((intensity * 100 / highestIntensity) * 360 / 100); 
		
		setBackgroundColor(color);
		setForegroundColor(ColorConstants.black);
	}

	@Override
	protected void fillShape(Graphics graphics) {
		graphics.fillArc (getBounds().x, getBounds().y,  width, height, begin, angle);	
		// put the percentage of the intensity to the center of the arc
		graphics.drawString(angle * 100 / 360 + "%", getBounds().x + width/2 - 10, getBounds().y + height/2 - 5);
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.drawArc(getBounds().x, getBounds().y, width, height, 0, 360);
	}
	
	@Override
	public IFigure getToolTip() {
		return new Label("Intensity: " + intensity);
	}
}
