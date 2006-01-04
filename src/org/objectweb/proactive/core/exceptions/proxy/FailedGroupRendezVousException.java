/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.exceptions.proxy;

import java.util.Iterator;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.group.ExceptionInGroup;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.ProxyForGroup;


class AutomaticPurgeGroup implements NFEListener {
    public boolean handleNFE(NonFunctionalException nfe) {
        Iterator exceptions;
        ProxyForGroup group;
        ExceptionListException exceptionList;

        try {
            FailedGroupRendezVousException fgrve = (FailedGroupRendezVousException) nfe;
            exceptionList = (ExceptionListException) fgrve.getCause();
            group = fgrve.getGroup();
        } catch (ClassCastException cce) {
            return false;
        }

        synchronized (exceptionList) {
            exceptions = exceptionList.iterator();

            while (exceptions.hasNext()) {
                ExceptionInGroup eig = (ExceptionInGroup) exceptions.next();
                group.remove(eig.getObject());
            }
        }

        return true;
    }
}


public class FailedGroupRendezVousException extends ProxyNonFunctionalException {
    public static final AutomaticPurgeGroup AUTO_GROUP_PURGE = new AutomaticPurgeGroup();
    private ProxyForGroup group;

    public FailedGroupRendezVousException(String message,
        ExceptionListException e, ProxyForGroup group) {
        super(message, e);
        this.group = group;
    }

    public ProxyForGroup getGroup() {
        return group;
    }
}
