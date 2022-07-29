package org.grits.toolbox.view.glycantree.views;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.grits.toolbox.view.glycantree.model.GlycanNode;

public class GraphContentProvider implements IGraphEntityContentProvider {

	Object currentNode;  // to be used when there is an option to show a single path only
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		currentNode = newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List)
			return ((List)inputElement).toArray();
		else if (inputElement instanceof GlycanNode) {
			return getConnectedTo (inputElement);
		}
		return null;
	}

	@Override
	public Object[] getConnectedTo(Object entity) {
		Object[] glycans = null;
		if (entity instanceof GlycanNode) {
			List<GlycanNode> c = ((GlycanNode) entity).getConnectedTo();
			if (c != null)
				glycans = c.toArray();
		}
		return glycans;
	}
}
