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
package org.objectweb.proactive.core.body.jini;

import java.rmi.RemoteException;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.rmi.RmiRemoteBody;
import org.objectweb.proactive.core.body.rmi.RmiRemoteBodyImpl;
import org.objectweb.proactive.core.rmi.RandomPortSocketFactory;


/**
 *   An adapter for a LocalBody to be able to receive jini calls. This helps isolate JINI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe jini objects library.
 */
public class JiniRemoteBodyImpl extends RmiRemoteBodyImpl
    implements RmiRemoteBody, java.rmi.server.Unreferenced {

    /**
     * A custom socket Factory
     */
    protected static RandomPortSocketFactory factory = new RandomPortSocketFactory(37002,
            5000);

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public JiniRemoteBodyImpl() throws RemoteException {
    }

    public JiniRemoteBodyImpl(UniversalBody body) throws RemoteException {
        // super(0, factory, factory);
        this.body = body;
    }

    public void unreferenced() {
        bodyLogger.info("JiniRemoteBodyImpl: unreferenced()");
        // System.gc();
    }
}
