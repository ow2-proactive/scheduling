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
package functionalTests.component;

import java.util.Set;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


/**
 * @author The ProActive Team
 */
public class PrimitiveComponentDbis extends PrimitiveComponentD {
    @Override
    public void bindFc(String clientItfName, Object serverItf) {
        if (clientItfName.startsWith(I2_ITF_NAME)) {
            i2Map.put(clientItfName, (I2) serverItf);
        } else {
            logger.error("Binding impossible : wrong client interface name");
        }
    }

    @Override
    public String[] listFc() {
        Set<String> itf_names = i2Map.keySet();
        return itf_names.toArray(new String[itf_names.size()]);
    }

    @Override
    public void unbindFc(String clientItf) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (i2Map.containsKey(clientItf)) {
            i2Map.remove(clientItf);
        } else {
            logger.error("client interface not found");
        }
    }

    @Override
    public Object lookupFc(String clientItf) throws NoSuchInterfaceException {
        if (i2Map.containsKey(clientItf)) {
            return i2Map.get(clientItf);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("cannot find " + clientItf + " interface");
            }
            return null;
        }
    }
}
