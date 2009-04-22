/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.body.future.FutureMonitoring;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.ow2.proactive.scheduler.common.exception.ExecutableCreationException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;


/**
 * This Executable is responsible for executing another executable in a separate JVM. 
 * It receives a reference to a remote taskLauncher object, and delegates execution to this object.
 * 
 * @author The ProActive Team
 *
 */
@Entity
@Table(name = "FORKED_JAVA_EXECUTABLE")
@AccessType("field")
@Proxy(lazy = false)
public class ForkedJavaExecutable extends JavaExecutable implements ExecutableContainer {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JavaExecutableContainer.class)
    private JavaExecutableContainer executableContainer = null;

    @Transient
    private TaskLauncher taskLauncher = null;

    private static final int TIMEOUT = 1000;

    /** Hibernate default constructor */
    @SuppressWarnings("unused")
    private ForkedJavaExecutable() {
    }

    /**
     * Constructor 
     * @param container contains the executable object that should run user java task
     * @param tl remote object residing in a dedicated JVM 
     */
    public ForkedJavaExecutable(JavaExecutableContainer container, TaskLauncher tl) {
        this.executableContainer = container;
        this.taskLauncher = tl;
    }

    /**
     * Task execution, in fact this method delegates execution to a remote taskLauncher object
     * @see org.ow2.proactive.scheduler.common.task.executable.Executable#execute(org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    public Serializable execute(TaskResult... results) throws Throwable {
        TaskResult result = taskLauncher.doTask(null /* no need here to pass schedulerCore object */,
                executableContainer, results);
        while (!isKilled()) {
            try {
                /* the below method throws an exception if timeout expires */
                PAFuture.waitFor(result, TIMEOUT);
                break;
            } catch (ProActiveTimeoutException e) {
            }
        }
        if (isKilled()) {
            FutureMonitoring.removeFuture(((FutureProxy) ((StubObject) result).getProxy()));
            throw new SchedulerException("Walltime exceeded");
        }
        return result;
    }

    /**
     * The kill method should result in killing the executable, and cleaning after launching the separate JVM
     */
    public void kill() {
        //this method is called by the scheduler core or by the TimerTask of the walltime.
        //No need to terminate current taskLauncher for both cases because :
        //If the schedulerCore call it, the task is killed and the taskLauncher terminated.
        //If the TimerTask call it, so the taskLauncher is already terminated by throwing an exception.
        //This method is obviously useless because it just executes parent one.
        super.kill();
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#getExecutable()
     */
    public Executable getExecutable() throws ExecutableCreationException {
        return this.executableContainer.getExecutable();
    }

    /**
     * @see org.ow2.proactive.scheduler.task.ExecutableContainer#init(org.ow2.proactive.scheduler.task.ExecutableContainerInitializer)
     */
    public void init(ExecutableContainerInitializer initializer) {
        // nothing to do
    }

}
