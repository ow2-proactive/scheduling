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
package org.objectweb.proactive.core.component.adl;

import java.util.HashMap;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;


/**
 * A ProActive factory customizing the Fractal ADL.
 *
 * @author Matthieu Morel
 */
public class FactoryFactory {
    public final static String PROACTIVE_BACKEND = "org.objectweb.proactive.core.component.adl.ProActiveBackend";
    public final static String PROACTIVE_FACTORY = "org.objectweb.proactive.core.component.adl.ProActiveFactory";
    public final static String PROACTIVE_NFBACKEND = "org.objectweb.proactive.core.component.adl.ProActiveNFBackend";
    public final static String PROACTIVE_NFFACTORY = "org.objectweb.proactive.core.component.adl.ProActiveNFFactory";

    private FactoryFactory() {
    }

    /**
     * Returns a factory for the ProActive ADL.
     *
     * @see org.objectweb.fractal.adl.FactoryFactory#getFactory(java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    public static Factory getFactory() throws ADLException {
        return org.objectweb.fractal.adl.FactoryFactory.getFactory(PROACTIVE_FACTORY, PROACTIVE_BACKEND,
                new HashMap());
    }

    /**
     * Returns a factory for the ProActive ADL.
     *
     * @see org.objectweb.fractal.adl.FactoryFactory#getFactory(java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    public static Factory getNFFactory() throws ADLException {
        return org.objectweb.fractal.adl.FactoryFactory.getFactory(PROACTIVE_NFFACTORY, PROACTIVE_NFBACKEND,
                new HashMap());
    }
}
