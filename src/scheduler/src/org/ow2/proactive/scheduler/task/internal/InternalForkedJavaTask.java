/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
public class InternalForkedJavaTask extends InternalJavaTask {

    /**  */
	private static final long serialVersionUID = 21L;

	public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    /** Policy content for the forked VM (declared as static element to be done once) */
    @Transient
    private static StringBuilder policyContent = null;

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
                policyContent = new StringBuilder("");
                String forkedPolicyFilePath = PASchedulerProperties
                        .getAbsolutePath(PASchedulerProperties.SCHEDULER_DEFAULT_FJT_SECURITY_POLICY
                                .getValueAsString());
                BufferedReader brin = new BufferedReader(new FileReader(forkedPolicyFilePath));
                String line;
                while ((line = brin.readLine()) != null) {
                    policyContent.append(line + "\n");
                }
            } catch (Exception e) {
                logger_dev.error("Policy file not read, applying default basic permission", e);
                policyContent = new StringBuilder("grant {\npermission java.security.BasicPermission;\n};\n");
            }
        }
        return policyContent.toString();
    }
}
