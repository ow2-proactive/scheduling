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
package org.objectweb.proactive.core.component.gen;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class GatherItfAdapterProxy implements InvocationHandler, Serializable {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_GATHERCAST);
    Object delegatee = null;

    public GatherItfAdapterProxy(Object delegatee) {
        this.delegatee = delegatee;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("gather proxy trying to invoke method : " +
                    method.toGenericString());
            }

            //                Method serverMethod = GatherBindingChecker.searchMatchingMethod(method, delegatee.getClass().getMethods());
            // need to change invocation parameters
            Object result = delegatee.getClass()
                                     .getMethod(method.getName(),
                    method.getParameterTypes()).invoke(delegatee, args);
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
}
