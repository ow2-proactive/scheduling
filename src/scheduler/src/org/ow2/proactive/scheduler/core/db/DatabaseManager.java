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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.RecoverCallback;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * DatabaseManager is responsible of every database transaction.<br />
 * It provides method to register, delete, synchronize objects in database.
 * Each method will work on the object and its inheritance.
 * An implementation of empty DB that keep everything in memory is also provided.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public abstract class DatabaseManager {

    private static DatabaseManager dbManager;

    public static DatabaseManager getInstance() {
        if (dbManager == null) {
            if (System.getProperty("scheduler.database.nodb") != null) {
                dbManager = new EmptyDatabaseManager();
            } else {
                dbManager = new HibernateDatabaseManager();
            }
        }
        return dbManager;
    }

    /**
     * Set user define property for the database session.<br />
     * Properties that are set after building process won't be used. Set property before building the session factory.<br />
     * This method override the default properties.
     *
     * @param propertyName the name of the property to set.
     * @param value the value of the property.
     */
    public abstract void setProperty(String propertyName, String value);

    /**
     * Build the database.<br/>
     * Call this method when you want to build the database. If not called,
     * it will be automatically build when needed.<br />
     * For performance reason, It is recommended to call this method during your application building process.<br /><br/>
     * It is also possible to set some properties before building the session.
     */
    public abstract void build();

    /**
     * Close the databasae
     */
    public abstract void close();

    /**
     * Force a transaction to be started. This method has to be used only when multiple calls
     * to methods of this class have to be performed.<br />
     * For simple atomic call, transaction is implicit.<br /><br />
     *
     * To use the manual transaction, call this startTransaction() method,<br/>
     * then, (when multiple modifications are done) a call to {@link #commitTransaction()} OR {@link #rollbackTransaction()}
     * will terminate the transaction.
     */
    public abstract void startTransaction();

    /**
     * Force a manually opened transaction to be committed.
     * See {@link #startTransaction()} for details.
     */
    public abstract void commitTransaction();

    /**
     * Force a manually opened transaction to be rolledback.
     * See {@link #startTransaction()} for details.
     */
    public abstract void rollbackTransaction();

    /**
     * Register an object.
     * This method will persist the given object and store it in the database.
     *
     * @param o the new object to store.
     */
    public abstract void register(Object o);

    /**
     * Delete an object.
     * This method will delete the given object and also every dependences.
     *
     * @param o the new object to delete.
     */
    public abstract void delete(Object o);

    /**
     * Update the given entity and every dependences.
     *
     * @param o the entity to update.
     */
    public abstract void update(Object o);

    /**
     * Return a list of every <T> type stored in the database.
     *
     * @param <T> The type to be returned by the recovering call.
     * @param egClass The class that represents the real type to be recover.
     * @return a list of every <T> type stored in the database.
     */
    public abstract <T> List<T> recover(Class<T> egClass);

    /**
     * Return a list of every <T> type stored matching the given conditions.
     *
     * @param <T> The type to be returned by the recovering call.
     * @param egClass The class that represents the real type to be recover.
     * @param conditions a list of condition that represents the conditions of the request.
     * @return a list of every <T> type stored matching the given conditions.
     */
    public abstract <T> List<T> recover(Class<T> egClass, Condition... conditions);

    /**
     * This method is specially design to recover all jobs that have to be recovered.
     * It is also a way to get them without taking care of conditions.
     * This method also ensure that every returned jobs have their @unloadable fields unloaded.
     * 
     * @return The list of unloaded job entities.
     */
    public abstract List<InternalJob> recoverAllJobs();

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
    public abstract List<InternalJob> recoverAllJobs(RecoverCallback callback);

    /**
     * Synchronize the given object in the database.<br />
     * The object must have @alterable field(s).
     * In fact this method will only update the database for @alterable field for this object.
     *
     * @param o the object entity to synchronize.
     */
    public abstract void synchronize(Object o);

    /**
     * Load the @unloadable field in the given object.<br />
     * This method will set, from the values in the database, every NULL fields that have the @unloadable annotation.
     *
     * @param o the object entity to load.
     */
    public abstract void load(Object o);

    /**
     * Cause every @unloadable fields in the given object to be set to NULL.<br />
     * Primitive field type won't be unloaded.
     *
     * @param o the object to unload.
     */
    public abstract void unload(Object o);

}

/**
 * This is an implementation of memory database.<br>
 * Everything is kept in memory. Can be used for testing purpose.<br>
 * <b>WARNING</B> this implementation can lead to "out of memory" error.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
class EmptyDatabaseManager extends DatabaseManager {

    private Map<JobId, InternalJob> jobs;

    /**
     * Create a new instance of EmptyDatabaseManager
     */
    public EmptyDatabaseManager() {
        jobs = new HashMap<JobId, InternalJob>();
    }

    /**
     * Do nothing
     */
    @Override
    public void setProperty(String propertyName, String value) {
    }

    /**
     * Do nothing
     */
    @Override
    public void build() {
    }

    /**
     * Do nothing
     */
    @Override
    public void close() {
    }

    /**
     * Do nothing
     */
    @Override
    public void startTransaction() {
    }

    /**
     * Do nothing
     */
    @Override
    public void commitTransaction() {
    }

    /**
     * Do nothing
     */
    @Override
    public void rollbackTransaction() {
    }

    /**
     * Store the given object
     *
     * @param o the new object to store.
     */
    @Override
    public void register(Object o) {
        InternalJob job = (InternalJob) o;
        jobs.put(job.getId(), job);
    }

    /**
     * Do nothing
     */
    @Override
    public void delete(Object o) {
    }

    /**
     * Do nothing
     */
    @Override
    public void update(Object o) {
    }

    /**
     * Return an empty list
     */
    @Override
    public <T> List<T> recover(Class<T> egClass) {
        return recover(egClass, new Condition[] {});
    }

    /**
     * If egClass is JobResultImpl, return the jobResult corresponding to the given JobId in the given condition,
     * If egClass is TaskresultImpl, return the taskResult corresponding to the given TaskId in the given condition,
     * Otherwise, return an empty list
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> recover(Class<T> egClass, Condition... conditions) {
        if (conditions == null || conditions.length == 0) {
            return new ArrayList<T>();
        }
        if (JobResult.class.isAssignableFrom(egClass)) {
            ArrayList<T> res = new ArrayList<T>();
            res.add((T) jobs.get(((JobId) conditions[0].getValue())).getJobResult());
            return res;
        } else if (TaskResult.class.isAssignableFrom(egClass)) {
            ArrayList<T> res = new ArrayList<T>();
            TaskId id = (TaskId) conditions[0].getValue();
            res.add((T) jobs.get(id.getJobId()).getJobResult().getResult(id.getReadableName()));
            return res;
        } else {
            return new ArrayList<T>();
        }
    }

    /**
     * Return an empty list of InternalJob
     */
    @Override
    public List<InternalJob> recoverAllJobs() {
        return recoverAllJobs(null);
    }

    /**
     * Return an empty list of InternalJob
     */
    @Override
    public List<InternalJob> recoverAllJobs(RecoverCallback callback) {
        return new ArrayList<InternalJob>();
    }

    /**
     * Do nothing
     */
    @Override
    public void synchronize(Object o) {
    }

    /**
     * Do nothing
     */
    @Override
    public void load(Object o) {
    }

    /**
     * Do nothing
     */
    @Override
    public void unload(Object o) {
    }

}
