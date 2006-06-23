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


public class TransferableUniqueID implements java.awt.datatransfer.Transferable {
    public static final java.awt.datatransfer.DataFlavor UNIQUEID_FLAVOR = new java.awt.datatransfer.DataFlavor(UniqueID.class,
            "UniqueID");
    private UniqueID id;
    private java.awt.datatransfer.DataFlavor[] supportedFlavors;

    public TransferableUniqueID(UniqueID id) {
        this.id = id;
        supportedFlavors = new java.awt.datatransfer.DataFlavor[] {
                UNIQUEID_FLAVOR
            };
    }

    /**
     * Returns an object which represents the data to be transferred.
     */
    public Object getTransferData(java.awt.datatransfer.DataFlavor flavor) {
        return id;
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data can be provided in.
     */
    public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors;
    }

    /**
     * Returns whether or not the specified data flavor is supported for this object
     */
    public boolean isDataFlavorSupported(
        java.awt.datatransfer.DataFlavor flavor) {
        return UNIQUEID_FLAVOR.equals(flavor);
    }
}
