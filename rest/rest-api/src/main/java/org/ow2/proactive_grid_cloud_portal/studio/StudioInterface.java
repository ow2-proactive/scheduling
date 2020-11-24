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
package org.ow2.proactive_grid_cloud_portal.studio;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.io.IOException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;


@Path("/studio")
@Produces(APPLICATION_JSON)
public interface StudioInterface {
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
    @Path("workflows")
    List<Workflow> getWorkflows(@HeaderParam("sessionid") String sessionId)
            throws NotConnectedRestException, IOException;

    @POST
    @Path("workflows")
    @Consumes(APPLICATION_JSON)
    Workflow createWorkflow(@HeaderParam("sessionid") String sessionId, Workflow workflow)
            throws NotConnectedRestException, IOException;

    @GET
    @Path("workflows/{id}")
    @Produces(APPLICATION_JSON)
    Workflow getWorkflow(@HeaderParam("sessionid") String sessionId, @PathParam("id") String workflowId)
            throws NotConnectedRestException, IOException;

    @GET
    @Path("workflows/{id}/xml")
    @Produces(APPLICATION_XML)
    String getWorkflowXmlContent(@HeaderParam("sessionid") String sessionId, @PathParam("id") String workflowId)
            throws NotConnectedRestException, IOException;

    @PUT
    @Path("workflows/{id}")
    @Consumes(APPLICATION_JSON)
    Workflow updateWorkflow(@HeaderParam("sessionid") String sessionId, @PathParam("id") String workflowId,
            Workflow workflow) throws NotConnectedRestException, IOException;

    @DELETE
    @Path("workflows/{id}")
    void deleteWorkflow(@HeaderParam("sessionid") String sessionId, @PathParam("id") String workflowId)
            throws NotConnectedRestException, IOException;

    @GET
    @Path("scripts")
    List<Script> getScripts(@HeaderParam("sessionid") String sessionId) throws NotConnectedRestException, IOException;

    @POST
    @Path("scripts")
    String createScript(@HeaderParam("sessionid") String sessionId, @FormParam("name") String name,
            @FormParam("content") String content) throws NotConnectedRestException, IOException;

    @POST
    @Path("scripts/{name}")
    String updateScript(@HeaderParam("sessionid") String sessionId, @PathParam("name") String name,
            @FormParam("content") String content) throws NotConnectedRestException, IOException;

    @GET
    @Path("classes")
    ArrayList<String> getClasses(@HeaderParam("sessionid") String sessionId) throws NotConnectedRestException;

    @POST
    @Path("classes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    String createClass(@HeaderParam("sessionid") String sessionId, MultipartFormDataInput multipart)
            throws NotConnectedRestException, IOException;

    /**
     * Validates a job.
     * @param multipart a HTTP multipart form which contains the job-descriptor
     * @return the result of job validation
     */
    @POST
    @Path("{path:validate}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    JobValidationData validate(@HeaderParam("sessionid") String sessionId, @PathParam("path") PathSegment pathSegment,
            MultipartFormDataInput multipart) throws NotConnectedRestException;

    /**
     * Submits a job to the scheduler
     * @param sessionId a valid session id
     * @param multipart a form with the job file as form data
     * @return the <code>jobid</code> of the newly created job
     */
    @POST
    @Path("{path:submit}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    JobIdData submit(@HeaderParam("sessionid") String sessionId, @PathParam("path") PathSegment pathSegment,
            MultipartFormDataInput multipart) throws JobCreationRestException, NotConnectedRestException,
            PermissionRestException, SubmissionClosedRestException, IOException;

    /**
     * Submit a job to job planner
     * @param sessionId a valid session id
     * @param pathSegment variables string
     * @param jobContentXmlString job content in xml string
     * @return true if the job is submitted successfully, false otherwise
     * @throws JobCreationRestException
     * @throws NotConnectedRestException
     * @throws PermissionRestException
     * @throws SubmissionClosedRestException
     * @throws IOException
     */
    @POST
    @Path("{path:plannings}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    String submitPlannings(@HeaderParam("sessionid") String sessionId, @PathParam("path") PathSegment pathSegment,
            Map<String, String> jobContentXmlString) throws JobCreationRestException, NotConnectedRestException,
            PermissionRestException, SubmissionClosedRestException, IOException;

    @GET
    @Path("visualizations/{id}")
    String getVisualization(@HeaderParam("sessionid") String sessionId, @PathParam("id") String jobId)
            throws NotConnectedRestException, IOException;

    @POST
    @Path("visualizations/{id}")
    boolean updateVisualization(@HeaderParam("sessionid") String sessionId, @PathParam("id") String jobId,
            @FormParam("visualization") String visualization) throws NotConnectedRestException, IOException;

}
