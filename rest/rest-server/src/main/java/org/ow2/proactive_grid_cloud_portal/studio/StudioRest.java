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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.security.auth.login.LoginException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;

import org.apache.commons.io.FileUtils;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateRest;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

public class StudioRest implements StudioInterface {

    private final static Logger logger = Logger.getLogger(StudioRest.class);
    private SchedulerStateRest schedulerRest = null;

    private SchedulerRestInterface scheduler() {
        if (schedulerRest == null) {
            schedulerRest = new SchedulerStateRest();
        }
        return schedulerRest;
    }

    private static final FileStorageSupport fileStorage = new FileStorageSupport();


    private String getUserName(String sessionId) throws NotConnectedException {
        Session ss = SharedSessionStore.getInstance().get(sessionId);
        if (ss == null) {
            throw new NotConnectedException("you are not connected to the scheduler, you should log on first");
        }
        return ss.getUserName();
    }

    @Override
    public String login(@FormParam("username")
                        String username, @FormParam("password")
                        String password) throws KeyException, LoginException, RMException, ActiveObjectCreationException,
            NodeException, SchedulerRestException {
        logger.info("Logging as " + username);
        return scheduler().login(username, password);
    }

    @Override
    public String loginWithCredential(@MultipartForm
                                      LoginForm multipart) throws ActiveObjectCreationException, NodeException, KeyException, IOException,
            LoginException, RMException, SchedulerRestException {
        logger.info("Logging using credential file");
        return scheduler().loginWithCredential(multipart);
    }

    @Override
    public void logout(@HeaderParam("sessionid")
                       final String sessionId) throws PermissionRestException, NotConnectedRestException {
        logger.info("logout");
        scheduler().disconnect(sessionId);
    }

    @Override
    public boolean isConnected(@HeaderParam("sessionid")
                               String sessionId) {
        try {
            getUserName(sessionId);
            return true;
        } catch (NotConnectedException e) {
            return false;
        }
    }

    @Override
    public List<Workflow> getWorkflows(@HeaderParam("sessionid")
                                       String sessionId) throws NotConnectedException {
        String userName = getUserName(sessionId);
        logger.info("Reading workflows as " + userName);
        return fileStorage.getWorkflowStorage(userName).read();
    }

    @Override
    public Workflow createWorkflow(@HeaderParam("sessionid")
                                   String sessionId, Workflow workflow) throws NotConnectedException {
        String userName = getUserName(sessionId);
        logger.info("Creating workflow as " + userName);
        return fileStorage.getWorkflowStorage(userName).store(workflow);
    }

    @Override
    public Workflow updateWorkflow(@HeaderParam("sessionid")
                                   String sessionId, @PathParam("id")
                                   String workflowId, Workflow workflow) throws NotConnectedException, IOException {
        String userName = getUserName(sessionId);
        logger.info("Updating workflow " + workflowId + " as " + userName);
        return fileStorage.getWorkflowStorage(userName).update(workflowId, workflow);
    }

    @Override
    public void deleteWorkflow(@HeaderParam("sessionid")
                               String sessionId, @PathParam("id")
                               String workflowId) throws NotConnectedException, IOException {
        String userName = getUserName(sessionId);
        logger.info("Deleting workflow " + workflowId + " as " + userName);
        fileStorage.getWorkflowStorage(userName).delete(workflowId);
    }

    @Override
    public List<Workflow> getTemplates(@HeaderParam("sessionid")
                                       String sessionId) throws NotConnectedException {
        return fileStorage.getTemplateStorage().read();
    }

    @Override
    public Workflow createTemplate(@HeaderParam("sessionid")
                                   String sessionId, Workflow template) throws NotConnectedException {
        return fileStorage.getTemplateStorage().store(template);
    }

    @Override
    public Workflow updateTemplate(@HeaderParam("sessionid")
                                   String sessionId, @PathParam("id")
                                   String templateId, Workflow template) throws NotConnectedException, IOException {
        return fileStorage.getTemplateStorage().update(templateId, template);
    }

    @Override
    public void deleteTemplate(@HeaderParam("sessionid")
                               String sessionId, @PathParam("id")
                               String templateId) throws NotConnectedException, IOException {
        fileStorage.getTemplateStorage().delete(templateId);
    }

