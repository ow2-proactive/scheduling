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
package org.ow2.proactive_grid_cloud_portal.scheduler.client;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.exceptionmapper.ExceptionToJson;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.utils.Zipper;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;


public class SchedulerRestClient {

    private SchedulerRestInterface scheduler;

    private String restEndpointURL;

    private ClientHttpEngine httpEngine;

    private ResteasyProviderFactory providerFactory;

    public SchedulerRestClient(String restEndpointURL) {
        this(restEndpointURL, null);
    }

    public SchedulerRestClient(String restEndpointURL, ClientHttpEngine httpEngine) {
        this.restEndpointURL = restEndpointURL;
        this.httpEngine = httpEngine;

        providerFactory = ResteasyProviderFactory.getInstance();
        if (!providerFactory.isRegistered(JacksonContextResolver.class)) {
            providerFactory.registerProvider(JacksonContextResolver.class);
        }

        scheduler = createRestProxy(providerFactory, restEndpointURL, httpEngine);
    }

    public JobIdData submitXml(String sessionId, InputStream jobXml) throws Exception {
        return submitXml(sessionId, jobXml, null);
    }

    public JobIdData submitXml(String sessionId, InputStream jobXml, Map<String, String> variables) throws Exception {
        return submit(sessionId, jobXml, MediaType.APPLICATION_XML_TYPE, variables);
    }

    public JobIdData submitJobArchive(String sessionId, InputStream jobArchive) throws Exception {
        return submitJobArchive(sessionId, jobArchive, null);
    }

    public JobIdData submitJobArchive(String sessionId, InputStream jobArchive, Map<String, String> variables)
            throws Exception {
        return submit(sessionId, jobArchive, MediaType.APPLICATION_OCTET_STREAM_TYPE, variables);
    }

    public boolean pushFile(String sessionId, String space, String path, String fileName, InputStream fileContent)
            throws Exception {
        String uriTmpl = (new StringBuilder(restEndpointURL)).append(addSlashIfMissing(restEndpointURL))
                                                             .append("scheduler/dataspace/")
                                                             .append(space)
                                                             .append(URLEncoder.encode(path, "UTF-8"))
                                                             .toString();

        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine)
                                                           .providerFactory(providerFactory)
                                                           .build();
        ResteasyWebTarget target = client.target(uriTmpl);

        MultipartFormDataOutput formData = new MultipartFormDataOutput();
        formData.addFormData("fileName", fileName, MediaType.TEXT_PLAIN_TYPE);
        formData.addFormData("fileContent", fileContent, MediaType.APPLICATION_OCTET_STREAM_TYPE);

        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(formData) {
        };

