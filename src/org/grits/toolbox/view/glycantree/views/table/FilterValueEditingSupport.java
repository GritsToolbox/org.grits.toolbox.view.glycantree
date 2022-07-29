package org.grits.toolbox.view.glycantree.views.table;


import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.ms.annotation.sugar.GlycanExtraInfo;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.CustomExtraData.Type;
import org.grits.toolbox.view.glycantree.model.FilterData;

public class FilterValueEditingSupport extends EditingSupport {
	
	TableViewer viewer= null;
	
	TextCellEditor textEditor;
	TextCellEditor integerEditor;
	TextCellEditor doubleEditor;
	ComboBoxCellEditor cellEditorGlycanType;
	ComboBoxCellEditor cellEditorGlycanSubType;
	CheckboxCellEditor checkBoxEditor;
	
	String[] glycanType = new String[] {"N-Glycan", "O-GalNAc", "O-Man",  "O-Fuc", "GSL" };
	String[] glycanSubType = new String[] {"complex", "hybrid", "high mannose", "core 1", "core 2", "core 3", "core 4", "core 5", "core 6", "core 7"};

	public FilterValueEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
		init();
	}
	
	private void init() {
		this.integerEditor = new TextCellEditor(this.viewer.getTable());
		((Text)integerEditor.getControl()).setTextLimit(PropertyHandler.LABEL_TEXT_LIMIT);
        ControlDecoration intControlDecoration = new ControlDecoration(integerEditor.getControl(), SWT.CENTER);
        integerEditor.setValidator(new IntegerValidator(intControlDecoration));  
        
        this.doubleEditor = new TextCellEditor(this.viewer.getTable());
        ((Text)doubleEditor.getControl()).setTextLimit(PropertyHandler.LABEL_TEXT_LIMIT);
        ControlDecoration doubleControlDecoration = new ControlDecoration(doubleEditor.getControl(), SWT.CENTER);
        doubleEditor.setValidator(new DoubleValidator(doubleControlDecoration));  
        
        this.textEditor = new TextCellEditor(this.viewer.getTable());
        this.checkBoxEditor = new CheckboxCellEditor(this.viewer.getTable(), SWT.CHECK); 
        this.cellEditorGlycanSubType = new ComboBoxCellEditor(this.viewer.getTable(), glycanSubType);
        this.cellEditorGlycanType = new ComboBoxCellEditor(this.viewer.getTable(), glycanType);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if (element instanceof FilterData) {
			CustomExtraData column = ((FilterData) element).getColumn();
			if (column.getType() == Type.Boolean) {
				if (column.getKey().equals(GlycanExtraInfo.N_BRANCHES)) {
			        return integerEditor;
				}
				return checkBoxEditor;
			} else if (column.getType() == Type.Integer) {
				return integerEditor;
			} else if ( column.getType() == Type.Double) {
				return doubleEditor;
			} else if (column.getType() == Type.String) {
				if (column.getKey().equals(GlycanExtraInfo.TYPE)) {
					return this.cellEditorGlycanType;
				} else if (column.getKey().equals(GlycanExtraInfo.SUBTYPE)) {
					return this.cellEditorGlycanSubType;
				} 
				else
					return this.textEditor;
			}
		}
		return null;
	}

	@Override
	protected boolean canEdit(Object element) {
		if (element instanceof FilterData)
			return true;
		return false;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof FilterData) {
			CustomExtraData column = ((FilterData) element).getColumn();
			if (column.getType() == Type.Boolean) {
				Object value = ((FilterData) element).getValue();
				if (value == null)
					return false;
				else return ((Boolean)value).booleanValue(); 
			}
			else if (column.getType() == Type.Integer || column.getType() == Type.Double) {
				Object value = ((FilterData) element).getValue();
				if (column.getKey().equals(GlycanExtraInfo.N_BRANCHES)) {
					if (value == null)
						return "2";
					return value + "";
				} else {
					if (value == null)
						return "1";
					if (column.getType() == Type.Integer) 
						return ((Integer)value).intValue() + "";
					else if (column.getType() == Type.Double)
							return ((Double)value).doubleValue() + "";
				}
			} else if (column.getType() == Type.String) {
				if (column.getKey().equals(GlycanExtraInfo.TYPE)) {
					String value = (String)((FilterData) element).getValue();
					int i=0;
					for (String selected : glycanType) {
						if (value != null && value.equals(selected)) 
							return i;
						i++;
					}
					return 0;
				} else if (column.getKey().equals(GlycanExtraInfo.SUBTYPE)) {
					String value = (String)((FilterData) element).getValue();
					int i=0;
					for (String selected : glycanSubType) {
						if (value != null && value.equals(selected)) 
							return i;
						i++;
					}
					return 0;
				}
				else {
					String value = (String)((FilterData) element).getValue();
					if (value == null)
						return "";
					else return value;
				}
			}
		}
		return null;	
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (value == null)
			return;
		if (element instanceof FilterData) {
			CustomExtraData column = ((FilterData) element).getColumn();
			if (column.getType() == Type.Boolean) {
				((FilterData) element).setValue(value);    // value is from check box, true/false
			}
			else if  (column.getType() == Type.Integer) {
				// value is from TextCellEditor, so it is a string
				((FilterData) element).setValue(Integer.parseInt((String)value)); 
			} else if (column.getType() == Type.Double) {
				// value is from TextCellEditor, so it is a string
				((FilterData) element).setValue(Double.parseDouble((String)value)); 
			} else if (column.getType() == Type.String) {  // value is from combo box, so it is the index of the selected entry
				if (column.getKey().equals(GlycanExtraInfo.TYPE)) {
					((FilterData) element).setValue(glycanType[((Integer)value).intValue()]);
				} else if (column.getKey().equals(GlycanExtraInfo.SUBTYPE)) {
					((FilterData) element).setValue(glycanSubType[((Integer)value).intValue()]);
				} else {
					((FilterData) element).setValue(value);
				}
			}
		}
	    viewer.update(element, null);
	}

}
