package org.grits.toolbox.view.glycantree.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.Graph;
import org.grits.toolbox.merge.om.data.ExperimentAnnotation;
import org.grits.toolbox.merge.om.data.MergeSettings;
import org.grits.toolbox.view.glycantree.GlycanVizImages;
import org.grits.toolbox.view.glycantree.model.Decorator;
import org.grits.toolbox.view.glycantree.model.Filter;
import org.grits.toolbox.view.glycantree.model.FilterData;
import org.grits.toolbox.view.glycantree.model.GlycanTree;
import org.mihalis.opal.rangeSlider.RangeSlider;

class VisualizationForm {
	
	private static final Logger logger = Logger.getLogger(VisualizationForm.class);
	/*
	 * These are all the strings used in the form. These can probably be
	 * abstracted for internationalization
	 */
	private static String ChangeDecorator = "Change Decorator";
	private static String GlycanTree = "Glycan Tree";
	private static String Controls = "Options";
	private static String Filters = "Filters";
	private static String Decorators = "Decorator";
	private static String Legend = "Legend";

	/*
	 * Some parts of the form we may need access to
	 */
	private ScrolledForm form;
	private FormToolkit toolkit;
	private ManagedForm managedForm;
	private GraphViewer viewer;
	private GlycanTreeView view;

	/*
	 * Some buttons that we need to access in local methods
	 */
	private Button filterNow = null;
	private Button resetFilter = null;
	
	
	boolean highlight;
	private Button highlightButton;
	
	private SashForm sash;
	private Section filters;
	
	private Label intensityLabel;
	private Label intensityValue;
	
	RangeSlider slider;
	Label sliderLabel;
	
	Label maxValue;
	Label minValue;
	
	TableViewer tableViewer=null;
	private Button changeDecorator;
	private ComboViewer decoratorCombo;
	private Text hTextLower;
	private Text hTextUpper;
	
	Composite sampleGroup;
	
	/**
	 * Creates the form.
	 * 
	 * @param toolKit
	 * @return
	 */
	VisualizationForm(Composite parent, FormToolkit toolkit, GlycanTreeView view) {
		this.toolkit = toolkit;
		this.view = view;
		form = this.toolkit.createScrolledForm(parent);
		managedForm = new ManagedForm(this.toolkit, this.form);
		createHeaderRegion(form);
		FillLayout layout = new FillLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 4;
		form.getBody().setLayout(layout);

		this.toolkit.decorateFormHeading(this.form.getForm());
		createSash(form.getBody());
	}

	public void setFilteredName(String description) {
		if (description != null) {
			form.setText(GlycanTree + ": " + description);
		}
		else
			form.setText(GlycanTree);
		form.reflow(true);
	}

	/**
	 * Creates the header region of the form, with the search dialog, background
	 * and title.  It also sets up the error reporting
	 * @param form
	 */
	private void createHeaderRegion(ScrolledForm form) {
		Composite headClient = new Composite(form.getForm().getHead(), SWT.NULL);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = glayout.marginHeight = 0;
		glayout.numColumns = 3;
		headClient.setLayout(glayout);
		headClient.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		new Label (headClient, SWT.NONE);
		
		intensityLabel = new Label(headClient, SWT.NONE);
		intensityLabel.setText("Total Intensity:");
		
		intensityValue = new Label(headClient, SWT.NONE);
		toolkit.paintBordersFor(headClient);
		form.setHeadClient(headClient);
		form.setText(GlycanTree);
		form.setImage(GlycanVizImages.get(GlycanVizImages.IMG_REQ_PLUGIN_OBJ));
	}


	/**
	 * Creates the sashform to separate the graph from the controls.
	 * 
	 * @param parent
	 */
	private void createSash(Composite parent) {
		sash = new SashForm(parent, SWT.NONE);
		this.toolkit.paintBordersFor(parent);

		createGraphSection(sash);
		createControlsSection(sash);
		sash.setWeights(new int[] { 10, 3 });
	}

	private class MyGraphViewer extends GraphViewer {
		public MyGraphViewer(Composite parent, int style) {
			super(parent, style);
			Graph graph = new Graph(parent, style) {
				public Point computeSize(int hint, int hint2, boolean changed) {
					return new Point(0, 0);
				}
			};
			setControl(graph);
		}
	}

	/**
	 * Creates the section of the form where the graph is drawn
	 * 
	 * @param parent
	 */
	private void createGraphSection(Composite parent) {
		Section section = this.toolkit.createSection(parent, Section.TITLE_BAR);
		viewer = new MyGraphViewer(section, SWT.NONE);
		section.setClient(viewer.getControl());
	}

