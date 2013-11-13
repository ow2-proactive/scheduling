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

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSession;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerSessionMapper;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateRest;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.*;

import javax.security.auth.login.LoginException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


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

    private String getProjectsDirName() {
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
        File workflowsDir = new File(getProjectsDirName()+"/"+userName+"/workflows");

        if (!workflowsDir.exists()) {
            System.out.println("Creating dir " + workflowsDir.getAbsolutePath());
            workflowsDir.mkdirs();
        }

        System.out.println("Getting workflows as " + userName);
        ArrayList<Workflow> projects = new ArrayList<Workflow>();
        for (File f: workflowsDir.listFiles()) {
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
                    File metadataFile = new File(f.getAbsolutePath() + "/metadata");
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
        File workflowsDir = new File(getProjectsDirName()+"/"+userName+"/workflows");

        if (!workflowsDir.exists()) {
            System.out.println("Creating dir " + workflowsDir.getAbsolutePath());
            workflowsDir.mkdirs();
        }

        int projectId = 1;
        while (new File(workflowsDir.getAbsolutePath() + "/" + projectId).exists()) {
            projectId ++;
        }

        File newWorkflowFile = new File(workflowsDir.getAbsolutePath() + "/" + projectId);
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
        File workflowsDir = new File(getProjectsDirName()+"/"+userName+"/workflows/"+workflowId);

        String oldJobName = getFileContent(workflowsDir.getAbsolutePath() + "/name");
        if (name != null && !name.equals(oldJobName)) {
            // new job name
            System.out.println("Updating job name from " + oldJobName + " to " + name);
            System.out.println("Writing file " + workflowsDir.getAbsolutePath()+"/name");
            writeFileContent(workflowsDir.getAbsolutePath()+"/name", name);
            System.out.println("Deleting file " + workflowsDir.getAbsolutePath()+"/"+oldJobName+".xml");
            new File(workflowsDir.getAbsolutePath()+"/"+oldJobName+".xml").delete();
        }

        System.out.println("Writing file " + workflowsDir.getAbsolutePath()+"/metadata");
        writeFileContent(workflowsDir.getAbsolutePath()+"/metadata", metadata);
        System.out.println("Writing file " + workflowsDir.getAbsolutePath()+"/"+name+".xml");
        writeFileContent(workflowsDir.getAbsolutePath()+"/"+name+".xml", xml);

        return true;
    }

    @Override
    @DELETE
    @Path("workflows/{id}")
    @Produces("application/json")
    public boolean deleteWorkflow(@HeaderParam("sessionid") String sessionId, @PathParam("id") String workflowId) throws NotConnectedException, IOException {
        String userName = getUserName(sessionId);

        System.out.println("Deleting workflow " + workflowId + " as " + userName);
        File workflowsDir = new File(getProjectsDirName()+"/"+userName+"/workflows/"+workflowId);

        if (workflowsDir.exists()) {
            System.out.println("Removing dir " + workflowsDir.getAbsolutePath());
            delete(workflowsDir);
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
        File scriptDir = new File(getProjectsDirName()+"/"+userName + "/scripts");

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
    public String createScript(@HeaderParam("sessionid") String sessionId,
                      @FormParam("name") String name, @FormParam("content") String content) throws NotConnectedException {
        String userName = getUserName(sessionId);
        System.out.println("Creating script " + name + " as " + userName);
        File scriptDir = new File(getProjectsDirName()+"/"+userName+"/scripts");
        String fileName = scriptDir.getAbsolutePath()+"/"+name;
        writeFileContent(fileName, content);
        return fileName;
    }

    @Override
    @POST
    @Path("scripts/{name}")
    @Produces("application/json")
    public String updateScript(@HeaderParam("sessionid") String sessionId,
                                @PathParam("name") String name,
                                @FormParam("content") String content) throws NotConnectedException {

        return createScript(sessionId, name, content);
    }


    @Override
    @GET
    @Path("classes")
    @Produces("application/json")
    public ArrayList<String> getClasses(@HeaderParam("sessionid") String sessionId) throws NotConnectedException {
        String userName = getUserName(sessionId);
        File classesDir = new File(getProjectsDirName()+"/"+userName + "/classes");

        ArrayList<String> classes = new ArrayList<String>();
        if (classesDir.exists()) {
            File[] jars = classesDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }
            });

            for (File jar :jars) {
                JarFile jarFile = null;
                try {
                    jarFile = new JarFile(jar.getAbsolutePath());
                    Enumeration allEntries = jarFile.entries();
                    while (allEntries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) allEntries.nextElement();
                        String name = entry.getName();
                        if (name.endsWith(".class")) {
                            String noExt = name.substring(0, name.length() - ".class".length());
                            classes.add(noExt.replaceAll("/", "."));
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return classes;

    }

    @POST
    @Path("classes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public String createClass(@HeaderParam("sessionid")
                               String sessionId, MultipartFormDataInput input) throws NotConnectedException, IOException {

        String userName = getUserName(sessionId);
        File classesDir = new File(getProjectsDirName()+"/"+userName + "/classes");

        if (!classesDir.exists()) {
            System.out.println("Creating dir " + classesDir.getAbsolutePath());
            classesDir.mkdirs();
        }

        String fileName = "";

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        String name = uploadForm.keySet().iterator().next();
        List<InputPart> inputParts = uploadForm.get(name);

        for (InputPart inputPart : inputParts) {

            try {

                MultivaluedMap<String, String> header = inputPart.getHeaders();
                //convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class,null);
                byte[] bytes = IOUtils.toByteArray(inputStream);

                //constructs upload file path
                fileName = classesDir.getAbsolutePath() + "/" + name;

                writeFile(bytes,fileName);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }

        }

        return fileName;
    }

    private void writeFile(byte[] content, String filename) throws IOException {

        File file = new File(filename);

        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fop = new FileOutputStream(file);

        try {
            fop.write(content);
        } finally {
            fop.flush();
            fop.close();
        }
    }

    @Override
    public JobValidationData validate(MultipartFormDataInput multipart) {
        return scheduler().validate(multipart);
    }

    @Override
    public JobIdData submit(@HeaderParam("sessionid") String sessionId, MultipartFormDataInput multipart)
            throws IOException, JobCreationRestException,
            NotConnectedRestException, PermissionRestException, SubmissionClosedRestException {
        return scheduler().submit(sessionId, multipart);
    }
}
