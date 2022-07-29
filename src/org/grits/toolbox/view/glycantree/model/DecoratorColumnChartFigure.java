package org.grits.toolbox.view.glycantree.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class DecoratorColumnChartFigure extends Figure {
	public static Color color = new Color(null,255,128,0);
	
	Double intensity;
	int percentage;
	Integer standardPercentage=null;
	
	public DecoratorColumnChartFigure (Double intensity, double highestIntensity, Double standardIntensity) {
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
			//graphics.drawRectangle(getBounds().x+2, getBounds().y, getBounds().width-4, getBounds().height);
			int beginX = getBounds().x;
			int beginY = getBounds().y + getBounds().height -1;
			graphics.drawLine(beginX,  beginY, beginX + getBounds().width-1, beginY);
			
			graphics.setBackgroundColor(color);
			
			int y = getBounds().y + getBounds().height - (getBounds().height * percentage / 100);
			//graphics.drawRectangle(getBounds().x+2, y, getBounds().width-4, getBounds().height * percentage / 100);
			graphics.fillRectangle(getBounds().x+3, y+1, getBounds().width-6, (getBounds().height * percentage / 100 )-2);
			
			graphics.setForegroundColor(ColorConstants.black);
			graphics.setLineWidth(2);
			graphics.setLineStyle(SWT.LINE_SOLID);
			int x1=getBounds().x;
			int x2 = x1 + getBounds().width;
			int y1 = getBounds().y + getBounds().height - getBounds().height * standardPercentage / 100;
			graphics.drawLine(x1, y1 , x2, y1);
		}
		else {
			
			int beginX = getBounds().x;
			int beginY = getBounds().y + getBounds().height -1;
			graphics.drawLine(beginX,  beginY, beginX + getBounds().width-1, beginY);
			//graphics.drawRectangle(getBounds().x, getBounds().y, getBounds().width-1, getBounds().height);
			
			graphics.setBackgroundColor(color);
			int y = getBounds().y + getBounds().height - (getBounds().height * percentage / 100);
		//	graphics.drawRectangle(getBounds().x, y, getBounds().width, getBounds().height * percentage / 100);
			graphics.fillRectangle(getBounds().x+1, y+1, getBounds().width-2, (getBounds().height * percentage / 100 ) - 2);
		}
	}
	
	@Override
	public IFigure getToolTip() {
		if (intensity == null)
			return new Label ("Intensity: N/A");
		return new Label("Intensity: " + intensity);
	}
	

}
