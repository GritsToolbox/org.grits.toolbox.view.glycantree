package org.grits.toolbox.view.glycantree.analysis;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.similiarity.SearchEngine.NodeComparatorWithSubstituents;
import org.eurocarbdb.MolecularFramework.util.similiarity.SearchEngine.SearchEngine;
import org.eurocarbdb.MolecularFramework.util.similiarity.SearchEngine.SearchEngineException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.grits.toolbox.merge.om.data.ExperimentAnnotation;
import org.grits.toolbox.merge.om.data.ExtGlycanFeature;
import org.grits.toolbox.merge.om.data.MergeReport;
import org.grits.toolbox.merge.om.data.MergeSettings;
import org.grits.toolbox.merge.om.data.ReportRow;
import org.grits.toolbox.merge.xml.Deserialize;
import org.grits.toolbox.ms.annotation.sugar.GlycanExtraInfo;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.CustomExtraData.Type;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScanFeatures;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;
import org.grits.toolbox.view.glycantree.model.Decorator;
import org.grits.toolbox.view.glycantree.model.Filter;
import org.grits.toolbox.view.glycantree.model.FilterData;
import org.grits.toolbox.view.glycantree.model.GlycanNode;
import org.grits.toolbox.view.glycantree.model.GlycanTree;

public class AnalysisUtil implements IRunnableWithProgress {
	
	private static final Logger logger = Logger.getLogger(AnalysisUtil.class);
	
	String fileName;
	GlycanTree glycanTree;
	Filter filter;
	Decorator decoratorType = Decorator.BAR;
	Double[] standardIntensity = null;
	
	boolean mergeReport = false;
	
	public String getFileName() {
		return fileName;
	}

	public GlycanTree getGlycanTree() {
		return glycanTree;
	}

	public AnalysisUtil (String filename, Filter f, Double[] standardIntensity) {
		this(filename, f, standardIntensity, false);
	}
	
	public AnalysisUtil (String filename, Filter f, Double[] standardIntensity, boolean mergeReport) {
		this.fileName = filename;
		this.filter = f;
		this.mergeReport = mergeReport;
		this.standardIntensity = standardIntensity;
	}
	
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException{
		if (mergeReport) {
			try {
				readMergeReportFile (monitor);
			} catch (SugarImporterException e) {
				logger.error("Cannot read/parse the annotations", e);
				throw new InvocationTargetException(e);
			}
		} else {
			try {
				readAnnotationFile (monitor);
			} catch (SugarImporterException | GlycoVisitorException e) {
				logger.error("Cannot read/parse the annotations", e);
				throw new InvocationTargetException(e);
			}
		}
	}
		
