package org.grits.toolbox.view.glycantree.model;


import org.grits.toolbox.ms.om.data.CustomExtraData;

public class FilterData {
	
	CustomExtraData column;
	Object value=null;
	
	public CustomExtraData getColumn() {
		return column;
	}
	public void setColumn(CustomExtraData column) {
		this.column = column;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return column.getLabel() + ": " + (value == null ? "" : String.valueOf(value));
	}
	
}
