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
package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;


/**
 * @author The ProActive Team
 */
public class ChangePriorityJobAction extends Action implements IMenuCreator {
    private Menu fMenu;

    public ChangePriorityJobAction() {
        setText("Change job priority");
        setToolTipText("To change a job priority");
        setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/job_priority.png"));
        setMenuCreator(this);
    }

    /**
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
        if (fMenu != null) {
            fMenu.dispose();
        }
        fMenu = null;
    }

    /**
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent) {
        if (fMenu != null) {
            fMenu.dispose();
        }

        fMenu = new Menu(parent);
        boolean isAnAdmin = SchedulerProxy.getInstance().isAnAdmin();
        if (isAnAdmin) {
            addActionToMenu(fMenu, PriorityJobAction.getInstance(JobPriority.IDLE));
        }
        addActionToMenu(fMenu, PriorityJobAction.getInstance(JobPriority.LOWEST));
        addActionToMenu(fMenu, PriorityJobAction.getInstance(JobPriority.LOW));
        addActionToMenu(fMenu, PriorityJobAction.getInstance(JobPriority.NORMAL));
        if (isAnAdmin) {
            addActionToMenu(fMenu, PriorityJobAction.getInstance(JobPriority.HIGH));
            addActionToMenu(fMenu, PriorityJobAction.getInstance(JobPriority.HIGHEST));
        }
        return fMenu;
    }

    private void addActionToMenu(Menu parent, Action action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    /**
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
        return null;
    }
}
