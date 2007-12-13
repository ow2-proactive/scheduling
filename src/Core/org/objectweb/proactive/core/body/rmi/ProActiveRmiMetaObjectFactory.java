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
package org.objectweb.proactive.core.body.rmi;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class ProActiveRmiMetaObjectFactory extends ProActiveMetaObjectFactory implements java.io.Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MOP);

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    //    @Override
    //    protected RemoteBodyFactory newRemoteBodyFactorySingleton() {
    //        return new RemoteRmiBodyFactoryImpl();
    //    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    //    protected static class RemoteRmiBodyFactoryImpl implements RemoteBodyFactory,
    //        java.io.Serializable {
    //        public UniversalBody newRemoteBody(UniversalBody body) {
    //            try {
    //                return new org.objectweb.proactive.core.body.rmi.RmiBodyAdapter(body);
    //            } catch (ProActiveException e) {
    //                throw new ProActiveRuntimeException("Cannot create Remote body adapter ",
    //                    e);
    //            }
    //        }
    //    } // end inner class RemoteBodyFactoryImpl
}
