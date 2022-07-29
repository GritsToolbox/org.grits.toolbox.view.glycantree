package org.grits.toolbox.view.glycantree.model;

import java.util.List;

import org.grits.toolbox.merge.om.data.MergeSettings;

public class GlycanTree {
	
	String filterDescription;
	Double intensity;
	
	double lowestIntensity;
	double highestIntensity;
	
	List<GlycanNode> nodes;
	
	Double[] standardIntensity;
	boolean mergeResult = false;
	MergeSettings mergeSettings;
	
	public Double getIntensity() {
		return intensity;
	}

	public void setIntensity(Double intensity) {
		this.intensity = intensity;
	}

	public List<GlycanNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<GlycanNode> nodes) {
		this.nodes = nodes;
		this.intensity = 0D;
		this.lowestIntensity = 0.0;
		this.highestIntensity = 0.0;
		for (GlycanNode glycanNode : nodes) {
			for (Double intensity1 : glycanNode.getIntensity().values()) {
				if (intensity1 != null) {
					this.lowestIntensity = Math.min(lowestIntensity, intensity1);
					this.highestIntensity = Math.max(highestIntensity, intensity1);
					intensity += intensity1;
				}
			}
			
		}
	}

	public String getFilterDescription() {
		return filterDescription;
	}

	public void setFilterDescription(String filterDescription) {
		this.filterDescription = filterDescription;
	}

	public double getLowestIntensity() {
		return lowestIntensity;
	}

	public double getHighestIntensity() {
		return highestIntensity;
	}

	public void setLowestIntensity(double lowestIntensity) {
		this.lowestIntensity = lowestIntensity;
	}

	public void setHighestIntensity(double highestIntensity) {
		this.highestIntensity = highestIntensity;
	}

	public Double[] getStandardIntensity() {
		return standardIntensity;
	}

	public void setStandardIntensity(Double[] standardIntensity2) {
		this.standardIntensity = standardIntensity2;
	}

	public boolean isMergeResult() {
		return mergeResult;
	}

	public void setMergeResult(boolean mergeResult) {
		this.mergeResult = mergeResult;
	}

	public void setMergeSettings(MergeSettings settings) {
		this.mergeSettings = settings;
	}

	public MergeSettings getMergeSettings() {
		return mergeSettings;
	}
}
