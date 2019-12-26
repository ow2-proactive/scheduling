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
    @POST
    @Path("login")
    String login(@FormParam("username") String username, @FormParam("password") String password)
            throws KeyException, LoginException, SchedulerRestException;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    String loginWithCredential(@MultipartForm LoginForm multipart)
            throws IOException, KeyException, LoginException, SchedulerRestException;

    @PUT
    @Path("logout")
    void logout(@HeaderParam("sessionid")
    final String sessionId) throws RestException;

    @GET
    @Path("connected")
    boolean isConnected(@HeaderParam("sessionid") String sessionId);

    @GET
    @Path("currentuser")
    String currentUser(@HeaderParam("sessionid") String sessionId);

    @GET
    @Path("currentuserdata")
    UserData currentUserData(@HeaderParam("sessionid") String sessionId);

    @GET
    @Path("permissions/portals")
    List<String> portalsAccesses(@HeaderParam("sessionid") String sessionId,
            @QueryParam("portals") List<String> portals) throws RestException;

    @GET
    @Path("permissions/portals/{portal}")
    boolean portalAccess(@HeaderParam("sessionid") String sessionId, @PathParam("portal") String portal)
            throws RestException;

    /**
     *
     * Check if the user has admin privilege in notification service
     *
     * @return true if the user has the correct rights
     */
    @GET
    @Path("permissions/notification-service/admin")
    boolean checkSubscriptionAdmin(@HeaderParam("sessionid") String sessionId) throws RestException;

}
