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
package org.objectweb.proactive.extensions.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;


/**
 * @author The ProActive Team
 */
public class ChangeMaximizeListAction extends Action implements IMenuCreator {
    private static ChangeMaximizeListAction instance = null;
    private Menu fMenu;

    private ChangeMaximizeListAction() {
        setText("Maximize list");
        setToolTipText("To maximize a job list");
        //        setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "icons/job_priority.png"));
        setMenuCreator(this);
    }

    /*
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
        if (fMenu != null) {
            fMenu.dispose();
        }
        fMenu = null;
    }

    /*
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent) {
        if (fMenu != null) {
            fMenu.dispose();
        }

        fMenu = new Menu(parent);

        addActionToMenu(fMenu, MaximizeListAction.getInstance(MaximizeListAction.NONE));
        addActionToMenu(fMenu, MaximizeListAction.getInstance(MaximizeListAction.PENDING));
        addActionToMenu(fMenu, MaximizeListAction.getInstance(MaximizeListAction.RUNNING));
        addActionToMenu(fMenu, MaximizeListAction.getInstance(MaximizeListAction.FINISHED));
        return fMenu;
    }

    private void addActionToMenu(Menu parent, Action action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        MaximizeListAction.getInstance(MaximizeListAction.NONE).setEnabled(enabled);
        MaximizeListAction.getInstance(MaximizeListAction.PENDING).setEnabled(enabled);
        MaximizeListAction.getInstance(MaximizeListAction.RUNNING).setEnabled(enabled);
        MaximizeListAction.getInstance(MaximizeListAction.FINISHED).setEnabled(enabled);
    }

    /*
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
        return null;
    }

    public static ChangeMaximizeListAction newInstance() {
        instance = new ChangeMaximizeListAction();
        return instance;
    }

    public static ChangeMaximizeListAction getInstance() {
        return instance;
    }
}
