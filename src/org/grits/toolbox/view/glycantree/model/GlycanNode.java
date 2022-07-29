package org.grits.toolbox.view.glycantree.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.ReducingEnd;
import org.grits.toolbox.view.glycantree.util.ImageUtil;

public class GlycanNode {
	
	String id;
	IFigure figure;
	String structure;
	
	Map<Integer, Double> intensity;
	Map<Integer, Double> highestIntensity;
	
	IFigure figureWithDecorator;
	
	List<GlycanNode> connections;
	Set<GlycanNode> transitiveConnections;
	
	Map<CustomExtraData, Object> features;
	ReducingEnd reducingEnd;
	
	
	public String getId() {
		return id;
	}

	public IFigure getFigure() {
		return figure;
	}

	public String getStructure() {
		return structure;
	}

	public Map<Integer, Double> getIntensity() {
		return intensity;
	}

	public void setIntensity(Map<Integer, Double> intensity) {
		this.intensity = intensity;
	}

	public GlycanNode (String structure, String id, ReducingEnd reducingEnd) {
		this.id = id;
		this.structure= structure;
		this.connections = new ArrayList<>();
		
		this.features = new HashMap<>();
		this.transitiveConnections = new HashSet<>();
		this.reducingEnd = reducingEnd;
		this.figure = ImageUtil.getGlycanImage(this);
	}
	
	public GlycanNode (String structure, String id, Double intensity,  ReducingEnd reducingEnd) {
		this (structure, id, reducingEnd);
		if (this.intensity == null) {
			this.intensity = new HashMap<>();
		}
		this.intensity.put(0, intensity);   // for a single annotation
	}
	
	public GlycanNode (String structure, String id, Map<Integer, Double> intensity,  ReducingEnd reducingEnd) {
		this (structure, id, reducingEnd);
		this.intensity = intensity;
	}
	
	public static void addDecorator (IFigure newFigure, GlycanNode glycanNode, Decorator type, Double standardIntensity) {
		Double singleHighest = glycanNode.getIntensity(0);
		if (glycanNode.getHighestIntensity() != null) 
			singleHighest = glycanNode.getHighestIntensity().get(0);
		
		Figure decorator = null;
		if (type == Decorator.PIE) {
			Double firstIntensity = glycanNode.getIntensity(0);
			decorator = new DecoratorPieChartFigure(40, 40, 90, 270, firstIntensity, singleHighest);
			decorator.setBounds(new Rectangle(glycanNode.figure.getClientArea().getTopRight().x, glycanNode.figure.getClientArea().getCenter().y-20, 42, 42));
			newFigure.add(decorator);
			newFigure.setSize(glycanNode.figure.getSize().expand(45, 0));
		} else if (type == Decorator.BAR){
			int size = 130;
			int expandX =0;
			int figureLength = glycanNode.figure.getClientArea().width;
			if (size + 40 > figureLength)
				expandX = size - figureLength + 40;
			Double firstIntensity = glycanNode.getIntensity(0);
			decorator = new DecoratorBarChartFigure(firstIntensity, singleHighest, standardIntensity);
			decorator.setBounds(new Rectangle(glycanNode.figure.getClientArea().getCenter().x - 80, glycanNode.figure.getClientArea().getBottomLeft().y, size, 15));
			newFigure.add(decorator);
			int percentage = (int) Math.round(glycanNode.getIntensity(0) * 100 / singleHighest);
			IFigure percentageLabel = new Label(percentage + "%");
			percentageLabel.setBounds(new Rectangle(glycanNode.figure.getClientArea().getCenter().x - 80 + size, glycanNode.figure.getClientArea().getBottomLeft().y, 35, 15 ));
			newFigure.add(percentageLabel);
			
			newFigure.setSize(glycanNode.figure.getSize().expand(expandX, 45));
		} else {
			// do nothing, not supported for a single annotation
			newFigure.setSize(glycanNode.figure.getSize());
		}
		
	}
	
