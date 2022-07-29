package org.grits.toolbox.view.glycantree.views;

import org.eclipse.jface.viewers.ILabelProvider;
import org.grits.toolbox.view.glycantree.model.Filter;

public interface GlycanLabelProvider extends ILabelProvider {
	/**
	 * Sets the currently selected Object
	 * @param root The root node in this dependency
	 * @param currentSelection The currently selected node
	 */
	public void setCurrentSelection( Object root, Object currentSelection );
	
	public void setFilter (Filter filter);

	public void setSelectedNode(Object selectedElement);
	
}
