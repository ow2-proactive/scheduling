package org.objectweb.proactive.ic2d.timit.views;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.timit.actions.ShowInTreeViewAction;
import org.objectweb.proactive.ic2d.timit.data.ChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.editparts.ChartEditPart;
import org.objectweb.proactive.ic2d.timit.editparts.TimItEditPartFactory;


/**
 * The main TimItView class.
 * <p>
 * The view uses a buttons and actions.
 * <p>
 */
public class TimItView extends ViewPart {
    public static final String ID = "org.objectweb.proactive.ic2d.timit.views.TimItView";
    protected Button refreshSelectedButton;
    protected Button refreshAllButton;
    protected Button timerLevelButton;
    protected ShowInTreeViewAction showInTreeViewAction;
    protected ScrollingGraphicalViewer timItViewer;
    protected ChartContainerObject chartContainer;

    /**
     * The constructor.
     */
    public TimItView() {
        this.chartContainer = new ChartContainerObject();
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     *
     * @param Composite
     *            parent object
     */
    public void createPartControl(Composite parent) {
        FormLayout form = new FormLayout();
        parent.setLayout(form);

        final int limit = 62;

        // ////////////////////////
        createGraphicalViewer(parent);

        FormData graphicalViewerData = new FormData();
        graphicalViewerData.left = new FormAttachment(0, 0);
        graphicalViewerData.right = new FormAttachment(100, 0);
        graphicalViewerData.top = new FormAttachment(0, 0);
        graphicalViewerData.bottom = new FormAttachment(100, -limit);
        this.timItViewer.getControl().setLayoutData(graphicalViewerData);

        // ////////////////////////
        FormData drawingStyleData = new FormData();
        drawingStyleData.left = new FormAttachment(0, 0);
        drawingStyleData.right = new FormAttachment(100, 0);
        drawingStyleData.top = new FormAttachment(100, -limit);
        drawingStyleData.bottom = new FormAttachment(100, 0);
        Group groupD = new Group(parent, SWT.NONE);

        RowLayout rowLayout = new RowLayout();
        rowLayout.justify = true;
        groupD.setLayout(rowLayout);
        groupD.setLayoutData(drawingStyleData);

        // ////////////////////////
        Group refreshGroup = new Group(groupD, SWT.NONE);
        refreshGroup.setText("Refresh Charts");
        RowLayout refreshLayout = new RowLayout();
        refreshGroup.setLayout(refreshLayout);

        // --------------------
        this.refreshSelectedButton = new Button(refreshGroup, SWT.PUSH);

        this.refreshAllButton = new Button(refreshGroup, SWT.PUSH);

        this.timerLevelButton = new Button(refreshGroup, SWT.PUSH);

        // Configure buttons
        this.configureButtons();

        // ---------------------		
        IToolBarManager toolBarManager = getViewSite().getActionBars()
                                             .getToolBarManager();

        this.showInTreeViewAction = new ShowInTreeViewAction();
        toolBarManager.add(showInTreeViewAction);

        //        try{
        //        	
        //        final Display display = parent.getDisplay();
        //    	Shell shell = new Shell(display);
        //    	shell.setLayout(new FillLayout());
        //    	shell.setText("Show results as a bar chart in Tree");
        //    	final Tree tree = new Tree(shell, SWT.BORDER);
        //    	tree.setHeaderVisible(true);
        //    	tree.setLinesVisible(true);
        //    	TreeColumn column1 = new TreeColumn(tree, SWT.NONE);
        //    	column1.setText("Bug Status");
        //    	column1.setWidth(100);
        //    	final TreeColumn column2 = new TreeColumn(tree, SWT.NONE);
        //    	column2.setText("Percent");
        //    	column2.setWidth(200);
        //    	String[] states = new String[]{"Resolved", "New", "Won't Fix", "Invalid"};
        //    	String[] teams = new String[] {"UI", "SWT", "OSGI"};
        //    	for (int i=0; i<teams.length; i++) {
        //    		TreeItem item = new TreeItem(tree, SWT.NONE);
        //    		item.setText(teams[i]);
        //    		for (int j = 0; j < states.length; j++) {
        //    			TreeItem subItem = new TreeItem(item, SWT.NONE);
        //    			subItem.setText(states[j]);	
        //    		}
        //    	}
        //
        //    	/*
        //    	 * NOTE: MeasureItem, PaintItem and EraseItem are called repeatedly.
        //    	 * Therefore, it is critical for performance that these methods be
        //    	 * as efficient as possible.
        //    	 */
        //    	tree.addListener(SWT.PaintItem, new Listener() {
        //    		int[] percents = new int[] {50, 30, 5, 15};
        //    		public void handleEvent(Event event) {
        //    			if (event.index == 1) {
        //    				TreeItem item = (TreeItem)event.item;
        //    				TreeItem parent = item.getParentItem();
        //    				if (parent != null) {
        //    					GC gc = event.gc;
        //    					int index = parent.indexOf(item);
        //    					int percent = percents[index];
        //    					Color foreground = gc.getForeground();
        //    					Color background = gc.getBackground();
        //    					gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
        //    					gc.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
        //    					int width = (column2.getWidth() - 1) * percent / 100;
        //    					gc.fillGradientRectangle(event.x, event.y, width, event.height, true);					
        //    					Rectangle rect2 = new Rectangle(event.x, event.y, width-1, event.height-1);
        //    					gc.drawRectangle(rect2);
        //    					gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        //    					String text = percent+"%";
        //    					Point size = event.gc.textExtent(text);					
        //    					int offset = Math.max(0, (event.height - size.y) / 2);
        //    					gc.drawText(text, event.x+2, event.y+offset, true);
        //    					gc.setForeground(background);
        //    					gc.setBackground(foreground);
        //    				}
        //    			}
        //    		}
        //    	});		
        //    			
        //    	shell.pack();
        //    	shell.open();
        //    	while(!shell.isDisposed()) {
        //    		if(!display.readAndDispatch()) display.sleep();
        //    	}
        //    	display.dispose();
        //        }catch(Exception e){
        //        	e.printStackTrace();
        //        }
    }

    /**
     * Configures the tool bar buttons of the current view
     */
    private void configureButtons() {
        // Configuration of the refresh button
        refreshSelectedButton.setText("Refresh Selected");
        refreshSelectedButton.setToolTipText("Refresh the Selected Chart");
        refreshSelectedButton.setSelection(false);
        refreshSelectedButton.setEnabled(false);
        refreshSelectedButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (timItViewer.getSelectedEditParts().size() != 0) {
                        ChartObject c = (ChartObject) ((ChartEditPart) timItViewer.getSelectedEditParts()
                                                                                  .get(0)).getModel();
                        c.performSnapshot();
                    }
                }
            });

        // Configuration of the refresh all button
        refreshAllButton.setText("Refresh All");
        refreshAllButton.setToolTipText("Refresh All Available Charts");
        refreshAllButton.setSelection(false);
        refreshAllButton.setEnabled(false);
        refreshAllButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                for (ChartObject o : chartContainer.getChildrenList()) {
                                    o.performSnapshot();
                                }
                            }
                        });
                }
            });

        // Configuration of the switch button
        timerLevelButton.setText("Switch to Detailed");
        timerLevelButton.setToolTipText("Switch Timers Level");
        timerLevelButton.setSelection(false);
        timerLevelButton.setEnabled(false);
        timerLevelButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (timItViewer.getSelectedEditParts().size() != 0) {
                        ChartObject c = (ChartObject) ((ChartEditPart) timItViewer.getSelectedEditParts()
                                                                                  .get(0)).getModel();
                        timerLevelButton.setText("Switch To " +
                            c.switchTimerLevel());
                    }
                }
            });
    }

    /**
     * Creates the graphical viewer.
     *
     * @param parent
     *            The Composite parent object
     */
    private void createGraphicalViewer(Composite parent) {
        // create graphical viewer
        this.timItViewer = new ScrollingGraphicalViewer();
        this.timItViewer.createControl(parent);

        // configure the viewer
        this.timItViewer.getControl().setBackground(ColorConstants.white);
        ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
        this.timItViewer.setRootEditPart(root);

        // activate the viewer as selection provider for Eclipse
        //getSite().setSelectionProvider(this.timItViewer);

        // initialize the viewer with input
        this.timItViewer.setEditPartFactory(new TimItEditPartFactory(this));

        // //////////////////////////////////////////////////
        // Add contents
        this.timItViewer.setContents(chartContainer);

        // ADD for resize event
        parent.addControlListener(new ControlAdapter() { /* resize listener */
                public void controlResized(ControlEvent event) {
                    chartContainer.update(true); // timItViewer.getContents().refresh();
                }
            });
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    @Override
    public void setFocus() {
    }

    /**
     * A getter for the chartContainer (root model)
     *
     * @return ChartContainerObject reference
     */
    public ChartContainerObject getChartContainer() {
        return chartContainer;
    }

    /**
     * A setter for the chartContainer (root model)
     *
     * @param chartContainer
     */
    public void setChartContainer(ChartContainerObject chartContainer) {
        this.chartContainer = chartContainer;
    }

    public Button getRefreshAllButton() {
        return refreshAllButton;
    }

    public void setRefreshAllButton(Button refreshAllButton) {
        this.refreshAllButton = refreshAllButton;
    }

    public Button getRefreshSelectedButton() {
        return refreshSelectedButton;
    }

    public void setRefreshSelectedButton(Button refreshSelectedButton) {
        this.refreshSelectedButton = refreshSelectedButton;
    }

    public ShowInTreeViewAction getShowInTreeViewAction() {
        return showInTreeViewAction;
    }

    public void setShowInTreeViewAction(
        ShowInTreeViewAction showInTreeViewAction) {
        this.showInTreeViewAction = showInTreeViewAction;
    }

    public Button getTimerLevelButton() {
        return timerLevelButton;
    }

    public void setTimerLevelButton(Button timerLevelButton) {
        this.timerLevelButton = timerLevelButton;
    }
}
