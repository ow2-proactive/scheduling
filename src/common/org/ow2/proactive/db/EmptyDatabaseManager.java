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

import java.util.ArrayList;
import java.util.List;


/**
 * This is an implementation of memory database.<br>
 * Everything is kept in memory. Can be used for testing purpose.<br>
 * <b>WARNING</B> this implementation can lead to "out of memory" error.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class EmptyDatabaseManager implements DatabaseManager {

    /**
     * Create a new instance of EmptyDatabaseManager
     */
    public EmptyDatabaseManager() {
    }

    /**
     * Do nothing
     */
    public void setProperty(String propertyName, String value) {
    }

    /**
     * Do nothing
     */
    public void build() {
    }

    /**
     * Do nothing
     */
    public void close() {
    }

    /**
     * Do nothing
     */
    public void startTransaction() {
    }

    /**
     * Do nothing
     */
    public void commitTransaction() {
    }

    /**
     * Do nothing
     */
    public void runAsSingleTransaction(DatabaseCallback callback) {
    }

    /**
     * Do nothing
     */
    public void rollbackTransaction() {
    }

    /**
     * Store the given object
     *
     * @param o the new object to store.
     */
    public void register(Object o) {
    }

    /**
     * Do nothing
     */
    public void delete(Object o) {
    }

    /**
     * Do nothing
     */
    public void update(Object o) {
    }

    /**
     * Return an empty list
     */
    public <T> List<T> recover(Class<T> egClass) {
        return recover(egClass, new Condition[] {});
    }

    /**
     * Not implemented (Throws a runtimeException)
     */
    public <T> List<T> recover(Class<T> egClass, Condition... conditions) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Do nothing
     */
    public void synchronize(Object o) {
    }

    /**
     * Do nothing
     */
    public void load(Object o) {
    }

    /**
     * Do nothing
     */
    public void unload(Object o) {
    }

    /**
     * Do nothing and returns an enpty list
     */
    @SuppressWarnings("rawtypes")
    public List sqlQuery(String nativeQuery) {
        return new ArrayList<Object>();
    }

    /**
     * Do nothing
     */
    public void setCallback(FilteredExceptionCallback callback) {
    }

}