	private void readMergeReportFile(IProgressMonitor monitor)throws InvocationTargetException,
			InterruptedException, SugarImporterException {
		
		logger.debug("Reading merge file");
		monitor.beginTask("Generating Tree", IProgressMonitor.UNKNOWN);
		
		Deserialize des = new Deserialize();
		MergeReport data = des.deserialize(this.fileName);
		MergeSettings settings = data.getSettings();
		Map<Integer, Double> highestIntensity = new HashMap<>();
		List<ExperimentAnnotation> experiments = settings.getExperimentList();
		for (ExperimentAnnotation experimentAnnotation : experiments) {
			highestIntensity.put(experimentAnnotation.getAnnotationEntryId(), 0.0);
		}
		
		List<ReportRow> rows = data.getRows();
		int i=0;
		List<SearchObject> searchList  = new ArrayList<>();
		
		for (ReportRow reportRow : rows) {
			monitor.subTask("Reading row " + (i+1) + " of "+ rows.size() + "...");
			List<ExtGlycanFeature> annotations = reportRow.getAnnotations();
			Map<String, GlycanNode> nodeMap = new HashMap<>();
			for (ExtGlycanFeature extGlycanFeature : annotations) {
				GlycanNode node =  new GlycanNode(extGlycanFeature.getSequenceGWB(), extGlycanFeature.getStringAnnotationId(), extGlycanFeature.getReducingEnd());
				//initialize intensities
				initializeIntensity (node, experiments);
				if (nodeMap.containsValue(node)) {  // this means that the same glycan annotation exists for another sample (or same sample)
					GlycanNode existing = nodeMap.get(extGlycanFeature.getStringAnnotationId());
					existing.addIntensity(extGlycanFeature.getExpAnotationId(), extGlycanFeature.getIntensity());
					node = existing;
				}
				else {
					nodeMap.put (extGlycanFeature.getStringAnnotationId(), node);
					node.addIntensity(extGlycanFeature.getExpAnotationId(), extGlycanFeature.getIntensity());
				}
				
				Double highest = highestIntensity.get(extGlycanFeature.getExpAnotationId());
				if (highest != null) {
					highest = Math.max(highest, node.getIntensity(extGlycanFeature.getExpAnotationId()));
					highestIntensity.remove(extGlycanFeature.getExpAnotationId());
					highestIntensity.put(extGlycanFeature.getExpAnotationId(), highest);
				}
				
				// TODO use the following when the same is available for ExtGlycanFeature
				//Sugar sugar = GlycanExtraInfo.annotationToSugar(extGlycanFeature);
				
				String glycoCT = Glycan.fromString(extGlycanFeature.getSequenceGWB()).toGlycoCTCondensed();
				SugarImporterGlycoCTCondensed t_importer2 = new SugarImporterGlycoCTCondensed();
				Sugar sugar = t_importer2.parse(glycoCT);
				
	        	SearchObject searchObject = new SearchObject(sugar, node);
	        	readCustomExtraDataForMerge (extGlycanFeature, node);
	        	if (checkFilterOptions(node, this.filter)) {
	        		// do the filtering here and add only filtered ones into searchList
	        		if (!searchList.contains(searchObject))
	        			searchList.add(searchObject);
	        	}
			}
			i++;
			monitor.worked(1);
			
			// Check if the user pressed "cancel"
            if(monitor.isCanceled())
            {
                monitor.done();
                return;
            }
		}
		
		logger.debug ("Generating images for annotatins");
		// generate decorator images with the highestIntensity value
        i=0;
        for (SearchObject searchNode : searchList) {
        	monitor.subTask("Generating decorator images for annotation " + i+1 + " of " + searchList.size());
        	searchNode.getNode().setHighestIntensity(highestIntensity);
        	searchNode.getNode().generateFigureWithDecoratorForMerge(this.decoratorType, this.standardIntensity);
			monitor.worked(1);
			i++;
		}
        
        Collections.sort(searchList);
		Collections.reverse(searchList);
		
		logger.debug("Computing substructures/connections");
        boolean canceled = computeConnections(searchList, monitor);
        if (canceled)
        	return;
        
        List<GlycanNode> glycanList = new ArrayList<>();
        for (SearchObject searchObject: searchList) {
			glycanList.add(searchObject.getNode());
		}
        
        glycanTree = new GlycanTree();
        glycanTree.setNodes(glycanList);
        glycanTree.setStandardIntensity(this.standardIntensity);
        glycanTree.setMergeResult(true);
        glycanTree.setMergeSettings (settings);
        
        monitor.done();
        logger.debug("Finished processing merge file");
		
	}

	private void initializeIntensity(GlycanNode node,
			List<ExperimentAnnotation> experiments) {
		Map<Integer, Double> intensityMap = new LinkedHashMap <Integer, Double>();
		for (ExperimentAnnotation experimentAnnotation : experiments) {
			intensityMap.put (experimentAnnotation.getAnnotationEntryId(), null);
		}
		node.setIntensity(intensityMap);
	}

