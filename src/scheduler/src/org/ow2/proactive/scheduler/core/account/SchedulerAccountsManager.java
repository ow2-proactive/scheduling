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
package org.ow2.proactive.scheduler.core.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.account.AbstractAccountsManager;
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
    //private final SchedulerUsers connectedUsers;
    /**
     * Create a new instance of this class.
     */
    public SchedulerAccountsManager() {
        super("Scheduler Accounts Manager Refresher", ProActiveLogger.getLogger(SchedulerDevLoggers.DATABASE));

        // Get the database manager
        this.dbmanager = DatabaseManager.getInstance();
    }

    /**
     * Reads database and fills accounts for specified user.
     */
    protected SchedulerAccount readAccount(String username) {
        final String taskStatsQuery = SchedulerAccountsManager.taskStatsSQL(username);
        if (super.logger.isDebugEnabled()) {
            super.logger.debug(taskStatsQuery);
        }

        SchedulerAccount account = new SchedulerAccount();
        account.username = username;

        final List<?> taskStats = this.dbmanager.sqlQuery(taskStatsQuery);
        if (taskStats.size() > 0) {
            final Object[] tuple = (Object[]) taskStats.get(0);
            account.totalTaskCount = ((Number) tuple[0]).intValue();
            if (tuple[1] != null) {
                // sum could be null
                account.totalTaskDuration = ((Number) tuple[1]).longValue();
            }
        }

        final String jobStatsQuery = SchedulerAccountsManager.jobStatsSQL(username);
        if (super.logger.isDebugEnabled()) {
            super.logger.debug(jobStatsQuery);
        }
        final List<?> jobStats = this.dbmanager.sqlQuery(jobStatsQuery);
        if (jobStats.size() > 0) {
            final Object[] tuple = (Object[]) jobStats.get(0);
            account.totalJobCount = ((Number) tuple[0]).intValue();
            if (tuple[1] != null) {
                // sum could be null
                account.totalJobDuration = ((Number) tuple[1]).longValue();
            }
        }

        return account;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultCacheValidityTimeInSeconds() {
        return PASchedulerProperties.SCHEDULER_ACCOUNT_REFRESH_RATE.getValueAsInt();
    }

    // For the two requests, it does not take into account the following
    // tables : Internal_NTV_TASK, INTERNAL_FORKED_TASK
    private static String taskStatsSQL(String userName) {
        final StringBuilder builder = new StringBuilder("SELECT ");
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
        builder.append("a." + fn.get("finishedTime") + ">0 AND ");
        builder.append("d." + fn.get("InternalJob_owner") + "='" + userName + "'");
        return builder.toString();
    }

    private static String jobStatsSQL(String userName) {
        final StringBuilder builder = new StringBuilder("SELECT ");
        builder.append("count(*), ");
        builder.append("sum(" + fn.get("finishedTime") + " - " + fn.get("startTime") + ") s ");
        builder.append("FROM ");
        builder.append(fn.get("InternalTaskFlowJob") + " a,");
        builder.append(fn.get("JobInfoImpl") + " b ");
        builder.append("WHERE ");
        builder.append("a." + fn.get("JobInfo_REF") + "=b." + fn.get("JobInfo_HID") + " AND ");
        builder.append("b." + fn.get("JobInfo_finishedTime") + ">0 AND ");
        builder.append("a." + fn.get("InternalJob_owner") + "='" + userName + "'");
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
            fn.put("InternalJob_tasks", InternalJob.class.getDeclaredField("tasks").getName());
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

}