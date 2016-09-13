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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest.ds;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.ow2.proactive.authentication.ConnectionInfo;
import org.ow2.proactive.http.HttpClientBuilder;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSystemException;
import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;
import org.ow2.proactive.scheduler.rest.ISchedulerClient;
import org.ow2.proactive.scheduler.rest.SchedulerClient;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class DataSpaceClient implements IDataSpaceClient {

    private static final Logger log = Logger.getLogger(DataSpaceClient.class);

    private String restDataspaceUrl;
    private String sessionId;
    private ClientHttpEngine httpEngine;
    private ISchedulerClient client;

    public DataSpaceClient() {
    }

    public DataSpaceClient(String restServerUrl, ClientHttpEngine httpEngine) {
        this.httpEngine = httpEngine;
        this.restDataspaceUrl = restDataspaceUrl(restServerUrl);
    }

    public void init(String restServerUrl, ISchedulerClient client) {
        this.httpEngine =
                new ApacheHttpClient4Engine(
                        new HttpClientBuilder().disableContentCompression().useSystemProperties().build());
        this.restDataspaceUrl = restDataspaceUrl(restServerUrl);
        this.sessionId = client.getSession();
        this.client = client;
    }

    public void init(ConnectionInfo connectionInfo) throws Exception {
        ISchedulerClient client = SchedulerClient.createInstance();
        client.init(connectionInfo);
        init(connectionInfo.getUrl(), client);
    }

    @Override
    public boolean upload(final ILocalSource source, final IRemoteDestination destination)
            throws NotConnectedException, PermissionException {

        if (log.isDebugEnabled()) {
            log.debug("Uploading from " + source + " to " + destination);
        }

        StringBuffer uriTmpl = (new StringBuffer()).append(restDataspaceUrl).append(
                destination.getDataspace().value());
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine).build();
        ResteasyWebTarget target = client.target(uriTmpl.toString()).path(destination.getPath());
        Response response = null;
        try {
            response = target.request().header("sessionid", sessionId).put(
                    Entity.entity(new StreamingOutput() {
                        @Override
                        public void write(OutputStream outputStream) throws IOException,
                                WebApplicationException {
                            source.writeTo(outputStream);
                        }
                    }, new Variant(MediaType.APPLICATION_OCTET_STREAM_TYPE, (Locale) null, source
                            .getEncoding())));

            if (response.getStatus() != HttpURLConnection.HTTP_CREATED) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedException("User not authenticated or session timeout.");
                } else {
                    throw new RuntimeException("File upload failed. Status code:" + response.getStatus());
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Upload from " + source + " to " + destination + " performed with success");
            }

            return true;
        } catch (IOException ioe) {
            throw Throwables.propagate(ioe);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public boolean create(IRemoteSource source) throws NotConnectedException, PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Trying to create file " + source);
        }

        StringBuffer uriTmpl = (new StringBuffer()).append(restDataspaceUrl).append(
                source.getDataspace().value());
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine).build();
        ResteasyWebTarget target =
                client.target(uriTmpl.toString())
                        .path(source.getPath());

        Response response = null;
        try {
            Invocation.Builder request = target.request();
            request.header("sessionid", sessionId);

            Form form = new Form();
            form.param("mimetype", source.getType().getMimeType());
            response = request.post(Entity.form(form));

            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedException("User not authenticated or session timeout.");
                } else {
                    throw new RuntimeException("Cannot create file(s). Status code:" + response.getStatus());
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("File creation " + source + " performed with success");
            }

            return true;
        } finally{
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public boolean download(IRemoteSource source, ILocalDestination destination)
            throws NotConnectedException, PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Downloading from " + source + " to " + destination);
        }

        StringBuffer uriTmpl = (new StringBuffer()).append(restDataspaceUrl).append(
                source.getDataspace().value());
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine).build();
        ResteasyWebTarget target = client.target(uriTmpl.toString()).path(source.getPath());

        List<String> includes = source.getIncludes();
        if (includes != null && !includes.isEmpty()) {
            target = target.queryParam("includes", includes.toArray(new Object[includes.size()]));
        }
        List<String> excludes = source.getExcludes();
        if (excludes != null && !excludes.isEmpty()) {
            target = target.queryParam("excludes", excludes.toArray(new Object[excludes.size()]));
        }

        Response response = null;
        try {
            response = target.request().header("sessionid", sessionId).acceptEncoding("*", "gzip", "zip")
                    .get();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedException("User not authenticated or session timeout.");
                } else {
                    throw new RuntimeException(String.format("Cannot retrieve the file. Status code: %s",
                            response.getStatus()));
                }
            }

            if (response.hasEntity()) {
                InputStream is = response.readEntity(InputStream.class);
                destination.readFrom(is, response.getHeaderString(HttpHeaders.CONTENT_ENCODING));
            } else {
                throw new RuntimeException(String.format("%s in %s is empty.", source.getDataspace(), source
                        .getPath()));
            }

            if (log.isDebugEnabled()) {
                log.debug("Download from " + source + " to " + destination + " performed with success");
            }

            return true;
        } catch (IOException ioe) {
            throw Throwables.propagate(ioe);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public RemoteSpace getGlobalSpace() {
        return new RestRemoteSpace(Dataspace.GLOBAL);
    }

    @Override
    public RemoteSpace getUserSpace() {
        return new RestRemoteSpace(Dataspace.USER);
    }

    @Override
    public ListFile list(IRemoteSource source, boolean recursive) throws NotConnectedException, PermissionException {
        StringBuffer uriTmpl = (new StringBuffer()).append(restDataspaceUrl).append(
                source.getDataspace().value());
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine).build();
        ResteasyWebTarget target = client.target(uriTmpl.toString()).path(source.getPath()).queryParam(
                "comp", recursive ? "recursive" : "list");

        List<String> includes = source.getIncludes();
        if (includes != null && !includes.isEmpty()) {
            target = target.queryParam("includes", includes.toArray(new Object[includes.size()]));
        }
        List<String> excludes = source.getExcludes();
        if (excludes != null && !excludes.isEmpty()) {
            target = target.queryParam("excludes", excludes.toArray(new Object[excludes.size()]));
        }
        Response response = null;
        try {
            response = target.request().header("sessionid", sessionId).get();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedException("User not authenticated or session timeout.");
                } else {
                    throw new RuntimeException(String.format("Cannot list the specified location: %s", source
                            .getPath()));
                }
            }
            return response.readEntity(ListFile.class);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public boolean delete(IRemoteSource source) throws NotConnectedException, PermissionException {
        if (log.isDebugEnabled()) {
            log.debug("Trying to delete " + source);
        }

        StringBuffer uriTmpl = (new StringBuffer()).append(restDataspaceUrl).append(
                source.getDataspace().value());
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine).build();
        ResteasyWebTarget target = client.target(uriTmpl.toString()).path(source.getPath());

        List<String> includes = source.getIncludes();
        if (includes != null && !includes.isEmpty()) {
            target = target.queryParam("includes", includes.toArray(new Object[includes.size()]));
        }

        List<String> excludes = source.getExcludes();
        if (excludes != null && !excludes.isEmpty()) {
            target = target.queryParam("excludes", excludes.toArray(new Object[excludes.size()]));
        }

        Response response = null;
        try {
            response = target.request().header("sessionid", sessionId).delete();

            boolean noContent = false;
            if (response.getStatus() != HttpURLConnection.HTTP_NO_CONTENT) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedException("User not authenticated or session timeout.");
                } else {
                    throw new RuntimeException("Cannot delete file(s). Status code:" + response.getStatus());
                }
            } else {
                noContent = true;
                log.debug("No action performed for deletion since source " + source + " was not found remotely");
            }

            if (!noContent && log.isDebugEnabled()) {
                log.debug("Removal of " + source + " performed with success");
            }

            return true;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public Map<String, String> metadata(IRemoteSource source) throws NotConnectedException,
            PermissionException {
        StringBuffer uriTmpl = (new StringBuffer()).append(restDataspaceUrl).append(
                source.getDataspace().value());
        ResteasyClient client = new ResteasyClientBuilder().httpEngine(httpEngine).build();
        ResteasyWebTarget target = client.target(uriTmpl.toString()).path(source.getPath());
        Response response = null;
        try {
            response = target.request().header("sessionid", sessionId).head();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new NotConnectedException("User not authenticated or session timeout.");
                } else {
                    throw new RuntimeException(String.format(
                            "Cannot get metadata from %s in '%s' dataspace.", source.getPath(), source
                                    .getDataspace()));
                }
            }
            MultivaluedMap<String, Object> headers = response.getHeaders();
            Map<String, String> metaMap = Maps.newHashMap();
            if (headers.containsKey(HttpHeaders.LAST_MODIFIED)) {
                metaMap.put(HttpHeaders.LAST_MODIFIED, String.valueOf(headers
                        .getFirst(HttpHeaders.LAST_MODIFIED)));
            }
            return metaMap;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private String restDataspaceUrl(String restServerUrl) {
        return (new StringBuffer()).append(restServerUrl).append((restServerUrl.endsWith("/") ? "" : "/"))
                .append("data/").toString();
    }

    class RestRemoteSpace implements RemoteSpace {

        IDataSpaceClient.Dataspace space;

        public RestRemoteSpace(IDataSpaceClient.Dataspace space) {
            this.space = space;
        }


        @Override
        public List<String> listFiles(String remotePath, boolean recursive) throws FileSystemException {
            RemoteSource source = new RemoteSource(space, remotePath);
            return findFiles(source, recursive);
        }

        @Override
        public List<String> listFiles(String remotePath, String pattern) throws FileSystemException {
            RemoteSource source = new RemoteSource(space, remotePath);
            source.setIncludes(pattern);
            return findFiles(source, true);
        }

        private List<String> findFiles(RemoteSource source, boolean recursive) throws FileSystemException {
            List<String> answer = new LinkedList<>();
            try {
                ListFile listFiles = DataSpaceClient.this.list(source, recursive);
                for (String fileOrDirectory : listFiles.getFullListing()) {
                    answer.add(fileOrDirectory);
                }
                return answer;
            } catch (Exception e) {
                throw new FileSystemException(e);
            }
        }

        @Override
        public void pushFile(File localPath, String remotePath) throws FileSystemException {
            if (!localPath.exists()) {
                throw new FileSystemException("" + localPath + " does not exist");
            }
            ILocalSource source;
            if (localPath.isDirectory()) {
                source = new LocalDirSource(localPath);
            } else {
                source = new LocalFileSource(localPath);
            }

            try {
                DataSpaceClient.this.upload(source, new RemoteDestination(space, remotePath));
            } catch (Exception e) {
                throw new FileSystemException(e);
            }
        }


        @Override
        public void pushFiles(File localDirectory, String pattern, String remotePath) throws FileSystemException {
            if (!localDirectory.exists()) {
                throw new FileSystemException("" + localDirectory + " does not exist");
            }
            if (!localDirectory.isDirectory()) {
                throw new FileSystemException("" + localDirectory + " is not a directory");
            }
            LocalDirSource source = new LocalDirSource(localDirectory);
            source.setIncludes(pattern);

            try {
                DataSpaceClient.this.upload(source, new RemoteDestination(space, remotePath));
            } catch (Exception e) {
                throw new FileSystemException(e);
            }
        }

        @Override
        public File pullFile(String remotePath, File localPath) throws FileSystemException {
            RemoteSource source = new RemoteSource(space, remotePath);
            LocalDestination dest = new LocalDestination(localPath);
            try {
                DataSpaceClient.this.download(source, dest);
            } catch (Exception e) {
                throw new FileSystemException(e);
            }
            if (!localPath.exists()) {
                throw new IllegalStateException("File not present after download : " + localPath);
            }
            return localPath;
        }

        @Override
        public Set<File> pullFiles(String remotePath, String pattern, File localPath) throws FileSystemException {
            RemoteSource source = new RemoteSource(space, remotePath);
            source.setIncludes(pattern);
            LocalDestination dest = new LocalDestination(localPath);
            try {
                DataSpaceClient.this.download(source, dest);
            } catch (Exception e) {
                throw new FileSystemException(e);
            }
            Set<File> answer = new LinkedHashSet<>();
            try {
                for (Path foundFile : Files.newDirectoryStream(localPath.toPath(), pattern)) {
                    answer.add(foundFile.toFile());
                }
            } catch (IOException e) {
                throw new FileSystemException(e);
            }
            return answer;
        }

        @Override
        public void deleteFile(String remotePath) throws FileSystemException {
            RemoteSource source = new RemoteSource(space, remotePath);
            try {
                DataSpaceClient.this.delete(source);
            } catch (Exception e) {
                throw new FileSystemException(e);
            }
        }

        @Override
        public void deleteFiles(String remotePath, String pattern) throws FileSystemException {
            RemoteSource source = new RemoteSource(space, remotePath);
            source.setIncludes(pattern);
            try {
                DataSpaceClient.this.delete(source);
            } catch (Exception e) {
                throw new FileSystemException(e);
            }
        }

        @Override
        public List<String> getSpaceURLs() throws FileSystemException {
            if (space == Dataspace.GLOBAL) {
                try {
                    return DataSpaceClient.this.client.getGlobalSpaceURIs();
                } catch (Exception e) {
                    throw new FileSystemException(e);
                }
            } else {
                try {
                    return DataSpaceClient.this.client.getUserSpaceURIs();
                } catch (Exception e) {
                    throw new FileSystemException(e);
                }
            }
        }

        @Override
        public InputStream getInputStream(String remotePath) throws FileSystemException {
            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream getOutputStream(String remotePath) throws FileSystemException {
            throw new UnsupportedOperationException();
        }
    }

}
