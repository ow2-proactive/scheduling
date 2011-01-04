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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


public class SchedulerClientEventsLogger implements SchedulerEventListener {

    public static String newline = System.getProperty("line.separator");

    private Scheduler user;
    private FileWriter writer;

    /**
     * @param args
     * @throws NodeException 
     * @throws ActiveObjectCreationException 
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage : " + SchedulerClientEventsLogger.class.getName() +
                " scheduler_URL log_file");
            System.exit(1);
        }
        File f = new File(args[1]);
        if (f.exists() && f.isDirectory()) {
            System.err.println("'log_file' must be a regular file !");
            System.exit(2);
        }
        //create Scheduler client as an active object
        SchedulerClientEventsLogger client = PAActiveObject.newActive(SchedulerClientEventsLogger.class,
                new Object[] {});
        //begin to use the client
        client.begin(args[0], f);
    }

    public void begin(String schedulerURL, File logs) throws Exception {
        if (!logs.exists()) {
            if (!logs.createNewFile()) {
                throw new RuntimeException("Cannot create file '" + logs.getAbsolutePath() + "' !");
            }
        }
        writer = new FileWriter(logs);
        //connect the Scheduler
        System.out.println("Connecting to Scheduler at " + schedulerURL + "...");
        //1. get the authentication interface using the SchedulerConnection
        SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin(schedulerURL);
        //2. get the user interface using the retrieved SchedulerAuthenticationInterface
        Credentials cred = Credentials.createCredentials(new CredData("admin", "admin"), auth.getPublicKey());
        user = auth.login(cred);

        //let the client be notified of its own 'job termination' -> job running to finished event
        user.addEventListener((SchedulerClientEventsLogger) PAActiveObject.getStubOnThis(), false);
        System.out.println("Connected and ready to listen (logs will be writen in " + logs.getAbsolutePath() +
            ")");
    }

    /**
     * {@inheritDoc}
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> arg0) {
        try {
            writer.write("JOB_STATE_UPDATED " + arg0.getEventType().toString().replaceAll(" ", "_") + " " +
                arg0.getData().getJobId() + newline);
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void jobSubmittedEvent(JobState arg0) {
        try {
            writer.write("JOB_STATE_UPDATED Job_submitted " + arg0.getId() + newline);
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent arg0) {
        try {
            writer.write("SCHEDULER_STATE_UPDATED " + arg0 + newline);
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> arg0) {
        try {
            writer.write("TASK_STATE_UPDATED " + arg0.getEventType().toString().replaceAll(" ", "_") + " " +
                arg0.getData().getTaskId() + newline);
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> arg0) {
        try {
            writer.write("USER_STATE_UPDATED " + arg0.getEventType().toString().replaceAll(" ", "_") + " " +
                arg0.getData().getUsername() + newline);
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}
