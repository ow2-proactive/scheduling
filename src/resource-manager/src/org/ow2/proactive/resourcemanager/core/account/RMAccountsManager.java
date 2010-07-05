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
package org.ow2.proactive.resourcemanager.core.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.account.AbstractAccountsManager;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.DatabaseManager;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * This class represents the Resource Manager accounts manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class RMAccountsManager extends AbstractAccountsManager<RMAccount> {

    /** Scheduler database manager used to submit SQL requests */
    private final org.ow2.proactive.db.DatabaseManager dbmanager;

    //private final String totalUsedNodeTimeSQL;

    //private final String totalProvidedNodeTimeAndNodeCountSQL;

    /**
     * Create a new instance of this class.
     */
    public RMAccountsManager() {
        super(new HashMap<String, RMAccount>(), "Resource Manager Accounts Refresher", ProActiveLogger
                .getLogger(RMLoggers.MONITORING));

        // Get the database manager
        this.dbmanager = DatabaseManager.getInstance();

        // Create the requests
        //this.totalUsedNodeTimeSQL = RMAccountsManager.totalUsedNodeTimeSQL();
        //this.totalProvidedNodeTimeAndNodeCountSQL = RMAccountsManager.totalProvidedNodeTimeAndNodeCountSQL();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultRefreshRateInSeconds() {
        int value = PAResourceManagerProperties.RM_ACCOUNT_REFRESH_RATE.getValueAsInt();
        return value;
    }

    /**
     * Reads database and fills accounts.
     */
    protected void internalRefresh(final Map<String, RMAccount> map) {
        // Get totalUsedNodeTime per node owner
        final List<?> usedRes = this.dbmanager.sqlQuery(RMAccountsManager.totalUsedNodeTimeSQL());

        // The result of the query is the tuple <NODEOWNER, DURATION>
        for (int i = 0; i < usedRes.size(); i++) {
            final Object[] tuple = (Object[]) usedRes.get(i);
            final String nodeOwner = (String) tuple[0];

            RMAccount acc = map.get(nodeOwner);
            if (acc == null) {
                acc = new RMAccount();
                map.put(nodeOwner, acc);
            }

            acc.usedNodeTime = ((Number) tuple[1]).longValue();
        }

        // Get totalProvidedNodeTime and node count per node provider
        final List<?> providedRes = this.dbmanager.sqlQuery(totalProvidedNodeTimeAndNodeCountSQL());

        // The result of the query is a tuple
        for (int i = 0; i < providedRes.size(); i++) {
            final Object[] tuple = (Object[]) providedRes.get(i);
            final String nodeProvider = (String) tuple[0];

            RMAccount acc = map.get(nodeProvider);
            if (acc == null) {
                acc = new RMAccount();
                map.put(nodeProvider, acc);
            }

            acc.providedNodeTime = ((Number) tuple[1]).longValue();
            acc.providedNodesCount = ((Number) tuple[2]).intValue();
        }
    }

    // Returned tuple: <NODEPROVIDER, DURATION>
    private static String totalUsedNodeTimeSQL() {
        final StringBuilder builder = new StringBuilder("SELECT tab.np, SUM(tab.duration) FROM ");
        builder.append("(SELECT t1.NODEPROVIDER as np, sum(t2.TIMESTAMP - t1.TIMESTAMP) as duration ");
        builder.append("FROM RMNODEEVENT t1 JOIN RMNODEEVENT t2 ON ");
        builder.append("t2.PREVIOUSEVENTID = t1.ID AND ");
        builder.append("t2.TYPE = 6 AND t2.NODESTATE=0 AND ");
        builder.append("t2.PREVIOUSNODESTATE=1 AND t1.TYPE = 6 AND t1.NODESTATE=1 ");
        builder.append("GROUP BY t1.NODEPROVIDER ");
        builder.append("UNION ");
        builder.append("SELECT t1.NODEPROVIDER as np, sum(t2.TIMESTAMP - t1.TIMESTAMP) as duration ");
        builder.append("FROM RMNODEEVENT t1 JOIN RMNODEEVENT t2 ON ");
        builder.append("(t2.PREVIOUSEVENTID = t1.ID AND ");
        builder.append("t2.TYPE = 7 AND t1.TYPE = 6 AND t1.NODESTATE=1) OR ");
        builder.append("(t2.PREVIOUSEVENTID = t1.ID AND ");
        builder.append("t2.TYPE = 6 AND t2.NODESTATE=2 AND t1.TYPE = 6 AND t1.NODESTATE=1) ");
        builder.append("GROUP BY t1.NODEPROVIDER ");
        builder.append("UNION ");
        builder.append("select t1.NODEPROVIDER as np, sum(" + System.currentTimeMillis() +
            " - t1.TIMESTAMP) as duration ");
        builder.append("from RMNODEEVENT t1 WHERE ");
        builder.append("t1.TYPE = 6 AND t1.NODESTATE=1 AND ");
        builder.append("t1.TIMESTAMP > (SELECT MAX(TIMESTAMP) FROM RMEVENT WHERE type=2) AND ");
        builder
                .append("(NOT EXISTS (select * from RMNODEEVENT WHERE PREVIOUSEVENTID=t1.ID AND PREVIOUSNODESTATE=1)) ");
        builder.append("GROUP BY t1.NODEPROVIDER ");
        builder.append(") tab GROUP BY tab.np");
        return builder.toString();
    }

    // FULL SQL REQUEST :

    //SELECT tab.np, SUM(tab.duration) FROM
    //(
    //-- Consistent GET duration from get->release
    //SELECT t1.NODEPROVIDER as np, sum(t2.TIMESTAMP - t1.TIMESTAMP) as duration
    //FROM RMNODEEVENT t1 JOIN RMNODEEVENT t2 ON
    //t2.PREVIOUSEVENTID = t1.ID AND
    //t2.TYPE = 6 AND t2.NODESTATE=0 AND
    //t2.PREVIOUSNODESTATE=1 AND t1.TYPE = 6 AND t1.NODESTATE=1
    //GROUP BY t1.NODEPROVIDER
    //UNION
    //-- Inconsistent GET duration from get->remove or get->down
    //SELECT t1.NODEPROVIDER as np, sum(t2.TIMESTAMP - t1.TIMESTAMP) as duration
    //FROM RMNODEEVENT t1 JOIN RMNODEEVENT t2 ON
    // -- remove
    //(t2.PREVIOUSEVENTID = t1.ID AND
    //t2.TYPE = 7 AND t1.TYPE = 6 AND t1.NODESTATE=1)
    //OR
    //-- down
    //(t2.PREVIOUSEVENTID = t1.ID AND
    //t2.TYPE = 6 AND t2.NODESTATE=2 AND t1.TYPE = 6 AND t1.NODESTATE=1)
    //GROUP BY t1.NODEPROVIDER
    //UNION
    // -- Inconsistent GET that are after the last restart (currently used nodes) must be SUM(System.currentTimeMillis() - t1.TIMESTAMP)
    //select t1.NODEPROVIDER as np, sum(t1.TIMESTAMP) as duration
    //from RMNODEEVENT t1
    //WHERE
    //-- Node GET event
    //t1.TYPE = 6 AND t1.NODESTATE=1
    //AND
    // -- From the last start
    //t1.TIMESTAMP > (SELECT MAX(TIMESTAMP) FROM RMEVENT WHERE type=2)
    //-- Inconsistent means not stopped and not removed
    //AND
    //(NOT EXISTS
    //   (select * from RMNODEEVENT WHERE PREVIOUSEVENTID = t1.ID AND PREVIOUSNODESTATE=1))
    //GROUP BY t1.NODEPROVIDER    
    //) tab
    //GROUP BY tab.np

    // Returned tuple: <NODEPROVIDER, DURATION, NODESCOUNT>
    private static String totalProvidedNodeTimeAndNodeCountSQL() {
        final StringBuilder builder = new StringBuilder("SELECT ");
        builder.append("tab.np, SUM(tab.d), COUNT(DISTINCT tab.nc) FROM ");
        // Compute all inconsistent adds before last start (the duration is computed using the heartbeat)
        builder
                .append("(select t1.NODEPROVIDER as np,  SUM(h1.TIMESTAMP - t1.TIMESTAMP) as d, t1.NODEURL as nc ");
        builder.append("from RMNODEEVENT t1 JOIN RMEVENT h1 ON ");
        builder.append("t1.TIMESTAMP < h1.TIMESTAMP WHERE ");
        builder.append("t1.TYPE=5 AND ");
        builder.append("h1.TIMESTAMP = (select MAX(TIMESTAMP) from RMEVENT h where ");
        builder.append("h.type = 8 AND ");
        builder
                .append("h.timestamp < (select MIN(TIMESTAMP) from RMEVENT where type = 2 AND timestamp > t1.timestamp)) ");
        builder
                .append("AND (NOT EXISTS (select * from RMNODEEVENT WHERE ADDEVENTID = t1.ID AND (TYPE=7 OR (TYPE=6 AND NODESTATE=2)))) ");
        builder.append("GROUP BY t1.NODEPROVIDER, t1.NODEURL ");
        builder.append("UNION ");
        // Same for current adds not followed by stop or remove
        builder.append("select t1.NODEPROVIDER as np, SUM(" + System.currentTimeMillis() +
            " - t1.TIMESTAMP) as d, t1.NODEURL as nc from RMNODEEVENT t1 ");
        builder.append("WHERE t1.TYPE=5 AND ");
        builder.append("(t1.TIMESTAMP > (SELECT MAX(TIMESTAMP) FROM RMEVENT WHERE type=2)) AND ");
        builder.append("(NOT EXISTS ");
        builder
                .append("(select * from RMNODEEVENT WHERE ADDEVENTID = t1.ID AND (TYPE=7 OR (TYPE=6 AND NODESTATE=2)))) ");
        builder.append("GROUP BY t1.NODEPROVIDER, t1.NODEURL ");
        builder.append("UNION ");
        builder
                .append("select t1.NODEPROVIDER as np, SUM(t2.TIMESTAMP - t1.TIMESTAMP) as d, t1.NODEURL as nc from RMNODEEVENT t1 ");
        builder.append("JOIN RMNODEEVENT t2 ON ");
        // Consistent adds are followed by stop
        builder.append("(t1.ID = t2.ADDEVENTID AND t1.TYPE = 5 AND (t2.TYPE = 6 AND t2.NODESTATE = 2)) OR ");
        // Or  followed by remove (without stop)
        builder
                .append("(t1.ID = t2.ADDEVENTID AND t1.TYPE = 5 AND t2.TYPE = 7 AND t2.PREVIOUSNODESTATE <> 2) ");
        builder.append("GROUP BY t1.NODEPROVIDER, t1.NODEURL ");
        builder.append(") tab GROUP BY tab.np");
        return builder.toString();
    }
}

