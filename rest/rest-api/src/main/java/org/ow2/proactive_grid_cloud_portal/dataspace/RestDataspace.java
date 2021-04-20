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

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;


/**
 * @author ActiveEon Team
 * @since 02/03/2020
 */
@Path("/data/")
public interface RestDataspace {

    /**
     * Upload a file or archive containing multiple files to the specified location in the <i>dataspace</i>. The
     * format of the PUT URI is:
     * <p>
     * {@code http://<rest-server-path>/data/<dataspace>/<path-name>}
     * <p>
     * Example:
     * <p>
     * {@code http://localhost:8080/rest/data/user/my-files/my-text-file.txt}
     * <ul>
     * <li><b>dataspace:</b> Can have two possible values, 'user' or 'global',
     * depending on target <i>DATASPACE</i>.</li>
     * <li><b>path-name:</b> Location in which the file will be stored or the archive will be expanded to.</li>
     * </ul>
     * <b>Notes:</b>
     * <ul>
     * <li>If 'gzip' or 'zip' is specified in the 'Content-Encoding' header, the
     * contents of the request body will be decoded before being stored.</li>
     * <li>Additionally, if 'zip' is specified, the archive will be expanded recursively in the dataspace, reproducing the archive directory structure.</li>
     * <li>Any other encoding format will be handled as it is (no decoding or recursive expanding).</li>
     * <li>Any file that already exists in the specified location will be
     * replaced.</li>
     * </ul>
     * @param sessionId a valid session id
     * @param dataspace can have two possible values, 'user' or 'global',
     * depending on the target <i>DATASPACE</i>
     * @param pathname location of the file or folder to retrieve
     * @param encoding encoding of the content, can be "gzip", "zip", empty or an arbitrary format which will be handled in the same way as empty.
     * @return a REST response with status 201(CREATED) if the operation was successful
     */
    @PUT
    @Path("/{dataspace}/{path-name:.*}")
    Response store(@HeaderParam("sessionid") String sessionId, @HeaderParam("Content-Encoding") String encoding,
            @PathParam("dataspace") String dataspace, @PathParam("path-name") String pathname, InputStream is)
            throws NotConnectedRestException, PermissionRestException;

    /**
     * Retrieves single or multiple files from specified location of the server.
     * The format of the GET URI is:
     * <P>
     * {@code http://<rest-server-path>/data/<dataspace>/<path-name>}
     * <p>
     * Example:
     * <p>
     * {@code http://localhost:8080/rest/data/user/my-files/my-text-file.txt}
     * <b>Notes:</b>
     * <ul>
     * <li>If 'list' is specified as the 'comp' query parameter, a
     * {@link ListFile} type object will be returned in JSON format. It will contain a list of files and folder contained in the selected
     * path, equivalent to a directory listing.
     * </li>
     * <li>If the pathname represents a file, its contents will be returned as:
     * <ul>
     * <li>an octet stream, if its a compressed file already or if the client doesn't
     * accept encoded content (encoding specified as "identity")</li>
     * <li>a 'gzip' encoded stream, if the client accepts 'gzip' encoded content
     * </li>
     * <li>a 'zip' encoded stream, if the client accepts 'zip' encoded contents</li>
     * </ul>
     * </li>
     * <li>If the pathname represents a directory, its contents will be included recursively and returned
     * as 'zip' encoded stream.</li>
     * <li>file names or regular expressions can be used as 'includes' and
     * 'excludes' query parameters, in order to select which files to be
     * returned from the specified location.</li>
     * </ul>
     * @param sessionId a valid session id
     * @param headerAcceptEncoding the accepted encoding supported by the client, can be "*", "gzip", "zip", "identity" or empty. When the query parameter {@code encoding} is specified, it is overridden by {@code encoding}.
     * @param dataspace can have two possible values, 'user' or 'global',
     * depending on the target <i>DATASPACE</i>
     * @param pathname location of the file or folder to retrieve
     * @param component can either be 'list' or empty. If 'list' is used, the response will contain in JSON format the list of files and folder presents at specified location, equivalent to a directory listing.
     * @param includes a list of inclusion directives
     * @param excludes a list of exclusion directives
     * @param encoding the accepted encoding supported by the client, can be "*", "gzip", "zip", "identity" or empty. It overrides the accepted encoding specified in {@code headerAcceptEncoding}.
     * @return a REST response which can have various content-types
     */
    @GET
    @Path("/{dataspace}/{path-name:.*}")
    @Produces(MediaType.WILDCARD)
    Response retrieve(@HeaderParam("sessionid") String sessionId,
            @HeaderParam("Accept-Encoding") String headerAcceptEncoding, @PathParam("dataspace") String dataspace,
            @PathParam("path-name") String pathname, @QueryParam("comp") String component,
            @QueryParam("includes") List<String> includes, @QueryParam("excludes") List<String> excludes,
            @QueryParam("encoding") String encoding) throws NotConnectedRestException, PermissionRestException;

