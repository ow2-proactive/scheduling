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
package org.ow2.proactive.resourcemanager.utils;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import org.apache.log4j.Logger;


/**
 * @author ActiveEon Team
 * @since 06/12/19
 */
public class ContextClassLoaderInjector {
    private static final Logger logger = Logger.getLogger(ContextClassLoaderInjector.class);

    private static ProxyFactory proxyFactory = new ProxyFactory();

    /**
     * Create an injected object which has been injected with specifying the thread context class loader during all its methods invocation.
     *
     * @param superClass The class whose object methods need to be injected
     * @param contextClassLoader the thread context class loader to inject in all the methods of the class object
     * @return the created injected object
     */
    public static Object createInjectedObject(Class<?> superClass, ClassLoader contextClassLoader) throws Exception {
        proxyFactory.setSuperclass(superClass);
        // The thread context class loader also needs to be sepecified during the object instantiation.
        Object object = switchContextClassLoader(contextClassLoader, () -> {
            Class<?> proxyClass = proxyFactory.createClass();
            return proxyClass.newInstance();
        });

        MethodHandler injectClassLoaderHandler = (self, method, proceed, args) -> {
            logger.debug("Delegating method: " + self.getClass().getSimpleName() + "." + method.getName());
            // inject setting of thread context classloader during execution of all the object original methods
            return switchContextClassLoader(contextClassLoader, () -> proceed.invoke(self, args));
        };
        ((Proxy) object).setHandler(injectClassLoaderHandler);

        return object;
    }

    /**
     * Switch the thread context class loader during the execution of the function
     * @param contextClassLoader the specified thread context class loader
     * @param func the function which needs to be set thread context class loader
     * @param <T> return type of function
     * @return the function return value
     * @throws Exception the exception thrown during function execution
     */
    public static <T> T switchContextClassLoader(ClassLoader contextClassLoader, SupplierWithException<T> func)
            throws Exception {
        // Remember original thread context classloader
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // context class loader should also be specified during creating and initializing the class object
            Thread.currentThread().setContextClassLoader(contextClassLoader);

            return func.get();

        } finally {
            // Replace the original classloader on the way out
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    /**
     * supplier which can throw exceptions
     * @param <T> supplier return type
     */
    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
