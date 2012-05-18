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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.launcher.NativeTaskLauncher;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * Description of a native task.
 * This task include the process
 * see also {@link InternalTask}
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@Entity
@Table(name = "I_NTVTASK")
@MappedSuperclass
@AccessType("field")
@Proxy(lazy = false)
@XmlRootElement(name = "task")
@XmlAccessorType(XmlAccessType.FIELD)
public class InternalNativeTask extends InternalTask {

    private static final long serialVersionUID = 32L;
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    @Id
    @GeneratedValue
    @XmlTransient
    protected long hId;

    /**
     * ProActive empty constructor.
     */
    public InternalNativeTask() {
    }

    /**
     * Create a new native task descriptor with the given command line.
     *
     * @param execContainer the Native Executable Container
     */
    public InternalNativeTask(NativeExecutableContainer execContainer) {
        this.executableContainer = execContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskLauncher createLauncher(InternalJob job, Node node) throws ActiveObjectCreationException,
            NodeException {
        NativeTaskLauncher launcher;

        logger_dev.info("Create native task launcher");
        TaskLauncherInitializer tli = getDefaultTaskLauncherInitializer(job);
        launcher = (NativeTaskLauncher) PAActiveObject.newActive(NativeTaskLauncher.class.getName(),
                new Object[] { tli }, node);

        setExecuterInformations(new ExecuterInformations(launcher, node));

        return launcher;
    }

    /**
     * {@inheritDoc}
     */
    public boolean handleResultsArguments() {
        return true; // Needed for exported properties
    }

}
