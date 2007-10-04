/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


public class EnableDisableMonitoringAction extends Action
    implements IWorkbenchWindowActionDelegate {
    public static final String ENABLE_DISABLE_MONITORING = "EnableDisbaleMonitoring";
    public static final boolean DEFAULT_IS_MONITORING = true;
    private WorldObject world;
    private boolean monitoring = DEFAULT_IS_MONITORING;
    private String enableMessage = "The Monitoring is Enabled";
    private String disableMessage = "The Monitoring is Disabled";

    public EnableDisableMonitoringAction(WorldObject world) {
        super("Enable/Disable Monitoring", AS_PUSH_BUTTON);
        this.world = world;
        this.setId(ENABLE_DISABLE_MONITORING);
        updateMonitoringState();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public void dispose() { /* Do nothing */
    }

    public void init(IWorkbenchWindow window) { /* Do nothing */
    }

    public void run(IAction action) {
        this.run();
    }

    public void selectionChanged(IAction action, ISelection selection) { /* Do nothing */
    }

    @Override
    public void run() {
        monitoring = !monitoring;
        updateMonitoringState();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void updateMonitoringState() {
        if (monitoring) {
            this.setImageDescriptor(ImageDescriptor.createFromFile(
                    this.getClass(), "openedEye.gif"));
            this.setToolTipText(enableMessage);
            Console.getInstance(Activator.CONSOLE_NAME).debug(enableMessage);
        } else {
            this.setImageDescriptor(ImageDescriptor.createFromFile(
                    this.getClass(), "closedEye.gif"));
            this.setToolTipText(disableMessage);
            Console.getInstance(Activator.CONSOLE_NAME).debug(disableMessage);
        }

        // TODO A faire
        /*world.enableMonitoring(monitoring);*/
    }
}
