package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.TaskSortParameter;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.db.TransactionHelper.SessionWork;


public class TaskDBUtils {

    public static SessionWork<List<TaskState>> taskStateSessionWork(final DBTaskDataParameters params) {

        return new SessionWork<List<TaskState>>() {

            @Override
            @SuppressWarnings("unchecked")
            public List<TaskState> executeWork(Session session) {
                Criteria criteria = session.createCriteria(TaskData.class);
                List<TaskState> result = null;
                
                if (params.getStatuses().isEmpty())
                    return new ArrayList<TaskState>();
                
                if (params.getLimit() > 0)
                    criteria.setMaxResults(params.getLimit());
                if (params.getOffset() >= 0)
                    criteria.setFirstResult(params.getOffset());
                if (params.getSortParameters() != null) {
                    Order sortOrder;
                    for (SortParameter<TaskSortParameter> param : params.getSortParameters()) {
                        switch (param.getParameter()) {
                            case ID:
                                sortOrder = configureSortOrderTask(param, Property.forName("id"));
                                break;
                            case NAME:
                                sortOrder = configureSortOrderTask(param, Property.forName("name"));
                                break;
                            default:
                                throw new IllegalArgumentException(
                                    "Unsupported sort parameter: " + param.getParameter());
                        }
                        criteria.addOrder(sortOrder);
                    }
                }

                if (params.getUser() != null && "".compareTo(params.getUser()) != 0) {
                    criteria.createAlias("jobData", "job")
                            .add(Restrictions.eq("job.owner", params.getUser()));
                }

                if (params.getFrom() != 0 && params.getTo() != 0) {
                    criteria.add(Restrictions.ge("startTime", params.getFrom()));
                    criteria.add(Restrictions.le("finishedTime", params.getTo()));
                }

                if (!params.getStatuses().isEmpty()) {
                    criteria.add(Restrictions.in("taskStatus", params.getStatuses()));
                }

                if (params.getTag() != null && "".compareTo(params.getTag()) != 0) {
                    criteria.add(Restrictions.eq("tag", params.getTag()));
                }

                List<TaskData> tasksList = criteria.list();
                result = new ArrayList<TaskState>(tasksList.size());
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
                
                if (params.getStatuses().isEmpty()) return new ArrayList<TaskInfo>();
                
                Criteria criteria = session.createCriteria(TaskData.class);

                if (params.getLimit() > 0)
                    criteria.setMaxResults(params.getLimit());
                if (params.getOffset() >= 0)
                    criteria.setFirstResult(params.getOffset());
                if (params.getSortParameters() != null) {
                    Order sortOrder;
                    for (SortParameter<TaskSortParameter> param : params.getSortParameters()) {
                        switch (param.getParameter()) {
                            case ID:
                                sortOrder = configureSortOrderTask(param, Property.forName("id"));
                                break;
                            case NAME:
                                sortOrder = configureSortOrderTask(param, Property.forName("name"));
                                break;
                            default:
                                throw new IllegalArgumentException(
                                    "Unsupported sort parameter: " + param.getParameter());
                        }
                        criteria.addOrder(sortOrder);
                    }
                }

                if (params.getUser() != null && "".compareTo(params.getUser()) != 0) {
                    criteria.createAlias("jobData", "job")
                            .add(Restrictions.eq("job.owner", params.getUser()));
                }

                if (params.getFrom() != 0 && params.getTo() != 0) {
                    criteria.add(Restrictions.ge("startTime", params.getFrom()));
                    criteria.add(Restrictions.le("finishedTime", params.getTo()));
                }

                criteria.add(Restrictions.in("taskStatus", params.getStatuses()));

                if (params.getTag() != null && "".compareTo(params.getTag()) != 0) {
                    criteria.add(Restrictions.eq("tag", params.getTag()));
                }

                List<TaskData> tasksList = criteria.list();
                List<TaskInfo> result = new ArrayList<TaskInfo>(tasksList.size());
                for (TaskData taskData : tasksList) {
                    result.add(taskData.toTaskInfo());
                }

                return result;
            }
        };

    }

    private static Order configureSortOrderTask(SortParameter<TaskSortParameter> param, Property property) {
        if (param.getSortOrder().isAscending()) {
            return property.asc();
        } else {
            return property.desc();
        }
    }

}
