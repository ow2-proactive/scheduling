/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;


/**
 * Simple cache for Statistic History
 * <p>
 * Fetching statistic history from the server can be costly,
 * but customization of the time range per request make the response vary.
 * <p>
 * This class will store the result of the requests along with the parameter,
 * so that future request matching the same parameter are directly retrieved from the cache.
 * 
 * @author mschnoor
 *
 */
public class StatHistoryCaching {

    private static final Logger LOGGER = Logger.getLogger(StatHistoryCaching.class);

    // invalidate entry after MAX_DURATION millis
    private static final long MAX_DURATION = 5000;

    public class StatHistoryCacheEntry {
        private long timeStamp;

        private String value;

        public StatHistoryCacheEntry(String value, long timeStamp) {
            this.timeStamp = timeStamp;
            this.value = value;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private HashMap<String, StatHistoryCacheEntry> statHistoryCache = null;

    private static StatHistoryCaching instance = null;

    private StatHistoryCaching() {
        this.statHistoryCache = new HashMap<>();
    }

    public static synchronized StatHistoryCaching getInstance() {
        if (instance == null)
            instance = new StatHistoryCaching();
        return instance;
    }

    /**
     * @param key key of the cache element to retrieve
     * @return the cache entry if it exists and has not expired, or null
     */
    public synchronized StatHistoryCacheEntry getEntry(String key) {
        StatHistoryCacheEntry entry = this.statHistoryCache.get(key);
        if (entry == null)
            return null;

        if (System.currentTimeMillis() - entry.timeStamp > MAX_DURATION) {
            this.statHistoryCache.remove(key);
            return null;
        }

        return entry;
    }

    public synchronized StatHistoryCacheEntry getEntryOrCompute(String key, RestCallable<String> valueCreator)
            throws ReflectionException, InterruptedException, NotConnectedException, IntrospectionException,
            IOException, InstanceNotFoundException, MalformedObjectNameException {
        StatHistoryCacheEntry entry = statHistoryCache.get(key);

        if (entry == null || (System.currentTimeMillis() - entry.timeStamp) > MAX_DURATION) {
            statHistoryCache.remove(key);
            long timeStamp = System.currentTimeMillis();
            String value = valueCreator.call();
            entry = new StatHistoryCacheEntry(value, timeStamp);
            statHistoryCache.put(key, entry);
        }

        return entry;
    }

    @FunctionalInterface
    public interface RestCallable<V> {
        V call() throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException,
                MalformedObjectNameException, NullPointerException, InterruptedException, NotConnectedException;
    }

    public synchronized void addEntry(String key, long timeStamp, String value) {
        StatHistoryCacheEntry entry = new StatHistoryCacheEntry(value, timeStamp);
        this.statHistoryCache.put(key, entry);
    }

}
