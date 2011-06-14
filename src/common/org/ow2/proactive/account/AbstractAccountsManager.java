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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.account;

import java.util.HashMap;
import java.util.Map;

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
    protected volatile Map<String, E> accountsMap;
    /** Cache valid time in seconds */
    private volatile int cacheValidTimeInSeconds;
    /** Last refresh duration */
    private volatile long lastCacheClearTimeStamp;
    /** Last refresh duration */
    private volatile long lastRefreshDurationInMilliseconds;

    protected AbstractAccountsManager(final String refreshThreadName, final Logger logger) {
        this.accountsMap = new HashMap<String, E>();
        this.cacheValidTimeInSeconds = this.getDefaultCacheValidityTimeInSeconds();
        this.logger = logger;
    }

    /**
     * Reads account information from the data base
     * @param username the name of the user for which account is read
     * @return account
     */
    protected abstract E readAccount(final String username);

    /**
     * Returns the accounts for the user specified by its username
     *
     * @param username the name of the user
     * @return The accounts of the user, can be null if the user is unknown
     * 
     */
    public E getAccount(final String username) {

        if (cacheValidTimeInSeconds == 0) {
            // the mean to disable the accounting
            throw new RuntimeException("The accounting is disabled.");
        }

        synchronized (accountsMap) {
            if (System.currentTimeMillis() - lastCacheClearTimeStamp > cacheValidTimeInSeconds * 1000) {
                clearCache();
            } else if (accountsMap.containsKey(username)) {
                return accountsMap.get(username);
            }
        }

        final long refreshStartTime = System.currentTimeMillis();
        E account = readAccount(username);
        lastRefreshDurationInMilliseconds = System.currentTimeMillis() - refreshStartTime;

        if (account != null) {
            synchronized (accountsMap) {
                accountsMap.put(username, account);
            }
        }

        return account;
    }

    /**
     * This methods performs a full refresh from database.
     */
    public synchronized void clearCache() {
        synchronized (accountsMap) {
            // clearing the map contained all the records
            // it will provoke the data base access next time the client request 
            // an accounting information
            this.accountsMap.clear();
            lastCacheClearTimeStamp = System.currentTimeMillis();
        }
    }

    /**
     * Sets the refresh rate of the accounts refresher.
     *
     * @param refreshRateInSeconds the refresh rate
     */
    public void setCacheValidityTimeInSeconds(final int cacheValidTimeInSeconds) {
        this.cacheValidTimeInSeconds = cacheValidTimeInSeconds;
    }

    /**
     * Returns the refresh rate of the accounts refresher.
     *
     * @return the current value of the refresh rate in seconds
     */
    public int getCacheValidityTimeInSeconds() {
        return this.cacheValidTimeInSeconds;
    }

    /**
     * Returns the default value of the accounts refresher.
     *
     * @return the default value of the refresh rate in seconds
     */
    public abstract int getDefaultCacheValidityTimeInSeconds();

    /**
     * Returns the duration of the last refresh performed by the account refresher.
     *
     * @return the duration of the last refresh
     */
    public long getLastRefreshDurationInMilliseconds() {
        return this.lastRefreshDurationInMilliseconds;
    }
}
