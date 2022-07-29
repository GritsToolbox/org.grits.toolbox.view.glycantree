package org.grits.toolbox.view.glycantree.util;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gmf.runtime.lite.svg.SVGFigure;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanRendererAWT;
import org.eurocarbdb.application.glycanbuilder.Union;
import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferences;
import org.grits.toolbox.datamodel.ms.annotation.glycan.preference.cartoon.MSGlycanAnnotationCartoonPreferencesLoader;
import org.grits.toolbox.ms.annotation.gelato.glycan.GlycanStructureAnnotation;
import org.grits.toolbox.view.glycantree.model.GlycanNode;
import org.grits.toolbox.view.glycantree.model.TooltipTableFigure;

public class ImageUtil {
	
	static int i=0;
	
	private static final Logger logger = Logger.getLogger(ImageUtil.class);
	
	static GlycanWorkspace glycanWorkspace = new GlycanWorkspace(new GlycanRendererAWT());
	
	public static IFigure getGlycanImage (GlycanNode node) {
		try {
	        Glycan t_glycan = Glycan.fromString(node.getStructure());
	        
	        if (node.getReducingEnd() != null) // below method does not handle null value
	        	t_glycan.setReducingEndType(GlycanStructureAnnotation.getResidueTypeForReducingEnd(node.getReducingEnd()) );
		
	        MSGlycanAnnotationCartoonPreferences options = MSGlycanAnnotationCartoonPreferencesLoader.getCartoonPreferences();
			glycanWorkspace.setNotation(options.getImageLayout());
			glycanWorkspace.setDisplay(options.getImageStyle());
						
		    // create the SVG
	        SVGFigureOutput output = SVGUtils.getVectorGraphics((GlycanRendererAWT)glycanWorkspace.getGlycanRenderer(),new Union<Glycan>(t_glycan), false, true);
	        String t_svg= output.getSvg();
	        
	        String tmpFolder = PropertyHandler.getVariable("workspace_location") + File.separator + ".temp";
	        File folder = new File (tmpFolder);
	        if (!folder.exists())
	        	folder.mkdirs();
	       
	        File file = new File (folder.getAbsolutePath() + File.separator + "glycan-"+ node.getId().hashCode() + "-" + i++ + ".svg");
	        URI fileUri = file.toURI();
	        
	        FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
	        fileWriter.write(t_svg);
	        fileWriter.close();
	        
	        SVGFigure figure = new SVGFigure();
	        figure.setURI(fileUri.toString());
	        figure.setSize(output.getDimension().width, output.getDimension().height);
	        
	        IFigure toolTip = new TooltipTableFigure(new Label(node.getId()));
	        ((TooltipTableFigure) toolTip).getMetadataCompartment().add(new Label("Mass: "));
	        ((TooltipTableFigure) toolTip).getMetadataCompartment().add(new Label("Calculated Mass: "));
	        
	        figure.setToolTip(toolTip);
	        
	        file.delete();
	        return figure;
		} catch (Exception e) {
			logger.error("Failed to generate the image for " + node.getId(), e);
		}
		
		return null;
	}
}
