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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.db.Condition;
import org.ow2.proactive.db.ConditionComparator;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.db.HibernateDatabaseManager;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.RecoverCallback;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * DatabaseManager is responsible of every database transaction.<br />
 * Hibernate entities can be managed by this manager. It provides method to register, delete, synchronize
 * objects in database.
 * Each method will work on the object and its inheritance.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SchedulerHibernateDatabaseManager extends HibernateDatabaseManager implements
        SchedulerDatabaseManager {

    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.DATABASE);
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.DATABASE);

    /**
     * This method is specially design to recover all jobs that have to be recovered.
     * It is also a way to get them without taking care of conditions.
     * This method also ensure that every returned jobs have their @unloadable fields unloaded.
     *
     * @return The list of unloaded job entities.
     */
    public List<InternalJob> recoverAllJobs() {
        return recoverAllJobs(null);
    }

    /**
     * This method is specially design to recover all jobs that have to be recovered.
     * It is also a way to get them without taking care of conditions.
     * This method also ensure that every returned jobs have their @unloadable fields unloaded.
     *
     * @param callback use a callback to be notified of the recovering process.
     * 			this method will first call the init method to set the number of job to recover,
     * 			then it will call the jobRecovered method each time a job is recovered.
     * @return The list of unloaded job entities.
     */
    @SuppressWarnings( { "unchecked", "serial" })
    public List<InternalJob> recoverAllJobs(RecoverCallback callback) {
        if (callback == null) {
            callback = new RecoverCallback() {
                public void init(int nb) {
                }

                public void jobRecovered() {
                }
            };
        }
        List<InternalJob> jobs = new ArrayList<InternalJob>();
        //class to recover
        Class<?> egClass = InternalJob.class;
        //create condition of recovering : recover only non-removed job
        Condition condition = new Condition("jobInfo.removedTime", ConditionComparator.LESS_EQUALS_THAN,
            (long) 0);
        logger_dev.info("Recovering all jobs using the removeTime condition");
        Session session = getSessionFactory().openSession();
        try {
            String conds = " WHERE c." + condition.getField() + " " + condition.getComparator().getSymbol() +
                " :C0";
            //find ID to recover
            String squery = "SELECT c.jobInfo.jobId from " + egClass.getName() + " c" + conds;
            Query query = session.createQuery(squery);
            logger_dev.info("Created query to find IDs : " + squery);
            query.setParameter("C0", condition.getValue());
            logger_dev.debug("Set parameter " + "'C0' value=" + condition.getValue());
            List<JobId> ids = (List<JobId>) query.list();
            int idsSize = ids.size();
            logger.info("Found " + idsSize + " jobs to retrieve");
            callback.init(idsSize);
            logger_dev.info("Creating queries for each job to recover");
            //for each ID get the entity
            for (JobId jid : ids) {
                squery = "SELECT c from " + egClass.getName() + " c WHERE c.jobInfo.jobId=:C0";
                query = session.createQuery(squery);
                logger_dev.debug("Created query : " + squery);
                query.setParameter("C0", jid);
                logger_dev.debug("Set parameter " + "'C0' value=" + jid);
                InternalJob job = (InternalJob) query.uniqueResult();
                try {
                    Collection<TaskResult> results = job.getJobResult().getAllResults().values();
                    //unload taskResult
                    logger_dev.debug("Unloading taskResult for job " + jid);
                    for (TaskResult r : results) {
                        unload(r);
                    }
                } catch (NullPointerException e) {
                    logger_dev.debug("No result yet in this job " + jid);
                    //this exception means their is no results in this job
                }
                //unload InternalTask
                logger_dev.debug("Unloading internalTask for job " + jid);
                Collection<InternalTask> tasks = job.getITasks();
                for (InternalTask it : tasks) {
                    unload(it);
                }
                jobs.add(job);
                logger_dev.info("Job " + jid + " added to the final list");
                callback.jobRecovered();
            }
            return jobs;
        } catch (Exception e) {
            logger_dev.error("", e);
            throw new DatabaseManagerException("Unable to recover a job !", e);
        } finally {
            session.close();
            logger_dev.debug("Session closed");
        }
    }

    @Override
    public String getConfigFile() {
        return PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_CONFIG
                .getValueAsString());
    }

    @Override
    public Logger getDevLogger() {
        return logger;
    }

    @Override
    public Logger getLogger() {
        return logger_dev;
    }
}
