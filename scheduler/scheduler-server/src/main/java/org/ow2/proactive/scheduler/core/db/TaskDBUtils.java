/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.scheduler.core.db;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.db.TransactionHelper.SessionWork;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility methods to fetch or update task related information from database.
 *
 * @author ActiveEon Team
 */
public class TaskDBUtils {

    public static SessionWork<Integer> getTotalNumberOfTasks(final DBTaskDataParameters params) {
        return new SessionWork<Integer>() {

            @Override
            public Integer executeWork(Session session) {
                Set<TaskStatus> taskStatuses = params.getStatuses();

                if (taskStatuses.isEmpty()) {
                    return 0;
                }

                boolean hasUser = params.hasUser();
                boolean hasTag = params.hasTag();
                boolean hasDateFrom = params.hasDateFrom();
                boolean hasDateTo = params.hasDateTo();

                String queryPrefix = "select count(*) from TaskData T where ";

                Query query = getQuery(session, params, taskStatuses, hasUser, hasTag, hasDateFrom, hasDateTo,
                        null, queryPrefix);

                Long count = (Long) query.uniqueResult();

                return count.intValue();
            }
        };
    }

    public static SessionWork<List<TaskState>> taskStateSessionWork(final DBTaskDataParameters params) {
        return new SessionWork<List<TaskState>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<TaskState> executeWork(Session session) {
                if (params.getStatuses().isEmpty()) {
                    return new ArrayList<TaskState>(0);
                }

                List<TaskData> tasksList = fetchTaskData(session, params);
                List<TaskState> result = new ArrayList<TaskState>(tasksList.size());

                for (TaskData taskData : tasksList) {
                    result.add(taskData.toTaskState());
                }

                return result;
            }
        };
    }

    public static SessionWork<List<TaskInfo>> taskInfoSessionWork(final DBTaskDataParameters params) {
        return new SessionWork<List<TaskInfo>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<TaskInfo> executeWork(Session session) {
                if (params.getStatuses().isEmpty()) {
                    return new ArrayList<TaskInfo>(0);
                }

                List<TaskData> tasksList = fetchTaskData(session, params);
                List<TaskInfo> result = new ArrayList<TaskInfo>(tasksList.size());

                for (TaskData taskData : tasksList) {
                    result.add(taskData.toTaskInfo());
                }

                return result;
            }
        };
    }

    private static List<TaskData> fetchTaskData(Session session, DBTaskDataParameters params) {
        Set<TaskStatus> taskStatuses = params.getStatuses();

        boolean hasUser = params.hasUser();
        boolean hasTag = params.hasTag();
        boolean hasDateFrom = params.hasDateFrom();
        boolean hasDateTo = params.hasDateTo();

        String queryPrefix = "select T from TaskData T where ";

        Query query = getQuery(session, params, taskStatuses, hasUser, hasTag, hasDateFrom, hasDateTo,
                params.getSortParams(), queryPrefix);
        query.setMaxResults(params.getLimit());
        query.setFirstResult(params.getOffset());

        return query.list();
    }

    @NotNull
    private static Query getQuery(Session session, DBTaskDataParameters params, Set<TaskStatus> taskStatuses,
                                  boolean hasUser, boolean hasTag, boolean hasDateFrom, boolean hasDateTo,
                                  SortSpecifierContainer sortParams, String queryPrefix) {
        StringBuilder queryString = new StringBuilder(queryPrefix);
        queryString.append(getQueryFilteringExpression(hasUser, hasTag, hasDateFrom, hasDateTo, sortParams));
        Query query = session.createQuery(queryString.toString());

        setQueryParameters(taskStatuses, hasUser, hasTag, hasDateFrom, hasDateTo,
                query, params);

        return query;
    }

    private static StringBuilder getQueryFilteringExpression(boolean hasUser, boolean hasTag,
                                                             boolean hasDateFrom, boolean hasDateTo,
                                                             SortSpecifierContainer sortParams) {
        StringBuilder result = new StringBuilder();

        result.append("T.jobData.removedTime = -1 ");

        // if 'from' and 'to' values are set
        if (hasDateFrom && hasDateTo) {
            result.append("and ( ( startTime >= :dateFrom and startTime <= :dateTo ) " +
                    "or ( scheduledTime >= :dateFrom and scheduledTime <= :dateTo ) " +
                    "or ( finishedTime >= :dateFrom and finishedTime <= :dateTo ) ) ");
        } else if (hasDateFrom && !hasDateTo) { // if 'from' only is set
            result.append("and ( startTime >= :dateFrom or finishedTime >= :dateFrom or scheduledTime >= :dateFrom ) ");
        } else if (!hasDateFrom && hasDateTo) { // if 'to' only is set
            result.append("and ( startTime <= :dateTo or finishedTime <= :dateTo or scheduledTime >= :dateTo ) ");
        } else {
            // no datetime filtering required
            // nothing to do
        }

        if (hasUser) {
            result.append("and T.jobData.owner = :user ");
        }

        if (hasTag) {
            result.append("and tag = :taskTag ");
        }

        result.append("and taskStatus in (:taskStatus) ");

        if (sortParams != null && !sortParams.getSortParameters().isEmpty()) {
            result.append("order by ");
            List<SortSpecifierContainer.SortSpecifierItem> items = sortParams.getSortParameters();
            for (int i = 0; i < items.size(); i++) {
                SortSpecifierContainer.SortSpecifierItem item = items.get(i);
                String order = "ascending".compareTo(item.getOrder().toString()) == 0 ? "ASC" : "DESC";
                result.append("T." + item.getField() + " " + order);
                if (i < items.size() - 1) result.append(",");
            }
            result.append(" ");
        }
        return result;
    }

    private static void setQueryParameters(Set<TaskStatus> taskStatuses, boolean hasUser, boolean hasTag,
                                           boolean hasDateFrom, boolean hasDateTo, Query query,
                                           DBTaskDataParameters params) {
        query.setParameterList("taskStatus", taskStatuses);

        if (hasUser) {
            query.setParameter("user", params.getUser());
        }

        if (hasDateFrom) {
            query.setParameter("dateFrom", params.getFrom());
        }

        if (hasDateTo) {
            query.setParameter("dateTo", params.getTo());
        }

        if (hasTag) {
            query.setParameter("taskTag", params.getTag());
        }

    }

}
