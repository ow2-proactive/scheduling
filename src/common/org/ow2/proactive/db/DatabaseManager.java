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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.db;

import java.util.List;


/**
 * DatabaseManager is responsible of every database transaction.<br />
 * It provides method to register, delete, synchronize objects in database.
 * Each method will work on the object and its inheritance.
 * An implementation of empty DB that keep everything in memory is also provided.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public interface DatabaseManager {

    /**
     * Set user define property for the database session.<br />
     * Properties that are set after building process won't be used. Set property before building the session factory.<br />
     * This method override the default properties.
     *
     * @param propertyName the name of the property to set.
     * @param value the value of the property.
     */
    public void setProperty(String propertyName, String value);

    /**
     * Build the database.<br/>
     * Call this method when you want to build the database. If not called,
     * it will be automatically build when needed.<br />
     * For performance reason, It is recommended to call this method during your application building process.<br /><br/>
     * It is also possible to set some properties before building the session.
     */
    public void build();

    /**
     * Close the databasae
     */
    public void close();

    /**
     * Force a transaction to be started. This method has to be used only when multiple calls
     * to methods of this class have to be performed.<br />
     * For simple atomic call, transaction is implicit.<br /><br />
     *
     * To use the manual transaction, call this startTransaction() method,<br/>
     * then, (when multiple modifications are done) a call to {@link #commitTransaction()} OR {@link #rollbackTransaction()}
     * will terminate the transaction.
     */
    public void startTransaction();

    /**
     * Force a manually opened transaction to be committed.
     * See {@link #startTransaction()} for details.
     */
    public void commitTransaction();

    /**
     * Force a manually opened transaction to be rolledback.
     * See {@link #startTransaction()} for details.
     */
    public void rollbackTransaction();

    /**
     * Execute database access code represented by the given DatabaseCallback
     * as single transaction. This method starts transaction before executing
     * callback and commits transaction when callback finishes. If any exception is thrown
     * by callback or commit then it tries to rollbacks transaction.
     */
    public void runAsSingleTransaction(DatabaseCallback callback);

    /**
     * Register an object.
     * This method will persist the given object and store it in the database.
     *
     * @param o the new object to store.
     */
    public void register(Object o);

    /**
     * Delete an object.
     * This method will delete the given object and also every dependences.
     *
     * @param o the new object to delete.
     */
    public void delete(Object o);

    /**
     * Update the given entity and every dependences.
     *
     * @param o the entity to update.
     */
    public void update(Object o);

    /**
     * Return a list of every <T> type stored in the database.
     *
     * @param <T> The type to be returned by the recovering call.
     * @param egClass The class that represents the real type to be recover.
     * @return a list of every <T> type stored in the database.
     */
    public <T> List<T> recover(Class<T> egClass);

    /**
     * Return a list of every <T> type stored matching the given conditions.
     *
     * @param <T> The type to be returned by the recovering call.
     * @param egClass The class that represents the real type to be recover.
     * @param conditions a list of condition that represents the conditions of the request.
     * @return a list of every <T> type stored matching the given conditions.
     */
    public <T> List<T> recover(Class<T> egClass, Condition... conditions);

    /**
     * Synchronize the given object in the database.<br />
     * The object must have @alterable field(s).
     * In fact this method will only update the database for @alterable field for this object.
     *
     * @param o the object entity to synchronize.
     */
    public void synchronize(Object o);

    /**
     * Load the @unloadable field in the given object.<br />
     * This method will set, from the values in the database, every NULL fields that have the @unloadable annotation.
     *
     * @param o the object entity to load.
     */
    public void load(Object o);

    /**
     * Cause every @unloadable fields in the given object to be set to NULL.<br />
     * Primitive field type won't be unloaded.
     *
     * @param o the object to unload.
     */
    public void unload(Object o);

    /**
     * Execute a native "SELECT" SQL query and return the result as a list. (see hibernate SQLQurey.list() for more details)
     * 
     * @param nativeQuery the query to be executed, "must be a SELECT query"
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List sqlQuery(String nativeQuery);

    /**
     * Set the callback value to the given callback value<br/>
     * call back system can be used to be notified when the database has encountered a disconnection
     *
     * @param callback the callback to set
     */
    public void setCallback(FilteredExceptionCallback callback);

    /**
     * FilteredExceptionCallback is used to notify the implementation
     * that a filtered exception has been raised.
     *
     * @author The ProActive Team
     * @since ProActive Scheduling 3.1
     */
    public interface FilteredExceptionCallback {
        /**
         * Notify the listener when a filtered exception is detected.<br/>
         * In such a case, this method is called on the listener. Argument is the exception that
         * caused this call wrapped in a DatabaseManagerException.<br/>
         * <br/>
         * Note : To get the real exception, just get the cause of the given exception.
         *
         * @param dme the DatabaseManagerException containing the cause.
         */
        public void notify(DatabaseManagerException dme);
    }
}