	/**
	 * Creates the section holding the analysis controls.
	 * 
	 * @param parent
	 */
	private void createControlsSection(Composite parent) {
		Section controls = this.toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		controls.setText(Controls);
		Composite controlComposite = new Composite(controls, SWT.NONE) {
			public Point computeSize(int hint, int hint2, boolean changed) {
				return new Point(0, 0);
			}
		};
		this.toolkit.adapt(controlComposite);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		controlComposite.setLayout(layout);
		
		Section decoratorOptions = this.toolkit.createSection(controlComposite, Section.EXPANDED);
		decoratorOptions.setText(Decorators);
		decoratorOptions.setLayout(new FillLayout());
		Composite decoratorOptionsComposite = this.toolkit.createComposite(decoratorOptions);
		decoratorOptionsComposite.setLayout(new TableWrapLayout());
		
		Composite decoratorGroup = new Composite(controlComposite, SWT.NONE);
		GridData decoratorGridData = new GridData();
		decoratorGridData.horizontalSpan = 1;
		decoratorGroup.setLayoutData(decoratorGridData);
		decoratorGroup.setLayout(new GridLayout(2,false));
		
		decoratorCombo = new ComboViewer(decoratorGroup, SWT.DROP_DOWN);
		decoratorCombo.setContentProvider(ArrayContentProvider.getInstance());
		decoratorCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Decorator) {
					return element.toString();
				}
				return super.getText(element);
			}
		});
		decoratorCombo.setInput (Decorator.values());
		decoratorCombo.setSelection(new StructuredSelection(decoratorCombo.getElementAt(0)), true);
		
		changeDecorator = this.toolkit.createButton(decoratorGroup, ChangeDecorator, SWT.PUSH);
		changeDecorator.setEnabled(false);
		changeDecorator.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				view.setDecorator((Decorator)((IStructuredSelection)decoratorCombo.getSelection()).getFirstElement());
			}
		});
		
		createAnalyteLegend(controlComposite);

		Section filterOptions = this.toolkit.createSection(controlComposite, Section.EXPANDED | Section.TITLE_BAR);
		filterOptions.setText(Filters);
		filterOptions.setLayout(new FillLayout());
		Composite filterOptionsComposite = this.toolkit.createComposite(filterOptions);
		filterOptionsComposite.setLayout(new TableWrapLayout());
		
		Composite buttonGroup = new Composite(controlComposite, SWT.NONE);
		GridData buttonGridData = new GridData();
		buttonGroup.setLayoutData(buttonGridData);
		buttonGridData.horizontalSpan = 1;
        buttonGroup.setLayout(new GridLayout(3,false));
		
		highlightButton = this.toolkit.createButton(buttonGroup, "Hightlight Only", SWT.CHECK);
		highlightButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				highlight = ((Button)e.getSource()).getSelection();
			};
		});
		
		filterNow = new Button (buttonGroup, SWT.PUSH);
		filterNow.setText("Filter");
		filterNow.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Filter selected");
				// update view
				if (tableViewer != null && tableViewer.getInput() != null) {
					Filter filter = new Filter();
					List<FilterData> options = (List<FilterData>) tableViewer.getInput();
					filter.setOptions(options);
					if (slider != null) {
						filter.setLowIntensity(slider.getLowerValue());
						filter.setHighIntensity(slider.getUpperValue());
					}
					view.setFilter (filter, highlight);
				}
				logger.debug("Filter finished");
			}
		});
		
		resetFilter = new Button (buttonGroup, SWT.PUSH);
		resetFilter.setText ("Reset filters");
		resetFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Resetting filters");
				// clear all selections
				tableViewer.setInput(new ArrayList<FilterData>());
        		view.resetFilters();
        		logger.debug("Filters reset");
			}
		});
		
		createIntensitySlider (controlComposite);
		
		createOtherFiltersSection (controlComposite);
		filterOptions.setClient(filterOptionsComposite);	
		decoratorOptions.setClient(decoratorOptionsComposite);

		controls.setClient(controlComposite);
	}
	
	private void createAnalyteLegend (Composite controlComposite) {
		Section sampleLegend = this.toolkit.createSection(controlComposite, Section.EXPANDED);
		sampleLegend.setText(Legend);
		sampleLegend.setLayout(new FillLayout());
		Composite sampleLegendComposite = this.toolkit.createComposite(sampleLegend);
		sampleLegendComposite.setLayout(new TableWrapLayout());
		
		sampleGroup = new Composite(controlComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		sampleGroup.setLayoutData(gridData);
		sampleGroup.setLayout(new GridLayout(3,false));
	}
	
	private void createIntensitySlider(Composite controlComposite) {
		Composite sliderGroup = new Composite(controlComposite, SWT.NONE);
		GridData sliderGridData = new GridData();
		sliderGroup.setLayoutData(sliderGridData);
		sliderGridData.horizontalSpan = 1;
		sliderGroup.setLayout(new GridLayout(4,false));
		
		sliderLabel = new Label(sliderGroup, SWT.NONE);
		sliderLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 4, 1));
		sliderLabel.setText("Intensity Range");
		
		minValue = new Label(sliderGroup, SWT.NONE);
		minValue.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 2));
		minValue.setText("0");
		
		slider = new RangeSlider(sliderGroup, SWT.HORIZONTAL);
		final GridData gd = new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 2);
        gd.widthHint = 250;
        slider.setLayoutData(gd);
        slider.setMinimum(0);
        slider.setMaximum(100);
        slider.setLowerValue(0);
        slider.setUpperValue(60);
        
        maxValue = new Label(sliderGroup, SWT.NONE);
        maxValue.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 2));
        maxValue.setText("100");
        
        final Label hLabelLower = new Label(sliderGroup, SWT.NONE);
        hLabelLower.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        hLabelLower.setText("Lower Value:");

        hTextLower = new Text(sliderGroup, SWT.BORDER);
        final GridData gd1 = new GridData(GridData.FILL, GridData.BEGINNING, false, false, 1, 1);
        gd1.widthHint = 100;
        hTextLower.setLayoutData(gd1);
        hTextLower.setText(slider.getLowerValue() + "   ");
        hTextLower.setEnabled(false);
        
        new Label(sliderGroup, SWT.NONE);

        final Label hLabelUpper = new Label(sliderGroup, SWT.NONE);
        hLabelUpper.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        hLabelUpper.setText("Upper Value:");

        hTextUpper = new Text(sliderGroup, SWT.BORDER);
        final GridData gd2 = new GridData(GridData.FILL, GridData.BEGINNING, false, false, 1, 1);
        gd2.widthHint = 100;
        hTextUpper.setLayoutData(gd2);
        hTextUpper.setText(slider.getUpperValue() + "   ");
        hTextUpper.setEnabled(false);
        new Label(sliderGroup, SWT.NONE);

        slider.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                        hTextLower.setText(slider.getLowerValue() + "   ");
                        hTextUpper.setText(slider.getUpperValue() + "   ");
                }
        });
	}

	private void createOtherFiltersSection(Composite parent) {
		
		filters = this.toolkit.createSection(parent,Section.TITLE_BAR);
		filters.setText("Add/Remove Filters");
		filters.setLayout(new FillLayout());
		Composite filterComposite = this.toolkit.createComposite(filters);
		this.toolkit.adapt(filterComposite);
		
		tableViewer = new FilterTableSetup().createFilterTableSection(filterComposite);
		filters.setClient(filterComposite);
	}
	
	
	public void setIntensityValue (double intensity) {
		if (intensityValue != null)
			intensityValue.setText(intensity + "");
	}
	
	public void setIntensityRange (double min, double max, double low, double high) {
		slider.setMinimum((int)Math.floor(min));
		slider.setMaximum((int)Math.ceil(max));
		slider.setLowerValue((int)Math.floor(low));
		slider.setUpperValue((int)Math.ceil(high));
		minValue.setText(min  + "");
		maxValue.setText(max + "");
		hTextLower.setText((int)Math.floor(low)+ "");
		hTextUpper.setText((int)Math.ceil(high) + "");
	}
	
	/**
	 * Gets the currentGraphViewern
	 * 
	 * @return
	 */
	public GraphViewer getGraphViewer() {
		return viewer;
	}

	/**
	 * Gets the form we created.
	 */
	public ScrolledForm getForm() {
		return form;
	}

	public ManagedForm getManagedForm() {
		return managedForm;
	}

	public void showLegendGroup(GlycanTree glycanTree) {
		MergeSettings settings = glycanTree.getMergeSettings();
		if (settings != null) {
			Control[] children = sampleGroup.getChildren();
			if (children.length == settings.getExperimentList().size() * 3) {
				// no need to add them again
			}
			else {
				List<ExperimentAnnotation> experiments = settings.getExperimentList();
				int i=1;
				for (ExperimentAnnotation experimentAnnotation : experiments) {
					Label legend = new Label (sampleGroup, SWT.NONE);
					legend.setText(i++ + "");
					
					Label arrow = new Label(sampleGroup, SWT.NONE);
					arrow.setImage(GlycanVizImages.get(GlycanVizImages.IMG_ARROW));
					
					Label shortName = new Label (sampleGroup, SWT.NONE);
					shortName.setText(experimentAnnotation.getAnnotationShortName());
				}
			}
		}
	}

	public void enableDecoratorChange(boolean b) {
		if (this.changeDecorator != null)
			this.changeDecorator.setEnabled(b);
	}

}
