/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler.client;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ExceptionToJson;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;

public class SchedulerRestClient {

    private SchedulerRestInterface scheduler;
    private String restEndpointURL;
    private ClientExecutor executor;

    public SchedulerRestClient(String restEndpointURL) {
        this(restEndpointURL, null);
    }

    public SchedulerRestClient(String restEndpointURL, ClientExecutor executor) {
        this.restEndpointURL = restEndpointURL;
        this.executor = executor;
        ResteasyProviderFactory provider = ResteasyProviderFactory.getInstance();
        provider.registerProvider(JacksonContextResolver.class);
        scheduler = createRestProxy(provider, restEndpointURL, executor);
    }

    public JobIdData submitXml(String sessionId, InputStream jobXml) throws Exception {
        return submit(sessionId, jobXml, MediaType.APPLICATION_XML_TYPE);
    }

    public JobIdData submitJobArchive(String sessionId, InputStream jobArchive) throws Exception {
        return submit(sessionId, jobArchive, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    public boolean pushFile(String sessionId, String space, String path, String fileName, InputStream fileContent)
            throws Exception {
        String uriTmpl = (new StringBuilder(restEndpointURL)).append(addSlashIfMissing(restEndpointURL))
                .append("scheduler/dataspace/").append(space).append(URLEncoder.encode(path, "UTF-8")).toString();

        ClientRequest request = (executor == null) ? new ClientRequest(uriTmpl) : new ClientRequest(uriTmpl, executor);
        request.header("sessionid", sessionId);

        MultipartFormDataOutput formData = new MultipartFormDataOutput();
        formData.addFormData("fileName", fileName, MediaType.TEXT_PLAIN_TYPE);
        formData.addFormData("fileContent", fileContent, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        request.body(MediaType.MULTIPART_FORM_DATA, formData);

        ClientResponse<Boolean> response = request.post(Boolean.class);
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new NotConnectedRestException("User not authenticated or session timeout.");
            } else {
                throw new Exception(String.format("File upload failed. Status code: %s", response.getStatus()));
            }
        }
        return response.getEntity();
    }

    private JobIdData submit(String sessionId, InputStream job, MediaType mediaType) throws Exception {
        String uriTmpl = restEndpointURL + addSlashIfMissing(restEndpointURL) + "scheduler/submit";
        ClientRequest request = (executor == null) ? new ClientRequest(uriTmpl) : new ClientRequest(uriTmpl, executor);
        request.header("sessionid", sessionId);
        MultipartFormDataOutput formData = new MultipartFormDataOutput();
        formData.addFormData("file", job, mediaType);
        request.body(MediaType.MULTIPART_FORM_DATA, formData);
        ClientResponse<JobIdData> response = request.post(JobIdData.class);
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new NotConnectedRestException("User not authenticated or session timeout.");
            } else {
                throw new Exception("Job submission failed status code:" + response.getStatus());
            }
        }
        return response.getEntity();
    }

    private String addSlashIfMissing(String url) {
        return url.endsWith("/") ? "" : "/";
    }

    public SchedulerRestInterface getScheduler() {
        return scheduler;
    }

    private static SchedulerRestInterface createRestProxy(ResteasyProviderFactory provider, String restEndpointURL,
            ClientExecutor executor) {
        final SchedulerRestInterface schedulerRestClient = (executor == null) ? ProxyFactory.create(
                SchedulerRestInterface.class, restEndpointURL, provider, Collections.<String, Object> emptyMap())
                : ProxyFactory.create(SchedulerRestInterface.class, URI.create(restEndpointURL), executor, provider);
        return createExceptionProxy(schedulerRestClient);
    }

    private static SchedulerRestInterface createExceptionProxy(final SchedulerRestInterface scheduler) {
        return (SchedulerRestInterface) Proxy.newProxyInstance(SchedulerRestInterface.class.getClassLoader(),
                new Class[] { SchedulerRestInterface.class }, new RestClientExceptionHandler(scheduler));
    }

    private static class RestClientExceptionHandler implements InvocationHandler {

        private final SchedulerRestInterface scheduler;

        public RestClientExceptionHandler(SchedulerRestInterface scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(scheduler, args);
            } catch (InvocationTargetException targetException) {
                if (targetException.getTargetException() instanceof ClientResponseFailure) {
                    ExceptionToJson json = (ExceptionToJson) ((ClientResponseFailure) targetException
                            .getTargetException()).getResponse().getEntity(ExceptionToJson.class);

                    throw rebuildException(json);
                }
                throw targetException;
            }
        }

        private Exception rebuildException(ExceptionToJson json) throws IllegalArgumentException,
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
    }
    

    @Provider
    public static class JacksonContextResolver implements ContextResolver<ObjectMapper> {

        public ObjectMapper getContext(Class<?> objectType) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper;
        }
    }
    
    private static Class<?> toClass(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // returns NULL
        }
        return clazz;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
        Constructor<?> ctor = null;
        try {
            ctor = clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            // returns NULL
        }
        return ctor;
    }
}
