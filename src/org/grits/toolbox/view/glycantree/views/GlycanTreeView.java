package org.grits.toolbox.view.glycantree.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.LayoutStyles;
import org.grits.toolbox.view.glycantree.Activator;
import org.grits.toolbox.view.glycantree.GlycanVizImages;
import org.grits.toolbox.view.glycantree.analysis.AnalysisUtil;
import org.grits.toolbox.view.glycantree.model.Decorator;
import org.grits.toolbox.view.glycantree.model.Filter;
import org.grits.toolbox.view.glycantree.model.GlycanNode;
import org.grits.toolbox.view.glycantree.model.GlycanTree;

public class GlycanTreeView extends ViewPart implements IZoomableWorkbenchPart{
	
	private static final Logger logger = Logger.getLogger(GlycanTreeView.class);
	
	public static final String ID = "glycanytree";
	
	Color LIGHT_BLUE_CYAN = new Color(Display.getDefault(), 213, 243, 255);

	private FormToolkit toolKit = null;
	private GraphViewer viewer;
	
	private GlycanLabelProvider currentLabelProvider;
	private GraphContentProvider contentProvider;
	
	private ZoomContributionViewItem contextZoomContributionViewItem;
	private ZoomContributionViewItem toolbarZoomContributionViewItem;
	private VisualizationForm visualizationForm;
	
	@SuppressWarnings("rawtypes")
	private Stack historyStack;
	
	@SuppressWarnings("rawtypes")
	private Stack forwardStack;
	
	private Action screenshotAction;
	
	private GlycanTree tree;
	private GlycanTree currentTree = null;
	
	private List<Object> selectedNodes = null;

	private Action historyAction;

	private Action forwardAction;
	
	/**
	 * The constructor.
	 */
	@SuppressWarnings("rawtypes")
	public GlycanTreeView() {
		historyStack = new Stack();
		forwardStack = new Stack();
	}
	
