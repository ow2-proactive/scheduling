/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.HeaderParam;

import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;

import com.google.common.collect.ImmutableSet;


public class SchedulerStateRestSecurityTest {

    private final SchedulerRestInterface restInterface = new SchedulerStateRest();

    private final Set<String> EXCLUDED_METHODS = ImmutableSet.of("loginOrRenewSession");

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
                    if (!exception.getCause().getClass().equals(NotConnectedRestException.class) &&
                        !exception.getCause().getCause().getClass().equals(NotConnectedRestException.class)) {
                        fail(method + " should throw a NotConnectedException");
                    }
                }
            }
        }
    }

    private boolean methodShouldBeSecured(Method method) {
        if (EXCLUDED_METHODS.contains(method.getName())) {
            return false;
        }

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
