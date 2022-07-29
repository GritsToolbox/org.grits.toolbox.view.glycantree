package org.grits.toolbox.view.glycantree.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.grits.toolbox.ms.annotation.sugar.GlycanExtraInfo;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.CustomExtraData.Type;
import org.grits.toolbox.view.glycantree.Activator;
import org.grits.toolbox.view.glycantree.model.FilterData;
import org.grits.toolbox.view.glycantree.views.table.FilterValueEditingSupport;

public class FilterTableSetup {
	
	private static final Image CHECKED = Activator.getImageDescriptor("icons/checkbox_yes.png").createImage();
	private static final Image UNCHECKED = Activator.getImageDescriptor("icons/checkbox_no.png").createImage();
	
	TableViewer tableViewer=null;
	
	public TableViewer createFilterTableSection (Composite filterComposite) {
		GridLayout gl_comp = new GridLayout();
        gl_comp.numColumns = 3;
		filterComposite.setLayout(gl_comp);
		
        // get all available filter option labels
        List<CustomExtraData> columns = GlycanExtraInfo.getColumns();
       
        final ComboViewer combo = new ComboViewer(filterComposite, SWT.READ_ONLY);

        combo.setContentProvider(ArrayContentProvider.getInstance());

        combo.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof CustomExtraData) {
                    CustomExtraData column = (CustomExtraData) element;
                    return column.getLabel();
                }
                return super.getText(element);
            }
        });
        
        combo.setInput(columns);
        
        Button addButton = new Button(filterComposite, SWT.PUSH);
        addButton.setText("Add");
        addButton.addSelectionListener(new SelectionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tableViewer != null) {
					IStructuredSelection selected = (IStructuredSelection) combo.getSelection();
					if (selected != null) {
						if (selected.getFirstElement() instanceof CustomExtraData) {
							FilterData option = new FilterData();
							option.setColumn((CustomExtraData) selected.getFirstElement());
							option.setValue(null);
							List<FilterData> currentInput = (List<FilterData>)tableViewer.getInput();
							//if (!currentInput.contains(option))
								currentInput.add(option);
							tableViewer.refresh();
						}
					}
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        
        Button removeButton = new Button(filterComposite, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.addSelectionListener(new SelectionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tableViewer != null) {
					IStructuredSelection selected = (IStructuredSelection) tableViewer.getSelection();
					if (selected != null) {
						if (selected.getFirstElement() instanceof FilterData) {
							FilterData filter = (FilterData)selected.getFirstElement();
							((List<FilterData>)tableViewer.getInput()).remove(filter);
							tableViewer.refresh();
						}
					}
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});        
        
        createTable(filterComposite);
        
        return this.tableViewer;
	}
	
	private void createTable (Composite parent) {
		  tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		  tableViewer.getTable().setHeaderVisible(true);
		  tableViewer.getTable().setLinesVisible(true);
		  
		  GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		  data.heightHint = 250;
		  data.horizontalSpan=3;
		  tableViewer.getTable().setLayoutData(data); 

		  TableViewerColumn filterColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		  filterColumn.getColumn().setText("Filter");
		  filterColumn.getColumn().setWidth(150);
		  
		  filterColumn.setLabelProvider(new ColumnLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  if (element instanceof FilterData) {
	                    CustomExtraData column = ((FilterData) element).getColumn();
	                    return column.getLabel();
	              }
	              return super.getText(element);
			  }
		  });
			
		  TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		  valueColumn.getColumn().setText("Value");
		  valueColumn.getColumn().setWidth(100);
		  valueColumn.setEditingSupport(new FilterValueEditingSupport(tableViewer));
		  valueColumn.setLabelProvider(new ColumnLabelProvider() {
			  	@Override
				public String getText(Object element) {
			  		if (element instanceof FilterData) {
	                    Object value = ((FilterData) element).getValue();
	                    Type type = ((FilterData) element).getColumn().getType();
	                    switch (type) {
	                    case Boolean :
	                    	return null;
	                    case String:
	                    	if (value == null) return "";
	                    	return (String) value;
	                    case Integer:
	                    case Double:
	                    	if (((FilterData) element).getColumn().getKey().equals(GlycanExtraInfo.N_BRANCHES)) {
	                    		if (value == null) return "2";
	                    		else return value+ "";
	                    	}
	                    	else {
	                    		if (value == null) return "1";
	                    		else return value +"";
	                    	}
						default:
							return null;
	                    }
	                }
			  		return super.getText(element);
				}
			  	
			  	@Override
			  	public Image getImage(Object element) {
			  		if (element instanceof FilterData) {
	                    Object value = ((FilterData) element).getValue();
	                    Type type = ((FilterData) element).getColumn().getType();
	                    switch (type) {
	                    case Boolean :
	                    	if (value == null)
	                    		return CHECKED;
	                    	if (((Boolean)value).booleanValue() == true)
	                    		return CHECKED;
	                    	else
	                    		return UNCHECKED;
	                    case String:
	                    	return null;
	                    case Integer:
	                    case Double:
	                    	return null;
						default:
							return null;
	                    }
			  		}
			  		return super.getImage(element);
			  	}
		  });
		  
		  tableViewer.setContentProvider(new IStructuredContentProvider() {
				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void dispose() {
					// TODO Auto-generated method stub
					
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public Object[] getElements(Object inputElement) {
					return ((List<FilterData>)inputElement).toArray();
				}
		  });
		  
		  tableViewer.setInput(new ArrayList<FilterData>());
	}
}
