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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.RestTestServer;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStoreTestUtils;

import com.google.common.collect.ImmutableSet;


/**
 * The purpose of the test is to check that each method from {@link
 * SchedulerRestInterface} invokes {@link SchedulerStateRest#renewLeaseForClient(Scheduler)}
 * if it tries to perform an action once logged.
 * <p/>
 * To this aim, the scheduler is mocked and the {@link SchedulerStateRest}
 * instance is spied thanks to Mockito. One test is generated per method to test
 * in {@link SchedulerRestInterface} by using Junit parameterized tests.
 */
@RunWith(Parameterized.class)
public class SchedulerStateRestRenewLeaseForClientTest extends RestTestServer {

    private static final String METHOD_NAME_THAT_MUST_BE_INVOKED = "renewLeaseForClient";

    private final Map<Class<?>, Object> defaultParameterValues;

    private SchedulerStateRest schedulerStateRest;

    private SchedulerProxyUserInterface scheduler;

    private Method methodToTest;

    public SchedulerStateRestRenewLeaseForClientTest(String testName, Method methodToTest) {
        this.defaultParameterValues = new HashMap<>();
        this.methodToTest = methodToTest;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() throws NoSuchMethodException, IllegalAccessException {
        Set<String> methodsToIgnore = ImmutableSet.of("getCreateCredential",
                                                      "getLoginFromSessionId",
                                                      "login",
                                                      "loginOrRenewSession",
                                                      "loginWithCredential",
                                                      "validate");

        Method[] methodsToTest = SchedulerRestInterface.class.getMethods();
        Object[][] data = new Object[methodsToTest.length][2];

        int nbMethodsToIgnore = 0;

        for (int i = 0; i < methodsToTest.length; i++) {
            Method methodToTest = methodsToTest[i];

            if (methodsToIgnore.contains(methodToTest.getName())) {
                nbMethodsToIgnore++;
                continue;
            }

            data[i - nbMethodsToIgnore][0] = "test" + WordUtils.capitalize(methodToTest.getName());
            data[i - nbMethodsToIgnore][1] = methodToTest;
        }

        if (nbMethodsToIgnore > 0) {
            data = Arrays.copyOfRange(data, 0, data.length - nbMethodsToIgnore);
        }

        return Arrays.asList(data);
    }

    @Before
    public void setUp() throws Exception {
        schedulerStateRest = spy(new SchedulerStateRest());
        scheduler = mock(SchedulerProxyUserInterface.class);

        String sessionId = SharedSessionStoreTestUtils.createValidSession(scheduler);

        defaultParameterValues.put(Character.class, '\0');
        defaultParameterValues.put(Integer.class, 0);
        defaultParameterValues.put(Long.class, 0L);
        defaultParameterValues.put(String.class, sessionId);
        defaultParameterValues.put(boolean.class, false);
        defaultParameterValues.put(char.class, '\0');
        defaultParameterValues.put(int.class, 0);
        defaultParameterValues.put(long.class, 0L);
        defaultParameterValues.put(scheduler.getClass(), scheduler);
    }

    @Test
    public void test() throws Throwable {
        Method methodThatMustBeInvoked = SchedulerStateRest.class.getDeclaredMethod(METHOD_NAME_THAT_MUST_BE_INVOKED,
                                                                                    Scheduler.class);

        try {
            methodToTest.invoke(schedulerStateRest, createMethodParameters(methodToTest));
        } catch (InvocationTargetException e) {
            // Ignore exceptions since the scheduler is mocked and dummy
            // parameters are passed to methods which are invoked.
            // However, renewLeaseForClient should have been invoked.
        }

        methodThatMustBeInvoked.invoke(verify(schedulerStateRest, times(1)), scheduler);
    }

    private Object[] createMethodParameters(Method method) throws IllegalAccessException {
        Object[] params = new Object[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < method.getParameterTypes().length; i++) {
            params[i] = defaultParameterValues.get(parameterTypes[i]);
        }

        return params;
    }

}
