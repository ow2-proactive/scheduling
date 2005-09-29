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
package org.objectweb.proactive.core.body.ft.message;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.exception.ProtocolErrorException;


/**
 * This class represent an ordered list of reception event.
 * @author cdelbe
 * @since 2.2
 */
public class ReceptionHistory implements Serializable {
    // the elements of the history
    private List elements;

    // the historized index of the last element 
    private long lastCommited;

    // the historized index of the first element
    private long base;

    // the last usable elements : the list elements can be longer that needed
    // if this histo has been updated but not commited
    private long lastRecoverable;

    /**
     * Constructor
     */
    public ReceptionHistory() {
        this.elements = new Vector();
        this.lastCommited = -1;
        this.lastRecoverable = -1;
        this.base = 0;
    }

    /**
     * Update this history up to last;
     * @param base the historized index of the first element of elts
     * @param last the historized index of the last element of elts
     * @param elts the elements to add to the history
     */
    public void updateHistory(HistoryUpdater hu) {
        long toAddBase = hu.base;
        long toAddLast = hu.last;
        List toAdd = hu.elements;

        // if there is a gap between lastCommited and toAddBase, we can
        // suppose that this gap is commited. The current history is then 
        // replaced by toAdd
        if (toAddBase > (this.lastCommited + 1)) {
            // history is not contigue
            this.elements = toAdd;
            this.base = toAddBase;
            this.lastCommited = toAddLast;
        } else if (this.lastCommited < toAddLast) {
            // history is contigue 
            Iterator it = toAdd.iterator();

            // shift in elts up to this.lastCommited+1
            for (long i = toAddBase; i <= this.lastCommited; i++) {
                it.next();
            }

            // add the rest to this.elements
            while (it.hasNext()) {
                this.elements.add(it.next());
            }
            this.lastCommited = toAddLast;
        }
    }

    /**
     * This method is called when elements between base and nextBase are no more
     * usefull : there a included in the state represented by the last recovery line.
     */

    // UNIQUEID FOR DEBUGGING
    public void goToNextBase(UniqueID id, long nextBase) {
        if (nextBase < this.base) {
            throw new ProtocolErrorException(
                "nextBase is lower than current base !");
        }
        int shift = (int) (nextBase - this.base);
        // particular case : shift==histo_size
        // minimal histo is empty for this checkpoint
        if (shift == (this.elements.size() + 1)) {
            this.elements.clear();
            this.base = nextBase;
        } else {
            this.elements.subList(0, shift).clear();
            this.base = nextBase;
        }
    }

    public void confirmLastUpdate() {
        this.lastRecoverable = this.lastCommited;
    }

    public long getLastCommited() {
        return this.lastCommited;
    }

    public long getLastRecoverable() {
        return this.lastRecoverable;
    }

    /**
     * Called only on recovery of the system. If recoverable hisotry is different from
     * stored history, stored history is replaced by recoverable history.
     * @return the recoverable hisotry;
     */
    public List getRecoverableHistory() {
        if (this.lastCommited == this.lastRecoverable) {
            return this.elements;
        } else {
            Vector toRet = new Vector();
            int histoSize = this.elements.size();
            for (int i = 0; i <= (this.lastRecoverable - this.base); i++) {
                toRet.add(this.elements.get(i));
            }

            // DELETE FROM LASTREC TO LASTCOMMITED !!!
            this.elements = toRet;
            return toRet;
        }
    }

    // delete hisotry from LastRec to LastCommited
    public void compactHistory() {
        if (this.lastCommited > this.lastRecoverable) {
            this.elements.subList((int) ((this.lastRecoverable + 1) -
                this.base), (int) ((this.lastCommited + 1) - this.base));
            this.lastCommited = this.lastRecoverable;
        }
    }
}
