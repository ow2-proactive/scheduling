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
package org.ow2.proactive_grid_cloud_portal.common;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.security.KeyException;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;


@Path("/common")
@Produces(APPLICATION_JSON)
public interface CommonRestInterface {

    /**
     * login to the server with username and password form.
     *
     * @param username
     *            username
     * @param password
     *            password
     * @return the session id associated to this new connection
     * @throws LoginException if the authentication fails
     * @throws SchedulerRestException if any other error occurs
     */
    @POST
    @Path("login")
    String login(@FormParam("username") String username, @FormParam("password") String password)
            throws LoginException, SchedulerRestException;

    /**
     * Login to the server using a multipart form.
     *
     * It can be used either by submitting:<ul>
     *     <li>2 fields: 'username' and 'password'</li>
     *     <li>a credential file with field name 'credential'</li>
     * </ul>
     *
     * @param multipart multipart form
     * @return the session id associated to this new connection
     * @throws LoginException if the authentication fails
     * @throws KeyException if the credentials file cannot be decrypted
     * @throws SchedulerRestException if any other error occurs
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    String loginWithCredential(@MultipartForm LoginForm multipart)
            throws KeyException, LoginException, SchedulerRestException;

    /**
     * Terminates a session.
     *
     * @param sessionId id of the session to terminate
     * @throws RestException if the server cannot be contacted
     */
    @PUT
    @Path("logout")
    void logout(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    /**
     * Tests whether the session is connected to the ProActive server.
     *
     * @param sessionId
     *            the session to test
     * @return true if the session is connected to the ProActive server, false otherwise.
     */
    @GET
    @Path("connected")
    boolean isConnected(@HeaderParam("sessionid") String sessionId);

    /**
     * Get the login string associated to a session.
     *
     * <br> NOTE: <br>
     * In case the given sessionId doesn't have an associated login (session id expired, or invalid),
     * this endpoint will return null
     *
     * @param sessionId id of a session
     * @return the associated login value or an empty string
     */
    @GET
    @Path("currentuser")
    String currentUser(@HeaderParam("sessionid") String sessionId);

    /**
     * Get a UserData object associated to a session.
     *
     * <br> NOTE: <br>
     * In case the given sessionId doesn't have an associated login (session id expired, or invalid),
     * this endpoint will return null
     *
     * @param sessionId id of a session
     * @return a UserData object or null
     */
    @GET
    @Path("currentuserdata")
    UserData currentUserData(@HeaderParam("sessionid") String sessionId);

    /**
     * Check multiple portals accesses.
     *
     * Test if a user has access to the portals specified.
     *
     * @param sessionId id of a session
     * @param portals a list of portals access to test
     * @throws RestException if an error occurs or the session is invalid
     * @return a sublist of accessible portals
     */
    @GET
    @Path("permissions/portals")
    List<String> portalsAccesses(@HeaderParam("sessionid") String sessionId,
            @QueryParam("portals") List<String> portals) throws RestException;

    /**
     * Check single portal access.
     *
     * Test if a user has access to the specified portal.
     *
     * @param sessionId id of a session
     * @param portal string value identifying the portal
     * @throws RestException if an error occurs or the session is invalid
     * @return true if the user can access the portal, false otherwise
     */
    @GET
    @Path("permissions/portals/{portal}")
    boolean portalAccess(@HeaderParam("sessionid") String sessionId, @PathParam("portal") String portal)
            throws RestException;

    /**
     * Check if a user has admin privilege in notification service.
     *
     * @param sessionId id of a session
     * @return true if the user has the correct rights
     * @throws RestException if an error occurs or the session is invalid
     */
    @GET
    @Path("permissions/notification-service/admin")
    boolean checkSubscriptionAdmin(@HeaderParam("sessionid") String sessionId) throws RestException;

    /**
     * Check if a user has admin privilege in cloud automation service.
     *
     * @param sessionId id of a session
     * @return true if the user has the correct rights
     * @throws RestException if an error occurs or the session is invalid
     */
    @GET
    @Path("permissions/cloud-automation-service/admin")
    boolean checkPcaAdmin(@HeaderParam("sessionid") String sessionId) throws RestException;

}
