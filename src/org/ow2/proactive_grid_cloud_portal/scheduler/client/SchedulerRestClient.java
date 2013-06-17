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
import java.util.Collections;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ExceptionToJson;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;


public class SchedulerRestClient {

    private SchedulerRestInterface scheduler;
    private String restEndpointURL;

    public SchedulerRestClient(String restEndpointURL) {
        this.restEndpointURL = restEndpointURL;

        ResteasyProviderFactory provider = ResteasyProviderFactory.getInstance();
        provider.registerProvider(JacksonContextResolver.class);
        scheduler = createRestProxy(provider, restEndpointURL);
    }

    public JobIdData submitXml(String sessionId, InputStream jobXml) throws Exception {
        return submit(sessionId, jobXml, MediaType.APPLICATION_XML_TYPE);
    }

    public JobIdData submitJobArchive(String sessionId, InputStream jobArchive) throws Exception {
        return submit(sessionId, jobArchive, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    private JobIdData submit(String sessionId, InputStream job, MediaType mediaType) throws Exception {
        ClientRequest request = new ClientRequest(restEndpointURL + addSlashIfMissing(restEndpointURL) +
            "scheduler/submit");
        request.header("sessionid", sessionId);
        MultipartFormDataOutput formData = new MultipartFormDataOutput();
        formData.addFormData("file", job, mediaType);
        request.body(MediaType.MULTIPART_FORM_DATA, formData);
        ClientResponse<JobIdData> response = request.post(JobIdData.class);
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new Exception("Job submission failed status code:" + response.getStatus());
        }
        return response.getEntity();
    }

    private String addSlashIfMissing(String url) {
        return url.endsWith("/") ? "" : "/";
    }

    public SchedulerRestInterface getScheduler() {
        return scheduler;
    }

    private static SchedulerRestInterface createRestProxy(ResteasyProviderFactory provider,
            String restEndpointURL) {
        final SchedulerRestInterface schedulerRestClient = ProxyFactory.create(SchedulerRestInterface.class,
                restEndpointURL, provider, Collections.<String, Object> emptyMap());
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

        private Exception rebuildException(ExceptionToJson json) throws ClassNotFoundException,
                NoSuchMethodException, InstantiationException, IllegalAccessException,
                InvocationTargetException {
            Throwable serverException = json.getException();
            String exceptionClassName = json.getExceptionClass();

            // instantiate a new exception with the original server exception class
            Class<?> exception = Class.forName(exceptionClassName);
            Constructor<?> constructor;
            try {
                // wrap the exception serialized in JSON inside an instance of the server exception class
                constructor = exception.getConstructor(Throwable.class);
                return (Exception) constructor.newInstance(serverException);
            } catch (NoSuchMethodException e) {
                // another kind of exception rebuild it without wrapping
                constructor = exception.getConstructor(String.class);
                Exception rebuiltException = (Exception) constructor.newInstance(json.getErrorMessage());
                rebuiltException.setStackTrace(json.getException().getStackTrace());
                return rebuiltException;
            }
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
}
