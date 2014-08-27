/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
package org.ow2.proactive_grid_cloud_portal.dataspace;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.vfs2.Selectors.SELECT_SELF;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SessionStore;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;
import org.ow2.proactive_grid_cloud_portal.dataspace.util.VFSZipper;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;


@Path("/data/")
public class RestDataspaceImpl {
    private static final Logger logger = Logger.getLogger(RestDataspaceImpl.class);

    private static SessionStore sessions = SharedSessionStore.getInstance();

    /**
     * Upload a file to the specified location in the <i>dataspace</i>. The
     * format of the PUT URI is:
     * <p>
     * {@code http://<rest-server-path>/data/<dataspace>/<path-name>}
     * <p>
     * Example:
     * <p>
     * {@code http://localhost:8080/rest/rest/data/user/my-files/my-text-file.txt}
     * <ul>
     * <li><b>dataspace:</b> Can have two possible values, 'user' or 'global',
     * depending on target <i>DATASPACE</i>.</li>
     * <li><b>path-name:</b> Location in which the file is stored.</li>
     * </ul>
     * <b>Notes:</b>
     * <ul>
     * <li>If 'gzip' or 'zip' is specified in the 'Content-Encoding' header, the
     * contents of the request body will be decoded before being stored.</li>
     * <li>Any file that already exists in the specified location, it will be
     * replaced.</li>
     * </ul>
     */
    @PUT
    @Path("/{dataspace}/{path-name:.*}")
    public Response store(@HeaderParam("sessionid")
    String sessionId, @HeaderParam("Content-Encoding")
    String encoding, @PathParam("dataspace")
    String dataspace, @PathParam("path-name")
    String pathname, InputStream is) throws NotConnectedRestException, PermissionRestException {
        checkPathParams(dataspace, pathname);
        Session session = checkSessionValidity(sessionId);
        try {
            writeFile(is, resolveFile(session, dataspace, pathname), encoding);
        } catch (Throwable error) {
            logger.error(String.format("Cannot save the requested file in %s.", dataspace), error);
            rethrow(error);
        }
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Retrieves single or multiple files from specified location of the server.
     * The format of the GET URI is:
     * <P>
     * {@code http://<rest-server-path>/data/<dataspace>/<path-name>}
     * <p>
     * Example:
     * <p>
     * {@code http://localhost:8080/rest/rest/data/user/my-files/my-text-file.txt}
     * <ul>
     * <li>dataspace: can have two possible values, 'user' or 'global',
     * depending on the target <i>DATASPACE</i></li>
     * <li>path-name: location from which the file will be retrieved.</li>
     * </ul>
     * <b>Notes:</b>
     * <ul>
     * <li>If 'list' is specified as the 'comp' query parameter, an
     * {@link ListFile} type object will be return in JSON format.</li>
     * <li>If the pathname represents a file its contents will be returned as:
     * <ul>
     * <li>an octet stream, if its a compressed file or the client doesn't
     * accept encoded content</li>
     * <li>a 'gzip' encoded stream, if the client accepts 'gzip' encoded content
     * </li>
     * <li>a 'zip' encoded stream, if the client accepts 'zip' encoded contents</li>
     * </ul>
     * </li>
     * <li>If the pathname represents a directory, its contents will be returned
     * as 'zip' encoded stream.</li>
     * <li>file names or regular expressions can be used as 'includes' and
     * 'excludes' query parameters, in order to select which files to be
     * returned can be used to select the files returned.</li>
     * </ul>
     */
    @GET
    @Path("/{dataspace}/{path-name:.*}")
    public Response retrieve(@HeaderParam("sessionid")
    String sessionId, @HeaderParam("Accept-Encoding")
    String encoding, @PathParam("dataspace")
    String dataspace, @PathParam("path-name")
    String pathname, @QueryParam("comp")
    String component, @QueryParam("includes")
    List<String> includes, @QueryParam("excludes")
    List<String> excludes) throws NotConnectedRestException, PermissionRestException {
        checkPathParams(dataspace, pathname);
        Session session = checkSessionValidity(sessionId);
        try {
            FileObject fo = resolveFile(session, dataspace, pathname);
            if (!fo.exists()) {
                return notFoundRes();
            }
            if (!Strings.isNullOrEmpty(component)) {
                return componentResponse(component, fo);
            }
            if (fo.getType() == FileType.FILE) {
                if (VFSZipper.isZipFile(fo)) {
                    return fileComponentResponse(fo);
                } else if (Strings.isNullOrEmpty(encoding) || encoding.contains("*") ||
                    encoding.contains("gzip")) {
                    return gzipComponentResponse(pathname, fo);
                } else if (encoding.contains("zip")) {
                    return zipComponentResponse(fo, null, null);
                } else {
                    return fileComponentResponse(fo);
                }
            } else {
                // folder
                if (Strings.isNullOrEmpty(encoding) || encoding.contains("*") || encoding.contains("zip")) {
                    return zipComponentResponse(fo, includes, excludes);
                } else {
                    return badRequestRes("Folder retrieval only supported with zip encoding.");
                }
            }
        } catch (Throwable error) {
            throw rethrow(error);
        }
    }

    /**
     * Delete file(s) from the specified location in the <i>dataspace</i>. The
     * format of the DELETE URI is:
     * <p>
     * {@code http://<rest-server-path>/data/<dataspace>/<path-name>}
     * <p>
     * Example:
     * {@code http://localhost:8080/rest/rest/data/user/my-files/my-text-file.txt}
     * <ul>
     * <li>dataspace: can have two possible values, 'user' or 'global',
     * depending on the target <i>DATASPACE</i></li>
     * <li>path-name: location of the file(s) to be deleted.</li>
     * </ul>
     * <b>Notes:</b>
     * <ul>
     * <li>Only empty directories can be deleted.</li>
     * <li>File names or regular expressions can be used as 'includes' and
     * 'excludes' query parameters, in order to select which files to be deleted
     * inside the specified directory (path-name).</li>
     * </ul>
     *
     */
    @DELETE
    @Path("/{dataspace}/{path-name:.*}")
    public Response delete(@HeaderParam("sessionid")
    String sessionId, @PathParam("dataspace")
    String dataspace, @PathParam("path-name")
    String pathname, @QueryParam("includes")
    List<String> includes, @QueryParam("excludes")
    List<String> excludes) throws NotConnectedRestException, PermissionRestException {
        checkPathParams(dataspace, pathname);
        Session session = checkSessionValidity(sessionId);
        try {
            FileObject fo = resolveFile(session, dataspace, pathname);
            if (!fo.exists()) {
                return notFoundRes();
            }
            if (fo.getType() == FileType.FOLDER) {
                return deleteDir(fo, includes, excludes);
            } else {
                return (fo.delete()) ? noContentRes()
                        : serverErrorRes("Cannot delete the file: %s", pathname);
            }
        } catch (Throwable error) {
            throw rethrow(error);
        }
    }

    /**
     * Retrieve metadata of file in the location specified in <i>dataspace</i>.
     * The format of the HEAD URI is:
     * <p>
     * {@code http://<rest-server-path>/data/<dataspace>/<path-name>}
     * <p>
     * Example:
     * {@code http://localhost:8080/rest/rest/data/user/my-files/my-text-file.txt}
     *
     */
    @HEAD
    @Path("/{dataspace}/{path-name:.*}")
    public Response metadata(@HeaderParam("sessionid")
    String sessionId, @PathParam("dataspace")
    String dataspacePath, @PathParam("path-name")
    String pathname) throws NotConnectedRestException, PermissionRestException {
        checkPathParams(dataspacePath, pathname);
        Session session = checkSessionValidity(sessionId);
        try {
            FileObject fo = resolveFile(session, dataspacePath, pathname);
            if (!fo.exists()) {
                return notFoundRes();
            }
            MultivaluedMap<String, Object> headers = new MultivaluedHashMap<String, Object>(FileSystem
                    .metadata(fo));
            return Response.ok().replaceAll(headers).build();
        } catch (Throwable error) {
            logger.error(String.format("Cannot retrive metadata of %s in %s.", pathname, dataspacePath),
                    error);
            throw rethrow(error);
        }
    }

    private Response componentResponse(String type, FileObject fo) throws FileSystemException {
        return ("list".equals(type)) ? Response.ok(FileSystem.list(fo), MediaType.APPLICATION_JSON).build()
                : Response.status(Response.Status.BAD_REQUEST).entity(
                        String.format("Unknown query parameter: comp=%s", type)).build();
    }

    private Response zipComponentResponse(final FileObject fo, final List<String> includes,
            final List<String> excludes) throws FileSystemException {
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try {
                    VFSZipper.ZIP.zip(fo, FileSystem.findFiles(fo, includes, excludes), outputStream);
                } catch (IOException ioe) {
                    throw new WebApplicationException(ioe, Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }).header(HttpHeaders.CONTENT_TYPE, mediaType(fo)).header(HttpHeaders.CONTENT_ENCODING, "zip")
                .build();
    }

    private Response gzipComponentResponse(final String pathname, final FileObject fo)
            throws FileSystemException {
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                try {
                    VFSZipper.GZIP.zip(fo, os);
                } catch (IOException ioe) {
                    throw new WebApplicationException(ioe, Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }).header(HttpHeaders.CONTENT_TYPE, mediaType(fo)).header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .header("x-pds-pathname", pathname).build();
    }

    private Response fileComponentResponse(final FileObject fo) throws FileSystemException {
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try {
                    FileSystem.copy(fo, outputStream);
                } catch (IOException ioe) {
                    throw new WebApplicationException(ioe, Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }).header(HttpHeaders.CONTENT_TYPE, mediaType(fo)).header(HttpHeaders.CONTENT_ENCODING, "zip")
                .build();
    }

    private Response deleteDir(FileObject fo, List<String> includes, List<String> excludes)
            throws FileSystemException {
        if ((includes == null || includes.isEmpty()) && (excludes == null || excludes.isEmpty())) {
            if (FileSystem.isEmpty(fo)) {
                return (fo.delete()) ? noContentRes() : serverErrorRes("Cannot delete the folder: %s", fo
                        .getName().getBaseName());
            } else {
                return serverErrorRes("Cannot delete a non-empty folder: %s", fo.getName().getBaseName());
            }
        } else {
            List<FileObject> children = FileSystem.findFiles(fo, includes, excludes);
            for (FileObject child : children) {
                if (!child.delete()) {
                    return serverErrorRes("Cannot delete the file: %s", fo.getName().getBaseName());
                }
            }
            return noContentRes();
        }
    }

    private String mediaType(FileObject fo) throws FileSystemException {
        String contentType = fo.getContent().getContentInfo().getContentType();
        return Strings.isNullOrEmpty(contentType) ? MediaType.APPLICATION_OCTET_STREAM : contentType;
    }

    private Response noContentRes() {
        return Response.noContent().build();
    }

    private Response notFoundRes() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private Response badRequestRes(String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response serverErrorRes(String format, Object... args) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                (args == null || args.length == 0) ? format : String.format(format, args)).build();
    }

    private FileObject resolveFile(Session session, String dataspace, String pathname)
            throws FileSystemException, NotConnectedException, PermissionException {
        return "user".equals(dataspace) ? fileSystem(session).resolveFileInUserspace(pathname) : fileSystem(
                session).resolveFileInGlobalspace(pathname);
    }

    private void checkPathParams(String dataspace, String pathname) {
        checkArgument(!Strings.isNullOrEmpty(dataspace), "Dataspace name cannot be null or empty.");
        checkArgument("user".equals(dataspace) || "global".equals(dataspace),
                "Invalid dataspace name: '%s', only 'user' or 'global' is allowed.", dataspace);
        checkArgument(!Strings.isNullOrEmpty(pathname), "Pathname cannot be null or empty.");
    }

    private void writeFile(InputStream inputStream, FileObject outputFile, String encoding)
            throws FileSystemException, IOException {
        try {
            if (outputFile.exists()) {
                outputFile.delete(SELECT_SELF);
            }
            if (Strings.isNullOrEmpty(encoding)) {
                outputFile.createFile();
                FileSystem.copy(inputStream, outputFile);
            } else if ("gzip".equals(encoding)) {
                VFSZipper.GZIP.unzip(inputStream, outputFile);
            } else if ("zip".equals(encoding)) {
                VFSZipper.ZIP.unzip(inputStream, outputFile);
            } else {
                outputFile.createFile();
                FileSystem.copy(inputStream, outputFile);
            }
        } catch (Throwable error) {
            if (outputFile != null) {
                try {
                    if (outputFile.exists()) {
                        outputFile.delete(SELECT_SELF);
                    }
                } catch (FileSystemException e1) {
                    logger.error("Error occurred while deleting partially created file.", e1);
                }
            }
            Throwables.propagateIfInstanceOf(error, FileSystemException.class);
            Throwables.propagateIfInstanceOf(error, IOException.class);
            Throwables.propagate(error);
        }
    }

    private FileSystem fileSystem(Session session) throws FileSystemException, NotConnectedException,
            PermissionException {
        FileSystem fs = session.fileSystem();
        if (fs == null) {
            synchronized (session) {
                fs = session.fileSystem();
                if (fs == null) {
                    fs = FileSystem.Builder.create(session.getScheduler());
                    session.fileSystem(fs);
                }
            }
        }
        return fs;
    }

    private Session checkSessionValidity(String sessionId) throws NotConnectedRestException {
        Session session = Strings.isNullOrEmpty(sessionId) ? null : sessions.get(sessionId);
        if (session == null || session.getScheduler() == null) {
            throw new NotConnectedRestException("User not authenticated or session timeout.");
        }
        return session;
    }

    private RuntimeException rethrow(Throwable error) throws PermissionRestException {
        if (error instanceof PermissionException) {
            throw new PermissionRestException(error);
        } else {
            throw Throwables.propagate(error);
        }
    }
}
