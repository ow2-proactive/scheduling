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
package org.ow2.proactive_grid_cloud_portal.studio.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class FileStorageSupportFactory {
    private final static Logger logger = Logger.getLogger(FileStorageSupportFactory.class);

    private static final String USER_WORKFLOWS_PROPERTY = "studio.workflows.user.dir";

    private static final String TEMPLATE_WORKFLOWS_PROPERTY = "studio.workflows.template.dir";

    private static final String REST_CONFIG_PATH = "config/web/settings.ini";

    private static volatile FileStorageSupport fileStorageSupport = createFromConfig();

    public static FileStorageSupport getInstance() {
        return fileStorageSupport;
    }

    private static FileStorageSupport createFromConfig() {
        logger.info("Initializing the studio rest api directories");

        Properties properties = getProperties();
        String userWorkflowsDir = properties.getProperty(USER_WORKFLOWS_PROPERTY, "data/defaultuser/");
        String templateWorkflowsDir = properties.getProperty(TEMPLATE_WORKFLOWS_PROPERTY,
                                                             "config/workflows/templates/");

        userWorkflowsDir = relativeToHomeIfNotAbsolute(userWorkflowsDir);
        templateWorkflowsDir = relativeToHomeIfNotAbsolute(templateWorkflowsDir);

        createIfNotExists(userWorkflowsDir);
        createIfNotExists(templateWorkflowsDir);

        logger.info("Web studio user workflows dir: " + userWorkflowsDir);
        logger.info("Web studio template workflows dir: " + templateWorkflowsDir);

        return new FileStorageSupport(new File(userWorkflowsDir), new File(templateWorkflowsDir));
    }

    private static String relativeToHomeIfNotAbsolute(String path) {
        if (!new File(path).isAbsolute()) {
            path = getSchedulerHome() + "/" + path;
        }
        return path;
    }

    private static void createIfNotExists(String userWorkflowsDir) {
        if (!new File(userWorkflowsDir).exists()) {
            new File(userWorkflowsDir).mkdirs();
        }
    }

    private static Properties getProperties() {
        File restPropertiesFile = new File(getSchedulerHome(), REST_CONFIG_PATH);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(restPropertiesFile));
        } catch (IOException e) {
            logger.warn("Could not load properties from file " + restPropertiesFile, e);
        }
        return properties;
    }

    private static String getSchedulerHome() {
        if (PASchedulerProperties.SCHEDULER_HOME.isSet()) {
            return PASchedulerProperties.SCHEDULER_HOME.getValueAsString();
        } else {
            return ".";
        }
    }
}
