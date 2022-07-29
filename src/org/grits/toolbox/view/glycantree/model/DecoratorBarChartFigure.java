package org.grits.toolbox.view.glycantree.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class DecoratorBarChartFigure extends Figure {

	public static Color color = new Color(null,255,128,0);
	
	Double intensity;
	int percentage;
	Integer standardPercentage=null;
	
	public DecoratorBarChartFigure (Double intensity, double highestIntensity, Double standardIntensity) {
		this.intensity = intensity;
		if (intensity == null)
			this.percentage = 0;
		else
			this.percentage = (int) Math.round(intensity * 100 / highestIntensity);
		
		if (standardIntensity != null) 
			this.standardPercentage = (int) Math.round(standardIntensity * 100 / highestIntensity);
			
		this.setForegroundColor(ColorConstants.black);
	}
	
	@Override
	protected void paintFigure(Graphics graphics) {
		if (standardPercentage != null) {
			graphics.drawRectangle(getBounds().x, getBounds().y+2, getBounds().width-1, getBounds().height-4);
			
			graphics.setBackgroundColor(color);
			graphics.drawRectangle(getBounds().x, getBounds().y+2, getBounds().width * percentage / 100, getBounds().height-4);
			graphics.fillRectangle(getBounds().x+1, getBounds().y+3, (getBounds().width * percentage / 100 )-1, getBounds().height-5);
			
			graphics.setForegroundColor(ColorConstants.black);
			graphics.setLineWidth(2);
			graphics.setLineStyle(SWT.LINE_SOLID);
			int x1=getBounds().x + getBounds().width * standardPercentage / 100;
			int y1 = getBounds().y;
			int y2 = y1 + getBounds().height;
			graphics.drawLine(x1, y1 , x1, y2);
		}
		else {
			graphics.drawRectangle(getBounds().x, getBounds().y, getBounds().width-1, getBounds().height-1);
			
			graphics.setBackgroundColor(color);
			graphics.drawRectangle(getBounds().x, getBounds().y, getBounds().width * percentage / 100, getBounds().height-1);
			graphics.fillRectangle(getBounds().x+1, getBounds().y+1, (getBounds().width * percentage / 100 )-1, getBounds().height-2);
		}
	}
	
	@Override
	public IFigure getToolTip() {
		if (intensity == null)
			return new Label ("Intensity: N/A");
		return new Label("Intensity: " + intensity);
	}
	
}
