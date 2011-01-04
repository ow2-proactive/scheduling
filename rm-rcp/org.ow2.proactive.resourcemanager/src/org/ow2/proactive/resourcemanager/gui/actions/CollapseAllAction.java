/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.gui.Internal;


/**
 * @author The ProActive Team
 *
 */
public class CollapseAllAction extends Action {

    /**
     * determine the enabled/disabled action state at its instantiation
     */
    public static final boolean ENABLED_AT_CONSTRUCTION = false;
    private static CollapseAllAction instance = null;
    private TreeViewer viewer = null;

    private CollapseAllAction(TreeViewer viewer) {
        this.viewer = viewer;
        this.setText("Collapse All");
        this.setToolTipText("To collapse all items");
        this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_COLLAPSEALL));
        this.setEnabled(ENABLED_AT_CONSTRUCTION);
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        viewer.collapseAll();
    }

    /**
     * return a new instance of CollapseAction class
     * @param viewer TreeViewer originating the action
     * @return an instance of CollapseAction
     */
    public static CollapseAllAction newInstance(TreeViewer viewer) {
        instance = new CollapseAllAction(viewer);
        return instance;
    }

    /**
     * Return an instance of this class
     * @return an instance of this class
     */
    public static CollapseAllAction getInstance() {
        return instance;
    }
}
