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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.security.auth.login.LoginException;
import javax.ws.rs.core.PathSegment;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.common.SessionStore;
import org.ow2.proactive_grid_cloud_portal.common.SharedSessionStore;
import org.ow2.proactive_grid_cloud_portal.common.dto.LoginForm;
import org.ow2.proactive_grid_cloud_portal.scheduler.SchedulerStateRest;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.JobCreationRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.PermissionRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.RestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SchedulerRestException;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.SubmissionClosedRestException;
import org.ow2.proactive_grid_cloud_portal.studio.storage.FileStorage;
import org.ow2.proactive_grid_cloud_portal.studio.storage.FileStorageSupport;
import org.ow2.proactive_grid_cloud_portal.studio.storage.FileStorageSupportFactory;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;


public class StudioRest implements StudioInterface {

    public static final String YOU_ARE_NOT_CONNECTED_TO_THE_SCHEDULER_YOU_SHOULD_LOG_ON_FIRST = "You are not connected to the scheduler, you should log on first";

    private static final Logger logger = Logger.getLogger(StudioRest.class);

    private static final String FILE_ENCODING = PASchedulerProperties.FILE_ENCODING.getValueAsString();

    private SchedulerStateRest schedulerRest = null;

    private SessionStore sessions = SharedSessionStore.getInstance();

    private SchedulerRestInterface scheduler() {
        if (schedulerRest == null) {
            schedulerRest = new SchedulerStateRest();
        }
        return schedulerRest;
    }

    public static FileStorageSupport getFileStorageSupport() {
        return FileStorageSupportFactory.getInstance();
    }

    private String getUserName(String sessionId) throws NotConnectedRestException {
        Session session = sessions.get(sessionId);
        return session.getUserName();
    }

    @Override
    public String login(String username, String password) throws LoginException, SchedulerRestException {
        logger.info("Logging as " + username);
        return scheduler().login(username, password);
    }

    @Override
    public String loginWithCredential(LoginForm multipart) throws KeyException, LoginException, SchedulerRestException {
        logger.info("Logging using credential file");
        return scheduler().loginWithCredential(multipart);
    }

    @Override
    public void logout(String sessionId) throws RestException {
        logger.info("logout");
        scheduler().disconnect(sessionId);
    }

    @Override
    public boolean isConnected(String sessionId) {
        try {
            getUserName(sessionId);
            return true;
        } catch (NotConnectedRestException e) {
            return false;
        }
    }

    @Override
    public String currentUser(String sessionId) {
        try {
            return getUserName(sessionId);
        } catch (NotConnectedRestException e) {
            return null;
        }
    }

    @Override
    public UserData currentUserData(String sessionId) {
        return scheduler().getUserDataFromSessionId(sessionId);
    }

    @Override
    public List<Workflow> getWorkflows(String sessionId) throws NotConnectedRestException, IOException {
        String userName = getUserName(sessionId);
        logger.info("Reading workflows as " + userName);
        return getFileStorageSupport().getWorkflowStorage(userName).readAll();
    }

    @Override
    public Workflow createWorkflow(String sessionId, Workflow workflow) throws NotConnectedRestException, IOException {
        String userName = getUserName(sessionId);
        logger.info("Creating workflow as " + userName);
        return getFileStorageSupport().getWorkflowStorage(userName).store(workflow);
    }

    @Override
    public Workflow getWorkflow(String sessionId, String workflowId) throws NotConnectedRestException, IOException {
        String userName = getUserName(sessionId);
        return getFileStorageSupport().getWorkflowStorage(userName).read(workflowId);
    }

    @Override
    public String getWorkflowXmlContent(String sessionId, String workflowId)
            throws NotConnectedRestException, IOException {
        String userName = getUserName(sessionId);
        return getFileStorageSupport().getWorkflowStorage(userName).read(workflowId).getXml();
    }

