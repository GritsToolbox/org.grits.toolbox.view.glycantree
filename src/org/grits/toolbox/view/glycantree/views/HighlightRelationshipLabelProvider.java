package org.grits.toolbox.view.glycantree.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.grits.toolbox.view.glycantree.analysis.AnalysisUtil;
import org.grits.toolbox.view.glycantree.model.GlycanNode;
import org.grits.toolbox.view.glycantree.model.GlycanTree;

public class HighlightRelationshipLabelProvider extends AbstractVisualizationLabelProvider {
	private static final Logger logger = Logger.getLogger(HighlightRelationshipLabelProvider.class);

	public HighlightRelationshipLabelProvider(GraphViewer viewer, AbstractVisualizationLabelProvider currentLabelProvider) {
		super(viewer, currentLabelProvider);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void calculateFilteredNodes(List filteredRels,
			List filteredEntities) {
		logger.debug("BEGIN: Calculate filteredNodes");
		final Object[] nodes;
		Object selected = getSelected();
		if (selected != null) {
			if (selected instanceof GlycanTree) {
				if (getFilter () != null) 
					nodes = AnalysisUtil.getFilteredNodes ((GlycanTree) selected, this.getFilter());
				else 
					nodes = ((GlycanTree) selected).getNodes().toArray();
				for (int i = 0; i < nodes.length; i++) {
					GlycanNode node = (GlycanNode)nodes[i];
					filteredEntities.add(node);
					for (GlycanNode connected : node.getConnectedTo()) {
						EntityConnectionData entityConnectionData = new EntityConnectionData(node, connected);
						if (!filteredRels.contains(entityConnectionData))
							filteredRels.add(entityConnectionData);
						if (!filteredEntities.add(connected))
							filteredEntities.add(connected);
					}
				}
			}
		}
		logger.debug("END: Calculate filteredNodes");
	}

}
