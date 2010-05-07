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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core.account;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.account.AbstractAccountsManager;
import org.ow2.proactive.scheduler.common.SchedulerUsers;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.core.db.DatabaseManager;
import org.ow2.proactive.scheduler.core.db.SchedulerDatabaseManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalTaskFlowJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;
import org.ow2.proactive.scheduler.task.internal.InternalJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * This class is responsible to read periodically accounts from the scheduler database.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class SchedulerAccountsManager extends AbstractAccountsManager<SchedulerAccount> {

    /** Scheduler db manager used to submit SQL requests */
    private final SchedulerDatabaseManager dbmanager;
    /** The list of connected users */
    private final SchedulerUsers connectedUsers;

    /**
     * Create a new instance of this class.
     */
    public SchedulerAccountsManager(final SchedulerUsers connectedUsers) {
        super(new HashMap<String, SchedulerAccount>(), "Scheduler Accounts Manager Refresher",
                ProActiveLogger.getLogger(SchedulerDevLoggers.DATABASE));

        // Get the database manager
        this.dbmanager = DatabaseManager.getInstance();

        // Connected users will be used later
        this.connectedUsers = connectedUsers;
    }

    /**
     * Reads database and fills accounts.
     */
    protected void internalRefresh(final Map<String, SchedulerAccount> map) {
        // Task stats
        final String taskStatsQuery = SchedulerAccountsManager.taskStatsSQL();
        if (super.logger.isDebugEnabled()) {
            super.logger.debug(taskStatsQuery);
        }

        final List<?> taskStats = this.dbmanager.sqlQuery(taskStatsQuery);
        // The result of the query is sorted by task duration (Descending order)
        for (int i = 0; i < taskStats.size(); i++) {
            final Object[] tuple = (Object[]) taskStats.get(i);
            final String username = (String) tuple[0];
            SchedulerAccount account = map.get(username);
            // The user may be unknown
            if (account == null) {
                account = new SchedulerAccount();
                account.username = username;
                map.put(username, account);
            }
            // Fill task related stats
            account.totalTaskCount = ((Number) tuple[1]).intValue();
            account.totalTaskDuration = ((Number) tuple[2]).longValue();
        }

        // Create job accounts query, submit it to the database and fill user stats
        final String jobStatsQuery = SchedulerAccountsManager.jobStatsSQL();
        if (super.logger.isDebugEnabled()) {
            super.logger.debug(jobStatsQuery);
        }
        // The result of the query is sorted by job duration (Descending order)
        final List<?> jobStats = this.dbmanager.sqlQuery(jobStatsQuery);
        for (int i = 0; i < jobStats.size(); i++) {
            final Object[] tuple = (Object[]) jobStats.get(i);
            final String username = (String) tuple[0];
            SchedulerAccount account = map.get(username);
            // The user may be unknown
            if (account == null) {
                account = new SchedulerAccount();
                account.username = username;
                map.put(username, account);
            }
            // Fill stats
            account.totalJobCount = ((Number) tuple[1]).intValue();
            account.totalJobDuration = ((Number) tuple[2]).longValue();
        }

        // Add all connected users info
        for (final UserIdentification userIdent : this.connectedUsers.getUsers()) {
            final String username = userIdent.getUsername();
            SchedulerAccount account = map.get(username);
            // Maybe there are some cases where a user is connected and is not
            // in the db
            if (account == null) {
                account = new SchedulerAccount();
                account.username = username;
                map.put(username, account);
            }
            // If the user is in database and has never been connected
            // or has reconnected
            if (account.ref == null || account.ref.get() == null) {
                account.ref = new WeakReference<UserIdentification>(userIdent);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultRefreshRateInSeconds() {
        return PASchedulerProperties.SCHEDULER_ACCOUNT_REFRESH_RATE.getValueAsInt();
    }

    // For the two requests, it does not take into account the following
    // tables : Internal_NTV_TASK, INTERNAL_FORKED_TASK
    private static String taskStatsSQL() {
        final StringBuilder builder = new StringBuilder("SELECT ");
        builder.append("d." + fn.get("InternalJob_owner") + ",");
        builder.append("count(*),");
        builder.append("sum(" + fn.get("finishedTime") + " - " + fn.get("startTime") + ") s ");
        builder.append("FROM ");
        builder.append(fn.get("TaskInfoImpl") + " a,");
        builder.append(fn.get("InternalJavaTask") + " b,");
        builder.append(fn.get("InternalTFJob_tasks") + " c,");
        builder.append(fn.get("InternalTaskFlowJob") + " d ");
        builder.append("WHERE ");
        builder.append("a." + fn.get("TaskInfo_HID") + "=b." + fn.get("TaskInfo_REF") + " AND ");
        builder.append("b." + fn.get("InternalJavaTask_HID") + "=c." + fn.get("InternalJob_tasksDep") +
            " AND ");
        builder.append("c." + fn.get("InternalJob_ITasksId") + "=d." + fn.get("InternalTFJob_HID") + " AND ");
        builder.append("a." + fn.get("finishedTime") + ">0 ");
        builder.append("GROUP BY d." + fn.get("InternalJob_owner"));
        return builder.toString();
    }

    private static String jobStatsSQL() {
        final StringBuilder builder = new StringBuilder("SELECT ");
        builder.append("a." + fn.get("InternalJob_owner") + ",");
        builder.append("count(*), ");
        builder.append("sum(" + fn.get("finishedTime") + " - " + fn.get("startTime") + ") s ");
        builder.append("FROM ");
        builder.append(fn.get("InternalTaskFlowJob") + " a,");
        builder.append(fn.get("JobInfoImpl") + " b ");
        builder.append("WHERE ");
        builder.append("a." + fn.get("JobInfo_REF") + "=b." + fn.get("JobInfo_HID") + " AND ");
        builder.append("b." + fn.get("JobInfo_finishedTime") + ">0 ");
        builder.append("GROUP BY a." + fn.get("InternalJob_owner"));
        return builder.toString();
    }

    /** Cache of the computed field name used in the different requests */
    private static final Map<String, String> fn = new HashMap<String, String>(18);
    static {
        try {
            // compute and check that field name have not changed
            // DB field name from annotation for TaskInfo
            fn.put("TaskInfoImpl", TaskInfoImpl.class.getAnnotation(Table.class).name());
            fn.put("startTime", TaskInfoImpl.class.getDeclaredField("startTime").getAnnotation(Column.class)
                    .name());
            fn.put("finishedTime", TaskInfoImpl.class.getDeclaredField("finishedTime").getAnnotation(
                    Column.class).name());
            fn.put("TaskInfo_HID", TaskInfoImpl.class.getDeclaredField("hId").getName().toUpperCase());
            // DB field name from annotation for InternalJavaTask
            fn.put("InternalJavaTask", InternalJavaTask.class.getAnnotation(Table.class).name());
            fn.put("TaskInfo_REF", InternalTask.class.getDeclaredField("taskInfo").getName().toUpperCase() +
                "_" + fn.get("TaskInfo_HID"));
            fn.put("InternalJavaTask_HID", InternalJavaTask.class.getDeclaredField("hId").getName()
                    .toUpperCase());
            // DB field name from annotation for Table Association between job
            // and tasks
            fn.put("InternalJob_tasks", InternalJob.class.getDeclaredField("tasks").getName().toUpperCase());
            fn.put("InternalTFJob_tasks", InternalTaskFlowJob.class.getAnnotation(Table.class).name() + "_" +
                fn.get("InternalJob_tasks"));
            fn.put("InternalJob_ITasksId", InternalJob.class.getDeclaredField("tasks").getAnnotation(
                    JoinTable.class).joinColumns()[0].name());
            fn.put("InternalJob_tasksDep", InternalJob.class.getDeclaredField("tasks").getAnnotation(
                    JoinTable.class).inverseJoinColumns()[0].name());
            // DB field name from annotation for InternalTFJob
            fn.put("InternalTFJob_HID", InternalTaskFlowJob.class.getDeclaredField("hId").getName()
                    .toUpperCase());
            fn.put("InternalTaskFlowJob", InternalTaskFlowJob.class.getAnnotation(Table.class).name());
            fn.put("InternalJob_owner", InternalJob.class.getDeclaredField("owner").getAnnotation(
                    Column.class).name());
            // DB field name from annotation for JobInfo
            fn.put("JobInfoImpl", JobInfoImpl.class.getAnnotation(Table.class).name());
            fn.put("JobInfo_HID", JobInfoImpl.class.getDeclaredField("hId").getName().toUpperCase());
            fn.put("JobInfo_REF", InternalJob.class.getDeclaredField("jobInfo").getName().toUpperCase() +
                "_" + fn.get("JobInfo_HID"));
            fn.put("JobInfo_finishedTime", JobInfoImpl.class.getDeclaredField("finishedTime").getAnnotation(
                    Column.class).name());
        } catch (Exception nsfe) {
            throw new RuntimeException(nsfe);
        }
    }

    public static void main(String[] args) {
        SchedulerUsers ss = new SchedulerUsers();
        SchedulerAccountsManager m = new SchedulerAccountsManager(ss);
        m.startAccountsRefresher();
        while (true) {
            try {
                Thread.sleep(5000);
                System.out.println("UsersStatisticsManager.main() --> " + m.getAllAccounts());
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}