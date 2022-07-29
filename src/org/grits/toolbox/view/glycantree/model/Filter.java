package org.grits.toolbox.view.glycantree.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.grits.toolbox.ms.om.data.CustomExtraData;

public class Filter {
	
	List<FilterData> options = new ArrayList<>();
	Integer lowIntensity=null;
	Integer highIntensity=null;

	public List<FilterData> getOptions() {
		return options;
	}

	public void setOptions(List<FilterData> options) {
		this.options = options;
	}
	

	public Integer getLowIntensity() {
		return lowIntensity;
	}

	public void setLowIntensity(int lowIntensity) {
		this.lowIntensity = lowIntensity;
	}

	public Integer getHighIntensity() {
		return highIntensity;
	}

	public void setHighIntensity(int highIntensity) {
		this.highIntensity = highIntensity;
	}

	public void addFilter (CustomExtraData column, Object value) {
		FilterData data = new FilterData();
		data.setColumn(column);
		data.setValue(value);
		
		options.add(data);
	}
	
	public void removeFilter (CustomExtraData column) {
		FilterData remove = null;
		for (FilterData data : options) {
			if (data.getColumn().equals(column)) {
				remove = data;
				break;
			}
		}
		if (remove != null)
			options.remove(remove);
	}
	
	@Override
	public String toString() {
		String toString = Arrays.asList(this.getOptions()).toString();
		if (lowIntensity != null || highIntensity != null) {
			toString += "[" + lowIntensity.intValue() + ":" + highIntensity.intValue() + "]";
		}
		return toString; 
	}
	
}
