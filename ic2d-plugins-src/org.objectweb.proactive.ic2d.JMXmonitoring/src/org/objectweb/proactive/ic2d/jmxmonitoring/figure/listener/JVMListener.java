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
package org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener;

import java.util.Iterator;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.jface.action.IAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.KillVMAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshJVMAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.StopMonitoringAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.dnd.DragAndDrop;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public class JVMListener implements MouseListener, MouseMotionListener {
    private ActionRegistry registry;
    private RuntimeObject jvm;
    private DragAndDrop dnd;

    public JVMListener(RuntimeObject jvm, MonitoringView monitoringView) {
        this.registry = monitoringView.getGraphicalViewer().getActionRegistry();
        this.dnd = monitoringView.getDragAndDrop();
        this.jvm = jvm;
    }

    public void mouseDoubleClicked(MouseEvent me) { /* Do nothing */
    }

    public void mousePressed(MouseEvent me) {
        if (me.button == 1) {
            dnd.reset();
            // Call setActiveSelect on all action ext points registred
            @SuppressWarnings("unchecked")
            final Iterator it = registry.getActions();
            while (it.hasNext()) {
                IAction act = (IAction) it.next();
                if (act instanceof IActionExtPoint) {
                    ((IActionExtPoint) act).setActiveSelect(this.jvm);
                }
            }
        } else if (me.button == 3) {
            @SuppressWarnings("unchecked")
            final Iterator it = registry.getActions();
            while (it.hasNext()) {
                final IAction act = (IAction) it.next();
                final Class<?> actionClass = act.getClass();
                if (actionClass == RefreshJVMAction.class) {
                    RefreshJVMAction refreshJVMAction = (RefreshJVMAction) act;
                    refreshJVMAction.setJVM(jvm);
                    refreshJVMAction.setEnabled(true);
                } else if (actionClass == StopMonitoringAction.class) {
                    StopMonitoringAction stopMonitoringAction = (StopMonitoringAction) act;
                    stopMonitoringAction.setObject(jvm);
                    stopMonitoringAction.setEnabled(true);
                } else if (actionClass == KillVMAction.class) {
                    KillVMAction killVMAction = (KillVMAction) act;
                    killVMAction.setVM(jvm);
                    killVMAction.setEnabled(true);
                } else if (act instanceof IActionExtPoint) {
                    ((IActionExtPoint) act).setAbstractDataObject(this.jvm);
                } else if (act instanceof ZoomOutAction || act instanceof ZoomInAction) {
                    act.setEnabled(true);
                } else {
                    act.setEnabled(false);
                }
            }
        }
    }

    public void mouseReleased(MouseEvent me) {
        dnd.reset();
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

    public void mouseDragged(MouseEvent me) { /* Do nothing */
    }

    public void mouseHover(MouseEvent me) { /* Do nothing */
    }

    public void mouseMoved(MouseEvent me) { /* Do nothing */
    }
}
