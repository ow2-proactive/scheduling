/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HeaderParam;

import org.junit.Test;

import static org.junit.Assert.fail;


public class SchedulerStateRestSecurityTest {

    private final SchedulerRestInterface restInterface = new SchedulerStateRest();

    // call all interface's methods using reflection
    @Test
    public void testAllMethodsAreSecured() throws Exception {
        for (Method method : SchedulerRestInterface.class.getMethods()) {
            if (methodShouldBeSecured(method)) {
                Object[] params = createMethodParameters(method);
                try {
                    // by default we are not connected, any call should fail
                    method.invoke(restInterface, params);
                    fail(method + " should throw a NotConnectedException");
                } catch (InvocationTargetException exception) {
                    if (!exception.getCause().getClass().equals(NotConnectedRestException.class)) {
                        fail(method + " should throw a NotConnectedException");
                    }
                }
            }
        }
    }

    private boolean methodShouldBeSecured(Method method) {
        if (method.getParameterTypes().length > 0) {
            Class<?> sessionIdParameterType = method.getParameterTypes()[0];
            if (method.getParameterAnnotations()[0].length > 0) {
                Annotation sessionIdAnnotation = method.getParameterAnnotations()[0][0];
                if (parameterIsSessionId(sessionIdParameterType, sessionIdAnnotation)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean parameterIsSessionId(Class<?> sessionIdParameterType, Annotation sessionIdAnnotation) {
        return sessionIdParameterType.equals(String.class) &&
            sessionIdAnnotation.annotationType().equals(HeaderParam.class) &&
            ((HeaderParam) sessionIdAnnotation).value().equals("sessionid");
    }

    private Object[] createMethodParameters(Method method) throws IllegalAccessException {
        Object[] params = new Object[method.getParameterTypes().length];
        int i = 0;
        for (Class<?> parameterTypes : method.getParameterTypes()) {
            try {
                params[i] = parameterTypes.newInstance();
            } catch (InstantiationException e) {
                params[i] = defaultValues.get(parameterTypes);
            }

            i++;
        }
        return params;
    }

    private final static Map<Class<?>, Object> defaultValues = new HashMap<>();

    static {
        defaultValues.put(String.class, "");
        defaultValues.put(Integer.class, 0);
        defaultValues.put(int.class, 0);
        defaultValues.put(Long.class, 0L);
        defaultValues.put(long.class, 0L);
        defaultValues.put(Character.class, '\0');
        defaultValues.put(char.class, '\0');
        defaultValues.put(boolean.class, false);
    }

}
