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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core.db;

import java.util.List;

import org.ow2.proactive.db.Condition;
import org.ow2.proactive.db.DatabaseManager.FilteredExceptionCallback;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.scheduler.core.RecoverCallback;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * SchedulerDatabaseManagerSelector is used to change the behavior of the database manager on event.<br/>
 * This manager selector will switch to an empty db manager strategy if the callback is notified.
 * And then, will notify its callback if it has been set.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.1
 */
public class SchedulerDatabaseManagerSelector implements SchedulerDatabaseManager, FilteredExceptionCallback {

    private SchedulerDatabaseManager strategy = null;
    private FilteredExceptionCallback callback = null;

    /**
     * Create a new instance of SchedulerDatabaseManagerSelector<br/>
     * This constructor will instanciate an empty database manager
     *
     */
    public SchedulerDatabaseManagerSelector() {
        this(new SchedulerEmptyDatabaseManager());
    }

    /**
     * Create a new instance of SchedulerDatabaseManagerSelector using the given strategy
     *
     * @param strategy the initial strategy to be used.
     */
    public SchedulerDatabaseManagerSelector(SchedulerDatabaseManager strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Given strategy cannot be null");
        }
        this.strategy = strategy;
    }

    /**
     * {@inheritDoc}
     */
    public void setProperty(String propertyName, String value) {
        strategy.setProperty(propertyName, value);
    }

    /**
     * {@inheritDoc}
     */
    public void build() {
        strategy.build();
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        strategy.close();
    }

    /**
     * {@inheritDoc}
     */
    public void startTransaction() {
        strategy.startTransaction();
    }

    /**
     * {@inheritDoc}
     */
    public void commitTransaction() {
        strategy.commitTransaction();
    }

    /**
     * {@inheritDoc}
     */
    public void rollbackTransaction() {
        strategy.rollbackTransaction();
    }

    /**
     * {@inheritDoc}
     */
    public void register(Object o) {
        strategy.register(o);
    }

    /**
     * {@inheritDoc}
     */
    public void delete(Object o) {
        strategy.delete(o);
    }

    /**
     * {@inheritDoc}
     */
    public void update(Object o) {
        strategy.update(o);
    }

    /**
     * {@inheritDoc}
     */
    public <T> List<T> recover(Class<T> egClass) {
        return strategy.recover(egClass);
    }

    /**
     * {@inheritDoc}
     */
    public <T> List<T> recover(Class<T> egClass, Condition... conditions) {
        return strategy.recover(egClass, conditions);
    }

    /**
     * {@inheritDoc}
     */
    public void synchronize(Object o) {
        strategy.synchronize(o);
    }

    /**
     * {@inheritDoc}
     */
    public void load(Object o) {
        strategy.load(o);
    }

    /**
     * {@inheritDoc}
     */
    public void unload(Object o) {
        strategy.unload(o);
    }

    /**
     * {@inheritDoc}
     */
    public List sqlQuery(String nativeQuery) {
        return strategy.sqlQuery(nativeQuery);
    }

    /**
     * {@inheritDoc}
     */
    public List<InternalJob> recoverAllJobs() {
        return strategy.recoverAllJobs();
    }

    /**
     * {@inheritDoc}
     */
    public List<InternalJob> recoverAllJobs(RecoverCallback callback) {
        return strategy.recoverAllJobs(callback);
    }

    /**
     * {@inheritDoc}
     */
    public void setCallback(FilteredExceptionCallback callback) {
        this.callback = callback;
        strategy.setCallback(this);
    }

    /**
     * {@inheritDoc}
     */
    public void notify(DatabaseManagerException dme) {
        //switch strategy and notify the the callback
        this.strategy = new SchedulerEmptyDatabaseManager();
        //then notify own callback
        if (this.callback != null) {
            this.callback.notify(dme);
        }
    }

}
