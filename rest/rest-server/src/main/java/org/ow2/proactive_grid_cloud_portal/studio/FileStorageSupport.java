package org.ow2.proactive_grid_cloud_portal.studio;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class FileStorageSupport {

    private static final String USER_WORKFLOWS_PROPERTY = "studio.workflows.user.dir";
    private static final String TEMPLATE_WORKFLOWS_PROPERTY = "studio.workflows.template.dir";
    private static final String REST_CONFIG_PATH = "/config/web/settings.ini";

    private final static Logger logger = Logger.getLogger(FileStorageSupport.class);

    private String userWorkflowsDir;
    private String templatesWorkflowsDir;

    public FileStorageSupport() {
        logger.info("Initializing the studio rest api directories");
        File restPropertiesFile = new File(getSchedulerHome() + REST_CONFIG_PATH);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(restPropertiesFile));
            userWorkflowsDir = properties.getProperty(USER_WORKFLOWS_PROPERTY);
            templatesWorkflowsDir = properties.getProperty(TEMPLATE_WORKFLOWS_PROPERTY);

            if (!new File(userWorkflowsDir).isAbsolute()) {
                userWorkflowsDir = getSchedulerHome() + "/" + userWorkflowsDir;
            }
            if (!new File(templatesWorkflowsDir).isAbsolute()) {
                templatesWorkflowsDir = getSchedulerHome() + "/" + templatesWorkflowsDir;
            }

            logger.info("Web studio user workflows dir: " + userWorkflowsDir);
            logger.info("Web studio template workflows dir: " + templatesWorkflowsDir);

            if (!new File(userWorkflowsDir).exists()) {
                new File(userWorkflowsDir).mkdirs();
            }

        } catch (IOException e) {
            logger.warn("Could not find REST properties" + restPropertiesFile, e);
        }
    }

    public String getUserWorkflowsDir() {
        return userWorkflowsDir;
    }

    public WorkflowStorage getTemplateStorage() {
        return new WorkflowStorage(new File(templatesWorkflowsDir));
    }

    public WorkflowStorage getWorkflowStorage(String userName) {
        File workflowsDir = new File(userWorkflowsDir + "/" + userName + "/workflows/");
        return new WorkflowStorage(workflowsDir);
    }

    private static String getSchedulerHome() {
        if (PASchedulerProperties.SCHEDULER_HOME.isSet()) {
            return PASchedulerProperties.SCHEDULER_HOME.getValueAsString();
        } else {
            return ".";
        }
    }
}
