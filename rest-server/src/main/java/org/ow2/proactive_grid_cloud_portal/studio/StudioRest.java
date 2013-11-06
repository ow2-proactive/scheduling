/*
 * ################################################################
 *
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.studio;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSession;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSessionMapper;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateRest;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;

import javax.security.auth.login.LoginException;
import javax.ws.rs.*;
import java.io.*;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Scanner;


@Path("/studio")
public class StudioRest implements StudioInterface {

    private static String PROJECT_NAME_PROPERTY = "proactive.projects.dir";
    private SchedulerStateRest schedulerRest = null;

    private SchedulerRestInterface scheduler() {
        if (schedulerRest==null) {
            schedulerRest = new SchedulerStateRest();
        }

        return schedulerRest;
    }

    private String getProjectsDir() {
        String projectDir = System.getProperty(PROJECT_NAME_PROPERTY);
        if (projectDir==null) {
            projectDir = System.getProperty("java.io.tmpdir");
        }
        return projectDir;
    }

    private String getUserName(String sessionId) throws NotConnectedException {
        SchedulerSession ss = SchedulerSessionMapper.getInstance().getSchedulerSession(sessionId);
        if (ss == null) {
            // logger.trace("not found a scheduler frontend for sessionId " +
            // sessionId);
            throw new NotConnectedException(
                    "you are not connected to the scheduler, you should log on first");
        }
        return ss.getUserName();
    }


    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    private void writeFileContent(String fileName, String content) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fileName);
            writer.println(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    private String getFileContent(String fileName) {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new FileReader(fileName));
            char[] buf = new char[1024];
            int numRead=0;
            while((numRead=reader.read(buf)) != -1){
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {}
        }
        return fileData.toString().trim();
    }

    @Override
    public String login(@FormParam("username") String username, @FormParam("password") String password) throws KeyException, LoginException, RMException, ActiveObjectCreationException, NodeException, SchedulerRestException {
        System.out.println("Logging as " + username);
        return scheduler().login(username, password);
    }

    @Override
    public String loginWithCredential(@MultipartForm LoginForm multipart) throws ActiveObjectCreationException, NodeException, KeyException, IOException, LoginException, RMException, SchedulerRestException {
        System.out.println("Logging using credential file");
        return scheduler().loginWithCredential(multipart);
    }

    @Override
    @GET
    @Path("connected")
    @Produces("application/json")
    public boolean isConnected(@HeaderParam("sessionid") String sessionId) {
        try {
            getUserName(sessionId);
            return true;
        } catch (NotConnectedException e) {
            return false;
        }
    }

    @Override
    @GET
    @Path("workflows")
    @Produces("application/json")
    public ArrayList<Workflow> getWorkflows(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        String userName = getUserName(sessionId);
        File folder = new File(getProjectsDir()+"/"+userName);
        System.out.println("Getting workflows as " + userName);

        if (!folder.exists()) {
            System.out.println("Creating dir " + folder.getAbsolutePath());
            folder.mkdirs();
        }

        ArrayList<Workflow> projects = new ArrayList<Workflow>();
        for (File f: folder.listFiles()) {
            if (f.isDirectory()) {
                File nameFile = new File(f.getAbsolutePath() + "/name");

                if (nameFile.exists()) {

                    Workflow wf = new Workflow();
                    wf.setId(Integer.parseInt(f.getName()));
                    wf.setName(getFileContent(nameFile.getAbsolutePath()));

                    File xmlFile = new File(f.getAbsolutePath() + "/" + wf.getName() + ".xml");
                    if (xmlFile.exists()) {
                        wf.setXml(getFileContent(xmlFile.getAbsolutePath()));
                    }
                    File metadataFile = new File(f.getAbsolutePath() + "/" + wf.getName() + "metadata");
                    if (metadataFile.exists()) {
                        wf.setMetadata(getFileContent(metadataFile.getAbsolutePath()));
                    }

                    projects.add(wf);
                }
            }
        }

        System.out.println(projects.size() + " workflows found");
        return projects;
    }

    @Override
    @POST
    @Path("workflows")
    @Produces("application/json")
    public long createWorkflow(@HeaderParam("sessionid") String sessionId,
                               @FormParam("name") String name, @FormParam("xml") String xml, @FormParam("metadata") String metadata) throws NotConnectedException {
        String userName = getUserName(sessionId);

        System.out.println("Creating workflow as " + userName);
        String projectsFolder = getProjectsDir()+"/"+userName + "/";
        File projectsFolderFile = new File(projectsFolder);

        if (!projectsFolderFile.exists()) {
            System.out.println("Creating dir " + projectsFolderFile.getAbsolutePath());
            projectsFolderFile.mkdirs();
        }

        int projectId = 1;
        while (new File(projectsFolder + projectId).exists()) {
            projectId ++;
        }

        File newWorkflowFile = new File(projectsFolder + projectId);
        System.out.println("Creating dir " + newWorkflowFile.getAbsolutePath());
        newWorkflowFile.mkdirs();

        System.out.println("Writing file " + newWorkflowFile.getAbsolutePath() + "/name");
        writeFileContent(newWorkflowFile.getAbsolutePath() + "/name", name);
        System.out.println("Writing file " + newWorkflowFile.getAbsolutePath() + "/metadata");
        writeFileContent(newWorkflowFile.getAbsolutePath() + "/metadata", metadata);
        System.out.println("Writing file " + newWorkflowFile.getAbsolutePath() + "/" + name + ".xml");
        writeFileContent(newWorkflowFile.getAbsolutePath() + "/" + name + ".xml", xml);

        return projectId;
    }

    @Override
    @POST
    @Path("workflows/{id}")
    @Produces("application/json")
    public boolean updateWorkflow(@HeaderParam("sessionid") String sessionId, @PathParam("id") String workflowId,
                                  @FormParam("name") String name, @FormParam("xml") String xml, @FormParam("metadata") String metadata) throws NotConnectedException {
        String userName = getUserName(sessionId);

        System.out.println("Updating workflow " + workflowId + " as " + userName);
        String projectsFolder = getProjectsDir()+"/"+userName + "/";
        File workflowFile = new File(projectsFolder + workflowId);

        String oldJobName = getFileContent(workflowFile.getAbsolutePath() + "/name");
        if (name != null && !name.equals(oldJobName)) {
            // new job name
            System.out.println("Updating job name from " + oldJobName + " to " + name);
            System.out.println("Writing file " + workflowFile.getAbsolutePath()+"/name");
            writeFileContent(workflowFile.getAbsolutePath()+"/name", name);
            System.out.println("Deleting file " + workflowFile.getAbsolutePath()+"/"+oldJobName+".xml");
            new File(workflowFile.getAbsolutePath()+"/"+oldJobName+".xml").delete();
        }

        System.out.println("Writing file " + workflowFile.getAbsolutePath()+"/metadata");
        writeFileContent(workflowFile.getAbsolutePath()+"/metadata", metadata);
        System.out.println("Writing file " + workflowFile.getAbsolutePath()+"/"+name+".xml");
        writeFileContent(workflowFile.getAbsolutePath()+"/"+name+".xml", xml);

        return true;
    }

    @Override
    @DELETE
    @Path("workflows/{id}")
    @Produces("application/json")
    public boolean deleteWorkflow(@HeaderParam("sessionid") String sessionId, @PathParam("id") String workflowId) throws NotConnectedException, IOException {
        String userName = getUserName(sessionId);

        System.out.println("Deleting workflow " + workflowId + " as " + userName);
        File workflowFolder = new File(getProjectsDir()+"/"+userName + "/" + workflowId);

        if (workflowFolder.exists()) {
            System.out.println("Removing dir " + workflowFolder.getAbsolutePath());
            delete(workflowFolder);
            return true;
        }
        return false;
    }

    @Override
    @GET
    @Path("scripts")
    @Produces("application/json")
    public ArrayList<Script> getScripts(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        String userName = getUserName(sessionId);
        File folder = new File(getProjectsDir()+"/"+userName);
        System.out.println("Getting workflows as " + userName);

        if (!folder.exists()) {
            System.out.println("Creating dir " + folder.getAbsolutePath());
            folder.mkdirs();
        }

        File scriptDir = new File(folder.getAbsolutePath());

        if (!scriptDir.exists()) {
            System.out.println("Creating dir " + scriptDir.getAbsolutePath());
            scriptDir.mkdirs();
        }

        ArrayList<Script> scripts = new ArrayList<Script>();
        for (File f: scriptDir.listFiles()) {

            Script script = new Script();

            script.setName(f.getName());
            script.setAbsolutePath(f.getAbsolutePath());
            script.setContent(getFileContent(f.getAbsolutePath()));
            scripts.add(script);
        }

        System.out.println(scripts.size() + " scripts found");
        return scripts;

    }

    @Override
    @POST
    @Path("scripts")
    @Produces("application/json")
    public boolean createScript(@HeaderParam("sessionid") String sessionId,
                      @FormParam("name") String name, @FormParam("content") String content) throws NotConnectedException {

        String userName = getUserName(sessionId);
        System.out.println("Creating script " + name + " as " + userName);
        File folder = new File(getProjectsDir()+"/"+userName);
        File scriptDir = new File(folder.getAbsolutePath());
        writeFileContent(name, content);
        return true;
    }

    @Override
    @POST
    @Path("scripts/{name}")
    @Produces("application/json")
    public boolean updateWorkflow(@HeaderParam("sessionid") String sessionId,
                           @PathParam("name") String name,
                           @FormParam("content") String content) throws NotConnectedException {

        return createScript(sessionId, name, content);
    }

}
