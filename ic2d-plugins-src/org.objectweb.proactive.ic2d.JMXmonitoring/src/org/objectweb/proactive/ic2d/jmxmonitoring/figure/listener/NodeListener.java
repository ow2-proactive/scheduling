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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.RefreshNodeAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.SetUpdateFrequenceAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.action.StopMonitoringAction;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.dnd.DragAndDrop;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.NodeFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public class NodeListener implements MouseListener, MouseMotionListener {
    private ActionRegistry registry;
    private NodeObject node;
    private NodeFigure figure;
    private DragAndDrop dnd;

    public NodeListener(NodeObject node, NodeFigure figure,
        MonitoringView monitoringView) {
        this.registry = monitoringView.getGraphicalViewer().getActionRegistry();
        this.node = node;
        this.figure = figure;
        this.dnd = monitoringView.getDragAndDrop();
    }

    public void mouseDoubleClicked(MouseEvent me) { /* Do nothing */
    }

    public void mousePressed(MouseEvent me) {
        if (me.button == 1) {
            dnd.reset();
        } else if (me.button == 3) {
            for (Iterator<IAction> action = (Iterator<IAction>) registry.getActions();
                    action.hasNext();) {
                IAction act = action.next();
                if (act instanceof RefreshNodeAction) {
                    RefreshNodeAction refreshNodeAction = (RefreshNodeAction) act;
                    refreshNodeAction.setNode(node);
                    refreshNodeAction.setEnabled(true);
                } else if (act instanceof StopMonitoringAction) {
                    StopMonitoringAction stopMonitoringAction = (StopMonitoringAction) act;
                    stopMonitoringAction.setObject(node);
                    stopMonitoringAction.setEnabled(true);
                } else if (act instanceof SetUpdateFrequenceAction) {
                    SetUpdateFrequenceAction setUpdateFrequenceAction = (SetUpdateFrequenceAction) act;
                    setUpdateFrequenceAction.setNode(node);
                    setUpdateFrequenceAction.setEnabled(true);
                } else if (act instanceof IActionExtPoint) {
                    IActionExtPoint extensionAction = (IActionExtPoint) act;
                    extensionAction.setAbstractDataObject(this.node);
                } else {
                    act.setEnabled(false);
                }
            }
        }
    }

    public void mouseReleased(MouseEvent me) {
        if (me.button == 1) {
            final ActiveObject source = dnd.getSource();
            NodeObject sourceNode = dnd.getSourceNode();
            if (source != null) {
                if ((sourceNode.getParent().equals(node.getParent())) ||
                        (node.getChild(source.getKey()) != null)) {
                    Console.getInstance(Activator.CONSOLE_NAME)
                           .warn("The active object originates from the same VM you're trying to migrate it to !");
                    figure.setHighlight(null);
                    dnd.reset();
                    return;
                }

                /*------------ Migration ------------*/
                new Thread(new Runnable() {
                        public void run() {
                            source.migrateTo(node.getUrl());
                        }
                    }).start();
                /*----------------------------------*/
                figure.setHighlight(null);
                dnd.reset();
            }
        }
    }

    //---- MouseMotionListener 
    public void mouseEntered(MouseEvent me) {
        if (dnd.getSource() != null) {
            dnd.refresh(figure);
            figure.setHighlight(ColorConstants.green);
        }
    }

    public void mouseExited(MouseEvent me) {
        if (dnd.getSource() != null) {
            dnd.refresh(figure);
            figure.setHighlight(null);
            figure.repaint();
        }
    }

    public void mouseDragged(MouseEvent me) { /* Do nothing */
    }

    public void mouseHover(MouseEvent me) { /* Do nothing */
    }

    public void mouseMoved(MouseEvent me) { /* Do nothing */
    }
}
