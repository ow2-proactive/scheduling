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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.account;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


/**
 * Sub classes may use the logger of this class.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 * @param <E> The specific Account implementation
 */
public abstract class AbstractAccountsManager<E extends Account> {
    /** The logger provided by sub-classes */
    protected final Logger logger;
    /** The map that contains all statistics */
    private volatile Map<String, E> accountsMap;
    /** Refresh delay changeable by the user */
    private volatile int refreshRateInSeconds;
    /** Last refresh duration */
    private volatile long lastRefreshDurationInMilliseconds;
    /** The single thread executor */
    private final ScheduledExecutorService executor;
    /** The accounts refresher runnable that resubmits itself to the executor */
    private final AccountsRefresher accountsRefresher;
    /** The refresh future */
    private volatile ScheduledFuture<?> refreshFuture;

    protected AbstractAccountsManager(final Map<String, E> accountsMap, final String refreshThreadName,
            final Logger logger) {
        this.accountsMap = accountsMap;
        this.refreshRateInSeconds = this.getDefaultRefreshRateInSeconds();
        // Create the single thread executor that creates min priority daemon
        // thread
        this.executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, refreshThreadName);
                t.setDaemon(true);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            }
        });
        this.logger = logger;

        // Create the cache refresher and start it
        this.accountsRefresher = new AccountsRefresher();
    }

    private void internalRefresh() {
        // Create new map
        final HashMap<String, E> newMap = new HashMap<String, E>(this.accountsMap.size());
        this.internalRefresh(newMap);
        this.accountsMap = newMap;
    }

    /**
     * Reads database and fills accounts.
     */
    protected abstract void internalRefresh(final Map<String, E> map);

    /**
     * Returns the accounts for the user specified by its username
     *
     * @param username the name of the user
     * @return The accounts of the user, can be null if the user is unknown (has
     *         never submitted jobs and is not connected)
     */
    public E getAccount(final String username) {
        return this.accountsMap.get(username);
    }

    /**
     * This methods returns all performs a full refresh from database so it can take some
     * time and return accounts for all users. Only one such method can be executed
     * at a time.
     *
     * @return a map of fresh user accounts from the database
     */
    public Map<String, E> getAllAccounts() {
        //        // If it is scheduled and not running
        //        if (this.refreshFuture.getDelay(TimeUnit.SECONDS) > 0) {
        //            // schedule it to run immediately
        //            this.internalSchedule(true);
        //        }
        //        try {
        //            this.refreshFuture.get();
        //        } catch (Exception e) {
        //            logger_dev.error("Unable to wait for the end of the refresh", e);
        //        }
        return this.accountsMap;
    }

    /**
     * This methods performs a full refresh from database.
     */
    public synchronized void refreshAllAccounts() {
        // If it is already running just return
        if (this.refreshFuture != null && this.refreshFuture.getDelay(TimeUnit.SECONDS) == 0) {
            return;
        }
        // schedule it to run immediately
        this.internalSchedule(true);
    }

    /**
     * If the accounts refresher is not running, starts a new thread that
     * refreshes the user statistics from the data base.
     */
    public void startAccountsRefresher() {
        this.internalSchedule(false);
    }

    private void internalSchedule(final boolean immediate) {
        long refreshRate = this.refreshRateInSeconds;
        if (immediate) {
            if (this.refreshFuture != null) {
                this.refreshFuture.cancel(true);
            }
            // The refresh rate is set to 0 to comply with the executor schedule() method semantic
            // that represents the delay (0 delay means immediate)
            refreshRate = 0;
        } else {
            // The refresh rate can be set to 0 by the user to disable the accounts refresh
            if (refreshRate == 0) {
                return;
            }
        }
        this.refreshFuture = this.executor.schedule(this.accountsRefresher, refreshRate, TimeUnit.SECONDS);
    }

    /**
     * Sets the refresh rate of the accounts refresher.
     *
     * @param refreshRateInSeconds the refresh rate
     */
    public void setRefreshRateInSeconds(final int refreshRateInSeconds) {
        final int oldValue = this.refreshRateInSeconds;
        this.refreshRateInSeconds = refreshRateInSeconds;
        // Reschedule if the account refresher is currently disabled (=0)
        if (oldValue == 0 && refreshRateInSeconds > 0) {
            this.internalSchedule(false);
        }
    }

    /**
     * Returns the refresh rate of the accounts refresher.
     *
     * @return the current value of the refresh rate in seconds
     */
    public int getRefreshRateInSeconds() {
        return this.refreshRateInSeconds;
    }

    /**
     * Returns the default value of the accounts refresher.
     *
     * @return the default value of the refresh rate in seconds
     */
    public abstract int getDefaultRefreshRateInSeconds();

    /**
     * Returns the duration of the last refresh performed by the account refresher.
     *
     * @return the duration of the last refresh
     */
    public long getLastRefreshDurationInMilliseconds() {
        return this.lastRefreshDurationInMilliseconds;
    }

    /**
     * Refreshes all accounts periodically from the database.
     */
    private final class AccountsRefresher implements Runnable {
        public void run() {
            final long refreshStartTime = System.currentTimeMillis();
            try {
                internalRefresh();
            } catch (Exception e) {
                logger.error("Exception when refreshing accounts", e);
            } finally {
                internalSchedule(false);
                lastRefreshDurationInMilliseconds = System.currentTimeMillis() - refreshStartTime;
            }
        }
    }
}
