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

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.editparts.RootTreeEditPart;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.timit.actions.tree.CollapseAllAction;
import org.objectweb.proactive.ic2d.timit.actions.tree.DeleteTreeAction;
import org.objectweb.proactive.ic2d.timit.actions.tree.ExpandAllAction;
import org.objectweb.proactive.ic2d.timit.actions.tree.SaveToXmlAction;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeNodeObject;
import org.objectweb.proactive.ic2d.timit.editparts.tree.TreeEditPartFactory;


/**
 * This class represents the timer tree view with sortable columns
 * that allows the user to analyse the hierarchical representation
 * of the timers data.<p>
 * The user can save the data in an xml file.
 *
 * @author vbodnart
 *
 */
public class TimerTreeView extends ViewPart {
    public static final int NUMBER_OF_COLUMNS = 5;
    public static final int NAME_COLUMN = 0;
    public static final int TIME_COLUMN = 1;
    public static final int TOTAL_PERCENT_COLUMN = 2;
    public static final int INVOCATIONS_COLUMN = 3;
    public static final int PARENT_PERCENT_COLUMN = 4;
    public static final String ID = "org.objectweb.proactive.ic2d.timit.views.TreeView";
    protected TreeViewer treeViewer;
    protected TimerTreeHolder timerTreeHolder;
    protected SaveToXmlAction saveToXmlAction;
    protected ExpandAllAction expandAllAction;
    protected CollapseAllAction collapseAllAction;
    protected DeleteTreeAction deleteTreeAction;
    private EditDomain editDomain;

    public TimerTreeView() {
        super();
        this.timerTreeHolder = new TimerTreeHolder();
    }

    @Override
    public void createPartControl(Composite parent) {
        this.treeViewer = new TreeViewer();
        this.treeViewer.createControl(parent);
        this.editDomain = new EditDomain();

        this.treeViewer.setEditDomain(this.editDomain);

        this.treeViewer.setEditPartFactory(new TreeEditPartFactory(this));
        this.treeViewer.setContents(this.timerTreeHolder);

        RootTreeEditPart t = (RootTreeEditPart) this.treeViewer.getRootEditPart();

        final Tree tree = (Tree) t.getWidget();

        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeColumn nameColumn = new TreeColumn(tree, SWT.CENTER);
        nameColumn.setText("Name");
        nameColumn.setWidth(200);

        TreeColumn timeColumn = new TreeColumn(tree, SWT.RIGHT);
        timeColumn.setText("Time [ms]");
        timeColumn.setWidth(100);

        TreeColumn totalPercentColumn = new TreeColumn(tree, SWT.CENTER);
        totalPercentColumn.setText("Total [%]");
        totalPercentColumn.setWidth(100);

        TreeColumn invocationsColumn = new TreeColumn(tree, SWT.CENTER);
        invocationsColumn.setText("Invocations");
        invocationsColumn.setWidth(80);

        TreeColumn parentPercentColumn = new TreeColumn(tree, SWT.CENTER);
        parentPercentColumn.setText("Parent [%]");
        parentPercentColumn.setWidth(100);

        //////////////////////////////////////////
        Listener sortListener = new Listener() {
                public void handleEvent(Event e) {
                    if (tree.getItems().length == 0) {
                        return;
                    }

                    // determine new sort column and direction
                    TreeColumn sortColumn = tree.getSortColumn();
                    TreeColumn currentColumn = (TreeColumn) e.widget;
                    int dir = tree.getSortDirection();
                    if (sortColumn == currentColumn) {
                        dir = (dir == SWT.UP) ? SWT.DOWN : SWT.UP;
                    } else {
                        tree.setSortColumn(currentColumn);
                        dir = SWT.UP;
                    }
                    final int columnIndex = tree.indexOf(currentColumn);
                    boolean up = (dir == SWT.UP);
                    for (final TimerTreeNodeObject t : timerTreeHolder.getDummyRoots()) {
                        // Get first child of each dummy root and fire sort event
                        TimerTreeNodeObject target = t.getChildren().get(0);
                        target.firePropertyChange(TimerTreeNodeObject.P_SORT,
                            columnIndex, up);
                        target.firePropertyChange(TimerTreeNodeObject.P_EXPAND_STATE,
                            null, true);
                    }
                    tree.setSortDirection(dir);
                }
            };
        timeColumn.addListener(SWT.Selection, sortListener);
        totalPercentColumn.addListener(SWT.Selection, sortListener);
        invocationsColumn.addListener(SWT.Selection, sortListener);
        parentPercentColumn.addListener(SWT.Selection, sortListener);

        // --------------------
        IToolBarManager toolBarManager = getViewSite().getActionBars()
                                             .getToolBarManager();

        // Adds "DeleteTreeAction" action to the view's toolbar
        this.deleteTreeAction = new DeleteTreeAction();
        toolBarManager.add(deleteTreeAction);

        toolBarManager.add(new Separator());

        // Adds "SaveToXmlAction" action to the view's toolbar
        this.saveToXmlAction = new SaveToXmlAction(this.timerTreeHolder);
        toolBarManager.add(saveToXmlAction);

        // Adds "ExpandAllAction" action to the view's toolbar
        this.expandAllAction = new ExpandAllAction(this.timerTreeHolder);
        toolBarManager.add(expandAllAction);

        // Adds "CollapseAllAction" action to the view's toolbar
        this.collapseAllAction = new CollapseAllAction(this.timerTreeHolder);
        toolBarManager.add(collapseAllAction);
    }

    @Override
    public void setFocus() {
    }

    public DeleteTreeAction getDeleteTreeAction() {
        return deleteTreeAction;
    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }
}
