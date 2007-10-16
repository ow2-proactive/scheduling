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
package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.dialog.MonitorNewHostDialog;


public class NewHostAction extends Action {
    private Display display;

    /** The World */
    private WorldObject world;
    public static final String NEW_HOST = "New host";

    public NewHostAction(Display display, WorldObject world) {
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "host.gif"));
        this.display = display;
        this.world = world;
        this.setId(NEW_HOST);
        this.setText("Monitor a new host...");
        setToolTipText("Monitor a new host");
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    protected void setWorldObject(WorldObject world) {
        this.world = world;
    }

    @Override
    public void run() {
        new MonitorNewHostDialog(display.getActiveShell(), world);
    }
}
