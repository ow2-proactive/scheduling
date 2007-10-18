/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.task.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.scheduler.common.exception.TaskCreationException;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableTask;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.task.ExecutableNativeTask;
import org.objectweb.proactive.extra.scheduler.task.NativeTaskLauncher;
import org.objectweb.proactive.extra.scheduler.task.TaskLauncher;


/**
 * Description of a native task.
 * This task include the process
 * see also {@link InternalTask}
 *
 * @author ProActive Team
 * @version 1.0, Jun 29, 2007
 * @since ProActive 3.2
 */
public class InternalNativeTask extends InternalTask {

    /** Serial Version UID */
    private static final long serialVersionUID = 2587936204570926300L;

    /** Command line to execute */
    private String cmd;

    /**
     * ProActive empty constructor.
     */
    public InternalNativeTask() {
    }

    /**
     * Create a new native task descriptor with the given command line.
     *
     * @param cmd the command line to execute
     */
    public InternalNativeTask(String cmd) {
        this.cmd = cmd;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.task.internal.InternalTask#getTask()
     */
    @Override
    public ExecutableTask getTask() throws TaskCreationException {
        //create the new task that will launch the command on execute.
        ExecutableNativeTask executableNativeTask = null;
        try {
            executableNativeTask = new ExecutableNativeTask() {
                        private static final long serialVersionUID = 0L;
                        private Process process;

                        /**
                         * @see org.objectweb.proactive.extra.scheduler.common.task.ExecutableTask#execute(org.objectweb.proactive.extra.scheduler.task.TaskResult[])
                         */
                        public Object execute(TaskResult... results) {
                            try {
                                process = Runtime.getRuntime().exec(cmd);
                                new Thread(new ThreadReader(
                                        new BufferedReader(
                                            new InputStreamReader(
                                                process.getInputStream())))).start();

                                new Thread(new ThreadReader(
                                        new BufferedReader(
                                            new InputStreamReader(
                                                process.getErrorStream())))).start();
                                process.waitFor();
                                return process.exitValue();
                            } catch (Exception e) {
                                //TODO send the exception or error to the user ?
                                e.printStackTrace();
                                return 255;
                            }
                        }

                        /**
                         * @see org.objectweb.proactive.extra.scheduler.task.ExecutableNativeTask#getProcess()
                         */
                        public Process getProcess() {
                            return process;
                        }
                    };
        } catch (Exception e) {
            throw new TaskCreationException("Cannot create native task !!", e);
        }
        return executableNativeTask;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.task.internal.InternalTask#createLauncher(java.lang.String, int, org.objectweb.proactive.core.node.Node)
     */
    @Override
    public TaskLauncher createLauncher(String host, int port, Node node)
        throws ActiveObjectCreationException, NodeException {
        NativeTaskLauncher launcher;
        if (getPreTask() == null) {
            launcher = (NativeTaskLauncher) ProActiveObject.newActive(NativeTaskLauncher.class.getName(),
                    new Object[] { getId(), getJobId(), host, port }, node);
        } else {
            launcher = (NativeTaskLauncher) ProActiveObject.newActive(NativeTaskLauncher.class.getName(),
                    new Object[] { getId(), getJobId(), getPreTask(), host, port },
                    node);
        }
        setExecuterInformations(new ExecuterInformations(launcher, node));
        return launcher;
    }

    protected class ThreadReader implements Runnable {
        private BufferedReader r;

        public ThreadReader(BufferedReader r) {
            this.r = r;
        }

        @Override
        public void run() {
            String str = null;
            try {
                while ((str = r.readLine()) != null) {
                    System.out.println(str);
                }
            } catch (IOException e) {
                //FIXME cdelbe gros vilain tu dois throw exception
                e.printStackTrace();
            }
        }
    }
}