// FULL SQL REQUEST :

//SELECT tab.np, SUM(tab.d), COUNT(DISTINCT tab.nc) FROM
//(
//-- All inconsistent adds before the last restart
//select t1.NODEPROVIDER as np,  SUM(h1.TIMESTAMP - t1.TIMESTAMP) as d, t1.NODEURL as nc
//from RMNODEEVENT t1
//JOIN RMEVENT h1
//ON
//    t1.TIMESTAMP < h1.TIMESTAMP
//WHERE
//    -- Node added event
//    t1.TYPE=5 AND
//    -- Before the last start
//--            (t1.TIMESTAMP <
//--                (SELECT MAX(TIMESTAMP) FROM RMEVENT WHERE type=2)) AND
//    -- The next heartbeat before the next start 
//    h1.TIMESTAMP = (select MAX(TIMESTAMP) from RMEVENT h where
//                     h.type = 8 AND
//                     h.timestamp < (select MIN(TIMESTAMP) from RMEVENT where type = 2 AND timestamp > t1.timestamp))
//    -- Inconsistent means not stopped and not removed
//    AND
//    (NOT EXISTS
//        (select * from RMNODEEVENT WHERE ADDEVENTID = t1.ID AND (TYPE=7 OR (TYPE=6 AND NODESTATE=2))))
// GROUP BY t1.NODEPROVIDER, t1.NODEURL
// UNION
//-- All inconsistent adds after the last restart (currently provided nodes) must be SUM(System.currentTimeMillis() - t1.TIMESTAMP)
//select t1.NODEPROVIDER as np, SUM(t1.TIMESTAMP) as d, t1.NODEURL as nc
//from RMNODEEVENT t1
//WHERE
//    -- Node added event
//    t1.TYPE=5
//    AND
//    -- From the last start
//    (t1.TIMESTAMP >
//        (SELECT MAX(TIMESTAMP) FROM RMEVENT WHERE type=2))
//    -- Inconsistent means not stopped and not removed
//    AND
//    (NOT EXISTS
//        (select * from RMNODEEVENT WHERE ADDEVENTID = t1.ID AND (TYPE=7 OR (TYPE=6 AND NODESTATE=2))))
//    GROUP BY t1.NODEPROVIDER, t1.NODEURL
//UNION
//-- All consistent adds
//select t1.NODEPROVIDER as np, SUM(t2.TIMESTAMP - t1.TIMESTAMP) as d, t1.NODEURL as nc from RMNODEEVENT t1
//JOIN RMNODEEVENT t2 ON
//(
//    t1.ID = t2.ADDEVENTID AND
//    t1.TYPE = 5 AND
//    (t2.TYPE = 6 AND t2.NODESTATE = 2)
//) OR
//(t1.ID = t2.ADDEVENTID AND t1.TYPE = 5 AND t2.TYPE = 7 AND t2.PREVIOUSNODESTATE <> 2)
//GROUP BY t1.NODEPROVIDER, t1.NODEURL
//) tab
//GROUP BY tab.np