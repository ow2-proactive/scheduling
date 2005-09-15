/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.component.adl;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;

import java.util.HashMap;


/**
 * A ProActive factory customizing the Fractal ADL.
 *
 * @author Dalmasso Nicolas
 */
public class ADL2NFactoryFactory {
    public final static String ADL2N_BACKEND = "org.objectweb.proactive.core.component.adl.ADL2NBackend";
    public final static String ADL2N_FACTORY = "org.objectweb.proactive.core.component.adl.ADL2NFactory";

    private ADL2NFactoryFactory() {
    }

    /**
     * Returns a factory for the ProActive ADL.
     *
     * @see org.objectweb.fractal.adl.FactoryFactory#getFactory(java.lang.String,
     *      java.lang.String, java.util.Map)
     */
    public static Factory getFactory() throws ADLException {
        return org.objectweb.fractal.adl.FactoryFactory.getFactory(ADL2N_FACTORY,
        		ADL2N_BACKEND, new HashMap());
    }
}
