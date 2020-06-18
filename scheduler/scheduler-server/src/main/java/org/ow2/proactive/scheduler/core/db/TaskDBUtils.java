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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Query;
import org.hibernate.Session;
import org.ow2.proactive.db.SessionWork;
import org.ow2.proactive.scheduler.common.SortSpecifierContainer;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;


/**
 * Utility methods to fetch or update task related information from database.
 *
 * @author ActiveEon Team
 */
public class TaskDBUtils {

    public static SessionWork<Integer> getTotalNumberOfTasks(final DBTaskDataParameters params) {
        return session -> {
            if (params.getStatus().isEmpty()) {
                return 0;
            }

            String queryPrefix = "select count(*) from TaskData T where ";

            Query query = getQuery(session,
                                   params,
                                   params.getStatus(),
                                   params.hasUser(),
                                   params.hasTag(),
                                   params.hasDateFrom(),
                                   params.hasDateTo(),
                                   SortSpecifierContainer.EMPTY_CONTAINER,
                                   queryPrefix);

            return ((Long) query.uniqueResult()).intValue();
        };
    }

    public static SessionWork<List<TaskState>> taskStateSessionWork(final DBTaskDataParameters params) {
        return session -> {
            if (params.getStatus().isEmpty()) {
                return new ArrayList<>(0);
            }

            List<TaskData> tasksList = fetchTaskData(session, params);

            return tasksList.stream().map(TaskData::toTaskState).collect(Collectors.toList());
        };
    }

    public static SessionWork<List<TaskInfo>> taskInfoSessionWork(final DBTaskDataParameters params) {
        return session -> {
            if (params.getStatus().isEmpty()) {
                return new ArrayList<>(0);
            }

            List<TaskData> tasksList = fetchTaskData(session, params);

            return tasksList.stream().map(TaskData::toTaskInfo).collect(Collectors.toList());

        };
    }

    private static List<TaskData> fetchTaskData(Session session, DBTaskDataParameters params) {
        return getQuery(session,
                        params,
                        params.getStatus(),
                        params.hasUser(),
                        params.hasTag(),
                        params.hasDateFrom(),
                        params.hasDateTo(),
                        params.getSortParams(),
                        "select T from TaskData T where ").setMaxResults(params.getLimit())
                                                          .setFirstResult(params.getOffset())
                                                          .list();
    }

    private static Query getQuery(Session session, DBTaskDataParameters params, Set<TaskStatus> taskStatuses,
            boolean hasUser, boolean hasTag, boolean hasDateFrom, boolean hasDateTo, SortSpecifierContainer sortParams,
            String queryPrefix) {
        Query query = session.createQuery(queryPrefix +
                                          getQueryFilteringExpression(hasUser,
                                                                      hasTag,
                                                                      hasDateFrom,
                                                                      hasDateTo,
                                                                      sortParams));
        setQueryParameters(taskStatuses, hasUser, hasTag, hasDateFrom, hasDateTo, query, params);
        return query;
    }

    private static StringBuilder getQueryFilteringExpression(boolean hasUser, boolean hasTag, boolean hasDateFrom,
            boolean hasDateTo, SortSpecifierContainer sortParams) {
        StringBuilder result = new StringBuilder();

        // Support for removedTime in queries has been discontinued
        //result.append("T.jobData.removedTime = -1 ");
        boolean oneClause = false;

        // if 'from' and 'to' values are set
        if (hasDateFrom && hasDateTo) {
            result.append("( ( startTime >= :dateFrom and startTime <= :dateTo ) " +
                          "or ( scheduledTime >= :dateFrom and scheduledTime <= :dateTo ) " +
                          "or ( finishedTime >= :dateFrom and finishedTime <= :dateTo ) ) ");
            oneClause = true;
        } else if (hasDateFrom && !hasDateTo) { // if 'from' only is set
            result.append("( startTime >= :dateFrom or finishedTime >= :dateFrom or scheduledTime >= :dateFrom ) ");
            oneClause = true;
        } else if (!hasDateFrom && hasDateTo) { // if 'to' only is set
            result.append("( startTime <= :dateTo or finishedTime <= :dateTo or scheduledTime >= :dateTo ) ");
            oneClause = true;
        } else {
            // no datetime filtering required
            // nothing to do
        }

        if (hasUser) {
            result.append((oneClause ? "and " : "") + "owner = :user ");
            oneClause = true;
        }

        if (hasTag) {
            result.append((oneClause ? "and " : "") + "tag = :taskTag ");
            oneClause = true;
        }

        result.append((oneClause ? "and " : "") + "taskStatus in (:taskStatus) ");

        if (!sortParams.getSortParameters().isEmpty()) {
            result.append("order by ");
            List<SortSpecifierContainer.SortSpecifierItem> items = sortParams.getSortParameters();
            for (int i = 0; i < items.size(); i++) {
                SortSpecifierContainer.SortSpecifierItem item = items.get(i);
                String order = "ascending".compareTo(item.getOrder().toString()) == 0 ? "ASC" : "DESC";
                if (item.getField().equalsIgnoreCase("jobid") || item.getField().equalsIgnoreCase("taskid")) {
                    result.append("T.id.").append(item.getField()).append(" ").append(order);
                } else {
                    result.append("T.").append(item.getField()).append(" ").append(order);
                }
                if (i < items.size() - 1) {
                    result.append(",");
                }
            }
            result.append(" ");
        }
        return result;
    }

    private static void setQueryParameters(Set<TaskStatus> taskStatuses, boolean hasUser, boolean hasTag,
            boolean hasDateFrom, boolean hasDateTo, Query query, DBTaskDataParameters params) {
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
