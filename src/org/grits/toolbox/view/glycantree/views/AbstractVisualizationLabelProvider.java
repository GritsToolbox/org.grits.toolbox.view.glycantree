package org.grits.toolbox.view.glycantree.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.grits.toolbox.view.glycantree.model.Filter;
import org.grits.toolbox.view.glycantree.model.GlycanNode;
import org.grits.toolbox.view.glycantree.model.GlycanTree;

abstract class AbstractVisualizationLabelProvider implements GlycanLabelProvider, IConnectionStyleProvider, IEntityStyleProvider, IFigureProvider {
	
	public Color LIGHT_BLUE = new Color(Display.getDefault(), 216, 228, 248);
	public Color DARK_BLUE = new Color(Display.getDefault(), 1, 70, 122);
	public Color GREY_BLUE = new Color(Display.getDefault(), 139, 150, 171);
	public Color LIGHT_BLUE_CYAN = new Color(Display.getDefault(), 213, 243, 255);
	public Color LIGHT_YELLOW = new Color(Display.getDefault(), 255, 255, 206);
	public Color GRAY = new Color(Display.getDefault(), 128, 128, 128);
	public Color LIGHT_GRAY = new Color(Display.getDefault(), 220, 220, 220);
	public Color BLACK = new Color(Display.getDefault(), 0, 0, 0);
	public Color RED = new Color(Display.getDefault(), 255, 0, 0);
	public Color DARK_RED = new Color(Display.getDefault(), 127, 0, 0);
	public Color ORANGE = new Color(Display.getDefault(), 255, 196, 0);
	public Color YELLOW = new Color(Display.getDefault(), 255, 255, 0);
	public Color GREEN = new Color(Display.getDefault(), 0, 255, 0);
	public Color DARK_GREEN = new Color(Display.getDefault(), 0, 127, 0);
	public Color LIGHT_GREEN = new Color(Display.getDefault(), 96, 255, 96);
	public Color CYAN = new Color(Display.getDefault(), 0, 255, 255);
	public Color BLUE = new Color(Display.getDefault(), 0, 0, 255);
	public Color WHITE = new Color(Display.getDefault(), 255, 255, 255);
	public Color EDGE_WEIGHT_0 = new Color(Display.getDefault(), 192, 192, 255);
	public Color EDGE_WEIGHT_01 = new Color(Display.getDefault(), 64, 128, 225);
	public Color EDGE_WEIGHT_02 = new Color(Display.getDefault(), 32, 32, 128);
	public Color EDGE_WEIGHT_03 = new Color(Display.getDefault(), 0, 0, 128);
	public Color EDGE_DEFAULT = new Color(Display.getDefault(), 64, 64, 128);
	public Color EDGE_HIGHLIGHT = new Color(Display.getDefault(), 192, 32, 32);
	public Color DISABLED = new Color(Display.getDefault(), 230, 240, 250);

	private Object selectedTree = null; 
	protected Object rootTree = null;
	private Color disabledColor = null;
	protected Object selectedNode = null;
	private GraphViewer viewer;
	
	private Filter filter = null;
	
	private Color rootColor = null;
	private Color rootSelectedColor = null;
	private List<Object> filteredNodes = new ArrayList<>();
	private List<Object> filteredRelationships = new ArrayList<>();
	