	public GlycanTree getCurrentTree() {
		return currentTree;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		setPartName(getViewSite().getSecondaryId());
		toolKit = new FormToolkit(parent.getDisplay());
		visualizationForm = new VisualizationForm(parent, toolKit, this);
		viewer = visualizationForm.getGraphViewer();

		this.contentProvider = new GraphContentProvider();
		this.currentLabelProvider = new HighlightRelationshipLabelProvider(this.viewer, null);
		viewer.setContentProvider(this.contentProvider);
		viewer.setLabelProvider(this.currentLabelProvider);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() == 1) {
					// first click
					// clear previous selections
					clearSelections();
				}
				List<Object> selectedElements = new ArrayList<>();
				for (Iterator iterator = selection.iterator(); iterator
						.hasNext();) {
					Object selectedElement = (Object) iterator.next();
				//	Object selectedElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (selectedElement instanceof EntityConnectionData) {
						return;
					}
					selectedElements.add(selectedElement);
					
				}
				GlycanTreeView.this.selectionChanged(selectedElements);
			}
		});

		toolbarZoomContributionViewItem = new ZoomContributionViewItem(this);
		contextZoomContributionViewItem = new ZoomContributionViewItem(this);

		makeActions();
		hookContextMenu();
		fillToolBar();
		
	}
	
	protected void selectionChanged(List<Object> selectedElements) {
		//currentLabelProvider.setSelectedNode(selectedElement);
		if (this.selectedNodes != null) {
			for (Object currentNode: selectedNodes) {
				if (!selectedElements.contains(currentNode))
					this.setSelectedNode(currentNode, false);
			}
		}
		for (Iterator iterator = selectedElements.iterator(); iterator
				.hasNext();) {
			Object selectedElement = (Object) iterator.next();	
			this.setSelectedNode(selectedElement, true);	
		}
		this.selectedNodes = selectedElements;
		viewer.refresh();
	}

	@SuppressWarnings("unchecked")
	public void filter (GlycanTree t, boolean recordHistory) {
		logger.debug("Filter Nodes");
		if (t != null) {
			if (t.equals(this.tree)) {
				// back to the original
				// enable decorator change
				visualizationForm.enableDecoratorChange(true);
			}
			else {
				visualizationForm.enableDecoratorChange(false);
			}
			clearSelections();  // to clear out the previous selections
			currentLabelProvider.setFilter(null);
			viewer.setInput(t.getNodes());
			viewer.setLayoutAlgorithm(new GlycanTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);	
			visualizationForm.setFilteredName(t.getFilterDescription());
			visualizationForm.setIntensityValue(((GlycanTree) t).getIntensity());
			visualizationForm.setIntensityRange (tree.getLowestIntensity(), tree.getHighestIntensity(), 
					((GlycanTree) t).getLowestIntensity(), ((GlycanTree) t).getHighestIntensity());
			visualizationForm.showLegendGroup(t);
			if (currentTree != null && recordHistory && currentTree != t) {
				historyStack.push(currentTree);
				historyAction.setEnabled(true);
				
			}
			currentTree = t;
		}
		logger.debug("Finished filtering");
	}
	
	private void clearSelections() {
		Iterator nodes1 = viewer.getGraphControl().getNodes().iterator();
		while (nodes1.hasNext()) {
			GraphNode node = (GraphNode) nodes1.next();
			deselectFigure(node.getNodeFigure());
		}
		this.selectedNodes = new ArrayList<>();
	}

	public void setInput (GlycanTree t, boolean initialize) {
		logger.debug("Setting input for the graph viewer");
		if (initialize) {
			this.tree = t;
			visualizationForm.enableDecoratorChange(true);
		}
		if (t != null) {
			viewer.setInput(t.getNodes());
			viewer.setLayoutAlgorithm(new GlycanTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
			visualizationForm.setFilteredName(t.getFilterDescription());
			visualizationForm.setIntensityValue(((GlycanTree) t).getIntensity());
			visualizationForm.setIntensityRange (tree.getLowestIntensity(), tree.getHighestIntensity(), 
					tree.getLowestIntensity(), tree.getHighestIntensity());
			visualizationForm.showLegendGroup(tree);
		}
		currentTree = t;
		highlight(currentTree, null);
		logger.debug("Finished setting input");
	}
	
	private void highlight(Object selected, Filter filter) {
		logger.debug("Highligthing");
		clearSelections();
		currentLabelProvider.setFilter(filter);
		currentLabelProvider.setCurrentSelection(currentTree, selected);
		//viewer.update(contentProvider.getElements(selected), null);
		viewer.refresh();
		logger.debug("Finished highlighting");
	}

	private void fillToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(toolbarZoomContributionViewItem);

		fillLocalToolBar(bars.getToolBarManager());

	}
	
	/**
	 * Add the actions to the tool bar
	 * 
	 * @param toolBarManager
	 */
	private void fillLocalToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(historyAction);
		toolBarManager.add(forwardAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(screenshotAction);
	}

	/**
	 * Creates the context menu for this view.
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		fillContextMenu(menuMgr);

		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				GlycanTreeView.this.fillContextMenu(manager);

			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);

	}

	/**
	 * Add the items to the context menu
	 * 
	 * @param manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(historyAction);
		manager.add(forwardAction);
		manager.add(new Separator());
		manager.add(screenshotAction);
		manager.add(new Separator());
		manager.add(contextZoomContributionViewItem);
	}

	private void makeActions() {
		screenshotAction = new Action() {
			public void run() {
				logger.debug("Generating a screenshot");
				Shell shell = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
				Graph g = (Graph) viewer.getControl();
				Rectangle bounds = g.getContents().getBounds();
				Point size = new Point(g.getContents().getSize().width, g.getContents().getSize().height);
				org.eclipse.draw2d.geometry.Point viewLocation = g.getViewport().getViewLocation();
				final Image image = new Image(null, size.x, size.y);
				GC gc = new GC(image);
				SWTGraphics swtGraphics = new SWTGraphics(gc);

				swtGraphics.translate(-1 * bounds.x + viewLocation.x, -1 * bounds.y + viewLocation.y);
				g.getViewport().paint(swtGraphics);
				gc.copyArea(image, 0, 0);
				gc.dispose();

				ImagePreviewPane previewPane = new ImagePreviewPane(shell);
				previewPane.setText("Image Preview");
				previewPane.open(image, size);
				logger.debug("Finished generating screenshot");
			}
		};

		screenshotAction.setText("Take A Screenshot");
		screenshotAction.setImageDescriptor(GlycanVizImages.DESC_SNAPSHOT);
		screenshotAction.setToolTipText("Take screenshot");
		screenshotAction.setEnabled(true);
		
		historyAction = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				logger.debug("BEGIN: History - Go Back");
				if (historyStack.size() > 0) {
					Object o = historyStack.pop();
					forwardStack.push(currentTree);
					forwardAction.setEnabled(true);
					filter((GlycanTree)o, false);
					if (historyStack.size() <= 0) {
						historyAction.setEnabled(false);
					}
				}
				logger.debug("END: History - Go Back");
			}
		};
		// @tag action : History action
		historyAction.setText("Back");
		historyAction.setToolTipText("Previous Tree");
		historyAction.setEnabled(false);
		historyAction.setImageDescriptor(GlycanVizImages.DESC_BACKWARD_ENABLED);

		forwardAction = new Action() {
			@SuppressWarnings("unchecked")
			public void run() {
				logger.debug("BEGIN: Forward");
				if (forwardStack.size() > 0) {
					Object o = forwardStack.pop();
					historyStack.push(currentTree);
					historyAction.setEnabled(true);
					filter((GlycanTree)o, false);
					if (forwardStack.size() <= 0) {
						forwardAction.setEnabled(false);
					}
				}
				logger.debug("END: Forward");
			}
		};

		forwardAction.setText("Forward");
		forwardAction.setToolTipText("Go forward");
		forwardAction.setEnabled(false);
		forwardAction.setImageDescriptor(GlycanVizImages.DESC_FORWARD_ENABLED);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	

	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}
	
	public void resetFilters () {
		setInput (this.currentTree, false);
	}

	public void setFilter(Filter filter, boolean highlightOnly) {
		if (currentTree != null) {
			
			if (highlightOnly) 
				this.highlight(currentTree, filter);
			else {
				GlycanTree newTree = new GlycanTree();
				
				GlycanNode[] newNodes = AnalysisUtil.getFilteredNodes(currentTree, filter);
				newTree.setNodes(Arrays.asList(newNodes));
				newTree.setStandardIntensity(currentTree.getStandardIntensity());
				newTree.setMergeResult(currentTree.isMergeResult());
				newTree.setMergeSettings(currentTree.getMergeSettings());
				
				if (filter.getLowIntensity() != null)
					newTree.setLowestIntensity(filter.getLowIntensity());
				if (filter.getHighIntensity() != null)
					newTree.setHighestIntensity(filter.getHighIntensity());
				String prev = currentTree.getFilterDescription();
				if (prev == null || prev.length() == 0)
					newTree.setFilterDescription(filter.toString());
				else 
					newTree.setFilterDescription(prev + " " + filter.toString());
				
				
				this.filter(newTree, true);
			}
		}
	}

	public GlycanTree getTree() {
		return this.tree;
	}
	
	public void setSelectedNode (Object selection, boolean select) {
		if (selection instanceof GlycanNode) {
			if (this.currentTree != null) {
				Iterator nodes1 = viewer.getGraphControl().getNodes().iterator();
				while (nodes1.hasNext()) {
					GraphNode node = (GraphNode) nodes1.next();
					GlycanNode glycanNode = (GlycanNode) node.getData();
					if (glycanNode.equals(selection)) {
						if (select)
							selectFigure (node.getNodeFigure());
						else 
							deselectFigure (node.getNodeFigure());
						break;
					}
				}
			}
		}
	}

	private void selectFigure(IFigure fig) {
		if (fig != null) {
			List children = fig.getChildren();
			if (children.size() > 0) {
				IFigure first = (IFigure)children.get(0);
				if (!(first instanceof RectangleFigure)) {
					IFigure rectangle = new RectangleFigure();
					rectangle.setForegroundColor(ColorConstants.red);
					if (fig.getBackgroundColor().equals(LIGHT_BLUE_CYAN))
						rectangle.setBackgroundColor(fig.getBackgroundColor());
					else rectangle.setBackgroundColor(ColorConstants.white);
					rectangle.setBounds(new Rectangle(fig.getBounds().x+1, fig.getBounds().y+1, fig.getBounds().width-1, fig.getBounds().height-1));
					rectangle.setOpaque(false);
					fig.add(rectangle, 0);
				}
			}		
		}
	}
	
	private void deselectFigure (IFigure fig) {
		if (fig != null) {
			List children = fig.getChildren();
			if (children.size() > 0) {
				IFigure first = (IFigure)children.get(0);
				if (first instanceof RectangleFigure)
					fig.remove(first);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void setDecorator(Decorator type) {
		logger.debug("Changing decorator");
		// re-generate figure with decorator and refresh the graph
		if (this.currentTree != null) {
			Iterator nodes1 = viewer.getGraphControl().getNodes().iterator();
			while (nodes1.hasNext()) {
				GraphNode node = (GraphNode) nodes1.next();
				GlycanNode glycanNode = (GlycanNode) node.getData();
				IFigure figure = node.getNodeFigure();
				if (currentTree.isMergeResult()) { // merge node
					changeFigureForMerge (figure, glycanNode, type, this.currentTree.getStandardIntensity());
				} else {
					changeFigure (figure, glycanNode, type, this.currentTree.getStandardIntensity());	
				}
			}
			logger.debug("Refreshing viewer");
			viewer.refresh();
		}
		logger.debug("Finished changing decorator");
	}
	
	private void changeFigureForMerge(IFigure figure, GlycanNode glycanNode,
			Decorator type, Double[] standardIntensity) {
		List<IFigure> children = new ArrayList<>();
		for (Object child : figure.getChildren()) {
			children.add((IFigure)child);	
		}
		int i=0;
		Dimension original=figure.getSize();
		boolean selected = false;
		for (IFigure iFigure : children) {
			if (iFigure instanceof RectangleFigure) {
				// selected, this is the first figure
				figure.remove(iFigure);
				selected = true;
			}
			if ((selected && i > 1)
				|| (!selected && i > 0))
				figure.remove(iFigure);
			else if ((selected && i == 1)
					|| (!selected && i == 0)){
				// first figure is the original, get the size
				original = iFigure.getSize();
			}
			i++;
		}
		
		figure.setSize(original);
		
		GlycanNode.addDecoratorForMerge(figure, glycanNode, type, standardIntensity);
		
		if (selected) // put the selection back
			selectFigure(figure);
		
	}

	private void changeFigure (IFigure figure, GlycanNode glycanNode, Decorator type, Double[] standardIntensity) {
		List<IFigure> children = new ArrayList<>();
		for (Object child : figure.getChildren()) {
			children.add((IFigure)child);	
		}
		int i=0;
		Dimension original=figure.getSize();
		boolean selected = false;
		for (IFigure iFigure : children) {
			if (iFigure instanceof RectangleFigure) {
				// selected, this is the first figure
				figure.remove(iFigure); // remove the selection
				selected = true;
			}
			if ((selected && i > 1)
				|| (!selected && i > 0))
				figure.remove(iFigure);
			else if ((selected && i == 1)
					|| (!selected && i == 0)){
				// first figure is the original, get the size
				original = iFigure.getSize();
			}
			i++;
		}
		
		figure.setSize(original);
		GlycanNode.addDecorator(figure, glycanNode, type, standardIntensity[0]);
		
		if (selected) 
			selectFigure(figure);
	}
}