        Response response = target.request()
                                  .header("sessionid", sessionId)
                                  .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new NotConnectedRestException("User not authenticated or session timeout.");
            } else {
                throwException(String.format("File upload failed. Status code: %d", response.getStatus()), response);
            }
        }
        return response.readEntity(Boolean.class);
    }

    public void pullFile(String sessionId, String space, String path, String outputPath) throws Exception {
        String uriTmpl = (new StringBuilder(restEndpointURL)).append(addSlashIfMissing(restEndpointURL))
                                                             .append("scheduler/dataspace/")
                                                             .append(space)
                                                             .append(URLEncoder.encode(path, "UTF-8"))
                                                             .toString();
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine)
                                                           .providerFactory(providerFactory)
                                                           .build();
        ResteasyWebTarget target = client.target(uriTmpl);
        Response response = target.request().header("sessionid", sessionId).get();
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new NotConnectedRestException("User not authenticated or session timeout.");
            } else {
                throwException(String.format("Cannot retrieve the file. Status code: %s", response.getStatus()),
                               response);
            }
        }
        try {
            File file = new File(outputPath);
            if (response.hasEntity()) {
                copyInputStreamToFile(response.readEntity(InputStream.class), file);
            } else {
                // creates an empty file
                file.createNewFile();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (response != null) {
                response.close();
            }
            if (!client.isClosed()) {
                client.close();
            }
        }
    }

    public boolean upload(String sessionId, File file, List<String> includes, List<String> excludes,
            String dataspacePath, final String path) throws Exception {
        StringBuffer uriTmpl = (new StringBuffer()).append(restEndpointURL)
                                                   .append(addSlashIfMissing(restEndpointURL))
                                                   .append("data/")
                                                   .append(dataspacePath)
                                                   .append('/')
                                                   .append(escapeUrlPathSegment(path));
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine)
                                                           .providerFactory(providerFactory)
                                                           .build();
        ResteasyWebTarget target = client.target(uriTmpl.toString());
        Response response = null;
        try {
            response = target.request()
                             .header("sessionid", sessionId)
                             .put(Entity.entity(new CompressedStreamingOutput(file, includes, excludes),
                                                new Variant(MediaType.APPLICATION_OCTET_STREAM_TYPE,
                                                            (Locale) null,
                                                            encoding(file))));
            if (response.getStatus() != HttpURLConnection.HTTP_CREATED) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedRestException("User not authenticated or session timeout.");
                } else {
                    throwException(String.format("File upload failed. Status code: %d", response.getStatus()),
                                   response);
                }
            }
            return true;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public boolean upload(String sessionId, StreamingOutput output, String encoding, String dataspace, String path)
            throws Exception {
        StringBuffer uriTmpl = (new StringBuffer()).append(restEndpointURL)
                                                   .append(addSlashIfMissing(restEndpointURL))
                                                   .append("data/")
                                                   .append(dataspace);
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine)
                                                           .providerFactory(providerFactory)
                                                           .build();
        ResteasyWebTarget target = client.target(uriTmpl.toString()).path(path);
        Response response = null;
        try {
            response = target.request()
                             .header("sessionid", sessionId)
                             .put(Entity.entity(output,
                                                new Variant(MediaType.APPLICATION_OCTET_STREAM_TYPE,
                                                            (Locale) null,
                                                            encoding)));
            if (response.getStatus() != HttpURLConnection.HTTP_CREATED) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedRestException("User not authenticated or session timeout.");
                } else {
                    throwException(String.format("File upload failed. Status code: %d" + response.getStatus()),
                                   response);
                }
            }
            return true;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public boolean download(String sessionId, String dataspacePath, String path, List<String> includes,
            List<String> excludes, String outputPath) throws Exception {
        return download(sessionId, dataspacePath, path, includes, excludes, new File(outputPath));
    }

    public boolean download(String sessionId, String dataspacePath, String path, List<String> includes,
            List<String> excludes, File outputFile) throws Exception {
        StringBuffer uriTmpl = (new StringBuffer()).append(restEndpointURL)
                                                   .append(addSlashIfMissing(restEndpointURL))
                                                   .append("data/")
                                                   .append(dataspacePath)
                                                   .append('/');

        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine)
                                                           .providerFactory(providerFactory)
                                                           .build();
        ResteasyWebTarget target = client.target(uriTmpl.toString()).path(path);

        if (includes != null && !includes.isEmpty()) {
            target = target.queryParam("includes", includes.toArray(new Object[includes.size()]));
        }
        if (excludes != null && !excludes.isEmpty()) {
            target = target.queryParam("excludes", excludes.toArray(new Object[excludes.size()]));
        }

        Response response = null;
        try {
            response = target.request().header("sessionid", sessionId).acceptEncoding("*", "gzip", "zip").get();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedRestException("User not authenticated or session timeout.");
                } else {
                    throwException(String.format("Cannot retrieve the file. Status code: %d", response.getStatus()),
                                   response);
                }
            }
            if (response.hasEntity()) {
                InputStream is = response.readEntity(InputStream.class);
                if (isGZipEncoded(response)) {
                    if (outputFile.exists() && outputFile.isDirectory()) {
                        outputFile = new File(outputFile, response.getHeaderString("x-pds-pathname"));
                    }
                    Zipper.GZIP.unzip(is, outputFile);
                } else if (isZipEncoded(response)) {
                    Zipper.ZIP.unzip(is, outputFile);
                } else {
                    File container = outputFile.getParentFile();
                    if (!container.exists()) {
                        container.mkdirs();
                    }
                    Files.asByteSink(outputFile).writeFrom(is);
                }
            } else {
                outputFile.createNewFile();
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return true;
    }

    public boolean delete(String sessionId, String dataspacePath, String path, List<String> includes,
            List<String> excludes) throws Exception {
        StringBuffer uriTmpl = (new StringBuffer()).append(restEndpointURL)
                                                   .append(addSlashIfMissing(restEndpointURL))
                                                   .append("data/")
                                                   .append(dataspacePath)
                                                   .append('/');
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine)
                                                           .providerFactory(providerFactory)
                                                           .build();
        ResteasyWebTarget target = client.target(uriTmpl.toString()).path(path);
        if (includes != null && !includes.isEmpty()) {
            target = target.queryParam("includes", includes.toArray(new Object[includes.size()]));
        }
        if (excludes != null && !excludes.isEmpty()) {
            target = target.queryParam("excludes", excludes.toArray(new Object[excludes.size()]));
        }
        Response response = null;
        try {
            response = target.request().header("sessionid", sessionId).delete();
            if (response.getStatus() != HttpURLConnection.HTTP_NO_CONTENT) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedRestException("User not authenticated or session timeout.");
                } else {
                    throwException(String.format("Cannot delete file(s). Status code: %s", response.getStatus()),
                                   response);
                }
            }
            return true;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public ListFile list(String sessionId, String dataspacePath, String pathname) throws Exception {
        StringBuffer uriTmpl = (new StringBuffer()).append(restEndpointURL)
                                                   .append(addSlashIfMissing(restEndpointURL))
                                                   .append("data/")
                                                   .append(dataspacePath)
                                                   .append('/');
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine)
                                                           .providerFactory(providerFactory)
                                                           .build();
        ResteasyWebTarget target = client.target(uriTmpl.toString()).path(pathname).queryParam("comp", "list");
        Response response = null;
        try {
            response = target.request().header("sessionid", sessionId).get();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedRestException("User not authenticated or session timeout.");
                } else {
                    throwException(String.format("Cannot list the specified location: %s", pathname), response);
                }
            }
            return response.readEntity(ListFile.class);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public Map<String, Object> metadata(String sessionId, String dataspacePath, String pathname) throws Exception {
        StringBuffer uriTmpl = (new StringBuffer()).append(restEndpointURL)
                                                   .append(addSlashIfMissing(restEndpointURL))
                                                   .append("data/")
                                                   .append(dataspacePath)
                                                   .append(escapeUrlPathSegment(pathname));
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine)
                                                           .providerFactory(providerFactory)
                                                           .build();
        ResteasyWebTarget target = client.target(uriTmpl.toString());
        Response response = null;
        try {
            response = target.request().header("sessionid", sessionId).head();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedRestException("User not authenticated or session timeout.");
                } else {
                    throwException(String.format("Cannot get metadata from %s in %s.", pathname, dataspacePath),
                                   response);
                }
            }
            MultivaluedMap<String, Object> headers = response.getHeaders();
            Map<String, Object> metaMap = Maps.newHashMap();
            if (headers.containsKey(HttpHeaders.LAST_MODIFIED)) {
                metaMap.put(HttpHeaders.LAST_MODIFIED, headers.getFirst(HttpHeaders.LAST_MODIFIED));
            }
            return metaMap;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private JobIdData submit(String sessionId, InputStream job, MediaType mediaType, Map<String, String> variables)
            throws Exception {
        String uriTmpl = restEndpointURL + addSlashIfMissing(restEndpointURL) + "scheduler/submit";

        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine)
                                                           .providerFactory(providerFactory)
                                                           .build();
        ResteasyWebTarget target = client.target(uriTmpl);
        if (variables != null) {
            for (String key : variables.keySet()) {
                target = target.matrixParam(key, variables.get(key));
            }
        }

        MultipartFormDataOutput formData = new MultipartFormDataOutput();
        formData.addFormData("file", job, mediaType);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(formData) {
        };

        Response response = target.request()
                                  .header("sessionid", sessionId)
                                  .post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new NotConnectedRestException("User not authenticated or session timeout.");
            } else {
                throwException(String.format("Job submission failed status code: %d", response.getStatus()), response);
            }
        }
        return response.readEntity(JobIdData.class);
    }

    private String addSlashIfMissing(String url) {
        return url.endsWith("/") ? "" : "/";
    }

    private boolean isGZipEncoded(Response response) {
        return "gzip".equals(response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
    }

    private boolean isZipEncoded(Response response) {
        return "zip".equals(response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
    }

    private String escapeUrlPathSegment(String unescaped) {
        return UrlEscapers.urlPathSegmentEscaper().escape(unescaped);
    }

    public SchedulerRestInterface getScheduler() {
        return scheduler;
    }

    private void throwException(String errorMessage, Response response) {
        Exception serverException = null;
        try {
            serverException = rebuildServerSideException(response.readEntity(ExceptionToJson.class));
        } catch (Exception ignorable) {
        }
        throw new RuntimeException(errorMessage, serverException);
    }

    private static SchedulerRestInterface createRestProxy(ResteasyProviderFactory provider, String restEndpointURL,
            ClientHttpEngine httpEngine) {
        ResteasyClient client = new ResteasyClientBuilder().providerFactory(provider).httpEngine(httpEngine).build();
        ResteasyWebTarget target = client.target(restEndpointURL);
        SchedulerRestInterface schedulerRestClient = target.proxy(SchedulerRestInterface.class);
        return createExceptionProxy(schedulerRestClient);
    }

    private static SchedulerRestInterface createExceptionProxy(final SchedulerRestInterface scheduler) {
        return (SchedulerRestInterface) Proxy.newProxyInstance(SchedulerRestInterface.class.getClassLoader(),
                                                               new Class[] { SchedulerRestInterface.class },
                                                               new RestClientExceptionHandler(scheduler));
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

    private static Exception rebuildServerSideException(ExceptionToJson json)
            throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
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

    private static String encoding(File file) throws FileNotFoundException {
        return file.isDirectory() ? "zip" : (Zipper.isZipFile(file)) ? null : "gzip";
    }

    private static class CompressedStreamingOutput implements StreamingOutput {
        private File file;

        private List<String> includes;

        private List<String> excludes;

        public CompressedStreamingOutput(File file, List<String> includes, List<String> excludes) {
            this.file = file;
            this.includes = includes;
            this.excludes = excludes;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
            if (file.isFile()) {
                if (Zipper.isZipFile(file)) {
                    Files.asByteSource(file).copyTo(outputStream);
                } else {
                    Zipper.GZIP.zip(file, outputStream);
                }
            } else {
                Zipper.ZIP.zip(file, includes, excludes, outputStream);
            }

        }

    }
}