	/**
	 * Create a new Abstract Visualization Label Provider
	 * 
	 * @param viewer
	 * @param currentLabelProvider
	 *            The current label provider (or null if none is present). This
	 *            is used to maintain state between the old one and the new one.
	 */
	public AbstractVisualizationLabelProvider(GraphViewer viewer, AbstractVisualizationLabelProvider currentLabelProvider) {
		this.viewer = viewer;
	}
	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		
	}
	
	/**
	 * Calculate all the interesting dependencies. Create an instance of this
	 * method to calculate the entities and relationships that should
	 * "stand-out" for this view.
	 * 
	 * @param interestingRels
	 * @param interestingEntities
	 */
	protected abstract void calculateFilteredNodes(List<Object> filteredRelationships2, List<Object> filteredNodes2);

	@Override
	public void dispose() {
		if (this.disabledColor != null) {
			this.disabledColor.dispose();
			this.disabledColor = null;
		}
		if ( this.rootColor != null) {
			this.rootColor.dispose();
			this.rootColor = null;
		}
		if ( this.rootSelectedColor != null) {
			this.rootSelectedColor.dispose();
			this.rootSelectedColor = null;
		}
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		
	}

	@Override
	public Color getNodeHighlightColor(Object entity) {
		return null;
	}

	@Override
	public Color getBorderColor(Object entity) {
		if (this.selectedTree != null) {
			GlycanTree tree = (GlycanTree)this.selectedTree;
			if (tree.getNodes().contains(entity)) {
				// If this is the selected node return no colour. The default
				// selected colour is fine.
				return BLACK;
			} else if (filteredNodes.contains(entity)) {
				return BLACK;
			}
			else {
				return LIGHT_GRAY;
			}
		}
		return BLACK;
	}

	@Override
	public Color getBorderHighlightColor(Object entity) {
		return CYAN;
	}

	@Override
	public int getBorderWidth(Object entity) {
		return 1;
	}

	@Override
	public Color getBackgroundColour(Object entity) {
		if (this.selectedTree != null) {
			GlycanTree tree = (GlycanTree)this.selectedTree;
			if (tree.getNodes().contains(entity)) { 
				return viewer.getGraphControl().DEFAULT_NODE_COLOR;
			}
			else {
				return getDisabledColor();
			}
		} else if (filteredNodes.contains(entity)) {
			return viewer.getGraphControl().HIGHLIGHT_ADJACENT_COLOR;
		}
		else {
			return getDisabledColor();
		}
	}
	
	private Color getDisabledColor() {
		if (disabledColor == null) {
			disabledColor = new Color(Display.getDefault(), new RGB(225, 238, 255));
		}
		return disabledColor;
	}

	@Override
	public Color getForegroundColour(Object entity) {
		if (this.selectedTree != null) {
			GlycanTree tree = (GlycanTree)this.selectedTree;
			if (tree.getNodes().contains(entity)) {
				// If this is the selected node return no colour. The default
				// selected colour is fine.
				return BLACK;
			} else if (filteredNodes.contains(entity)) {
				return BLACK;
			}
			else {
				return GRAY;
			}
		}
		return BLACK;
	}

	@Override
	public boolean fisheyeNode(Object entity) {
		return true;
	}

	@Override
	public int getConnectionStyle(Object rel) {
		if (filteredRelationships.contains(rel) || filter == null) {
			return ZestStyles.CONNECTIONS_DASH | ZestStyles.CONNECTIONS_DIRECTED;
		}
		return ZestStyles.CONNECTIONS_DIRECTED;
	}

	@Override
	public Color getColor(Object rel) {
		if (this.selectedTree != null) {	
			if (filteredRelationships.contains(rel) || filter == null) {
				return DARK_RED;
			}
			else 
				return LIGHT_GRAY;
		}
		return DARK_RED;
	}

	@Override
	public Color getHighlightColor(Object rel) {
		return DARK_RED;
	}

	@Override
	public int getLineWidth(Object rel) {
		return 1;
	}

	@Override
	public IFigure getTooltip(Object entity) {
		return null;
	}

	@Override
	public void setCurrentSelection(Object root, Object currentSelection) {
		for (Iterator<Object> iter = filteredRelationships.iterator(); iter.hasNext();) {
			EntityConnectionData entityConnectionData = (EntityConnectionData) iter.next();
			viewer.unReveal(entityConnectionData);
		}

		this.rootTree = root;

		this.selectedTree = currentSelection;
		
		Object[] connections = viewer.getConnectionElements();

		filteredNodes= new ArrayList<Object>();
		filteredRelationships = new ArrayList<Object>();
		if (this.selectedTree != null ) {
			calculateFilteredNodes(filteredRelationships, filteredNodes);
		}

		for (Iterator<Object> iter = filteredRelationships.iterator(); iter.hasNext();) {
			Object entityConnectionData = iter.next();
			viewer.reveal(entityConnectionData);
		}

		for (int i = 0; i < connections.length; i++) {
			viewer.update(connections[i], null);
		}
	}

	protected Object getSelected() {
		return selectedTree;
	}
	
	public Filter getFilter() {
		return filter;
	}
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	@Override
	public IFigure getFigure(Object element) {
		if (element instanceof GlycanNode) {
			if (filteredNodes.contains(element)) {
				IFigure fig = ((GlycanNode) element).getFigureWithDecorator();
				if (fig == null)
					fig = ((GlycanNode) element).getFigure();
				if (getFilter() != null) {
					fig.setBackgroundColor(LIGHT_BLUE_CYAN);
					fig.setOpaque(true);
				}
				else {
					fig.setOpaque(false);
				}
				return fig;
			}
			else {
				IFigure fig = ((GlycanNode) element).getFigureWithDecorator();
				if (fig == null)
					fig = ((GlycanNode) element).getFigure();
				fig.setOpaque(false);
				return fig;
			}
		}
		return null;
	}
	
	@Override
	public void setSelectedNode(Object selectedNode) {
		this.selectedNode = selectedNode;
	}
}
