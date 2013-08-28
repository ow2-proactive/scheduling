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
package org.ow2.proactive_grid_cloud_portal.common;

import java.util.HashMap;


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
        this.statHistoryCache = new HashMap<String, StatHistoryCacheEntry>();
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

    public synchronized void addEntry(String key, long timeStamp, String value) {
        StatHistoryCacheEntry entry = new StatHistoryCacheEntry(value, timeStamp);
        this.statHistoryCache.put(key, entry);
    }

}