	public static void addDecoratorForMerge (IFigure newFigure, GlycanNode glycanNode, Decorator type, Double[] standardIntensity) {
		if (type == Decorator.PIE) {
			// do nothing, this is not supported for merge
			newFigure.setSize(glycanNode.figure.getSize());
		}
		else if (type == Decorator.BAR) {
			int size = 130;
			int expandX = 0;
			int y = glycanNode.figure.getClientArea().getBottomLeft().y;
			int x = glycanNode.figure.getClientArea().getCenter().x - 60;
			int figureLength = glycanNode.figure.getClientArea().width;
			if (size + 65 > figureLength)
				expandX = size - figureLength + 65;
			int i=0;
			for (Iterator<Entry<Integer, Double>> iterator = glycanNode.getIntensity().entrySet().iterator(); iterator.hasNext();) {
				Entry<Integer, Double> entry = (Entry<Integer, Double>) iterator.next();
				Figure decorator = new DecoratorBarChartFigure(entry.getValue(), glycanNode.getHighestIntensity().get(entry.getKey()), standardIntensity[i]);
				decorator.setBounds(new Rectangle(x, y, size, 15));
				newFigure.add(decorator);
				
				// add legends
				IFigure legendLabel = new Label(i+1 + "");
				legendLabel.setBounds(new Rectangle(x-20, y, 20, 20 ));
				newFigure.add(legendLabel);
				
				IFigure percentageLabel;
				if (entry.getValue() == null) { // null for merged annotations that do not exist in one of the results
					percentageLabel = new Label ("N/A");
				} else {
					int percentage = (int) Math.round(entry.getValue() * 100 / glycanNode.getHighestIntensity().get(entry.getKey()));
					percentageLabel = new Label(percentage + "%");
				}
				percentageLabel.setBounds(new Rectangle(x+size, y, 35, 15 ));
				newFigure.add(percentageLabel);
				
				y += 15;
				i++;
			}
			
			newFigure.setSize(glycanNode.figure.getSize().expand(expandX, i*15 + 30));
		}
		else if (type == Decorator.COLUMN) {
			int y = glycanNode.figure.getClientArea().getBottomLeft().y;
			int x = glycanNode.figure.getClientArea().getCenter().x - 40;
			int i=0;
			for (Iterator<Entry<Integer, Double>> iterator = glycanNode.getIntensity().entrySet().iterator(); iterator.hasNext();) {
				Entry<Integer, Double> entry = (Entry<Integer, Double>) iterator.next();
				Figure decorator = new DecoratorColumnChartFigure(entry.getValue(), glycanNode.getHighestIntensity().get(entry.getKey()), standardIntensity[i]);
				decorator.setBounds(new Rectangle(x, y, 15, 80));
				newFigure.add(decorator);
				
			/*	IFigure percentageLabel;
				if (entry.getValue() == null) { // null for merged annotations that do not exist in one of the results
					percentageLabel = new Label ("N/A");
				} else {
					int percentage = (int) Math.round(entry.getValue() * 100 / glycanNode.getHighestIntensity().get(entry.getKey()));
					percentageLabel = new Label(percentage + "%");
				}
				percentageLabel.setBounds(new Rectangle(x, y-20, 30, 20 ));
				newFigure.add(percentageLabel);*/
				
				// add legends
				IFigure legendLabel = new Label(i+1 + "");
				legendLabel.setBounds(new Rectangle(x, y+85, 20, 20 ));
				newFigure.add(legendLabel);
				
				x += 15;
				i++;
			}
			newFigure.setSize(glycanNode.figure.getSize().expand(0, 110));
		}
	}

	public void generateFigureWithDecorator (Decorator type, Double standardIntensity ) {
		this.figureWithDecorator = new Figure ();
		figureWithDecorator.setLayoutManager(new FreeformLayout());
		figureWithDecorator.add(figure);
		
		GlycanNode.addDecorator(figureWithDecorator, this, type, standardIntensity);
	}
	
	public void generateFigureWithDecoratorForMerge (Decorator type, Double[] standardIntensity ) {
		this.figureWithDecorator = new Figure ();
		figureWithDecorator.setLayoutManager(new FreeformLayout());
		figureWithDecorator.add(figure);
		
		
		GlycanNode.addDecoratorForMerge (figureWithDecorator, this, type, standardIntensity);
	}
	
	public List<GlycanNode> getConnectedTo() {
		return connections;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GlycanNode)
			if (this.id != null && this.getId().equals(((GlycanNode) obj).getId()))
				return true;
		return false;
	}

	public Set<GlycanNode> getTransitiveConnections() {
		return transitiveConnections;
	}

	public void setTransitiveConnections(List<GlycanNode> transitiveConnections) {
		this.transitiveConnections.addAll(transitiveConnections);
	}

	public Map<CustomExtraData, Object> getFeatures() {
		return features;
	}

	public void setFeatures(Map<CustomExtraData, Object> features) {
		this.features = features;
	}
	
	public Object getFeature (CustomExtraData column) {
		for (CustomExtraData feature : features.keySet()) {
			if (feature.equals(column))
				return features.get(feature);
		}
		return null;
	}
	
	public void addFeature (CustomExtraData column, Object value) {
		features.put(column, value);
	}

	public void addConnection(GlycanNode connected) {
		if (connections != null) {
			if (!connections.contains(connected))
				connections.add(connected);
		}
	}

	public IFigure getFigureWithDecorator() {
		return figureWithDecorator;
	}

	public List<GlycanNode> getConnections() {
		return connections;
	}

	public Double getIntensity(Integer origin) {
		if (origin == null) {
			if (this.intensity != null && !this.intensity.isEmpty())
				// return the first one
				return this.intensity.values().iterator().next();
		}
		else
			if (this.intensity != null) 
				return this.intensity.get(origin);
		return null;
	}

	public void addIntensity(Integer origin, double intensity2) {
		if (this.intensity == null)
			this.intensity = new HashMap<>();
		Double i = intensity.get(origin);
		if (i != null) {
			i += intensity2;
			intensity.remove(origin);
			intensity.put(origin, i);
		}
		else {
			intensity.put(origin, intensity2);
		}
	}

	public void setFigureWithDecorator(IFigure figureWithDecorator2) {
		this.figureWithDecorator = figureWithDecorator2;
	}

	public Map<Integer, Double> getHighestIntensity() {
		return highestIntensity;
	}

	public void setHighestIntensity(Map<Integer, Double> highestIntensity2) {
		this.highestIntensity = highestIntensity2;
	}

	public ReducingEnd getReducingEnd() {
		return reducingEnd;
	}

	public void setReducingEnd(ReducingEnd reducingEnd) {
		this.reducingEnd = reducingEnd;
	}
}
