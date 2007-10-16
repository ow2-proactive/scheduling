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
package org.objectweb.proactive.ic2d.jobmonitoring.views;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.gef.editparts.RootTreeEditPart;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ModelRecorder;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jobmonitoring.actions.CollapseAllAction;
import org.objectweb.proactive.ic2d.jobmonitoring.actions.ExpandAllAction;
import org.objectweb.proactive.ic2d.jobmonitoring.editparts.JobMonitoringTreePartFactory;


/**
 *
 * @author Jean-Michael Legait and Michèle Reynier
 *
 */
public class JobMonitoringView extends ViewPart implements Observer {
    private TreeViewer treeViewer;
    private IMenuManager dropDownMenu;

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        FormLayout layout = new FormLayout();
        parent.setLayout(layout);

        // create graphical viewer
        treeViewer = new TreeViewer();
        treeViewer.createControl(parent);

        // configure the viewer
        Tree tree = new Tree(parent, SWT.SINGLE);
        ((RootTreeEditPart) treeViewer.getRootEditPart()).setWidget(tree);

        // hide an empty part in the view
        FormData formData1 = new FormData();
        formData1.top = new FormAttachment(0, 0);
        formData1.bottom = new FormAttachment(0, 0);
        formData1.left = new FormAttachment(0, 0);
        formData1.right = new FormAttachment(0, 0);
        parent.getChildren()[0].setLayoutData(formData1);

        FormData formData = new FormData();
        formData.top = new FormAttachment(0, 0);
        formData.bottom = new FormAttachment(100, 0);
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        parent.getChildren()[1].setLayoutData(formData);

        IToolBarManager toolBarManager = getViewSite().getActionBars()
                                             .getToolBarManager();

        toolBarManager.add(new ExpandAllAction(treeViewer));
        toolBarManager.add(new CollapseAllAction(treeViewer));

        // configure the menu
        IActionBars actionBars = getViewSite().getActionBars();
        dropDownMenu = actionBars.getMenuManager();
        ModelRecorder.getInstance().addObserver(this);

        // Monitor a new model
        Action newHost = new NewHostJobMonitoringAction(parent);
        dropDownMenu.add(newHost);
        Separator separator = new Separator();
        dropDownMenu.add(separator);

        // The list of existing models
        Object[] titles = ModelRecorder.getInstance().getNames().toArray();
        for (int i = 0, size = titles.length; i < size; i++) {
            dropDownMenu.add(new ViewAction((String) titles[i]));
        }

        /*****************************/

        //		ContextMenuProvider contextMenu = new JobMonitoringContextMenuProvider(treeViewer);
        //		treeViewer.setContextMenu(contextMenu);
        //		getSite().registerContextMenu(contextMenu, treeViewer);
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }

    public void update(Observable o, Object arg) {
        // A view has been added to the viewRecorder, this view contains a model (WorldObject)
        if (o instanceof ModelRecorder) {
            dropDownMenu.add(new ViewAction((String) arg));
            dropDownMenu.updateAll(true);
        }
    }

    //
    //-----INNER CLASSES------------
    //

    /**
     * To monitor a new host.
     */
    public class NewHostJobMonitoringAction extends org.objectweb.proactive.ic2d.jmxmonitoring.action.NewHostAction {
        public NewHostJobMonitoringAction(Composite parent) {
            super(parent.getDisplay(), null);
            setImageDescriptor(null);
        }

        @Override
        public void run() {
            treeViewer.setEditPartFactory(new JobMonitoringTreePartFactory());
            WorldObject world = new WorldObject();
            treeViewer.setContents(world);
            super.setWorldObject(world);
            super.run();
            dropDownMenu.updateAll(true);
        }
    }

    /**
     * To select a existing model.
     */
    public class ViewAction extends Action {
        public ViewAction(String title) {
            super(title);
            this.setToolTipText(title);
        }

        @Override
        public void run() {
            WorldObject world = ModelRecorder.getInstance().getModel(getText());
            if (world != null) {
                treeViewer.setEditPartFactory(new JobMonitoringTreePartFactory());
                treeViewer.setContents(world);
                dropDownMenu.updateAll(true);
            }
        }
    }
}