    @Override
    public ArrayList<Script> getScripts(@HeaderParam("sessionid")
                                        String sessionId) throws NotConnectedException {
        String userName = getUserName(sessionId);
        File scriptDir = new File(fileStorage.getUserWorkflowsDir() + "/" + userName +
                "/scripts");

        if (!scriptDir.exists()) {
            logger.info("Creating dir " + scriptDir.getAbsolutePath());
            scriptDir.mkdirs();
        }

        ArrayList<Script> scripts = new ArrayList<Script>();
        for (File f : scriptDir.listFiles()) {

            Script script = new Script();

            script.setName(f.getName());
            script.setAbsolutePath(f.getAbsolutePath());
            script.setContent(FileUtil.getFileContent(f.getAbsolutePath()));
            scripts.add(script);
        }

        logger.info(scripts.size() + " scripts found");
        return scripts;
    }

    @Override
    public String createScript(@HeaderParam("sessionid")
                               String sessionId, @FormParam("name")
                               String name, @FormParam("content")
                               String content) throws NotConnectedException {
        String userName = getUserName(sessionId);
        logger.info("Creating script " + name + " as " + userName);
        File scriptDir = new File(fileStorage.getUserWorkflowsDir() + "/" + userName +
                "/scripts");
        String fileName = scriptDir.getAbsolutePath() + "/" + name;
        FileUtil.writeFileContent(fileName, content);
        return fileName;
    }

    @Override
    public String updateScript(@HeaderParam("sessionid")
                               String sessionId, @PathParam("name")
                               String name, @FormParam("content")
                               String content) throws NotConnectedException {

        return createScript(sessionId, name, content);
    }

    @Override
    public ArrayList<String> getClasses(@HeaderParam("sessionid")
                                        String sessionId) throws NotConnectedException {
        String userName = getUserName(sessionId);
        File classesDir = new File(fileStorage.getUserWorkflowsDir() + "/" + userName +
                "/classes");

        ArrayList<String> classes = new ArrayList<String>();
        if (classesDir.exists()) {
            File[] jars = classesDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }
            });

            for (File jar : jars) {
                try {
                    JarFile jarFile = new JarFile(jar.getAbsolutePath());
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
                    logger.warn("Could not read jar file " + jar, e);
                }
            }
        }

        return classes;

    }

    @Override
    public String createClass(@HeaderParam("sessionid")
                              String sessionId, MultipartFormDataInput input) throws NotConnectedException, IOException {

        String userName = getUserName(sessionId);
        File classesDir = new File(fileStorage.getUserWorkflowsDir() + "/" + userName +
                "/classes");

        if (!classesDir.exists()) {
            logger.info("Creating dir " + classesDir.getAbsolutePath());
            classesDir.mkdirs();
        }

        String fileName = "";

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        String name = uploadForm.keySet().iterator().next();
        List<InputPart> inputParts = uploadForm.get(name);

        for (InputPart inputPart : inputParts) {

            try {
                //convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                byte[] bytes = IOUtils.toByteArray(inputStream);

                //constructs upload file path
                fileName = classesDir.getAbsolutePath() + "/" + name;

                FileUtils.writeByteArrayToFile(new File(fileName), bytes);
            } catch (IOException e) {
                logger.warn("Could not read input part", e);
                throw e;
            }

        }

        return fileName;
    }

    @Override
    public JobValidationData validate(MultipartFormDataInput multipart) {
        return scheduler().validate(multipart);
    }

    @Override
    public JobIdData submit(@HeaderParam("sessionid")
                            String sessionId, @PathParam("path")
                            PathSegment pathSegment, MultipartFormDataInput multipart) throws JobCreationRestException,
            NotConnectedRestException, PermissionRestException, SubmissionClosedRestException, IOException {
        return scheduler().submit(sessionId, pathSegment, multipart);
    }

    @Override
    public String getVisualization(@HeaderParam("sessionid")
                                   String sessionId, @PathParam("id")
                                   String jobId) throws NotConnectedException {
        File visualizationFile = new File(PortalConfiguration.jobIdToPath(jobId) + ".html");
        if (visualizationFile.exists()) {
            return FileUtil.getFileContent(visualizationFile.getAbsolutePath());
        }
        return "";
    }

    @Override
    public boolean updateVisualization(@HeaderParam("sessionid")
                                       String sessionId, @PathParam("id")
                                       String jobId, @FormParam("visualization")
                                       String visualization) throws NotConnectedException {
        File visualizationFile = new File(PortalConfiguration.jobIdToPath(jobId) + ".html");
        if (visualizationFile.exists()) {
            visualizationFile.delete();
        }
        FileUtil.writeFileContent(visualizationFile.getAbsolutePath(), visualization);
        return true;
    }
}
