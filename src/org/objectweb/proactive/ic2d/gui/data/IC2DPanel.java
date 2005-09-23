/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.ic2d.gui.data;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.data.HostObject;
import org.objectweb.proactive.ic2d.data.IC2DObject;
import org.objectweb.proactive.ic2d.data.NodeObject;
import org.objectweb.proactive.ic2d.data.VMObject;
import org.objectweb.proactive.ic2d.event.CommunicationEventListener;
import org.objectweb.proactive.ic2d.gui.ActiveObjectCommunicationRecorder;
import org.objectweb.proactive.ic2d.gui.ActiveObjectWatcher;
import org.objectweb.proactive.ic2d.gui.IC2DGUIController;
import org.objectweb.proactive.ic2d.spy.SpyEvent;
import org.objectweb.proactive.ic2d.spy.SpyMessageEvent;
import org.objectweb.proactive.ic2d.util.ActiveObjectFilter;


public class IC2DPanel extends AbstractDataObjectPanel
    implements CommunicationEventListener {
    private IC2DObject ic2dObject;
    private ActiveObjectCommunicationRecorder communicationRecorder;
    private ActiveObjectWatcher activeObjectWatcher;
    private WorldPanel worldPanel;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public IC2DPanel(java.awt.Frame parentFrame, IC2DObject ic2dObject,
        IC2DGUIController controller,
        ActiveObjectCommunicationRecorder communicationRecorder,
        ActiveObjectFilter filter, ActiveObjectWatcher activeObjectWatcher) {
        super(parentFrame, filter, controller, activeObjectWatcher, "IC2D",
            "IC2D");
        this.ic2dObject = ic2dObject;
        this.communicationRecorder = communicationRecorder;
        this.activeObjectWatcher = activeObjectWatcher;
        setBackground(java.awt.Color.white);

        //Create the worldPanel
        worldPanel = new WorldPanel(this, ic2dObject.getWorldObject(),
                communicationRecorder);
        putChild(ic2dObject.getWorldObject(), worldPanel);
        ic2dObject.getWorldObject().registerListener(worldPanel);
        setLayout(new java.awt.BorderLayout());

        // create panel to host WorldPanel
        javax.swing.JScrollPane scrollableWorldPanel = new javax.swing.JScrollPane(worldPanel,
                javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableWorldPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                "World Panel"));
        scrollableWorldPanel.setBackground(java.awt.Color.white);
        add(scrollableWorldPanel, java.awt.BorderLayout.CENTER);
        add(createControlPanel(), java.awt.BorderLayout.SOUTH);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public void updateFilteredClasses() {
        removeAllActiveObjectsFromWatcher();
        worldPanel.filterChangeParentNotification(null);
    }

    public WorldPanel getWorldPanel() {
        return worldPanel;
    }

    //
    // -- implements CommunicationEventListener -----------------------------------------------
    //
    public void objectWaitingForRequest(ActiveObject object, SpyEvent spyEvent) {
    }

    public void objectWaitingByNecessity(ActiveObject object, SpyEvent spyEvent) {
    }

    public void requestMessageSent(ActiveObject object, SpyEvent spyEvent) {
        // recordCommunication(object, ((SpyMessageEvent) spyEvent).getDestinationBodyID(), true);
        // already recorded in requestMessageReceived
    }

    public void replyMessageSent(ActiveObject object, SpyEvent spyEvent) {
        // recordCommunication(object, ((SpyMessageEvent) spyEvent).getDestinationBodyID(), true);
        // not recording replies
    }

    public void requestMessageReceived(ActiveObject object, SpyEvent spyEvent) {
        recordCommunication(object,
            ((SpyMessageEvent) spyEvent).getSourceBodyID(), false);
    }

    public void replyMessageReceived(ActiveObject object, SpyEvent spyEvent) {
        // recordCommunication(object, ((SpyMessageEvent) spyEvent).getSourceBodyID(), false);
        // not recording replies
    }

    public void voidRequestServed(ActiveObject object, SpyEvent spyEvent) {
    }

    public void allEventsProcessed() {
        worldPanel.repaint();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    public AbstractDataObject getAbstractDataObject() {
        return ic2dObject;
    }

    protected Object[][] getDataObjectInfo() {
        return new Object[][] {  };
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private javax.swing.JPanel createControlPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.FlowLayout(
                    java.awt.FlowLayout.CENTER));
        // Draw Topology 
        {
            final javax.swing.JCheckBox topo = new javax.swing.JCheckBox(
                    "Display topology");
            topo.setSelected(communicationRecorder.isEnabled());
            topo.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        communicationRecorder.setEnabled(topo.isSelected());
                        repaint();
                    }
                });
            p.add(topo);
        }

        // Topology proportional / Ratio / filaire
        javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();

        final javax.swing.JRadioButton b1 = new javax.swing.JRadioButton(
                "proportional");
        b1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (b1.isSelected()) {
                        communicationRecorder.setDrawingStyle(ActiveObjectCommunicationRecorder.PROPORTIONAL_DRAWING_STYLE);
                        repaint();
                    }
                }
            });
        b1.setSelected(communicationRecorder.getDrawingStyle() == ActiveObjectCommunicationRecorder.PROPORTIONAL_DRAWING_STYLE);
        group.add(b1);
        p.add(b1);

        final javax.swing.JRadioButton b2 = new javax.swing.JRadioButton(
                "ratio");
        b2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (b2.isSelected()) {
                        communicationRecorder.setDrawingStyle(ActiveObjectCommunicationRecorder.RATIO_DRAWING_STYLE);
                        repaint();
                    }
                }
            });
        b2.setSelected(communicationRecorder.getDrawingStyle() == ActiveObjectCommunicationRecorder.RATIO_DRAWING_STYLE);
        group.add(b2);
        p.add(b2);

        final javax.swing.JRadioButton b3 = new javax.swing.JRadioButton(
                "filaire");
        b3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (b3.isSelected()) {
                        communicationRecorder.setDrawingStyle(ActiveObjectCommunicationRecorder.FILAIRE_DRAWING_STYLE);
                        repaint();
                    }
                }
            });
        b3.setSelected(communicationRecorder.getDrawingStyle() == ActiveObjectCommunicationRecorder.FILAIRE_DRAWING_STYLE);
        group.add(b3);
        p.add(b3);

        // Reset topology 
        javax.swing.JButton b = new javax.swing.JButton("Reset Topology");
        b.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    communicationRecorder.clear();
                    repaint();
                }
            });
        b.setToolTipText("Reset the current topology");
        p.add(b);
        // Enable monitoring 
        {
            final javax.swing.JCheckBox cb = new javax.swing.JCheckBox(
                    "Monitoring enable");
            cb.setSelected(ic2dObject.getController().isMonitoring());
            cb.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        ic2dObject.getController().setMonitoring(cb.isSelected());
                    }
                });
            p.add(cb);
        }

        return p;
    }

    // Record that two objects talked together
    private void recordCommunication(ActiveObject activeObjectOriginator,
        UniqueID peerActiveObjectID, boolean isSend) {
        ActiveObject peerActiveObject = ic2dObject.getWorldObject()
                                                  .findActiveObjectById(peerActiveObjectID);
        if (peerActiveObject == null) {
            return;
        }

        // now get the 2 panels associated with the two objects
        ActiveObjectPanel originatorPanel = getActiveObjectPanel(activeObjectOriginator);
        if (originatorPanel == null) {
            return;
        }
        ActiveObjectPanel peerPanel = getActiveObjectPanel(peerActiveObject);
        if (peerPanel == null) {
            return;
        }
        if (isSend) {
            communicationRecorder.recordCommunication(originatorPanel, peerPanel);
        } else {
            communicationRecorder.recordCommunication(peerPanel, originatorPanel);
        }
    }

    private ActiveObjectPanel getActiveObjectPanel(ActiveObject activeObject) {
        NodeObject nodeObject = (NodeObject) activeObject.getParent();
        VMObject vmObject = (VMObject) nodeObject.getParent();
        HostObject hostObject = (HostObject) vmObject.getParent();
        HostPanel hostPanel = worldPanel.getHostPanel(hostObject);
        if (hostPanel == null) {
            return null;
        }
        VMPanel vmPanel = hostPanel.getVMPanel(vmObject);
        if (vmPanel == null) {
            return null;
        }
        NodePanel nodePanel = vmPanel.getNodePanel(nodeObject);
        if (nodePanel == null) {
            return null;
        }
        return nodePanel.getActiveObjectPanel(activeObject);
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
}
