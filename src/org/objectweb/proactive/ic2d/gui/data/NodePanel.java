/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.gui.data;

import java.util.Iterator;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.data.NodeObject;
import org.objectweb.proactive.ic2d.data.VMObject;
import org.objectweb.proactive.ic2d.event.NodeObjectListener;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredAO;


public class NodePanel extends AbstractDataObjectPanel
    implements NodeObjectListener {
    private static javax.swing.border.Border STANDARD_LINE_BORDER = javax.swing.BorderFactory.createLineBorder(new java.awt.Color(
                0, 128, 128));
    private static javax.swing.border.Border ACCEPT_LINE_BORDER = javax.swing.BorderFactory.createLineBorder(java.awt.Color.green);
    private static javax.swing.border.Border ACCEPTED_LINE_BORDER = javax.swing.BorderFactory.createLineBorder(java.awt.Color.red);
    private static javax.swing.border.Border MARGIN_EMPTY_BORDER = javax.swing.BorderFactory.createEmptyBorder(4,
            4, 4, 3);
    private NodeObject nodeObject;
    private final javax.swing.border.TitledBorder currentTitledBorder;
    private final javax.swing.border.Border currentBorder;

    /** enables this component to be a dropTarget */
    private java.awt.dnd.DropTarget dropTarget;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public NodePanel(AbstractDataObjectPanel parentDataObjectPanel,
        NodeObject targetNodeObject) {
        this(parentDataObjectPanel, targetNodeObject,
            new java.awt.Color(0xd0, 0xd0, 0xe0));
    }

    public NodePanel(AbstractDataObjectPanel parentDataObjectPanel,
        NodeObject targetNodeObject, java.awt.Color c) {
        super(parentDataObjectPanel, targetNodeObject.getURL(), "NodeObject");
        if (targetNodeObject.getProtocol().equals("jini:")) {
            c = java.awt.Color.cyan;
        }
        if (targetNodeObject.getProtocol().equals("http:")) {
            c = java.awt.Color.orange;
        }
        if (targetNodeObject.getProtocol().equals("rmissh:")) {
            c = java.awt.Color.white;
        }
        nodeObject = targetNodeObject;
        // dnd stuff
        dropTarget = new java.awt.dnd.DropTarget(this,
                java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE,
                new MyDropTargetListener(), true);
        // ui look
        setBackground(c);
        setLayout(new java.awt.GridLayout(0, 1, 2, 2));
        String name = nodeObject.getName();
        currentTitledBorder = javax.swing.BorderFactory.createTitledBorder(STANDARD_LINE_BORDER,
                name);
        currentBorder = javax.swing.BorderFactory.createCompoundBorder(currentTitledBorder,
                MARGIN_EMPTY_BORDER);
        setBorder(currentBorder);
        setMinimumSize(new java.awt.Dimension(100, 20));

        // The popup
        PanelPopupMenu popup = new PanelPopupMenu("Node " + name);
        popup.addGenericMenu();
        popup.add(new javax.swing.AbstractAction("Look for new ActiveObjects",
                null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ((VMObject) nodeObject.getParent()).sendEventsForAllActiveObjects();
                }
            });
        popup.add(new javax.swing.AbstractAction("Create a new ActiveObject",
                null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doCreateObject();
                }
            });
        popup.addSeparator();

        final VMPanel parentPanel = (VMPanel) getParentDataObjectPanel();
        popup.add(new javax.swing.AbstractAction("Stop monitoring this node",
                null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    nodeObject.destroyObject();
                    parentPanel.stopMonitorNode(nodeObject);
                }
            });

        addMouseListener(popup.getMenuMouseListener());
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements NodeObjectListener -----------------------------------------------
    //
    public void activeObjectAdded(ActiveObject activeObject) {
        ActiveObjectPanel panel = new ActiveObjectPanel(this, activeObject);
        addChild(activeObject, panel);
        activeObject.registerListener(panel);
        showBorder(STANDARD_LINE_BORDER);
        if (activeObject.isViewedInEventList()) {
            activeObjectWatcher.addActiveObject(activeObject);
        }

        // refresh automatically
        ((WorldPanel) ((HostPanel) ((VMPanel) getParent()).getParent()).getParent()).getMonitorThread()
         .updateHosts();
    }

    public void activeObjectRemoved(ActiveObject activeObject) {
        removeChild(activeObject);
        activeObjectWatcher.removeActiveObject(activeObject);
        //      refresh automatically
        //((WorldPanel)((HostPanel)((VMPanel) getParent()).getParent()).getParent()).getMonitorThread().updateHosts() ;
    }

    public void nodeNotResponding() {
        //setBackground(ActiveObjectPanel.COLOR_WHEN_NOT_RESPONDING);
        repaint();
        nodeObject.setAlive(false);
        Iterator it = childsIterator();

        // all children (active objects) are also not responding
        while (it.hasNext()) {
            ActiveObjectPanel aoPanel = (ActiveObjectPanel) it.next();
            aoPanel.activeObjectNotResponding();
        }
    }

    /**
     * stop the monitoring of an active object. This object is added
     * to the skipped list
     * @param aObject
     */
    public void stopMonitorAO(ActiveObject aObject) {
        // TODO it doesn't work, at the next refresh the AO is once again displayed
        MonitoredAO object = new MonitoredAO(aObject.getClassName(),
                aObject.getID().toString());

        //System.out.println("---> id is correct ? : " + aObject.getID().toString());
        //System.out.println("---> classname is correct ? : " + aObject.getClassName());
        ((WorldPanel) (this.getParent().getParent().getParent())).getMonitorThread()
         .addObjectToSkip(object);
        ((WorldPanel) (this.getParent().getParent().getParent())).getMonitorThread()
         .removeAsso(object);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    public AbstractDataObject getAbstractDataObject() {
        return nodeObject;
    }

    protected ActiveObjectPanel getActiveObjectPanel(ActiveObject activeObject) {
        return (ActiveObjectPanel) getChild(activeObject);
    }

    protected Object[][] getDataObjectInfo() {
        return new Object[][] {
            { "Url", name },
            { "Active objects", new Integer(nodeObject.getChildObjectsCount()) }
        };
    }

    protected void setFontSize(java.awt.Font font) {
        super.setFontSize(font);
        currentTitledBorder.setTitleFont(font);
        showBorder(STANDARD_LINE_BORDER);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void doCreateObject() {
        Object result = javax.swing.JOptionPane.showInputDialog(parentFrame, // Component parentComponent,
                "Please enter the classname of the object to create on node " +
                nodeObject.getURL(), // Object message,
                "Creating a new ActiveObject", // String title,
                javax.swing.JOptionPane.PLAIN_MESSAGE // int messageType,
            );
        if ((result == null) || (!(result instanceof String))) {
            return;
        }
        nodeObject.createNewRemoteObject((String) result);
    }

    private void showBorder(javax.swing.border.Border b) {
        currentTitledBorder.setBorder(b);
        setBorder(currentBorder);
        repaint();
    }

    private void invokeMigrateTo(final ActiveObject activeObject,
        final String destURL) {
        new Thread(new Runnable() {
                public void run() {
                    boolean b = activeObject.migrateTo(destURL);
                    if (!b) {
                        ActiveObjectPanel activeObjectPanel = getTopLevelParent()
                                                                  .findActiveObjectPanelByActiveObject(activeObject);
                        if (activeObjectPanel != null) {
                            activeObjectPanel.cancelMigration();
                        }
                    }
                }
            }).start();
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //    
    private class MyDropTargetListener extends UniqueIDDropTargetListener {

        /**
         * processes the drop and return false if the drop is rejected or true if the drop is accepted.
         * The method must not call the rejectDrop as returning false signel that the drop is rejected.
         * On the other hand it is the responsability of this method to call the acceptDrop and dropComplete
         * when accepting the drop and returning true
         */
        protected boolean processDrop(java.awt.dnd.DropTargetDropEvent event,
            UniqueID uniqueID) {
            // check if not the same node
            if (nodeObject.getActiveObject(uniqueID) != null) {
                controller.warn(
                    "The active object originates from the same VM you're trying to migrate it to !");
                return false;
            }
            ActiveObject activeObject = nodeObject.getTopLevelParent()
                                                  .findActiveObjectById(uniqueID);
            if (activeObject == null) {
                controller.warn("Cannot find the ActiveObject id=" + uniqueID +
                    ". It must have been removed.");
                return false;
            }
            if (event.getDropAction() == java.awt.dnd.DnDConstants.ACTION_MOVE) {
                event.acceptDrop(java.awt.dnd.DnDConstants.ACTION_MOVE);
                // do migration
                controller.log("Received object " + activeObject.getName() +
                    " to move to " + nodeObject.getURL());
                invokeMigrateTo(activeObject, nodeObject.getURL());

                //success = activeObject.migrateTo(nodeObject.getURL());
            } else if (event.getDropAction() == java.awt.dnd.DnDConstants.ACTION_COPY) {
                event.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                // do migration clone
                controller.log("Received object " + activeObject.getName() +
                    " to copy to " + nodeObject.getURL());
                invokeMigrateTo(activeObject, nodeObject.getURL());
            }
            showBorder(STANDARD_LINE_BORDER);
            event.dropComplete(true);
            return true;
        }

        /**
         * Displays a user feed back to show that the drag is going on
         */
        protected void showDragFeedBack() {
            showBorder(ACCEPT_LINE_BORDER);
        }

        /**
         * Displays a user feed back to show that the drop is going on
         */
        protected void showDropFeedBack() {
            showBorder(ACCEPTED_LINE_BORDER);
        }

        /**
         * Removes the user feed back that shows the drag
         */
        protected void hideDnDFeedBack() {
            showBorder(STANDARD_LINE_BORDER);
        }
    } // end inner class MyDropTargetListener
}
