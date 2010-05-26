/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.gui.Internal;


/**
 * @author The ProActive Team
 */
public class ChangeMaximizeListAction extends Action implements IMenuCreator {
    private Menu fMenu;

    public ChangeMaximizeListAction() {
        setText("Maximize list");
        setToolTipText("Maximize a job list");
        setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Internal.IMG_MAXIMIZE));
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

    /**
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
        return null;
    }
}
