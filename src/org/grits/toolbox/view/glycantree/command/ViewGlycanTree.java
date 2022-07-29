package org.grits.toolbox.view.glycantree.command;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.report.property.MSGlycanAnnotationReportProperty;
import org.grits.toolbox.merge.om.data.MergeReport;
import org.grits.toolbox.merge.om.data.MergeSettings;
import org.grits.toolbox.merge.xml.Deserialize;
import org.grits.toolbox.view.glycantree.Perspective;
import org.grits.toolbox.view.glycantree.analysis.AnalysisUtil;
import org.grits.toolbox.view.glycantree.dialog.TreeGenerationOptionsDialog;
import org.grits.toolbox.view.glycantree.views.GlycanTreeView;

public class ViewGlycanTree implements IHandler {
	
	private static final Logger logger = Logger.getLogger(ViewGlycanTree.class);

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		logger.debug("BEGIN: ViewGlycanTree");
		StructuredSelection to = PropertyHandler.getDataModel().getLastSelection();
        Entry entry = null;
        if (to.size() == 1)
        {
            //get the selected node..
            entry = (Entry)to.getFirstElement();
        } else {
        	// cannot handle multiple selection
            return null;
        }
        Property prop = entry.getProperty();
        if (prop instanceof MSGlycanAnnotationProperty) {
        	MSGlycanAnnotationProperty msProperty = (MSGlycanAnnotationProperty)prop;
        	boolean canceled = false;
        	// open the view with secondary id: entry.getDisplayName()
            try {
	            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	            IViewReference[] views = page.getViewReferences();
	            boolean found = false;
	            for (IViewReference v : views) {
					if (v.getSecondaryId() != null && v.getSecondaryId().equals(entry.getDisplayName())) {
						// switch to glycantree perspective
			            PropertyHandler.changePerspective(Perspective.ID);
						v.getView(true);
						found = true;
						break;
					}
				}
	            
	            if (!found) {
	            	// create the view for the first time
	            	String fileName = msProperty.getFullyQualifiedArchiveFileNameByAnnotationID(entry);
	            	Shell shell = new Shell();
	            	TreeGenerationOptionsDialog dialog = new TreeGenerationOptionsDialog(shell, entry.getDisplayName());
	            	if (dialog.open() == Window.OK) {
		            	AnalysisUtil analysis = new AnalysisUtil(fileName, dialog.getFilter(), dialog.getStandardIntensity());  
		            	ProgressMonitorDialog monitor = new ProgressMonitorDialog(shell);
		            	monitor.run(true, true, analysis);
		            	if (monitor.getReturnCode() != Window.CANCEL) {
		            		// switch to glycantree perspective
		                    PropertyHandler.changePerspective(Perspective.ID);
			                IViewPart view = page.showView(GlycanTreeView.ID, entry.getDisplayName(), IWorkbenchPage.VIEW_CREATE);
							// set the glycans for the view
							((GlycanTreeView)view).setInput(analysis.getGlycanTree(), true);
		            	} else 
		            		canceled = true;
	            	}
	            	else 
	            		canceled = true;
	            }
	            if (!canceled) page.showView(GlycanTreeView.ID, entry.getDisplayName(), IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				logger.error("Cannot find view", e);
			} catch (InvocationTargetException e) {
				logger.error("Failed to get the input for the view" , e);
			} catch (InterruptedException e) {
				logger.warn("Interrupted", e);
			}  
        } else if (prop instanceof MSGlycanAnnotationReportProperty) {
        	try {
	        	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	            IViewReference[] views = page.getViewReferences();
	            boolean found = false;
	            boolean canceled = false;
	            for (IViewReference v : views) {
					if (v.getSecondaryId() != null && v.getSecondaryId().equals(entry.getDisplayName())) {
						// switch to glycantree perspective
			            PropertyHandler.changePerspective(Perspective.ID);
						v.getView(true);
						found = true;
						break;
					}
				}
	            
	            if (!found) {
		        	// use merge results
		        	MSGlycanAnnotationReportProperty p = (MSGlycanAnnotationReportProperty)prop;
		        	String fileName = p.getFullyQualifiedXMLFileName(entry);
		        	Deserialize des = new Deserialize();
		    		MergeReport data = des.deserialize(fileName);
		    		MergeSettings settings = data.getSettings();
		        	Shell shell = new Shell();
		        	TreeGenerationOptionsDialog dialog = new TreeGenerationOptionsDialog(shell, settings);
		        	if (dialog.open() == Window.OK) {
			        	// read the merge file and generate the GlycanNodes
			        	AnalysisUtil analysis = new AnalysisUtil(fileName, dialog.getFilter(), dialog.getStandardIntensity(), true);  	
			        	ProgressMonitorDialog monitor = new ProgressMonitorDialog(shell);
			        	monitor.run(true, true, analysis);
			        	if (monitor.getReturnCode() != Window.CANCEL) {
			        		// switch to glycantree perspective
			                PropertyHandler.changePerspective(Perspective.ID);
				            IViewPart view = page.showView(GlycanTreeView.ID, entry.getDisplayName(), IWorkbenchPage.VIEW_CREATE);
							// set the glycans for the view
							((GlycanTreeView)view).setInput(analysis.getGlycanTree(), true); 
			        	} else canceled = true;
		        	} else canceled = true;
	            }
	            if (!canceled) page.showView(GlycanTreeView.ID, entry.getDisplayName(), IWorkbenchPage.VIEW_ACTIVATE);
        	} catch (PartInitException e) {
				logger.error("Cannot find view", e);
			} catch (InvocationTargetException e) {
				logger.error("Failed to get the input for the view" , e);
			} catch (InterruptedException e) {
				logger.warn("Interrupted", e);
			}  
        	
        }
        logger.debug("END: ViewGlycanTree");
		return null;
	}

	@Override
	public boolean isEnabled() {
		StructuredSelection to = PropertyHandler.getDataModel().getLastSelection();
        Entry entry = null;
        if (to.size() == 1)
        {
            //get the selected node..
            entry = (Entry)to.getFirstElement();
			if (((Entry)entry).getProperty().getType().equals(MSGlycanAnnotationProperty.TYPE) ||
					((Entry)entry).getProperty().getType().equals(MSGlycanAnnotationReportProperty.TYPE))
					return true;
        }
		return false;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

}
