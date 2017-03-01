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
package org.ow2.proactive.scheduler.core.db.helpers;

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
public class HousekeepingSessionWork implements SessionWork<Void> {

    private static List<Long> jobIdList;
    private static boolean shouldRemoveFromDb;

    public HousekeepingSessionWork(List<Long> jobIdList, boolean shouldRemoveFromDb) {
        this.jobIdList = jobIdList;
        this.shouldRemoveFromDb = shouldRemoveFromDb;
    }

    private void deleteInconsistentData(Session session) {
        session.createSQLQuery("delete from TASK_DATA where JOB_ID = null");
        session.createSQLQuery("delete from TASK_RESULT_DATA where TASK_ID = null");
        session.createSQLQuery("delete from TASK_RESULT_DATA where JOB_ID = null");
        session.createSQLQuery("delete from TASK_DATA where JOB_ID not in (select ID from JOB_DATA)");
        session.createSQLQuery("delete from TASK_RESULT_DATA where TASK_ID not in (select TASK_ID_TASK from TASK_DATA)");
        session.createSQLQuery("delete from TASK_RESULT_DATA where JOB_ID not in (select ID from JOB_DATA)");
    }

    private void removeJobScriptsInBulk(Session session, List<Long> jobIdList) {
        session.getNamedQuery("updateTaskDataJobScriptsInBulk")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
        session.getNamedQuery("deleteScriptDataInBulk").setParameterList("jobIdList", jobIdList).executeUpdate();
        session.getNamedQuery("deleteSelectionScriptDataInBulk")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
    }

    private void removeFromDb(Session session) {
        session.getNamedQuery("deleteEnvironmentModifierDataInBulk")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
        session.getNamedQuery("deleteTaskDataVariableInBulk")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
        session.getNamedQuery("deleteSelectorDataInBulk")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
        session.createSQLQuery("delete from TASK_DATA_DEPENDENCIES where JOB_ID in :jobIdList")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
        session.createSQLQuery("delete from TASK_DATA_JOINED_BRANCHES where JOB_ID in :jobIdList")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
        removeJobScriptsInBulk(session, jobIdList);
        session.getNamedQuery("deleteSelectionScriptDataInBulk")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
        session.createSQLQuery("delete from TASK_RESULT_DATA where JOB_ID in :jobIdList")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
        session.getNamedQuery("deleteTaskDataInBulk").setParameterList("jobIdList", jobIdList).executeUpdate();
        session.createSQLQuery("delete from JOB_CONTENT where JOB_ID in :jobIdList")
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
        session.getNamedQuery("deleteJobDataInBulk").setParameterList("jobIdList", jobIdList).executeUpdate();
        deleteInconsistentData(session);
    }

    private void updateAsRemoved(Session session) {
        session.getNamedQuery("updateJobDataRemovedTimeInBulk")
                .setParameter("removedTime", System.currentTimeMillis())
                .setParameter("lastUpdatedTime", new Date().getTime())
                .setParameterList("jobIdList", jobIdList)
                .executeUpdate();
    }

    @Override
    public Void doInTransaction(Session session) {
        if (shouldRemoveFromDb) {
            removeFromDb(session);
        } else {
            updateAsRemoved(session);
        }
        return null;
    }
}
