/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core.db;

import java.util.List;

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
            dbManager = new HibernateDatabaseManager();
            //dbManager = new EmptyDatabaseManager();
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
