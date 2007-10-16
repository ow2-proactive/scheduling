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
package org.objectweb.proactive.core.body.ibis;

import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;


/**
 * <p>
 * This class provides singleton instances of all default factories
 * creating MetaObjects used in the Body.
 * </p>
 * <p>
 * One can inherit from this class in order to provide custom implementation
 * of one or several factories. This class provide a default implementation that
 * makes the factories a singleton. One instance of each mata object factory is
 * created when this object is built and the same instance is returned each time
 * somebody ask for an instance.
 * </p>
 * <p>
 * In order to change one meta object factory following that singleton pattern,
 * only the protected method <code>newXXXSingleton</code> has to be overwritten.
 * The method <code>newXXXSingleton</code> is guarantee to be called only once at
 * construction time of this object.
 * </p>
 * <p>
 * In order to change one meta object factory that does not follow the singleton
 * pattern, the public method <code>newXXX</code> has to be overwritten in order
 * to return a new instance of the factory each time. The default implementation
 * of each <code>newXXX</code> method if to return the singleton instance of the
 * factory created from <code>newXXXSingleton</code> method call.
 * </p>
 * <p>
 * Each sub class of this class should be implemented as a singleton and provide
 * a static method <code>newInstance</code> for this purpose.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
public class ProActiveIbisMetaObjectFactory extends ProActiveMetaObjectFactory
    implements java.io.Serializable {
    //static {
    //IbisProperties.load();
    //}
    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    //    @Override
    //    protected RemoteBodyFactory newRemoteBodyFactorySingleton() {
    //        return new RemoteIbisBodyFactoryImpl();
    //    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    //    protected static class RemoteIbisBodyFactoryImpl
    //        implements RemoteBodyFactory, java.io.Serializable {
    //        public UniversalBody newRemoteBody(UniversalBody body) {
    //            try {
    //                // 	System.out.println("Creating ibis remote body adapter");
    //                return new IbisBodyAdapter(body);
    //            } catch (ProActiveException e) {
    //                throw new ProActiveRuntimeException("Cannot create Ibis Remote body adapter ",
    //                    e);
    //            }
    //        }
    //    } // end inner class RemoteBodyFactoryImpl
}
