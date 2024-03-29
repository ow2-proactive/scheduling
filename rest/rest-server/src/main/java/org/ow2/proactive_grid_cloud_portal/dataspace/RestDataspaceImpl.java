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
package org.ow2.proactive_grid_cloud_portal.dataspace;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.vfs2.Selectors.SELECT_ALL;
import static org.apache.commons.vfs2.Selectors.SELECT_SELF;
import static org.ow2.proactive.permissions.RoleUtils.findRole;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.ws.rs.*;
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
import org.ow2.proactive.permissions.*;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SessionStore;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.common.TokenStore;
import org.ow2.proactive_grid_cloud_portal.dataspace.util.VFSZipper;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;


@Path("/data/")
public class RestDataspaceImpl implements RestDataspace {

    private static final Logger logger = Logger.getLogger(RestDataspaceImpl.class);

    public static final String USER = "user";

    public static final String GLOBAL = "global";

    public static final String LIST_METADATA = "list-metadata";

    private static SessionStore sessions = SharedSessionStore.getInstance();

    @Override
    @RoleWrite
    public Response store(@HeaderParam("sessionid") String sessionId, @HeaderParam("Content-Encoding") String encoding,
            @PathParam("dataspace") String dataspace, @PathParam("path-name") String pathname, InputStream is)
            throws NotConnectedRestException, PermissionRestException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        Session session = checkAuthorization(sessionId,
                                             dataspace,
                                             currentMethod,
                                             "You are not authorized to write to files in dataspace " + dataspace);
        try {
            checkPathParams(dataspace, pathname);
            logger.debug(String.format("Storing file(s) in %s/%s", dataspace.toUpperCase(), pathname));
            FileObject fileObject = resolveFile(session, dataspace, pathname);
            if (!fileObject.isWriteable()) {
                return unauthorizedWriteRes(pathname);
            }
            writeFile(is, fileObject, encoding);
        } catch (Throwable error) {
            logger.error(String.format("Cannot save the requested file to %s in %s.",
                                       pathname,
                                       dataspace.toUpperCase()),
                         error);
            rethrow(error);
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    @RoleRead
    public Response retrieve(@HeaderParam("sessionid") String sessionId,
            @HeaderParam("Accept-Encoding") String headerAcceptEncoding, @PathParam("dataspace") String dataspace,
            @PathParam("path-name") String pathname, @QueryParam("comp") String component,
            @QueryParam("includes") List<String> includes, @QueryParam("excludes") List<String> excludes,
            @QueryParam("encoding") String encoding, @QueryParam("token") String token)
            throws NotConnectedRestException, PermissionRestException {
        if (encoding == null) {
            encoding = headerAcceptEncoding;
        }
        logger.debug(String.format("Retrieving file %s in %s with encoding %s",
                                   pathname,
                                   dataspace.toUpperCase(),
                                   encoding));

        // When the sessionId is not specified in the request header, we will use query parameter token for authentication
        if (sessionId == null && token != null) {
            sessionId = TokenStore.getInstance().getSessionId(token);
        }

        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        Session session = checkAuthorization(sessionId,
                                             dataspace,
                                             currentMethod,
                                             "You are not authorized to read files from dataspace " + dataspace);
        try {
            checkPathParams(dataspace, pathname);
            FileObject fo = resolveFile(session, dataspace, pathname);

            if (!fo.exists()) {
                return notFoundRes();
            }
            if (!fo.isReadable()) {
                return unauthorizedReadRes(pathname);
            }
            if (!Strings.isNullOrEmpty(component)) {
                return componentResponse(component, fo, includes, excludes);
            }

            if (fo.getType() == FileType.FILE) {
                if (VFSZipper.isZipFile(fo)) {
                    logger.debug(String.format("Retrieving file %s in %s", pathname, dataspace.toUpperCase()));
                    return fileComponentResponse(fo);
                } else if (Strings.isNullOrEmpty(encoding) || encoding.contains("*") || encoding.contains("gzip")) {
                    logger.debug(String.format("Retrieving file %s as gzip in %s", pathname, dataspace.toUpperCase()));
                    return gzipComponentResponse(pathname, fo);
                } else if (encoding.contains("zip")) {
                    logger.debug(String.format("Retrieving file %s as zip in %s", pathname, dataspace.toUpperCase()));
                    return zipComponentResponse(fo, null, null);
                } else {
                    logger.debug(String.format("Retrieving file %s in %s", pathname, dataspace.toUpperCase()));
                    return fileComponentResponse(fo);
                }
            } else {
                // folder
                if (Strings.isNullOrEmpty(encoding) || encoding.contains("*") || encoding.contains("zip")) {
                    logger.debug(String.format("Retrieving folder %s as zip in %s", pathname, dataspace.toUpperCase()));
                    return zipComponentResponse(fo, includes, excludes);
                } else {
                    return badRequestRes("Folder retrieval only supported with zip encoding.");
                }
            }
        } catch (Throwable error) {
            logger.error(String.format("Cannot retrieve %s in %s.", pathname, dataspace.toUpperCase()), error);
            throw rethrow(error);
        }
    }

    @Override
    @RoleWrite
    public Response delete(@HeaderParam("sessionid") String sessionId, @PathParam("dataspace") String dataspace,
            @PathParam("path-name") String pathname, @QueryParam("includes") List<String> includes,
            @QueryParam("excludes") List<String> excludes) throws NotConnectedRestException, PermissionRestException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        Session session = checkAuthorization(sessionId,
                                             dataspace,
                                             currentMethod,
                                             "You are not authorized to delete files from dataspace " + dataspace);

        try {
            checkPathParams(dataspace, pathname);
            FileObject fo = resolveFile(session, dataspace, pathname);

            if (!fo.exists()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            if (!fo.isWriteable()) {
                return unauthorizedWriteRes(pathname);
            }
            if (fo.getType() == FileType.FOLDER) {
                logger.debug(String.format("Deleting directory %s in %s", pathname, dataspace.toUpperCase()));
                return deleteDir(fo, includes, excludes);
            } else {
                logger.debug(String.format("Deleting file %s in %s", pathname, dataspace.toUpperCase()));
                fo.close();
                return fo.delete() ? noContentRes() : serverErrorRes("Cannot delete the file: %s", pathname);
            }
        } catch (Throwable error) {
            logger.error(String.format("Cannot delete %s in %s.", pathname, dataspace.toUpperCase()), error);
            throw rethrow(error);
        }
    }

    @Override
    @RoleRead
    public Response metadata(@HeaderParam("sessionid") String sessionId, @PathParam("dataspace") String dataspacePath,
            @PathParam("path-name") String pathname) throws NotConnectedRestException, PermissionRestException {
        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        Session session = checkAuthorization(sessionId,
                                             dataspacePath,
                                             currentMethod,
                                             "You are not authorized to read files information from dataspace " +
                                                            dataspacePath);
        try {
            checkPathParams(dataspacePath, pathname);
            FileObject fo = resolveFile(session, dataspacePath, pathname);
            if (!fo.exists()) {
                return notFoundRes();
            }
            logger.debug(String.format("Retrieving metadata for %s in %s", pathname, dataspacePath));
            MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>(FileSystem.metadata(fo));
            return Response.ok().replaceAll(headers).build();
        } catch (Throwable error) {
            logger.error(String.format("Cannot retrieve metadata for %s in %s.", pathname, dataspacePath.toUpperCase()),
                         error);
            throw rethrow(error);
        }
    }

    @Override
    @RoleWrite
    public Response create(@HeaderParam("sessionid") String sessionId, @PathParam("dataspace") String dataspacePath,
            @PathParam("path-name") String pathname, @FormParam("mimetype") String mimeType)
            throws NotConnectedRestException, PermissionRestException {

        Method currentMethod = new Object() {
        }.getClass().getEnclosingMethod();
        Session session = checkAuthorization(sessionId,
                                             dataspacePath,
                                             currentMethod,
                                             "You are not authorized to create files in dataspace " + dataspacePath);
        try {
            checkPathParams(dataspacePath, pathname);
            FileObject fileObject = resolveFile(session, dataspacePath, pathname);

            if (!fileObject.isWriteable()) {
                return unauthorizedWriteRes(pathname);
            }

            if (mimeType.equals(org.ow2.proactive_grid_cloud_portal.common.FileType.FOLDER.getMimeType())) {
                logger.debug(String.format("Creating folder %s in %s", pathname, dataspacePath.toUpperCase()));
                fileObject.createFolder();
            } else if (mimeType.equals(org.ow2.proactive_grid_cloud_portal.common.FileType.FILE.getMimeType())) {
                logger.debug(String.format("Creating file %s in %s", pathname, dataspacePath.toUpperCase()));
                fileObject.createFile();
            } else {
                return serverErrorRes("Cannot create specified file since mimetype is not specified");
            }

            return Response.ok().build();
        } catch (FileSystemException e) {
            logger.error(String.format("Cannot create %s in %s", pathname, dataspacePath.toUpperCase()), e);
            throw rethrow(e);
        } catch (Throwable e) {
            logger.error(String.format("Cannot create %s in %s", pathname, dataspacePath.toUpperCase()), e);
            throw rethrow(e);
        }
    }

    private Response componentResponse(String type, FileObject fo, List<String> includes, List<String> excludes)
            throws FileSystemException {
        switch (type) {
            case "list":
                return Response.ok(FileSystem.list(fo, includes, excludes), MediaType.APPLICATION_JSON).build();
            case LIST_METADATA:
                return Response.ok(FileSystem.listMetadata(fo, includes, excludes), MediaType.APPLICATION_JSON).build();
            default:
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity(String.format("Unknown query parameter: comp=%s", type))
                               .build();
        }
    }

    private Response zipComponentResponse(final FileObject fo, final List<String> includes, final List<String> excludes)
            throws FileSystemException {
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try {
                    VFSZipper.ZIP.zip(fo, FileSystem.findFiles(fo, includes, excludes), outputStream);
                } catch (IOException ioe) {
                    throw new WebApplicationException(ioe, Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        }).header(HttpHeaders.CONTENT_TYPE, mediaType(fo)).header(HttpHeaders.CONTENT_ENCODING, "zip").build();
    }

    private Response gzipComponentResponse(final String pathname, final FileObject fo) throws FileSystemException {
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                try {
                    VFSZipper.GZIP.zip(fo, os);
                } catch (IOException ioe) {
                    throw new WebApplicationException(ioe, Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
        })
                       .header(HttpHeaders.CONTENT_TYPE, mediaType(fo))
                       .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                       .header("x-pds-pathname", pathname)
                       .build();
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
        }).header(HttpHeaders.CONTENT_TYPE, mediaType(fo)).header(HttpHeaders.CONTENT_ENCODING, "identity").build();
    }

    private Response deleteDir(FileObject fo, List<String> includes, List<String> excludes) throws FileSystemException {
        if ((includes == null || includes.isEmpty()) && (excludes == null || excludes.isEmpty())) {
            fo.delete(SELECT_ALL);
            fo.delete();
            return noContentRes();
        } else {
            List<FileObject> children = FileSystem.findFiles(fo, includes, excludes);
            for (FileObject child : children) {
                if (!child.delete()) {
                    child.delete(SELECT_ALL);
                    child.delete();
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

    private Response unauthorizedReadRes(String pathname) {
        return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized read access to " + pathname).build();
    }

    private Response unauthorizedWriteRes(String pathname) {
        return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized write access to " + pathname).build();
    }

    private Response serverErrorRes(String format, Object... args) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity((args == null || args.length == 0) ? format : String.format(format, args))
                       .build();
    }

    public FileObject resolveFile(Session session, String dataspace, String pathname)
            throws FileSystemException, NotConnectedRestException, PermissionRestException {
        try {
            return USER.equalsIgnoreCase(dataspace) ||
                   SchedulerConstants.USERSPACE_NAME.toString()
                                                    .equalsIgnoreCase(dataspace) ? fileSystem(session).resolveFileInUserspace(pathname)
                                                                                 : fileSystem(session).resolveFileInGlobalspace(pathname);
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException(e);
        } catch (PermissionException e) {
            throw new PermissionRestException(e);
        }
    }

    private void checkPathParams(String dataspace, String pathname) {
        checkArgument(!Strings.isNullOrEmpty(dataspace), "Dataspace name cannot be null or empty.");
        checkArgument(USER.equalsIgnoreCase(dataspace) || GLOBAL.equalsIgnoreCase(dataspace) ||
                      SchedulerConstants.USERSPACE_NAME.toString().equalsIgnoreCase(dataspace) ||
                      SchedulerConstants.GLOBALSPACE_NAME.toString().equalsIgnoreCase(dataspace),
                      "Invalid dataspace name: '%s'.",
                      dataspace);
        checkArgument(!Strings.isNullOrEmpty(pathname), "Pathname cannot be null or empty.");
    }

    public void writeFile(InputStream inputStream, FileObject outputFile, String encoding)
            throws FileSystemException, IOException {
        try {
            if (outputFile.exists()) {
                outputFile.delete(SELECT_SELF);
            }
            if (Strings.isNullOrEmpty(encoding)) {
                outputFile.createFile();
                logger.debug("Writing single file " + outputFile);
                FileSystem.copy(inputStream, outputFile);
            } else if ("gzip".equals(encoding)) {
                logger.debug("Expanding gzip archive into " + outputFile);
                VFSZipper.GZIP.unzip(inputStream, outputFile);
            } else if ("zip".equals(encoding)) {
                logger.debug("Expanding zip archive into " + outputFile);
                VFSZipper.ZIP.unzip(inputStream, outputFile);
            } else {
                logger.debug("Writing single file " + outputFile);
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

    public FileSystem fileSystem(Session session)
            throws FileSystemException, NotConnectedException, PermissionException {
        FileSystem fs = session.fileSystem();
        if (fs == null) {
            synchronized (session) {
                fs = session.fileSystem();
                if (fs == null) {
                    fs = FileSystem.Builder.create(session);
                    session.fileSystem(fs);
                }
            }
        }
        return fs;
    }

    public Session checkSessionValidity(String sessionId) throws NotConnectedRestException {
        Session session = sessions.get(sessionId);
        if (session.getScheduler() == null) {
            throw new NotConnectedRestException("User not authenticated or session timeout.");
        }
        return session;
    }

    private boolean isSuperAdmin(Subject subject) {
        if (System.getSecurityManager() != null) {
            try {
                checkPermission(subject, new AllPermission(), "");
            } catch (PermissionRestException e) {
                return false;
            }
        }
        return true;
    }

    public Session checkAuthorization(String sessionId, String dataspace, Method method, String permissionMsg)
            throws NotConnectedRestException, PermissionRestException {
        Session session = sessions.get(sessionId);
        if (session.getScheduler() == null) {
            throw new NotConnectedRestException("User not authenticated or session timeout.");
        }

        Subject subject;
        String userName;

        try {
            subject = session.getScheduler().getSubject();
            userName = session.getScheduler().getCurrentUser();
        } catch (NotConnectedException e) {
            throw new NotConnectedRestException("User not authenticated or session timeout.");
        }

        if (isSuperAdmin(subject)) {
            return session;
        }

        String dataspacePath = Strings.isNullOrEmpty(dataspace) ? "" : dataspace.toLowerCase() + ".";

        final String fullMethodName = RestDataspaceImpl.class.getName() + "." + dataspacePath + method.getName();
        final String fullMethodRole = RestDataspaceImpl.class.getName() + "." + dataspacePath + findRole(method);
        final MethodCallPermission methodCallPermission = new MethodCallPermission(fullMethodName);
        final ServiceRolePermission serviceRolePermission = new ServiceRolePermission(fullMethodRole);
        final DeniedMethodCallPermission deniedMethodCallPermission = new DeniedMethodCallPermission(fullMethodName);

        if (System.getSecurityManager() != null) {
            try {
                checkPermission(subject, deniedMethodCallPermission, permissionMsg);
                // double-check that this method call has previously been denied for this user. If not, ignore it.
                if (DeniedMethodCallPermissionRepository.getInstance()
                                                        .checkAndSetDeniedMethodCall(userName, fullMethodName, true)) {
                    logger.trace("Denied method access : " + fullMethodName);
                    throw new DeniedMethodCallException(permissionMsg);
                }
            } catch (PermissionRestException ex) {
                // ok, the check should throw an exception unless it is denied
                // double-check that this method call has previously been denied for this user. If yes, throw an exception.
                if (DeniedMethodCallPermissionRepository.getInstance()
                                                        .checkAndSetDeniedMethodCall(userName, fullMethodName, false)) {
                    logger.trace("Denied method access : " + fullMethodName);
                    throw new PermissionRestException(permissionMsg);
                }
            } catch (DeniedMethodCallException e) {
                throw new PermissionRestException(permissionMsg);
            }
        }

        try {
            checkPermission(subject, methodCallPermission, permissionMsg);
        } catch (PermissionRestException ex) {
            try {
                checkPermission(subject, serviceRolePermission, permissionMsg);
            } catch (PermissionRestException ex2) {
                logger.debug(permissionMsg);
                throw ex2;
            }
        }

        return session;
    }

    /**
     * Checks if user has the specified permission.
     *
     * @return true if it has, throw {@link SecurityException} otherwise with specified error message
     */
    public boolean checkPermission(final Subject subject, final Permission permission, String errorMessage)
            throws PermissionRestException {
        try {
            Subject.doAsPrivileged(subject, new PrivilegedAction<Object>() {
                public Object run() {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(permission);
                    }
                    return null;
                }
            }, null);
        } catch (SecurityException ex) {
            throw new PermissionRestException(errorMessage);
        }

        return true;
    }

    private RuntimeException rethrow(Throwable error) throws PermissionRestException {
        if (error instanceof PermissionException) {
            throw new PermissionRestException(error);
        } else {
            throw Throwables.propagate(error);
        }
    }
}
