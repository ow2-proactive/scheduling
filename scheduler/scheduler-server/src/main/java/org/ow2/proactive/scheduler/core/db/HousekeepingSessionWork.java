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
package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.ow2.proactive.db.SessionWork;


/**
 * Handles the Housekeeping in the database.
 *
 * @author ActiveEon Team
 * @since 01/03/17
 */
public class HousekeepingSessionWork {

    private static List<Long> jobIdList;

    private static boolean shouldRemoveFromDb;

    public HousekeepingSessionWork(List<Long> jobIdList, boolean shouldRemoveFromDb) {
        HousekeepingSessionWork.jobIdList = jobIdList;
        HousekeepingSessionWork.shouldRemoveFromDb = shouldRemoveFromDb;
    }

    private void deleteInconsistentData(Session session) {
        session.createSQLQuery("delete from TASK_DATA where JOB_ID = null");
        session.createSQLQuery("delete from TASK_RESULT_DATA where TASK_ID = null");
        session.createSQLQuery("delete from TASK_RESULT_DATA where JOB_ID = null");
        session.createSQLQuery("delete from TASK_DATA where JOB_ID not in (select ID from JOB_DATA)");
        session.createSQLQuery("delete from TASK_RESULT_DATA where TASK_ID not in (select TASK_ID_TASK from TASK_DATA)");
        session.createSQLQuery("delete from TASK_RESULT_DATA where JOB_ID not in (select ID from JOB_DATA)");
    }

    public List<SessionWork<Integer>> getAllTransactions() {
        if (shouldRemoveFromDb) {
            return getAllTransactionsRemoveFromDb();
        } else {
            return getAllTransactionsUpdateAsRemoved();
        }
    }

    private List<SessionWork<Integer>> getAllTransactionsRemoveFromDb() {
        List<SessionWork<Integer>> allTransactions = new ArrayList<>();
        allTransactions.add(session -> ((List<Long>) session.getNamedQuery("getParentIds")
                                                            .setParameterList("ids", jobIdList)
                                                            .list()).stream()
                                                                    .map(parentId -> session.getNamedQuery("decreaseJobDataChildrenCount")
                                                                                            .setParameter("jobId",
                                                                                                          parentId)
                                                                                            .setParameter("lastUpdatedTime",
                                                                                                          new Date().getTime())
                                                                                            .executeUpdate())
                                                                    .reduce(0, Integer::sum));

        allTransactions.add(session -> session.getNamedQuery("deleteEnvironmentModifierDataInBulk")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.getNamedQuery("deleteJobDataVariableInBulk")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.getNamedQuery("deleteTaskDataVariableInBulk")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.getNamedQuery("deleteSelectorDataInBulk")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.createSQLQuery("delete from TASK_DATA_DEPENDENCIES where JOB_ID in :jobIdList")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.createSQLQuery("delete from TASK_DATA_JOINED_BRANCHES where JOB_ID in :jobIdList")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());

        allTransactions.add(session -> session.getNamedQuery("updateTaskDataJobScriptsInBulk")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.getNamedQuery("deleteScriptDataInBulk")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.getNamedQuery("deleteSelectionScriptDataInBulk")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.createSQLQuery("delete from TASK_RESULT_DATA where JOB_ID in :jobIdList")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.getNamedQuery("deleteTaskDataInBulk")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());

        allTransactions.add(session -> session.createSQLQuery("delete from JOB_CONTENT where JOB_ID in :jobIdList")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        allTransactions.add(session -> session.getNamedQuery("deleteJobDataInBulk")
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());

        return allTransactions;
    }

    private List<SessionWork<Integer>> getAllTransactionsUpdateAsRemoved() {
        List<SessionWork<Integer>> allTransactions = new ArrayList<>();
        allTransactions.add(session -> ((List<Long>) session.getNamedQuery("getParentIds")
                                                            .setParameterList("ids", jobIdList)
                                                            .list()).stream()
                                                                    .map(parentId -> session.getNamedQuery("decreaseJobDataChildrenCount")
                                                                                            .setParameter("jobId",
                                                                                                          parentId)
                                                                                            .setParameter("lastUpdatedTime",
                                                                                                          new Date().getTime())
                                                                                            .executeUpdate())
                                                                    .reduce(0, Integer::sum));
        allTransactions.add(session -> session.getNamedQuery("updateJobDataRemovedTimeInBulk")
                                              .setParameter("removedTime", System.currentTimeMillis())
                                              .setParameter("lastUpdatedTime", new Date().getTime())
                                              .setParameterList("jobIdList", jobIdList)
                                              .executeUpdate());
        return allTransactions;
    }
}