	public void readAnnotationFile (IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException, SugarImporterException, GlycoVisitorException {
		
		logger.debug("Reading annotation");
		monitor.beginTask("Generating Tree", IProgressMonitor.UNKNOWN);
		
		AnnotationReader rd = new AnnotationReader();
    	Data data = rd.readDataWithoutFeatures(fileName);
        ScanFeatures features = rd.readScanAnnotation(fileName, 1);
        
        List<SearchObject> searchList = new ArrayList<>();
        int i=0;
        double highestIntensity = 0.0;
        for (Annotation t_annotation : data.getAnnotation())
	    {
			monitor.subTask("Reading annotation " + (i+1) + " of "+ features.getFeatures().size() + "...");
	        GlycanAnnotation t_glycan = (GlycanAnnotation)t_annotation;
	            
	        Double intensity = findIntensity (data, features, t_glycan.getId());
	      
	        if (intensity != null) {  // means selected
	        	highestIntensity = Math.max(intensity, highestIntensity);
		        GlycanNode node =  new GlycanNode(t_glycan.getSequenceGWB(), t_glycan.getGlycanId(), intensity, t_glycan.getReducingEnd());
		        Sugar sugar = GlycanExtraInfo.annotationToSugar(t_glycan);
	        	SearchObject searchObject = new SearchObject(sugar, node);
	        	readCustomExtraData (t_glycan, node);
	        	if (checkFilterOptions(node, this.filter)) {
	        		// do the filtering here and add only filtered ones into searchList
	        		if (!searchList.contains(searchObject))
	        			searchList.add(searchObject);
	        	}
	        }
	        
			i++;
			monitor.worked(1);

            // Check if the user pressed "cancel"
            if(monitor.isCanceled())
            {
                monitor.done();
                return;
            }
		}
        
        Map<Integer, Double> highestIntensityMap = new HashMap<>();
		highestIntensityMap.put(new Integer(0),  highestIntensity);
		  
		logger.debug("Generating images for annotation");
        // generate decorator images with the highestIntensity value
        i=0;
        for (SearchObject searchNode : searchList) {
        	monitor.subTask("Generating decorator images for annotation " + i+1 + " of " + searchList.size());
        	searchNode.getNode().setHighestIntensity(highestIntensityMap);
        	searchNode.getNode().generateFigureWithDecorator(this.decoratorType, this.standardIntensity[0]);
			monitor.worked(1);
			i++;
		}
        
        Collections.sort(searchList);
		Collections.reverse(searchList);
        
		logger.debug("Computing substructures/connections for annotation");
        boolean canceled = computeConnections(searchList, monitor);
        if (canceled)
        	return;
        
        List<GlycanNode> glycanList = new ArrayList<>();
        for (SearchObject searchObject: searchList) {
			glycanList.add(searchObject.getNode());
		}
        
        glycanTree = new GlycanTree();
        glycanTree.setNodes(glycanList);
        glycanTree.setStandardIntensity(this.standardIntensity);
        glycanTree.setMergeResult(false);
        
        monitor.done();
        logger.debug("Finished reading annotation");
	}
	

	/**
	 * 
	 * @param data
	 * @param features
	 * @param integer
	 * @return the total intensity for this annotation from all selected features and all their peaks
	 */
	private Double findIntensity(Data data, ScanFeatures features, Integer integer) {
		Double intensity = null;
		if (data != null && features != null && data.getScans() != null) {
			Scan ms1Scan = data.getScans().get(1);  
			// sum up the intensities for the selected features for this annotation
			for (Feature feature:  features.getFeatures()) {
    			if (feature.getAnnotationId().equals(integer)) {
    				if (feature.getSelected()) {
    					intensity = 0.0;
    					List<Integer> peakIds = feature.getPeaks();
						if (ms1Scan != null) {
							List<Peak> peaks = ms1Scan.getPeaklist();
							for (Peak peak : peaks) {
								for (Integer peakId : peakIds) {
									if (peakId.equals(peak.getId())) {
										// add the intensity from this peak
										intensity += peak.getIntensity();
									}
								}
							}
						}
    				}
    			}
			}
		}
		
		return intensity;
	}
	
	private void readCustomExtraDataForMerge(ExtGlycanFeature extGlycanFeature,
			GlycanNode node) {
		List<CustomExtraData> columns = GlycanExtraInfo.getColumns();
		for (CustomExtraData customExtraData : columns) {
			String key = customExtraData.getKey();
			Type type = customExtraData.getType();
			Object value = null;
			switch (type) {
			case String:
				value = extGlycanFeature.getAnnotationStringProp().get(key);
				break;
			case Integer:
				value = extGlycanFeature.getAnnotationIntegerProp().get(key);
				break;
			case Boolean:
				value = extGlycanFeature.getAnnotationBooleanProp().get(key);
				break;
			case Double:
				value = extGlycanFeature.getAnnotationDoubleProp().get(key);
				break;		
			}
			
			if (value != null) {
				node.addFeature(customExtraData, value);
			}
		}
	}

	private void readCustomExtraData (GlycanAnnotation t_glycan, GlycanNode node) {
		List<CustomExtraData> columns = GlycanExtraInfo.getColumns();
		for (CustomExtraData customExtraData : columns) {
			String key = customExtraData.getKey();
			Type type = customExtraData.getType();
			Object value = null;
			switch (type) {
			case String:
				value = t_glycan.getStringProp().get(key);
				break;
			case Integer:
				value = t_glycan.getIntegerProp().get(key);
				break;
			case Boolean:
				value = t_glycan.getBooleanProp().get(key);
				break;
			case Double:
				value = t_glycan.getDoubleProp().get(key);
				break;		
			}
			
			if (value != null) {
				node.addFeature(customExtraData, value);
			}
		}
	}
	

	/**
	 * internal object to compare and sort the glycans
	 * 
	 * @author sena
	 *
	 */
	private class SearchObject implements Comparable<SearchObject>, Comparator<SearchObject>{
		Sugar sugar;
		GlycanNode node;
		
		public SearchObject (Sugar sugar, GlycanNode n) {
			this.sugar = sugar;
			this.node= n;
		}
		
		@Override
		public int compare(SearchObject o1, SearchObject o2) {
			if (o1 == null || o2 == null)
				return 0;
			return  o1.sugar.getNodes().size() - o2.sugar.getNodes().size();
		}
		@Override
		public int compareTo(SearchObject o) {
			return  this.sugar.getNodes().size() - o.sugar.getNodes().size();
		}
		
		public Sugar getSugar() {
			return sugar;
		}
		public GlycanNode getNode() {
			return node;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SearchObject) 
				return this.node.equals(((SearchObject) obj).getNode());
			return false;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public boolean computeConnections (List<SearchObject> sortedGlycanList, IProgressMonitor monitor) {	
		long startTime = System.currentTimeMillis();
		int i=0;
		for (SearchObject searchNode : sortedGlycanList) {
			monitor.subTask("Searching substructures for " + (i+1) + " of " + sortedGlycanList.size() + "...");
			GlycanNode glycan = searchNode.getNode();
			try {
				Collection<GlycanNode> c = subStructureSearch(searchNode, sortedGlycanList, i++);
				for (Iterator iterator = c.iterator(); iterator.hasNext();) {
					GlycanNode otherNode = (GlycanNode) iterator.next();
					if (!otherNode.equals(glycan)) {
						glycan.getConnectedTo().add(otherNode);
					}
				}
				monitor.worked(1);
				
				if (monitor.isCanceled()) {
					monitor.done();
					return true;
				}
			} catch (SugarImporterException | GlycoVisitorException
					| GlycoconjugateException | SearchEngineException e) {
				logger.error ("Error searching for substructures", e);
			}
		}
		logger.debug ("Substructure search took " + (System.currentTimeMillis() - startTime)/1000 + " seconds");
		
		return false;
	}
	
	public static boolean checkFilterOptions(GlycanNode node, Filter filter) {
		List<FilterData> options = filter.getOptions();
		if (options == null || options.size() == 0) 
			return checkIntensityRange(node, filter);
		for (FilterData option : options) {
			Object value = node.getFeature(option.getColumn());
			switch (option.getColumn().getType()) {
			case String:
				if (option.getValue() != null && ((String)option.getValue()).equals((String)value)) {
					return checkIntensityRange(node, filter);
				}
				break;
			case Integer:
				if (option.getColumn().getKey().equals(GlycanExtraInfo.N_BRANCHES)) { // special case
					// values need to match exactly
					if (option.getValue() != null && value != null && ((Integer)value).intValue() == ((Integer)option.getValue()).intValue()) {
						return checkIntensityRange(node, filter);
					}
					else if (option.getValue() == null && value != null && ((Integer)value).intValue() == 2) {  // 2 is the default selection
						return checkIntensityRange(node, filter);
					}
				} else {
					if (option.getValue() != null && value != null && ((Integer)value).intValue() == ((Integer)option.getValue()).intValue()) {
						return checkIntensityRange(node, filter);
					}
					else if (option.getValue() == null && value != null && ((Integer)value).intValue() == 1) {  // 1 is the default selection
						return checkIntensityRange(node, filter);
					}
				}
				break;
			case Double:
				if (option.getValue() != null && value != null && ((Double)value).doubleValue() == ((Double)option.getValue()).doubleValue()) {
					return checkIntensityRange(node, filter);
				}
				else if (option.getValue() == null && value != null && ((Double)value).doubleValue() == 1) {  // 1 is the default selection
					return checkIntensityRange(node, filter);
				}
				break;
			case Boolean:
				if (option.getValue() != null && ((Boolean)option.getValue()).booleanValue()) {
					if (value != null && ((Boolean)value).booleanValue())
						return checkIntensityRange(node, filter);
				} else {
					if (value != null && !((Boolean)value).booleanValue())
						return checkIntensityRange(node, filter);
				}
				break;
			default:
				break;
			}
		}
		
		// if the glycan node has none of filter options set to true, then return false
		return false;
	}
	
	public static boolean checkIntensityRange (GlycanNode node, Filter filter) {
		if (node != null && filter != null) {
			if (filter.getHighIntensity() != null && filter.getLowIntensity() != null) {
				for (Double double1 : node.getIntensity().values()) {   // if any of the intensities is in the given range, return true
					if (double1 != null && double1 >= filter.getLowIntensity() && double1 <= filter.getHighIntensity()) {
						return true;
					}
				}
				
			}
			else { // if any of them is null
				return true;
			}
		}
		return false;
	}

	private List<GlycanNode> subStructureSearch(SearchObject glycan, List<SearchObject> sortedGlycanList,
		 int index)
			throws SugarImporterException, GlycoVisitorException, GlycoconjugateException, SearchEngineException {
		
        List<GlycanNode> matches = new ArrayList<GlycanNode>();
	
        SearchEngine search = new SearchEngine ();
        // parse the sequence
        Sugar sugar = glycan.getSugar();
        search.setQueryStructure(sugar);
        search.setNodeComparator(new NodeComparatorWithSubstituents());
        
        List<GlycanNode> nodesToSkip = new ArrayList<>();
        int j=sortedGlycanList.size()-1;    // need to check in reverse order to be able to eliminate checking already connected nodes
        Object[] glycanNodes = sortedGlycanList.toArray();
        while (j >= 0) {
        	SearchObject glycanNode = (SearchObject)glycanNodes[j];
        	if (index <= j) {   // do not check the upper part of the matrix
        		// skip
        		j--;
        		continue;
        	}
        	if (nodesToSkip.contains(glycanNode.getNode())) {
        		// skip
        		j--;
        		continue;
        	}
        	Sugar existingStructure = glycanNode.getSugar();
			search.setQueriedStructure(existingStructure);
            search.match();
            if (search.isExactMatch())
            {
            	matches.add(glycanNode.getNode());
            	// when we find a match, we don't need to check the nodes connected to this match anymore
            	nodesToSkip.addAll (getPath(glycanNode.getNode()));
            	glycan.getNode().setTransitiveConnections(nodesToSkip);
            }
            j--;
		}
		return matches;
	}
	
	private List<GlycanNode> getPath (GlycanNode glycan) {
		List<GlycanNode> connectedNodes = new ArrayList<>();
		if (glycan.getConnectedTo() == null)
			return connectedNodes;
		for (GlycanNode glycanNode : glycan.getConnectedTo()) {
			connectedNodes.add(glycanNode);
			connectedNodes.addAll(getPath(glycanNode));
		}
		
		return connectedNodes;
	}

	public static GlycanNode[] getFilteredNodes(GlycanTree tree, Filter filter) {
		logger.debug("Filtering...");
		if (tree != null) {
			List<GlycanNode> newNodes = new ArrayList<>();
			List<GlycanNode> nodes = tree.getNodes();
			for (GlycanNode glycanNode : nodes) {
				if (AnalysisUtil.checkFilterOptions(glycanNode, filter)) {
					// keep the node
					newNodes.add(glycanNode);
				}
			}
			
			List<GlycanNode> copiedNodes = new ArrayList<>();
			// go over the newNodes and compute the connections
			for (GlycanNode glycanNode : newNodes) {
				GlycanNode copy = new GlycanNode(glycanNode.getStructure(), glycanNode.getId(), glycanNode.getReducingEnd());
				copy.setIntensity (glycanNode.getIntensity());
				copy.setHighestIntensity(glycanNode.getHighestIntensity());
				copy.setFigureWithDecorator (glycanNode.getFigureWithDecorator());
				copy.setFeatures(glycanNode.getFeatures());
				if (!copiedNodes.contains(copy))
					copiedNodes.add(copy);
				List<GlycanNode> connectedNodes = glycanNode.getConnectedTo();
				for (GlycanNode connectedNode : connectedNodes) {
					if (newNodes.contains(connectedNode)) {
						GlycanNode connectedCopy=null;
						// keep the connection
						if (!copiedNodes.contains(connectedNode)) {  // create a copy for the first time and add it to copiedNodes
							connectedCopy = new GlycanNode(connectedNode.getStructure(), connectedNode.getId(), connectedNode.getReducingEnd());
							connectedCopy.setIntensity(connectedNode.getIntensity());
							connectedCopy.setHighestIntensity(connectedNode.getHighestIntensity());
							connectedCopy.setFigureWithDecorator(connectedNode.getFigureWithDecorator());
							copiedNodes.add(connectedCopy);
							connectedCopy.setFeatures(connectedNode.getFeatures());
						}
						else { // find the object from copiedNodes
							for (GlycanNode glycanNode2 : copiedNodes) {
								if (glycanNode2.equals(connectedNode)) {
									connectedCopy = glycanNode2;
									break;
								}
							}
						}
						copy.addConnection (connectedCopy); 
					}
				}
				// go over the transitive connections to see if anyone needs to be moved to connections
				Set<GlycanNode> otherConnections = glycanNode.getTransitiveConnections();
				for (GlycanNode otherNode : otherConnections) {
					if (copiedNodes.contains(otherNode)) {
						GlycanNode otherCopy = null;
						for (GlycanNode other : copiedNodes) {
							if (other.equals(otherNode)) {
								otherCopy = other;
								break;
							}
						}
						copy.addConnection(otherCopy);
					}
					
				}
			}
			
			cleanupConnections (copiedNodes);
			
			logger.debug("Finished filtering");
			return copiedNodes.toArray(new GlycanNode[copiedNodes.size()]);
		}
		
		return null;
	}
		
	private static void cleanupConnections(List<GlycanNode> glycans) {
		for (GlycanNode glycan : glycans) {
			List<GlycanNode> toBeRemoved = new ArrayList<GlycanNode>();
			List<GlycanNode> nodes = glycan.getConnectedTo();
			for (GlycanNode glycanNode : nodes) {
				for (GlycanNode glycanNode2 : nodes) {
					if (isThereAPath(glycanNode, glycanNode2)) {
						toBeRemoved.add(glycanNode2);
					}
				}
			}
			glycan.setTransitiveConnections(toBeRemoved);
			for (GlycanNode glycanNode : toBeRemoved) {
				glycan.getConnectedTo().remove(glycanNode);
			}
		}
	}
	
	private static boolean isThereAPath (GlycanNode source, GlycanNode target) {
		if (source.equals(target))
			return false;
		
		List<GlycanNode> connected = source.getConnectedTo();
		if (connected.contains(target))
			return true;
		else {
			for (GlycanNode glycanNode : connected) {
				if (isThereAPath(glycanNode, target)) 
					return true;
			}
		}
		
		return false;
	}
}
