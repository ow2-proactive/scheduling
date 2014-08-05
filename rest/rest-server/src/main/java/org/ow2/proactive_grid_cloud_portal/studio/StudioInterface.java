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

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
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


@Path("/studio")
public interface StudioInterface {
    @POST
    @Path("login")
    @Produces("application/json")
    String login(@FormParam("username")
    String username, @FormParam("password")
    String password) throws KeyException, LoginException, RMException, ActiveObjectCreationException,
            NodeException, SchedulerRestException;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("login")
    @Produces("application/json")
    String loginWithCredential(@MultipartForm
    LoginForm multipart) throws ActiveObjectCreationException, NodeException, KeyException, IOException,
            LoginException, RMException, SchedulerRestException;

    @PUT
    @Path("logout")
    @Produces("application/json")
    public void logout(@HeaderParam(
      "sessionid") final String sessionId) throws PermissionRestException, NotConnectedRestException;

    @GET
    @Path("connected")
    @Produces("application/json")
    boolean isConnected(@HeaderParam("sessionid")
    String sessionId);

    @GET
    @Path("workflows")
    @Produces("application/json")
    ArrayList<Workflow> getWorkflows(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException;

    @POST
    @Path("workflows")
    @Produces("application/json")
    long createWorkflow(@HeaderParam("sessionid")
    String sessionId, @FormParam("name")
    String name, @FormParam("xml")
    String xml, @FormParam("metadata")
    String metadata) throws NotConnectedException;

    @POST
    @Path("workflows/{id}")
    @Produces("application/json")
    boolean updateWorkflow(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String workflowId, @FormParam("name")
    String name, @FormParam("xml")
    String xml, @FormParam("metadata")
    String metadata) throws NotConnectedException, IOException;

    @DELETE
    @Path("workflows/{id}")
    @Produces("application/json")
    boolean deleteWorkflow(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String workflowId) throws NotConnectedException, IOException;

    @GET
    @Path("scripts")
    @Produces("application/json")
    ArrayList<Script> getScripts(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException;

    @POST
    @Path("scripts")
    @Produces("application/json")
    String createScript(@HeaderParam("sessionid")
    String sessionId, @FormParam("name")
    String name, @FormParam("content")
    String content) throws NotConnectedException;

    @POST
    @Path("scripts/{name}")
    @Produces("application/json")
    String updateScript(@HeaderParam("sessionid")
    String sessionId, @PathParam("name")
    String name, @FormParam("content")
    String content) throws NotConnectedException;

    @GET
    @Path("classes")
    @Produces("application/json")
    ArrayList<String> getClasses(@HeaderParam("sessionid")
    String sessionId) throws NotConnectedException;

    @POST
    @Path("classes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public String createClass(@HeaderParam("sessionid")
    String sessionId, MultipartFormDataInput multipart) throws NotConnectedException, IOException;

    /**
     * Validates a job.
     * @param multipart a HTTP multipart form which contains the job-descriptor or the job archive file
     * @return the result of job validation
     *
     */
    @POST
    @Path("validate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
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
    @Produces("application/json")
    public JobIdData submit(@HeaderParam("sessionid") String sessionId,
            @PathParam("path") PathSegment pathSegment, MultipartFormDataInput multipart)
            throws JobCreationRestException, NotConnectedRestException, PermissionRestException,
            SubmissionClosedRestException, IOException;

    @GET
    @Path("visualizations/{id}")
    @Produces("application/json")
    String getVisualization(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String jobId) throws NotConnectedException;

    @POST
    @Path("visualizations/{id}")
    @Produces("application/json")
    boolean updateVisualization(@HeaderParam("sessionid")
    String sessionId, @PathParam("id")
    String jobId, @FormParam("visualization")
    String visualization) throws NotConnectedException;

}
