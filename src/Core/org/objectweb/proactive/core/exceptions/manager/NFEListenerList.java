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
package org.objectweb.proactive.core.exceptions.manager;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;


public class NFEListenerList implements NFEProducer, Serializable {

    /* The registered handlers */
    private Collection<NFEListener> listeners;

    public NFEListenerList() {
        listeners = new LinkedList<NFEListener>();
    }

    public void addNFEListener(NFEListener listener) {
        listeners.add(listener);
    }

    public void removeNFEListener(NFEListener listener) {
        listeners.remove(listener);
    }

    public int fireNFE(NonFunctionalException e) {
        int nbListeners = 0;

        Iterator<NFEListener> iter = listeners.iterator();
        while (iter.hasNext()) {
            NFEListener listener = iter.next();
            if (listener.handleNFE(e)) {
                nbListeners++;
            }
        }

        return nbListeners;
    }
}
