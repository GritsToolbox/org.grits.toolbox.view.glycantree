package org.grits.toolbox.view.glycantree.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.merge.om.data.ExperimentAnnotation;
import org.grits.toolbox.merge.om.data.MergeSettings;
import org.grits.toolbox.view.glycantree.model.Filter;
import org.grits.toolbox.view.glycantree.model.FilterData;
import org.grits.toolbox.view.glycantree.views.FilterTableSetup;

public class TreeGenerationOptionsDialog extends TitleAreaDialog {

	Filter filter = null;
	TableViewer tableViewer = null;
	ControlDecoration[] dec;
	
	Double[] standardIntensity=new Double[1];
	int standardIntensitySize;
	MergeSettings settings = null;  // for merge results
	String entryName = null; // for single annotation
	private Table table;
	
	@Override
	protected Point getInitialSize() {
		return new Point(500, 650);
	}
	
	public TreeGenerationOptionsDialog(Shell parentShell, String entryName) {
		super(parentShell);
		this.entryName = entryName;
	}	
	
	public TreeGenerationOptionsDialog(Shell shell, MergeSettings mergeSettings) {
		super(shell);
		this.settings = mergeSettings;
		this.standardIntensitySize = mergeSettings.getExperimentList().size();
		this.standardIntensity = new Double[standardIntensitySize];
	}

	public Control createDialogArea(final Composite parent) 
    {
        Composite comp = (Composite) super.createDialogArea(parent);
        
        setMessage("Please select filter options for Tree Generation");
        setTitle("Generate Glycan Tree");
        Composite container = new Composite(comp, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        
        Composite tableContainer = new Composite (container, SWT.NONE);
        tableViewer  = new FilterTableSetup().createFilterTableSection(tableContainer);
        
        createSeparator(container, 1);
        Group standardGroup = new Group (container, SWT.NONE);
        standardGroup.setLayout(new GridLayout(1, false));
        
        // Specify the decoration image and description
  		final Image image = FieldDecorationRegistry.
  			  getDefault().
  			  getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).
  			  getImage();
        
        table = new Table(standardGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        TableColumn tblclmnEntry = new TableColumn(table, SWT.NONE);
        tblclmnEntry.setWidth(100);
        tblclmnEntry.setText("Entry");
        
        TableColumn tblclmnStandard = new TableColumn(table, SWT.NONE);
        tblclmnStandard.setWidth(118);
        tblclmnStandard.setText("Standard (intensity)");
        
        final TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.LEFT;
		editor.minimumWidth = 50;
        
        table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Clean up any previous editor control
				Control oldEditor = editor.getEditor();
				if (oldEditor != null) oldEditor.dispose();
		
				// Identify the selected row
				TableItem item = (TableItem)e.item;
				if (item == null) return;
		
				// The control that will be the editor must be a child of the Table
				Text newEditor = new Text(table, SWT.NONE);
				newEditor.setText(item.getText(1));
				newEditor.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent me) {
						Text text = (Text)editor.getEditor();
						editor.getItem().setText(1, text.getText());
					}
				});
				
				newEditor.selectAll();
				newEditor.setFocus();
				editor.setEditor(newEditor, item, 1);
				
				 if (entryName != null && settings == null) {
		        	// single annotation
		        	dec = new ControlDecoration[1];
		        	// Create a control decoration for the control.
			  		dec[0] = new ControlDecoration(editor.getEditor(), SWT.TOP | SWT.LEFT);
			  		dec[0].setImage(image);
			  		dec[0].setDescriptionText("Intensity should be a number");
			  		dec[0].hide();
				 }
				 else if (settings != null) {
		        	// merge result
		        	dec = new ControlDecoration[settings.getExperimentList().size()];
		        	for (int i=0; i < settings.getExperimentList().size(); i++) {
		        		// Create a control decoration for the control.
				  		dec[i] = new ControlDecoration(editor.getEditor(), SWT.TOP | SWT.LEFT);
				  		dec[i].setImage(image);
				  		dec[i].setDescriptionText("Intensity should be a number");
				  		dec[i].hide();
					}
				 }
			}
		});
           
        if (entryName != null && settings == null) {
        	// single annotation
        	dec = new ControlDecoration[1];
    		
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, entryName);
		}
        else if (settings != null) {
        	// merge result
        	dec = new ControlDecoration[settings.getExperimentList().size()];
        	for (ExperimentAnnotation experiment : settings.getExperimentList()) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0, experiment.getAnnotationShortName());
			}
        }
        
        return comp;
    }
	
	protected Label createSeparator(Composite container, int span) {
        GridData separatorData = new GridData();
        separatorData.grabExcessHorizontalSpace = true;
        separatorData.horizontalAlignment = GridData.FILL;
        separatorData.horizontalSpan = span;
        Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(separatorData);
        return separator;
    }
	
	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		
		if (tableViewer != null) {
			filter = new Filter();
			filter.setOptions((List<FilterData>) tableViewer.getInput());
			
			if (validate ())
				super.okPressed();
		}
		
	}
	
	public Filter getFilter () {
		return this.filter;
	}

	public Double[] getStandardIntensity() {
		return standardIntensity;
	}

	public void setStandardIntensity(Double[] standardIntensity) {
		this.standardIntensity = standardIntensity;
	}
	
	private boolean validate() {
		boolean allValid = true;
		for (int i = 0; i < table.getItemCount(); i++) {
			TableItem row = table.getItem(i);
			String s = (String)row.getText(1);
			if (s != null  && s.trim().length() != 0) {
				try {
					standardIntensity[i] = Double.parseDouble(s);
					dec[i].hide();
				} catch (NumberFormatException e) {
					dec[i].show();
					allValid = false;
				}
			}
		}
		
		return allValid;
	}
}
