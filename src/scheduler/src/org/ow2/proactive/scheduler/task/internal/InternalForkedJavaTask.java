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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.internal;

import java.io.BufferedReader;
import java.io.FileReader;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutableContainer;
import org.ow2.proactive.scheduler.task.launcher.ForkedJavaTaskLauncher;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * Description of a java task at internal level.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
@Entity
@Table(name = "INTERNAL_FORKED_TASK")
@MappedSuperclass
@AccessType("field")
@Proxy(lazy = false)
@XmlAccessorType(XmlAccessType.FIELD)
public class InternalForkedJavaTask extends InternalJavaTask {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    /** Policy content for the forked VM (declared as static element to be cached) */
    @Transient
    private static StringBuilder policyContent = null;

    /** Log4j content for the forked VM (declared as static element to be cached) */
    @Transient
    private static StringBuilder log4JContent = null;

    /** PAConfiguration content for the forked VM (declared as static element to be cached) */
    @Transient
    private static StringBuilder paConfigContent = null;

    /**
     * ProActive empty constructor
     */
    public InternalForkedJavaTask() {
    }

    /**
     * Create a new Java task descriptor using instantiated java task.
     *
     * @param execContainer the forked Java Executable Container
     */
    public InternalForkedJavaTask(ForkedJavaExecutableContainer execContainer) {
        this.executableContainer = execContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskLauncher createLauncher(InternalJob job, Node node) throws ActiveObjectCreationException,
            NodeException {

        TaskLauncherInitializer tli = getDefaultTaskLauncherInitializer(job);
        tli.setPolicyContent(getJavaPolicy());
        tli.setLog4JContent(getLog4J());
        tli.setPaConfigContent(getPAConfiguration());
        logger_dev.info("Create forked java task launcher");
        TaskLauncher launcher = (TaskLauncher) PAActiveObject.newActive(ForkedJavaTaskLauncher.class
                .getName(), new Object[] { tli }, node);
        setExecuterInformations(new ExecuterInformations(launcher, node));

        return launcher;
    }

    /**
     * Return the content of the forked java policy or a default one if not found.
     *
     * @return the content of the forked java policy or a default one if not found.
     */
    private static String getJavaPolicy() {
        if (policyContent == null) {
            try {
                policyContent = getFileContent(PASchedulerProperties
                        .getAbsolutePath(PASchedulerProperties.SCHEDULER_DEFAULT_FJT_SECURITY_POLICY
                                .getValueAsString()));
            } catch (Exception e) {
                logger_dev.error("Policy file not read, applying default basic permission", e);
                policyContent = new StringBuilder("grant {\npermission java.security.BasicPermission;\n};\n");
            }
        }
        return policyContent.toString();
    }

    /**
     * Return the content of the log4J file or a default one if not found.
     *
     * @return the content of the log4J file or a default one if not found.
     */
    private static String getLog4J() {
        if (log4JContent == null) {
            try {
                log4JContent = getFileContent(PASchedulerProperties
                        .getAbsolutePath(PASchedulerProperties.SCHEDULER_DEFAULT_FJT_LOG4J.getValueAsString()));
            } catch (Exception e) {
                logger_dev.error("Log4J file not read, applying default basic content", e);
                //default ProActive log4j file is not suitable because CONSOLE appender is not supported under windows
                log4JContent = new StringBuilder("log4j.rootLogger=INFO,NULL\n");
            }
        }
        return log4JContent.toString();
    }

    /**
     * Return the content of the PAConfiguration file or a default one if not found.
     *
     * @return the content of the PAConfiguration file or a default one if not found.
     */
    private static String getPAConfiguration() {
        if (paConfigContent == null) {
            try {
                paConfigContent = getFileContent(PASchedulerProperties
                        .getAbsolutePath(PASchedulerProperties.SCHEDULER_DEFAULT_FJT_PAConfig
                                .getValueAsString()));
            } catch (Exception e) {
                logger_dev.error("PAConfiguration file not read, applying default basic content", e);
                paConfigContent = new StringBuilder("<ProActiveUserProperties>\n" + "<properties>\n"
                    + "<prop key=\"proactive.communication.protocol\" value=\"http\"/>\n" + "</properties>\n"
                    + "</ProActiveUserProperties>\n");
            }
        }
        return paConfigContent.toString();
    }

    /**
     * Return the content of a file in a string
     *
     * @param filePath the file path to be read
     * @return a string that represents the content of the given filePath
     * @throws Exception if an exception occurs while reading file
     */
    private static StringBuilder getFileContent(String filePath) throws Exception {
        BufferedReader brin = null;
        try {
            StringBuilder fileContent = new StringBuilder("");
            brin = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = brin.readLine()) != null) {
                fileContent.append(line + "\n");
            }
            return fileContent;
        } finally {
            if (brin != null) {
                brin.close();
            }
        }
    }
}
