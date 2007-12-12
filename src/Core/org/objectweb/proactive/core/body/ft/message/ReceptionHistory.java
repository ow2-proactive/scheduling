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
package org.objectweb.proactive.core.body.ft.message;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.core.UniqueID;


/**
 * This class represent an ordered list of reception events.
 * @author cdelbe
 * @since 3.0
 */
public class ReceptionHistory implements Serializable {

    /**
         *
         */

    // the elements of the history
    private List<UniqueID> elements;

    // the historized index of the last element
    private long lastCommited;

    // the historized index of the first element
    private long base;

    // the last usable elements : the list elements can be longer that needed
    // if this histo has been updated but not commited
    private long lastRecoverable;

    /**
     * Create a new reception history
     */
    public ReceptionHistory() {
        this.elements = new Vector<UniqueID>();
        this.lastCommited = -1;
        this.lastRecoverable = -1;
        this.base = 0;
    }

    /**
     * Update this history up to last;
     * @param hu the history updater that must be used.
     */
    public void updateHistory(HistoryUpdater hu) {
        long toAddBase = hu.base;
        long toAddLast = hu.last;
        List<UniqueID> toAdd = hu.elements;

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
            Iterator<UniqueID> it = toAdd.iterator();

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
     * Then the base of the history (i.e. the first usefull element) is nextBase.
     * @param nextBase the new base of history.
     */
    public void goToNextBase(long nextBase) {
        if (nextBase < this.base) {
            // TODO: nextbase could be less than base ?
            return;
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

    /**
     * Called when an update is confirmed. In this case, the commited history becomes
     * the recoverable history, i.e an history that can be used for recovery.
     */
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
     * Called only on recovery of the system. If recoverable history is different from
     * stored history, stored history is replaced by recoverable history.
     * @return the recoverable history;
     */
    public List<UniqueID> getRecoverableHistory() {
        if (this.lastCommited == this.lastRecoverable) {
            return this.elements;
        } else {
            Vector<UniqueID> toRet = new Vector<UniqueID>();
            for (int i = 0; i <= (this.lastRecoverable - this.base); i++) {
                toRet.add(this.elements.get(i));
            }
            this.elements = toRet;
            return toRet;
        }
    }

    /**
     * Called to delete unusefull elements in this history.
     */
    public void compactHistory() {
        if (this.lastCommited > this.lastRecoverable) {
            this.elements.subList((int) ((this.lastRecoverable + 1) -
                this.base), (int) ((this.lastCommited + 1) - this.base));
            this.lastCommited = this.lastRecoverable;
        }
    }
}