    @Override
    public Workflow updateWorkflow(String sessionId, String workflowId, Workflow workflow)
            throws NotConnectedRestException, IOException {
        String userName = getUserName(sessionId);
        logger.info("Updating workflow " + workflowId + " as " + userName);
        return getFileStorageSupport().getWorkflowStorage(userName).update(workflowId, workflow);
    }

    @Override
    public void deleteWorkflow(String sessionId, String workflowId) throws NotConnectedRestException, IOException {
        String userName = getUserName(sessionId);
        logger.info("Deleting workflow " + workflowId + " as " + userName);
        getFileStorageSupport().getWorkflowStorage(userName).delete(workflowId);
    }

    @Override
    public List<Script> getScripts(String sessionId) throws NotConnectedRestException, IOException {
        String userName = getUserName(sessionId);
        return getFileStorageSupport().getScriptStorage(userName).readAll();
    }

    @Override
    public String createScript(String sessionId, String name, String content)
            throws NotConnectedRestException, IOException {
        String userName = getUserName(sessionId);
        FileStorage<Script> scriptStorage = getFileStorageSupport().getScriptStorage(userName);
        Script storedScript = scriptStorage.store(new Script(name, content));
        return storedScript.getAbsolutePath();
    }

    @Override
    public String updateScript(String sessionId, String name, String content)
            throws NotConnectedRestException, IOException {

        return createScript(sessionId, name, content);
    }

    @Override
    public ArrayList<String> getClasses(String sessionId) throws NotConnectedRestException {
        String userName = getUserName(sessionId);
        File classesDir = new File(getFileStorageSupport().getWorkflowsDir(userName), "classes");

        ArrayList<String> classes = new ArrayList<>();
        if (classesDir.exists()) {
            File[] jars = classesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));

            for (File jar : jars) {
                try (JarFile jarFile = new JarFile(jar.getAbsolutePath())) {
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
    public String createClass(String sessionId, MultipartFormDataInput input)
            throws NotConnectedRestException, IOException {
        try {
            String userName = getUserName(sessionId);
            File classesDir = new File(getFileStorageSupport().getWorkflowsDir(userName), "classes");

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
                    fileName = classesDir.getAbsolutePath() + File.separator + name;

                    FileUtils.writeByteArrayToFile(new File(fileName), bytes);
                } catch (IOException e) {
                    logger.warn("Could not read input part", e);
                    throw e;
                }

            }

            return fileName;
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    @Override
    public JobValidationData validate(String sessionId, PathSegment pathSegment, MultipartFormDataInput multipart)
            throws NotConnectedRestException {
        return scheduler().validate(sessionId, pathSegment, multipart);
    }

    @Override
    public JobIdData submit(String sessionId, PathSegment pathSegment, MultipartFormDataInput multipart)
            throws JobCreationRestException, NotConnectedRestException, PermissionRestException,
            SubmissionClosedRestException, IOException {
        return scheduler().submit(sessionId, pathSegment, multipart, null);
    }

    @Override
    public String submitPlannings(String sessionId, PathSegment pathSegment, Map<String, String> jobContentXmlString)
            throws JobCreationRestException, NotConnectedRestException, PermissionRestException,
            SubmissionClosedRestException, IOException {
        return scheduler().submitPlannings(sessionId, pathSegment, jobContentXmlString);
    }

    @Override
    public String getVisualization(String sessionId, String jobId) throws IOException {
        File visualizationFile = new File(PortalConfiguration.jobIdToPath(jobId) + ".html");
        if (visualizationFile.exists()) {
            return FileUtils.readFileToString(new File(visualizationFile.getAbsolutePath()),
                                              Charset.forName(FILE_ENCODING));
        }
        return "";
    }

    @Override
    public boolean updateVisualization(String sessionId, String jobId, String visualization) throws IOException {
        File visualizationFile = new File(PortalConfiguration.jobIdToPath(jobId) + ".html");
        Files.deleteIfExists(visualizationFile.toPath());
        FileUtils.write(new File(visualizationFile.getAbsolutePath()), visualization, Charset.forName(FILE_ENCODING));
        return true;
    }
}
