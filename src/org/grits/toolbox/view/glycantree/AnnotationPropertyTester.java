package org.grits.toolbox.view.glycantree;

import org.eclipse.core.expressions.PropertyTester;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;


public class AnnotationPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof Entry) {
			if (((Entry)receiver).getProperty().getType().equals(MSGlycanAnnotationProperty.TYPE) ||
				((Entry)receiver).getProperty().getType().equals(MSGlycanAnnotationReportProperty.TYPE))
				return true;
			
		}
		return false;
	}

}
