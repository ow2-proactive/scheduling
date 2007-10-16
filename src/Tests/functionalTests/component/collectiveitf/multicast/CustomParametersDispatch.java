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
package functionalTests.component.collectiveitf.multicast;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatch;


public class CustomParametersDispatch implements ParamDispatch {

    /*
       * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#dispatch(java.lang.Object, int)
       */
    public List<Object> dispatch(Object inputParameter, int nbOutputReceivers)
        throws ParameterDispatchException {
        if (!(inputParameter instanceof List) ||
                !(((List) inputParameter).size() >= 1) ||
                !(((List) inputParameter).get(0) instanceof WrappedInteger)) {
            throw new ParameterDispatchException(
                "needs a List of (at least 1) WrappedInteger elements");
        }

        List<Object> result = new ArrayList<Object>();
        result.add((WrappedInteger) ((List) inputParameter).get(0));
        return result;
    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#expectedDispatchSize(java.lang.Object, int)
     */
    public int expectedDispatchSize(Object inputParameter, int nbOutputReceivers)
        throws ParameterDispatchException {
        return 1;
    }

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.collective.ParamDispatch#match(java.lang.reflect.Type, java.lang.reflect.Type)
     */
    public boolean match(Type clientSideInputParameterType,
        Type serverSideInputParameterType) throws ParameterDispatchException {
        try {
            boolean one = ((Class<?>) ((ParameterizedType) clientSideInputParameterType).getRawType()).equals(List.class);
            boolean two = ((Class<?>) ((ParameterizedType) clientSideInputParameterType).getActualTypeArguments()[0]).equals(WrappedInteger.class);
            boolean three = ((Class<?>) serverSideInputParameterType).equals(WrappedInteger.class);
            return one && two && three;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
