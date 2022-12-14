package org.grits.toolbox.view.glycantree.views.table;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;

public class DoubleValidator implements ICellEditorValidator {
	private static final String INVALID_MESSAGE = "Not a Double Value";
	private ControlDecoration controlDecoration;
	
	public DoubleValidator(ControlDecoration controlDecoration) {
	    this.controlDecoration = controlDecoration;
	    Image errorImage = FieldDecorationRegistry.getDefault()
	            .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
	            .getImage();
	    this.controlDecoration.setMarginWidth(2);
	    this.controlDecoration.setImage(errorImage);
	    this.controlDecoration.setDescriptionText(INVALID_MESSAGE);
	}
	
	@Override
	public String isValid(Object value) {
		String inValidMessage = null;
		this.controlDecoration.hide();
		if (value != null) {
			String stringValue = (String) value;
			if (!stringValue.isEmpty()) {
				try {
					Double.parseDouble(stringValue);
				} catch (NumberFormatException e) {
					inValidMessage = INVALID_MESSAGE;
					this.controlDecoration.show();
				}
			}
		}
		return inValidMessage;
	}
}
