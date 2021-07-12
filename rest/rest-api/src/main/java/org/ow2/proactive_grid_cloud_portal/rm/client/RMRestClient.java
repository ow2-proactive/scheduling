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
package org.ow2.proactive_grid_cloud_portal.rm.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ow2.proactive_grid_cloud_portal.common.RMRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ExceptionToJson;


public class RMRestClient {

    private RMRestInterface rm;

    private String restEndpointURL;

    private ResteasyProviderFactory providerFactory;

    private static ClientHttpEngine httpEngine;

    private static SSLContext sslContext;

    public RMRestClient(String restEndpointURL, ClientHttpEngine httpEngine) {

        if (httpEngine == null) {
            setBlindTrustSSLContext();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                                                                                                   NoopHostnameVerifier.INSTANCE);
            HttpClient httpClient = HttpClientBuilder.create().setSSLSocketFactory(sslConnectionSocketFactory).build();
            httpEngine = new ApacheHttpClient4Engine(httpClient);
        }

        RMRestClient.httpEngine = httpEngine;
        this.restEndpointURL = restEndpointURL;

        providerFactory = ResteasyProviderFactory.getInstance();
        if (!providerFactory.isRegistered(RMRestClient.JacksonContextResolver.class)) {
            providerFactory.registerProvider(RMRestClient.JacksonContextResolver.class);
        }

        rm = createRestProxy(providerFactory, restEndpointURL, httpEngine);
    }

    public RMRestInterface getRm() {
        return rm;
    }

    private void setBlindTrustSSLContext() {
        try {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Provider
    @Consumes({ MediaType.APPLICATION_JSON, "text/json" })
    @Produces({ MediaType.APPLICATION_JSON, "text/json" })
    public static class JacksonContextResolver implements ContextResolver<ObjectMapper> {
        @Override
        public ObjectMapper getContext(Class<?> objectType) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper;
        }
    }

    private static RMRestInterface createRestProxy(ResteasyProviderFactory provider, String restEndpointURL,
            ClientHttpEngine httpEngine) {
        ResteasyClient client = buildResteasyClient(provider);
        ResteasyWebTarget target = client.target(restEndpointURL);
        RMRestInterface rmRestInterface = target.proxy(RMRestInterface.class);
        return createExceptionProxy(rmRestInterface);
    }

    private static ResteasyClient buildResteasyClient(ResteasyProviderFactory provider) {
        return new ResteasyClientBuilder().providerFactory(provider).httpEngine(httpEngine).build();
    }

    private static RMRestInterface createExceptionProxy(final RMRestInterface rmRestInterface) {
        return (RMRestInterface) Proxy.newProxyInstance(RMRestInterface.class.getClassLoader(),
                                                        new Class[] { RMRestInterface.class },
                                                        new RMRestClient.RestClientExceptionHandler(rmRestInterface));
    }

    private static class RestClientExceptionHandler implements InvocationHandler {

        private final RMRestInterface rm;

        public RestClientExceptionHandler(RMRestInterface rm) {
            this.rm = rm;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(rm, args);
            } catch (InvocationTargetException targetException) {
                if (targetException.getTargetException() instanceof WebApplicationException) {
                    WebApplicationException clientException = (WebApplicationException) targetException.getTargetException();
                    try {
                        ExceptionToJson json = clientException.getResponse().readEntity(ExceptionToJson.class);
                        // here we take the server side exception and recreate it on the client side
                        throw rebuildServerSideException(json);
                    } catch (ProcessingException couldNotReadJsonException) {
                        // rethrow server side exception as runtime exception but do not transform it
                        throw clientException;
                    } catch (IllegalStateException couldNotReadJsonException) {
                        // rethrow server side exception as runtime exception but do not transform it
                        throw clientException;
                    }
                }
                // rethrow real exception as runtime (client side exception)
                throw new RuntimeException(targetException.getTargetException());
            }
        }

        private static Exception rebuildServerSideException(ExceptionToJson json) throws IllegalArgumentException,
                InstantiationException, IllegalAccessException, InvocationTargetException {
            Throwable serverException = json.getException();
            String exceptionClassName = json.getExceptionClass();
            String errMsg = json.getErrorMessage();
            if (errMsg == null) {
                errMsg = "An error has occurred.";
            }

            if (serverException != null && exceptionClassName != null) {
                Class<?> exceptionClass = toClass(exceptionClassName);
                if (exceptionClass != null) {
                    // wrap the exception serialized in JSON inside an
                    // instance of
                    // the server exception class
                    Constructor<?> constructor = getConstructor(exceptionClass, Throwable.class);
                    if (constructor != null) {
                        return (Exception) constructor.newInstance(serverException);
                    }
                    constructor = getConstructor(exceptionClass, String.class);
                    if (constructor != null) {
                        Exception built = (Exception) constructor.newInstance(errMsg);
                        built.setStackTrace(serverException.getStackTrace());
                        return built;
                    }
                }
            }

            Exception built = new Exception(errMsg);
            if (serverException != null) {
                built.setStackTrace(serverException.getStackTrace());
            }
            return built;
        }

        private static Class<?> toClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
            try {
                return clazz.getConstructor(paramTypes);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
    }
}
