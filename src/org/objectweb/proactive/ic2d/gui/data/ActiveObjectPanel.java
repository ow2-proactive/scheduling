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

import org.objectweb.proactive.ic2d.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.data.ActiveObject;
import org.objectweb.proactive.ic2d.event.ActiveObjectListener;


public class ActiveObjectPanel extends AbstractDataObjectPanel
    implements ActiveObjectListener {
    private static final String GENERIC_TOOL_TIP_TEXT = "Drag to migrate";
    private static final String WAITING_REQUEST_TOOL_TIP_TEXT = GENERIC_TOOL_TIP_TEXT +
        " -- WaitingForRequest";
    private static final String INACTIVE_TOOL_TIP_TEXT = "Cannot Migrate -- Inactive";
    private static final String SERVING_REQUEST_TOOL_TIP_TEXT = GENERIC_TOOL_TIP_TEXT +
        " -- ServingRequest";
    private static final String WAITING_BY_NECESSITY_TOOL_TIP_TEXT = GENERIC_TOOL_TIP_TEXT +
        " -- WaitByNecessity";
    private static final String ACTIVE_TOOL_TIP_TEXT = GENERIC_TOOL_TIP_TEXT +
        " -- Active, not serving request";
    public static final java.awt.Color COLOR_WHEN_ACTIVE = new java.awt.Color(180,
            255, 180);
    public static final java.awt.Color COLOR_WHEN_WAITING_BY_NECESSITY = new java.awt.Color(255,
            205, 110);
    public static final java.awt.Color COLOR_WHEN_SERVING_REQUEST = java.awt.Color.white;
    public static final java.awt.Color COLOR_WHEN_WAITING_REQUEST = new java.awt.Color(225,
            225, 225);
    public static final java.awt.Color COLOR_WHEN_MIGRATING = java.awt.Color.red;
    public static final java.awt.Color COLOR_REQUEST_SINGLE = java.awt.Color.green;
    public static final java.awt.Color COLOR_REQUEST_SEVERAL = java.awt.Color.red;
    public static final java.awt.Color COLOR_REQUEST_MANY = new java.awt.Color(150,
            0, 255);
    public static final int SHOWN_REQUEST_QUEUE_LENGTH = 5;
    public static final int NUMBER_OF_REQUESTS_FOR_SEVERAL = 5;
    public static final int NUMBER_OF_REQUESTS_FOR_MANY = 50;
    private ActiveObject activeObject;
    private javax.swing.JLabel nameLabel;

    /**
     * enables this component to be a Drag Source
     */
    private java.awt.dnd.DragSource dragSource;
    private java.awt.dnd.DragSourceListener dragSourceListener;
    private boolean isGhost;

    //
    // -- CONTRUCTORS -----------------------------------------------
    //
    public ActiveObjectPanel(AbstractDataObjectPanel parentDataObjectPanel,
        ActiveObject targetActiveObject) {
        super(parentDataObjectPanel, targetActiveObject.getClassName(),
            "ActiveObject");
        activeObject = targetActiveObject;
        setBackground(COLOR_WHEN_WAITING_REQUEST);
        setOpaque(false);
        nameLabel = new javax.swing.JLabel(activeObject.getName());
        setFontSize(defaultFont);
        add(nameLabel);

        // dnd stuff
        dragSource = java.awt.dnd.DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
            java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE,
            new MyDragGestureListener());
        dragSourceListener = new MyDragSourceListener();

        // Popup menu
        PanelPopupMenu popup = new PanelPopupMenu("Object " + name);
        popup.add(new javax.swing.AbstractAction("Filter class " + name, null) {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    boolean b = activeObjectFilter.addClass(activeObject.getClassName());
                    if (b) {
                        filterChangeParentNotification(activeObject.getClassName());
                    }
                }
            });
        addMouseListener(popup.getMenuMouseListener());
        setVisibleFromFilter();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public ActiveObject getActiveObject() {
        return activeObject;
    }

    /** Redraw the component */
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        java.awt.Color old = g.getColor();
        if (isGhost) {
            g.setColor(COLOR_WHEN_MIGRATING);
        } else {
            g.setColor(getBackground());
        }

        // 	g.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
        g.fillOval(0, 0, getWidth(), getHeight());

        // paint request queue information    
        int length = activeObject.getRequestQueueLength();
        int numSingle;
        int numSeveral;
        int numMany;
        numSingle = Math.min(length, SHOWN_REQUEST_QUEUE_LENGTH);
        length -= numSingle;
        numSeveral = Math.min((int) Math.ceil(
                    length / (double) NUMBER_OF_REQUESTS_FOR_SEVERAL),
                SHOWN_REQUEST_QUEUE_LENGTH);
        length -= (numSeveral * NUMBER_OF_REQUESTS_FOR_SEVERAL);
        numMany = (int) Math.ceil(length / (double) NUMBER_OF_REQUESTS_FOR_MANY);
        if (numSingle > 0) {
            int requestQueueX = (getWidth() - (6 * numSingle)) / 2;
            int requestQueueY = 2;
            g.setColor(COLOR_REQUEST_SINGLE);
            for (int i = 0; i < numSingle; i++)
                g.fillRect(requestQueueX + (i * 6), requestQueueY, 4, 4);
        }
        if (numSeveral > 0) {
            int requestQueueX = (getWidth() - (6 * (numSeveral + numMany))) / 2;
            int requestQueueY = getHeight() - 6;
            g.setColor(COLOR_REQUEST_SEVERAL);
            for (int i = 0; i < numSeveral; i++)
                g.fillRect(requestQueueX + (i * 6), requestQueueY, 4, 4);
        }
        if (numMany > 0) {
            int requestQueueX = ((getWidth() - (6 * (numSeveral + numMany))) / 2) +
                (6 * numSeveral);
            int requestQueueY = getHeight() - 6;
            g.setColor(COLOR_REQUEST_MANY);
            for (int i = 0; i < numMany; i++)
                g.fillRect(requestQueueX + (i * 6), requestQueueY, 4, 4);
        }

        g.setColor(old);
        paintChildren(g);
    }

    //
    // -- implements ActiveObjectListener -----------------------------------------------
    //
    public void servingStatusChanged(int v) {
        if (v == ActiveObject.STATUS_SERVING_REQUEST) {
            // busy
            setBackground(COLOR_WHEN_SERVING_REQUEST);
            setToolTipText(SERVING_REQUEST_TOOL_TIP_TEXT);
        } else if ((v == ActiveObject.STATUS_WAITING_BY_NECESSITY_WHILE_ACTIVE) ||
                (v == ActiveObject.STATUS_WAITING_BY_NECESSITY_WHILE_SERVING)) {
            // waiting by necessity
            setBackground(COLOR_WHEN_WAITING_BY_NECESSITY);
            setToolTipText(WAITING_BY_NECESSITY_TOOL_TIP_TEXT);
        } else if (v == ActiveObject.STATUS_WAITING_FOR_REQUEST) {
            // waiting for request
            setBackground(COLOR_WHEN_WAITING_REQUEST);
            setToolTipText(WAITING_REQUEST_TOOL_TIP_TEXT);
        } else {
            // active
            setBackground(COLOR_WHEN_ACTIVE);
            setToolTipText(ACTIVE_TOOL_TIP_TEXT);
        }
        repaint();
    }

    public void requestQueueLengthChanged(int value) {
        repaint();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void cancelMigration() {
        isGhost = false;
        repaint();
    }

    protected AbstractDataObject getAbstractDataObject() {
        return activeObject;
    }

    protected void activeObjectAddedToFilter() {
        setVisibleFromFilter();
    }

    protected void activeObjectRemovedFromFilter() {
        setVisibleFromFilter();
    }

    protected void setFontSize(java.awt.Font font) {
        super.setFontSize(font);
        nameLabel.setFont(font);
    }

    protected java.awt.Dimension getMinimumSizeInternal() {
        return null;
    }

    protected Object[][] getDataObjectInfo() {
        return new Object[][] {
            { "Class", name },
            { "ID", activeObject.getID() }
        };
    }

    protected ActiveObjectPanel findActiveObjectPanelByActiveObject(
        ActiveObject activeObject) {
        if (activeObject == this.activeObject) {
            return this;
        } else {
            return null;
        }
    }

    protected void addAllActiveObjectsToWatcher() {
        activeObjectWatcher.addActiveObject(activeObject);
    }

    protected void removeAllActiveObjectsFromWatcher() {
        activeObjectWatcher.removeActiveObject(activeObject);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void setVisibleFromFilter() {
        setVisible(!activeObjectFilter.isClassFiltered(
                activeObject.getClassName()));
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //

    /**
     * a listener that will start the drag.
     * has access to top level's dsListener and dragSource
     * @see java.awt.dnd.DragGestureListener
     * @see java.awt.dnd.DragSource
     * @see java.awt.datatransfer.StringSelection
     */
    private class MyDragGestureListener
        implements java.awt.dnd.DragGestureListener {

        /**
         * Start the drag if the operation is ok.
         * uses java.awt.datatransfer.StringSelection to transfer
         * the label's data
         * @param e the event object
         */
        public void dragGestureRecognized(java.awt.dnd.DragGestureEvent event) {
            // if the action is ok we go ahead otherwise we punt
            if (isGhost) {
                return; // cannot migrate if isGhost or if non active
            }
            if ((event.getDragAction() &
                    java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
                return;
            }

            // get the label's text and put it inside a Transferable
            // Transferable transferable = new StringSelection( DragLabel.this.getText() );
            java.awt.datatransfer.Transferable transferable = new TransferableUniqueID(activeObject.getID());

            // now kick off the drag
            try {
                // initial cursor, transferrable, dsource listener      
                event.startDrag(java.awt.dnd.DragSource.DefaultMoveNoDrop,
                    transferable, dragSourceListener);
            } catch (java.awt.dnd.InvalidDnDOperationException e) {
            }
        }
    }

    /**
     * MyDragSourceListener
     * a listener that will track the state of the DnD operation
     *
     * @see java.awt.dnd.DragSourceListener
     * @see java.awt.dnd.DragSource
     * @see java.awt.datatransfer.StringSelection
     */
    private class MyDragSourceListener
        implements java.awt.dnd.DragSourceListener {

        /**
         * @param e the event
         */
        public void dragDropEnd(java.awt.dnd.DragSourceDropEvent event) {
            if (!event.getDropSuccess()) {
                return;
            }
            if (event.getDropAction() == java.awt.dnd.DnDConstants.ACTION_MOVE) {
                if (controller != null) {
                    controller.log("Object " + activeObject.getName() +
                        " migrated.");
                }
            } else if (event.getDropAction() == java.awt.dnd.DnDConstants.ACTION_COPY) {
                if (controller != null) {
                    controller.log("Object " + activeObject.getName() +
                        " cloned.");
                }
            }
            isGhost = true;
            repaint();
        }

        /**
         * @param e the event
         */
        public void dragEnter(java.awt.dnd.DragSourceDragEvent event) {
            java.awt.dnd.DragSourceContext context = event.getDragSourceContext();

            //intersection of the users selected action, and the source and target actions
            int myaction = event.getDropAction();
            if ((myaction & java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
                context.setCursor(java.awt.dnd.DragSource.DefaultCopyDrop);
            } else {
                context.setCursor(java.awt.dnd.DragSource.DefaultCopyNoDrop);
            }
        }

        /**
         * @param e the event
         */
        public void dragOver(java.awt.dnd.DragSourceDragEvent event) {
        }

        /**
         * @param e the event
         */
        public void dragExit(java.awt.dnd.DragSourceEvent event) {
            java.awt.dnd.DragSourceContext context = event.getDragSourceContext();
            context.setCursor(java.awt.dnd.DragSource.DefaultCopyNoDrop);
        }

        /**
         * for example, press shift during drag to change to
         * a link action
         * @param e the event
         */
        public void dropActionChanged(java.awt.dnd.DragSourceDragEvent event) {
            dragEnter(event);
        }
    } // end inner class MyDraSourceListener
}
