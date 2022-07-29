package org.grits.toolbox.view.glycantree.model;

public enum Decorator {
		BAR ("Bar Chart"), 
		PIE ("Pie Chart"),
		COLUMN ("Histogram");
		private final String stringValue;
		private Decorator(final String s) { stringValue = s; }
		public String toString() { return stringValue; }
}
