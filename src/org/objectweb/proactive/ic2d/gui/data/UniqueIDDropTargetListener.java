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

import org.objectweb.proactive.core.UniqueID;


/**
 * MyDropTargetListener
 * a listener that tracks the state of the operation
 * @see java.awt.dnd.DropTargetListener
 * @see java.awt.dnd.DropTarget
 */
public abstract class UniqueIDDropTargetListener
    implements java.awt.dnd.DropTargetListener {
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public UniqueIDDropTargetListener() {
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements DropTargetListener -----------------------------------------------
    //

    /**
     * start "drag under" feedback on component
     * invoke acceptDrag or rejectDrag based on isDragOk
     */
    public void dragEnter(java.awt.dnd.DropTargetDragEvent e) {
        //System.out.println("dragEnter");
        if (isDragOk(e)) {
            showDragFeedBack();
            e.acceptDrag(e.getDropAction());
        } else {
            hideDnDFeedBack();
            e.rejectDrag();
        }
    }

    /**
     * continue "drag under" feedback on component
     * invoke acceptDrag or rejectDrag based on isDragOk
     */
    public void dragOver(java.awt.dnd.DropTargetDragEvent e) {
        //System.out.println("dragOver");
        if (isDragOk(e)) {
            e.acceptDrag(e.getDropAction());
        } else {
            e.rejectDrag();
        }
    }

    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent e) {
        //System.out.println("dropActionChanged");
        if (isDragOk(e)) {
            e.acceptDrag(e.getDropAction());
        } else {
            e.rejectDrag();
        }
    }

    public void dragExit(java.awt.dnd.DropTargetEvent e) {
        //System.out.println("dragExit");
        hideDnDFeedBack();
    }

    /**
     * perform action from getSourceActions on the transferrable
     * invoke acceptDrop or rejectDrop, invoke dropComplete
     * get the transferable according to the chosen flavor, do the transfer
     */
    public void drop(java.awt.dnd.DropTargetDropEvent event) {
        showDropFeedBack();
        java.awt.datatransfer.Transferable transferable = event.getTransferable();

        // we accept only UniqueID      
        if (!transferable.isDataFlavorSupported(
                    TransferableUniqueID.UNIQUEID_FLAVOR)) {
            rejectDrop(event);
            return;
        }

        // try to get the ActiveObject
        Object data = null;
        try {
            data = event.getTransferable().getTransferData(TransferableUniqueID.UNIQUEID_FLAVOR);
        } catch (java.io.IOException e) {
            rejectDrop(event);
            return;
        } catch (java.awt.datatransfer.UnsupportedFlavorException e) {
            rejectDrop(event);
            return;
        }
        if ((data == null) || !(data instanceof UniqueID)) {
            rejectDrop(event);
            return;
        }
        UniqueID uniqueID = (UniqueID) data;
        boolean result = processDrop(event, uniqueID);

        // check if not the same node
        if (!result) {
            rejectDrop(event);
            return;
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Processes the drop and return false if the drop is rejected or true if the drop is accepted.
     * The method must not call the rejectDrop as returning false signel that the drop is rejected.
     * On the other hand it is the responsability of this method to call the acceptDrop and dropComplete
     * when accepting the drop and returning true
     */
    protected abstract boolean processDrop(
        java.awt.dnd.DropTargetDropEvent event, UniqueID id);

    /**
     * Displays a user feed back to show that the drag is going on
     */
    protected abstract void showDragFeedBack();

    /**
     * Displays a user feed back to show that the drop is going on
     */
    protected abstract void showDropFeedBack();

    /**
     * Removes the user feed back that shows the drag
     */
    protected abstract void hideDnDFeedBack();

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void rejectDrop(java.awt.dnd.DropTargetDropEvent event) {
        event.rejectDrop();
        hideDnDFeedBack();
    }

    /**
     * Called by dragEnter and dragOver
     * Checks the flavors and operations
     * @param e the event object
     * @return whether the flavor and operation is ok
     */
    private boolean isDragOk(java.awt.dnd.DropTargetDragEvent event) {
        java.awt.datatransfer.DataFlavor[] df = event.getCurrentDataFlavors();
        if (!event.isDataFlavorSupported(TransferableUniqueID.UNIQUEID_FLAVOR)) {
            return false;
        }

        // we're saying that these actions are necessary      
        if ((event.getDropAction() &
                java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
            return false;
        }
        return true;
    }
}
