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
package org.objectweb.proactive.core;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * UniqueID is a unique object identifier across all jvm. It is made of a unique VMID combined
 * with a unique UID on that VM.
 * </p><p>
 * The UniqueID is used to identify object globally, even in case of migration.
 * </p>
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class UniqueID implements java.io.Serializable, Comparable {
    private java.rmi.server.UID id;
    private java.rmi.dgc.VMID vmID;

    //the Unique ID of the JVM
    private static java.rmi.dgc.VMID uniqueVMID = new java.rmi.dgc.VMID();
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);

    // Optim
    private final String cachedShortString;
    private final String cachedCanonString;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new UniqueID
     */
    public UniqueID() {
        this.id = new java.rmi.server.UID();
        this.vmID = uniqueVMID;
        this.cachedCanonString = "" + id + " " + vmID;
        this.cachedShortString = "" +
            Math.abs(cachedCanonString.hashCode() % 65536);
    }

    //
    // -- PUBLIC STATIC METHODS -----------------------------------------------
    //

    /**
     * Returns the VMID of the current VM in which this class has been loaded.
     * @return the VMID of the current VM
     */
    public static java.rmi.dgc.VMID getCurrentVMID() {
        return uniqueVMID;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Returns the VMID of this UniqueID. Note that the VMID of one UniqueID may differ
     * from the local VMID (that one can get using <code>getCurrentVMID()</code> in case
     * this UniqueID is attached to an object that has migrated.
     * @return the VMID part of this UniqueID
     */
    public java.rmi.dgc.VMID getVMID() {
        return vmID;
    }

    /**
     * Returns the UID part of this UniqueID.
     * @return the UID part of this UniqueID
     */
    public java.rmi.server.UID getUID() {
        return id;
    }

    /**
     * Returns a string representation of this UniqueID.
     * @return a string representation of this UniqueID
     */
    @Override
    public String toString() {
        if (logger.isDebugEnabled()) {
            return "<" +
            vmID.toString()
                .substring(vmID.toString().length() - 9,
                vmID.toString().length() - 6) + ">";
        } else {
            return getCanonString();
        }
    }

    public String shortString() {
        return this.cachedShortString;
    }

    public String getCanonString() {
        return this.cachedCanonString;
    }

    public int compareTo(Object o) {
        UniqueID u = (UniqueID) o;
        return getCanonString().compareTo(u.getCanonString());
    }

    /**
     * Overrides hashCode to take into account the two part of this UniqueID.
     * @return the hashcode of this object
     */
    @Override
    public int hashCode() {
        return id.hashCode() + vmID.hashCode();
    }

    /**
     * Overrides equals to take into account the two part of this UniqueID.
     * @return the true if and only if o is an UniqueID equals to this UniqueID
     */
    @Override
    public boolean equals(Object o) {
        //System.out.println("Now checking for equality");
        if (o instanceof UniqueID) {
            return ((id.equals(((UniqueID) o).id)) &&
            (vmID.equals(((UniqueID) o).vmID)));
        } else {
            return false;
        }
    }

    /**
     * for debug purpose
     */
    public void echo() {
        logger.info("UniqueID The Id is " + id + " and the address is " + vmID);
    }
}
