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
package org.ow2.proactive.scheduler.common.job;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatesPage;
import org.ow2.proactive.scheduler.common.util.PageBoundaries;
import org.ow2.proactive.scheduler.common.util.Pagination;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;


/**
 * This class contains all informations about the state of the job.
 * It also provides methods and static fields to order the jobs if you hold them in a list.
 * It references the list of task contains in the job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class JobState extends Job implements Comparable<JobState> {

    /** Used to sort by id */
    public static final int SORT_BY_ID = 1;
    /** Used to sort by name */
    public static final int SORT_BY_NAME = 2;
    /** Used to sort by priority */
    public static final int SORT_BY_PRIORITY = 3;
    /** Used to sort by type */
    public static final int SORT_BY_TYPE = 4;
    /** Used to sort by description */
    public static final int SORT_BY_DESCRIPTION = 5;
    /** Used to sort by owner */
    public static final int SORT_BY_OWNER = 6;
    /** Used to sort by status */
    public static final int SORT_BY_STATUS = 7;
    /** Used to sort by project name */
    public static final int SORT_BY_PROJECT = 8;
    /** Used to sort according to ascendant order */
    public static final int ASC_ORDER = 1;
    /** Used to sort according to descendant order */
    public static final int DESC_ORDER = 2;
    private static int currentSort = SORT_BY_ID;
    private static int currentOrder = ASC_ORDER;

    
    /** ProActive default constructor */
    public JobState() {
    }

    /**
     * Set the jobInfo contained in the TaskInfo to this job.
     *
     * @param info a taskInfo containing a job info.
     */
    public abstract void update(TaskInfo info);

    /**
     * To update the content of this job with a jobInfo.
     *
     * @param jobInfo the jobInfo to set
     */
    public abstract void update(JobInfo jobInfo);

    /**
     * Set the field to sort on.
     *
     * @param sortBy the field on which the sort will be made.
     */
    public static void setSortingBy(int sortBy) {
        currentSort = sortBy;
    }

    /**
     * Set the order for the next sort.
     *
     * @param order
     */
    public static void setSortingOrder(int order) {
        if ((order == ASC_ORDER) || (order == DESC_ORDER)) {
            currentOrder = order;
        } else {
            currentOrder = ASC_ORDER;
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param job The internal job to be compared.
     * @return  a negative integer, zero, or a positive integer as this job
     *		is less than, equal to, or greater than the specified job.
     *
     */
    public int compareTo(JobState job) {
        switch (currentSort) {
            case SORT_BY_DESCRIPTION:
                return (currentOrder == ASC_ORDER) ? (description.compareTo(job.description))
                        : (job.description.compareTo(description));
            case SORT_BY_NAME:
                return (currentOrder == ASC_ORDER) ? (name.compareTo(job.name)) : (job.name.compareTo(name));
            case SORT_BY_PRIORITY:
                return (currentOrder == ASC_ORDER) ? (getJobInfo().getPriority().getPriority() - job
                        .getJobInfo().getPriority().getPriority()) : (job.getJobInfo().getPriority()
                        .getPriority() - getJobInfo().getPriority().getPriority());
            case SORT_BY_TYPE:
                return (currentOrder == ASC_ORDER) ? (getType().compareTo(job.getType())) : (job.getType()
                        .compareTo(getType()));
            case SORT_BY_OWNER:
                return (currentOrder == ASC_ORDER) ? (getOwner().compareTo(job.getOwner())) : (job.getOwner()
                        .compareTo(getOwner()));
            case SORT_BY_STATUS:
                return (currentOrder == ASC_ORDER) ? (getJobInfo().getStatus().compareTo(job.getJobInfo()
                        .getStatus())) : (job.getJobInfo().getStatus().compareTo(getJobInfo().getStatus()));
            case SORT_BY_PROJECT:
                return (currentOrder == ASC_ORDER) ? (getProjectName().compareTo(job.getProjectName()))
                        : (job.getProjectName().compareTo(getProjectName()));
            default:
                return (currentOrder == ASC_ORDER) ? (getId().compareTo(job.getId())) : (job.getId()
                        .compareTo(getId()));
        }
    }

    /**
     * To get the jobInfo of this job.
     *
     * @return the jobInfo of this job.
     */
    public abstract JobInfo getJobInfo();

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getId()
     */
    @Override
    public JobId getId() {
        return getJobInfo().getJobId();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getPriority()
     */
    @Override
    public JobPriority getPriority() {
        return getJobInfo().getPriority();
    }

    /**
     * To get the tasks as an array list.
     *
     * @return the tasks contains in this job.
     */
    public abstract List<TaskState> getTasks();

    /**
     * To get the tasks as a hash map.
     *
     * @return the tasks as a hash map
     */
    public abstract Map<TaskId, TaskState> getHMTasks();


    /**
     * To get the task as an array list and filtered by a given tag.
     * @param tag the used to filter the tasks.
     * @return the set of filtered task states.
     */
    public List<TaskState> getTasksByTag(final String tag){
        List<TaskState> tasks = this.getTasks();
        return (List<TaskState>) CollectionUtils.select(tasks, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                String taskTag = ((TaskState) object).getTag();
                return (taskTag != null) && (taskTag.equals(tag));
            }
        });
    }
    
    
    /**
     * To get the paginated tasks.
     * The tasks are paginated with the following rule: [offset,limit[
     * @param offset the starting index of the sublist of tasks to get
     * @param limit the last index (non inclusive) of the sublist of tasks to get
     * @return a TaskStatePage which includes subset of tasks and the total number of all tasks
     */
    public TaskStatesPage getTasksPaginated(final int offset, final int limit) {
        return getTaskStatesPage(offset, limit, getTasks());
    }

    /**
     * To get the paginated filtered tasks by a given tag.
     * The filtered tasks are paginated with the following rule: [offset,limit[
     * @param tag used to filter the tasks
     * @param offset the starting index of the sublist of tasks to get
     * @param limit the last index (non inclusive) of the sublist of tasks to get
     * @return a TaskStatePage which includes subset of filtered tasks and the total number of all filtered tasks
     */
    public TaskStatesPage getTaskByTagPaginated(final String tag, final int offset, final int limit) {
        return getTaskStatesPage(offset, limit, getTasksByTag(tag));
    }

    private TaskStatesPage getTaskStatesPage(int offset, int limit, List<TaskState> tasks) {
        PageBoundaries pageBoundaries =
                Pagination.getTasksPageBoundaries(
                        offset, limit, PASchedulerProperties.TASKS_PAGE_SIZE.getValueAsInt());

        int nbTasks = tasks.size();
        int indexLastItemToReturn =  pageBoundaries.getOffset() + pageBoundaries.getLimit();

        offset = pageBoundaries.getOffset();
        if (offset >= nbTasks) {
            offset = 0;
        }

        if (indexLastItemToReturn >= nbTasks) {
            indexLastItemToReturn = nbTasks;
        }

        return new TaskStatesPage(tasks.subList(offset, indexLastItemToReturn), nbTasks);
    }

    /**
     * To get the list of available tags in a job.
     * @return the list of tags.
     */
    public List<String> getTags(){
        Set<String> result = new HashSet<>();
        String tag = null;
        for(TaskState task: this.getTasks()){
            tag = task.getTag();
            if(tag != null) {
                result.add(task.getTag());
            }
        }
        return new ArrayList<>(result);
    }


    /**
     * To get the list of available tags in a job and that matches a given prefix.
     * @param prefix the prefix used to filter the tags.
     * @return the list of tags.
     */
    public List<String> getTags(String prefix){
        Set<String> result = new HashSet<>();
        String tag = null;
        for(TaskState task: this.getTasks()){
            tag = task.getTag();
            if(tag != null && tag.startsWith(prefix)) {
                result.add(task.getTag());
            }
        }
        return new ArrayList<>(result);
    }


    /**
     * To get the numberOfFinishedTask
     *
     * @return the numberOfFinishedTask
     */
    public int getNumberOfFinishedTasks() {
        return getJobInfo().getNumberOfFinishedTasks();
    }

    /**
     * To know if the job is finished or not.
     *
     * @return true if the job is finished, false otherwise.
     */
    public boolean isFinished() {
        return this.getNumberOfFinishedTasks() == this.getTotalNumberOfTasks();
    }

    /**
     * To get the finishedTime
     *
     * @return the finishedTime
     */
    public long getFinishedTime() {
        return getJobInfo().getFinishedTime();
    }

    /**
     * To get the numberOfPendingTask
     *
     * @return the numberOfPendingTask
     */
    public int getNumberOfPendingTasks() {
        return getJobInfo().getNumberOfPendingTasks();
    }

    /**
     * To get the numberOfRunningTask
     *
     * @return the numberOfRunningTask
     */
    public int getNumberOfRunningTasks() {
        return getJobInfo().getNumberOfRunningTasks();
    }

    /**
     * To get the numberOfFailedTask
     *
     * @return the numberOfFailedTask
     */
    public int getNumberOfFailedTasks() {
        return getJobInfo().getNumberOfFailedTasks();
    }

    /**
     * To get the numberOfFailedTask
     *
     * @return the numberOfFailedTask
     */
    public int getNumberOfFaultyTasks() {
        return getJobInfo().getNumberOfFaultyTasks();
    }

    /**
     * To get the numberOfInErrorTask
     *
     * @return the numberOfInErrorTask
     */
    public int getNumberOfInErrorTasks() {
        return getJobInfo().getNumberOfInErrorTasks();
    }

    /**
     * To get the startTime
     *
     * @return the startTime
     */
    public long getStartTime() {
        return getJobInfo().getStartTime();
    }

    /**
     * To get the totalNumberOfTasks
     *
     * @return the totalNumberOfTasks
     */
    public int getTotalNumberOfTasks() {
        return getJobInfo().getTotalNumberOfTasks();
    }

    /**
     * To get the removedTime
     *
     * @return the removedTime
     */
    public long getRemovedTime() {
        return getJobInfo().getRemovedTime();
    }

    /**
     * To get the submittedTime
     *
     * @return the submittedTime
     */
    public long getSubmittedTime() {
        return getJobInfo().getSubmittedTime();
    }

    /**
     * To get the status of the job.
     *
     * @return the status of the job.
     */
    public JobStatus getStatus() {
        return getJobInfo().getStatus();
    }

    /**
     * To get the owner of the job.
     *
     * @return the owner of the job.
     */
    public abstract String getOwner();

    /**
     * Get the toBeRemoved property.
     * If this method returns true, this job is about to be removed from scheduler.
     *
     * @return the toBeRemoved property.
     */
    public boolean isToBeRemoved() {
        return getJobInfo().isToBeRemoved();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getName()
     */
    @Override
    public String getName() {
        if (getId() == null || getId().getReadableName().equals(SchedulerConstants.JOB_DEFAULT_NAME)) {
            return super.getName();
        } else {
            return getId().getReadableName();
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof JobState) {
            return getId().equals(((JobState) o).getId());
        }

        return false;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getId() + "]";
    }
    
}
