package org.grits.toolbox.view.glycantree;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.grits.toolbox.view.glycantree.views.GlycanTreeView;

public class Perspective implements IPerspectiveFactory {

	public static String ID = "org.grits.toolbox.glycantree.perspective";
	
	public void createInitialLayout(IPageLayout layout) {
		addPerspectiveShortcuts(layout);
		
		layout.setEditorAreaVisible(false);
		//layout.setFixed(true);
		layout.addView("projectexplorer.views.ProjectExplorerView", IPageLayout.LEFT, 0.22f, IPageLayout.ID_EDITOR_AREA);
		//layout.addStandaloneView(GlycanTreeView.ID, true, IPageLayout.BOTTOM, 1f, IPageLayout.ID_EDITOR_AREA);
		
		layout.addPlaceholder(GlycanTreeView.ID + ":*", IPageLayout.BOTTOM, IPageLayout.RATIO_MAX, IPageLayout.ID_EDITOR_AREA);
	}
	
	private void addPerspectiveShortcuts(IPageLayout layout) {
		layout.addPerspectiveShortcut(ID);
	}

}
