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
package org.ow2.proactive_grid_cloud_portal.studio;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/studio")
@Produces(APPLICATION_JSON)
public interface StudioInterface {
    @POST
    @Path("login")
    String login(@FormParam("username")
    String username, @FormParam("password")
    String password) throws KeyException, LoginException, SchedulerRestException;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    String loginWithCredential(@MultipartForm
    LoginForm multipart) throws IOException, KeyException, LoginException, SchedulerRestException;

    @PUT
    @Path("logout")
    public void logout(@HeaderParam("sessionid")
    final String sessionId) throws PermissionRestException, NotConnectedRestException;

    @GET
    @Path("connected")
    boolean isConnected(@HeaderParam("sessionid")
    String sessionId);

    @GET
    @Path("workflows")
    public List<Workflow> getWorkflows(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedRestException, IOException;

    @POST
    @Path("workflows")
    @Consumes(APPLICATION_JSON)
    Workflow createWorkflow(@HeaderParam("sessionid")
    String sessionId, Workflow workflow) throws NotConnectedRestException, IOException;

    @PUT
    @Path("workflows/{id}")
    @Consumes(APPLICATION_JSON)
    Workflow updateWorkflow(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String workflowId, Workflow workflow) throws NotConnectedRestException, IOException;

    @DELETE
    @Path("workflows/{id}")
    void deleteWorkflow(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String workflowId) throws NotConnectedRestException, IOException;

    @GET
    @Path("templates")
    public List<Workflow> getTemplates(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedRestException, IOException;

    @POST
    @Path("templates")
    @Consumes(APPLICATION_JSON)
    Workflow createTemplate(@HeaderParam("sessionid")
    String sessionId, Workflow template) throws NotConnectedRestException, IOException;

    @PUT
    @Path("templates/{id}")
    @Consumes(APPLICATION_JSON)
    Workflow updateTemplate(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String templateId, Workflow template) throws NotConnectedRestException, IOException;

    @DELETE
    @Path("templates/{id}")
    void deleteTemplate(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String templateId) throws NotConnectedRestException, IOException;

    @GET
    @Path("scripts")
    List<Script> getScripts(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedRestException, IOException;

    @POST
    @Path("scripts")
    String createScript(@HeaderParam("sessionid")
    String sessionId, @FormParam("name")
    String name, @FormParam("content")
    String content) throws NotConnectedRestException, IOException;

    @POST
    @Path("scripts/{name}")
    String updateScript(@HeaderParam("sessionid")
    String sessionId, @PathParam("name")
    String name, @FormParam("content")
    String content) throws NotConnectedRestException, IOException;

    @GET
    @Path("classes")
    ArrayList<String> getClasses(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedRestException;

    @POST
    @Path("classes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String createClass(@HeaderParam("sessionid")
    String sessionId, MultipartFormDataInput multipart) throws NotConnectedRestException, IOException;

    /**
     * Validates a job.
     * @param multipart a HTTP multipart form which contains the job-descriptor or the job archive file
     * @return the result of job validation
     *
     */
    @POST
    @Path("validate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public abstract JobValidationData validate(MultipartFormDataInput multipart);

    /**
     * Submits a job to the scheduler
     * @param sessionId a valid session id
     * @param multipart a form with the job file as form data
     * @return the <code>jobid</code> of the newly created job
     */
    @POST
    @Path("{path:submit}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public JobIdData submit(@HeaderParam("sessionid")
    String sessionId, @PathParam("path")
    PathSegment pathSegment, MultipartFormDataInput multipart) throws JobCreationRestException,
            NotConnectedRestException, PermissionRestException, SubmissionClosedRestException, IOException;

    @GET
    @Path("visualizations/{id}")
    String getVisualization(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String jobId) throws NotConnectedRestException, IOException;

    @POST
    @Path("visualizations/{id}")
    boolean updateVisualization(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String jobId, @FormParam("visualization")
    String visualization) throws NotConnectedRestException, IOException;

}