    /**
     * Delete file(s) from the specified location in the <i>dataspace</i>.
     * The format of the DELETE URI is:
     * <p>
     * {@code http://<rest-server-path>/data/<dataspace>/<path-name>}
     * <p>
     * Example:
     * {@code http://localhost:8080/rest/data/user/my-files/my-text-file.txt}
     * <b>Notes:</b>
     * <ul>
     * <li>Non-empty directories can be deleted recursively.</li>
     * <li>File names or regular expressions can be used as 'includes' and
     * 'excludes' query parameters, in order to select which files to be deleted
     * inside the specified directory (path-name).</li>
     * </ul>
     * @param sessionId a valid session id
     * @param dataspace can have two possible values, 'user' or 'global',
     * depending on the target <i>DATASPACE</i>
     * @param pathname location of the file or folder to delete
     * @param includes a list of inclusion directives
     * @param excludes a list of exclusion directives
     * @return a REST response with status 204 (NO_CONTENT) if the operation was successful
     */
    @DELETE
    @Path("/{dataspace}/{path-name:.*}")
    Response delete(@HeaderParam("sessionid") String sessionId, @PathParam("dataspace") String dataspace,
            @PathParam("path-name") String pathname, @QueryParam("includes") List<String> includes,
            @QueryParam("excludes") List<String> excludes) throws NotConnectedRestException, PermissionRestException;

    /**
     * Retrieve metadata of file or folder in the location specified in <i>dataspace</i>.
     * The format of the HEAD URI is:
     * <p>
     * {@code http://<rest-server-path>/data/<dataspace>/<path-name>}
     * <p>
     * Example:
     * {@code http://localhost:8080/rest/data/user/my-files/my-text-file.txt}
     * @param sessionId a valid session id
     * @param dataspacePath can have two possible values, 'user' or 'global',
     * depending on the target <i>DATASPACE</i>
     * @param pathname location of the file or folder to retrieve metadata
     * @return a REST response with status 200(OK) containing metadata in response headers. Which can be:<ul>
     *     <li>For a folder: x-proactive-ds-type=DIRECTORY, Last-Modified</li>
     *     <li>For a file: x-proactive-ds-type=FILE, Last-Modified, Content-Type, Content-Length</li>
     * </ul>
     */
    @HEAD
    @Path("/{dataspace}/{path-name:.*}")
    Response metadata(@HeaderParam("sessionid") String sessionId, @PathParam("dataspace") String dataspacePath,
            @PathParam("path-name") String pathname) throws NotConnectedRestException, PermissionRestException;

    /**
     * Create an empty file or folder in the specified location in the <i>dataspace</i>. The
     * format of the CREATE URI is:
     * <p>
     * {@code http://<rest-server-path>/data/<dataspace>/<path-name>}
     * <p>
     * Example:
     * {@code http://localhost:8080/rest/data/user/my-files/my-text-file.txt}
     *
     * @param sessionId a valid session id
     * @param dataspacePath can have two possible values, 'user' or 'global',
     * depending on the target <i>DATASPACE</i>
     * @param pathname location of the file or folder to create
     * @param mimeType used to decide whether a file ('application/file') or folder ('application/folder') must be created
     * @return a REST response with status 200(OK) if the operation was successful
     */
    @POST
    @Path("/{dataspace}/{path-name:.*}")
    Response create(@HeaderParam("sessionid") String sessionId, @PathParam("dataspace") String dataspacePath,
            @PathParam("path-name") String pathname, @FormParam("mimetype") String mimeType)
            throws NotConnectedRestException, PermissionRestException;
}
