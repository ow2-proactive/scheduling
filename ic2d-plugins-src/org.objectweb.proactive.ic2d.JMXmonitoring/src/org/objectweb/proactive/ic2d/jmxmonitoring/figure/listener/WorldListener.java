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
package org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.HorizontalLayoutAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.KillVMAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.NewHostAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshHostAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshJVMAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshNodeAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.SetDepthAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.SetTTRAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.SetUpdateFrequenceAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.StopMonitoringAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.VerticalLayoutAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.dnd.DragAndDrop;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public class WorldListener implements MouseListener, MouseMotionListener {
    private DragAndDrop dnd;
    private DragHost dragHost;
    private ActionRegistry registry;
    private WorldObject world;

    public WorldListener(MonitoringView monitoringView) {
        this.world = monitoringView.getWorld();
        this.dnd = monitoringView.getDragAndDrop();
        this.dragHost = monitoringView.getDragHost();
        this.registry = monitoringView.getGraphicalViewer().getActionRegistry();
    }

    public void mouseDoubleClicked(MouseEvent me) { /* Do nothing */
    }

    public void mousePressed(MouseEvent me) {
        if (me.button == 1) {
            dnd.reset();
        } else if (me.button == 3) {
            // Monitor a new host
            registry.getAction(NewHostAction.NEW_HOST).setEnabled(true);

            // Set depth control
            registry.getAction(SetDepthAction.SET_DEPTH).setEnabled(true);

            // Refresh
            registry.getAction(RefreshAction.REFRESH).setEnabled(true);

            // Set time to refresh
            registry.getAction(SetTTRAction.SET_TTR).setEnabled(true);

            // Look for new JVM
            registry.getAction(RefreshHostAction.REFRESH_HOST).setEnabled(false);

            // Look for new Nodes
            registry.getAction(RefreshJVMAction.REFRESH_JVM).setEnabled(false);

            // Look for new Active Objects
            registry.getAction(RefreshNodeAction.REFRESH_NODE).setEnabled(false);

            // Stop monitoring this ...
            registry.getAction(StopMonitoringAction.STOP_MONITORING)
                    .setEnabled(false);

            // Set update frequence...
            registry.getAction(SetUpdateFrequenceAction.SET_UPDATE_FREQUENCE)
                    .setEnabled(false);

            // Kill VM
            registry.getAction(KillVMAction.KILLVM).setEnabled(false);

            // Vertical Layout
            registry.getAction(VerticalLayoutAction.VERTICAL_LAYOUT)
                    .setEnabled(false);

            // Horizontal Layout
            registry.getAction(HorizontalLayoutAction.HORIZONTAL_LAYOUT)
                    .setEnabled(false);

            // Manual handling of an action for timer snapshot ... needs
            // improvement
            if (this.world.getMonitoredChildrenSize() != 0) {
                IAction anAction = registry.getAction("Get timer snapshot");
                if (anAction != null) {
                    ((IActionExtPoint) anAction).setAbstractDataObject(this.world);
                    anAction.setText("Gather All Stats");
                    anAction.setEnabled(true);
                }
            }
        }
    }

    public void mouseReleased(MouseEvent me) {
        dnd.reset();
        dragHost.mouseReleased(me);
        ;
    }

    //---- MouseMotionListener 
    public void mouseEntered(MouseEvent me) {
        if (dnd.getSource() != null) {
            dnd.refresh(null);
        }
    }

    public void mouseExited(MouseEvent me) {
        if (dnd.getSource() != null) {
            dnd.refresh(null);
        }
    }

    public void mouseDragged(MouseEvent me) {
        dragHost.mouseDragged(me);
    }

    public void mouseHover(MouseEvent me) { /* Do nothing */
    }

    public void mouseMoved(MouseEvent me) { /* Do nothing */
    }
}
