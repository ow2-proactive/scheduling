/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.timit.actions.GenerateReportAndSaveToHtmlAction;
import org.objectweb.proactive.ic2d.timit.actions.ShowInTreeViewAction;
import org.objectweb.proactive.ic2d.timit.actions.StartRecordingTimeLineAction;
import org.objectweb.proactive.ic2d.timit.actions.StopRecordingTimeLineAction;
import org.objectweb.proactive.ic2d.timit.data.BasicChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.BasicChartObject;
import org.objectweb.proactive.ic2d.timit.editparts.BasicChartEditPart;
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
    protected BasicChartContainerObject chartContainer;
    private static TimItView instance;

    public static TimItView getInstance() {
        return instance;
    }

    /**
     * The constructor.
     */
    public TimItView() {
        instance = this;
        this.chartContainer = new BasicChartContainerObject();
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
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();

        this.showInTreeViewAction = new ShowInTreeViewAction();
        toolBarManager.add(showInTreeViewAction);

        StartRecordingTimeLineAction startRecordingTimeLineAction = new StartRecordingTimeLineAction();
        startRecordingTimeLineAction.setTarget(this.chartContainer);
        toolBarManager.add(startRecordingTimeLineAction);

        StopRecordingTimeLineAction stopRecordingTimeLineAction = new StopRecordingTimeLineAction(
            startRecordingTimeLineAction);
        startRecordingTimeLineAction.setStopRecordingTimeLineAction(stopRecordingTimeLineAction);
        toolBarManager.add(stopRecordingTimeLineAction);

        GenerateReportAndSaveToHtmlAction generateReportAndSaveToHtmlAction = new GenerateReportAndSaveToHtmlAction(
            this);
        toolBarManager.add(generateReportAndSaveToHtmlAction);
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
                    BasicChartObject c = (BasicChartObject) ((BasicChartEditPart) timItViewer
                            .getSelectedEditParts().get(0)).getModel();
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
                        for (BasicChartObject o : chartContainer.getChildrenList()) {
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
                    BasicChartObject c = (BasicChartObject) ((BasicChartEditPart) timItViewer
                            .getSelectedEditParts().get(0)).getModel();
                    timerLevelButton.setText("Switch To " + c.switchTimerLevel());
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
    public BasicChartContainerObject getChartContainer() {
        return chartContainer;
    }

    /**
     * A setter for the chartContainer (root model)
     *
     * @param chartContainer
     */
    public void setChartContainer(BasicChartContainerObject chartContainer) {
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

    public void setShowInTreeViewAction(ShowInTreeViewAction showInTreeViewAction) {
        this.showInTreeViewAction = showInTreeViewAction;
    }

    public Button getTimerLevelButton() {
        return timerLevelButton;
    }

    public void setTimerLevelButton(Button timerLevelButton) {
        this.timerLevelButton = timerLevelButton;
    }
}
