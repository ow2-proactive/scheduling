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
package org.objectweb.proactive.extra.scheduler.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.objectweb.proactive.extra.scheduler.common.task.ExecutableTask;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * This is the execution entry point for the native task.
 * The execute(TaskResult...) method will be override by the scheduler to launch the native process.
 * This class provide a getProcess method that will return the current running native process.
 *
 * @author ProActive Team
 * @version 1.0, Aug 21, 2007
 * @since ProActive 3.2
 */
public class ExecutableNativeTask extends ExecutableTask {

    /** Process that start the native task */
    private Process process;

    /** Command that should be executed */
    private String command;

    /**
     * Create a new native task that execute command.
     * @param command the command to be executed.
     */
    public ExecutableNativeTask(String command) {
        this.command = command;
    }

    /**
     * Return the current native running process.
     * It is used by the scheduler to allow it to kill the process.
     *
     * @return the current native running process.
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.task.ExecutableTask#execute(org.objectweb.proactive.extra.scheduler.task.TaskResult[])
     */
    public Object execute(TaskResult... results) {
        try {
            process = Runtime.getRuntime().exec(this.command);
            new Thread(new ThreadReader(
                    new BufferedReader(
                        new InputStreamReader(process.getInputStream())))).start();

            new Thread(new ThreadReader(
                    new BufferedReader(
                        new InputStreamReader(process.getErrorStream())))).start();
            process.waitFor();
            return process.exitValue();
        } catch (Exception e) {
            //TODO send the exception or error to the user ?
            e.printStackTrace();
            return 255;
        }
    }

    @Override
    public final void init(Map<String, Object> args) throws Exception {
        throw new RuntimeException(
            "This method should have NEVER been called in this context !!");
    }

    protected class ThreadReader implements Runnable {
        private BufferedReader r;

        public ThreadReader(BufferedReader r) {
            this.r = r;
        }

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
