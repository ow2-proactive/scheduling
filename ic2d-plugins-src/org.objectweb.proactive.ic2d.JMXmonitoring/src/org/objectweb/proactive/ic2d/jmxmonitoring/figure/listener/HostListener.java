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
import org.eclipse.jface.action.IAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.HorizontalLayoutAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshHostAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.StopMonitoringAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.VerticalLayoutAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.dnd.DragAndDrop;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.HostFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public class HostListener implements MouseListener, MouseMotionListener {
    private ActionRegistry registry;
    private HostObject host;
    private HostFigure figure;
    private DragAndDrop dnd;
    private DragHost dragHost;

    public HostListener(HostObject host, HostFigure figure,
        MonitoringView monitoringView) {
        this.registry = monitoringView.getGraphicalViewer().getActionRegistry();
        this.host = host;
        this.figure = figure;
        this.dnd = monitoringView.getDragAndDrop();
        this.dragHost = monitoringView.getDragHost();
    }

    public void mouseDoubleClicked(MouseEvent me) { /* Do nothing */
    }

    public void mousePressed(MouseEvent me) {
        if (me.button == 1) {
            dnd.reset();
            dragHost.mousePressed(me);
        } else if (me.button == 3) {
            for (Iterator<IAction> action = (Iterator<IAction>) registry.getActions();
                    action.hasNext();) {
                IAction act = action.next();
                if (act instanceof RefreshHostAction) {
                    RefreshHostAction refreshHostAction = (RefreshHostAction) act;
                    refreshHostAction.setHost(host);
                    refreshHostAction.setEnabled(true);
                } else if (act instanceof StopMonitoringAction) {
                    StopMonitoringAction stopMonitoringAction = (StopMonitoringAction) act;
                    stopMonitoringAction.setObject(host);
                    stopMonitoringAction.setEnabled(true);
                } else if (act instanceof VerticalLayoutAction) {
                    VerticalLayoutAction verticalLayoutAction = (VerticalLayoutAction) act;
                    verticalLayoutAction.setHost(figure);
                    if (figure.isVerticalLayout()) {
                        verticalLayoutAction.setChecked(true);
                    }
                    verticalLayoutAction.setEnabled(true);
                } else if (act instanceof HorizontalLayoutAction) {
                    HorizontalLayoutAction horizontalLayoutAction = (HorizontalLayoutAction) act;
                    horizontalLayoutAction.setHost(figure);
                    if (figure.isVerticalLayout()) {
                        horizontalLayoutAction.setChecked(false);
                    }
                    horizontalLayoutAction.setEnabled(true);
                } else if (act instanceof IActionExtPoint) {
                    IActionExtPoint extensionAction = (IActionExtPoint) act;
                    extensionAction.setAbstractDataObject(this.host);
                } else {
                    act.setEnabled(false);
                }
            }
        }
    }

    public void mouseReleased(MouseEvent me) {
        dnd.reset();
        dragHost.mouseReleased(me);
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
