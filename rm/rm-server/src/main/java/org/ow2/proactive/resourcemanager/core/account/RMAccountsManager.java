/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.resourcemanager.core.account;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.ow2.proactive.account.AbstractAccountsManager;
import org.ow2.proactive.resourcemanager.core.history.NodeHistory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.RMDBManager;


/**
 * This class represents the Resource Manager accounts manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class RMAccountsManager extends AbstractAccountsManager<RMAccount> {

    /** Scheduler database manager used to submit SQL requests */
    private final RMDBManager dbmanager;

    /**
     * Create a new instance of this class.
     */
    public RMAccountsManager() {
        super("Resource Manager Accounts Refresher", Logger.getLogger(RMAccountsManager.class));
        // Get the database manager
        this.dbmanager = RMDBManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultCacheValidityTimeInSeconds() {
        int value = PAResourceManagerProperties.RM_ACCOUNT_REFRESH_RATE.getValueAsInt();
        return value;
    }

    /**
     * 
     * Computes user account data by scanning the data base.
     * 
     */
    public RMAccount readAccount(final String user) {

        try {

            RMAccount account = new RMAccount();
            account.username = user;

            String history = NodeHistory.class.getAnnotation(Table.class).name();
            String endTime = NodeHistory.class.getDeclaredField("endTime").getAnnotation(Column.class).name();
            String startTime = NodeHistory.class.getDeclaredField("startTime").getAnnotation(Column.class)
                    .name();
            String nodeState = NodeHistory.class.getDeclaredField("nodeState").getAnnotation(Column.class)
                    .name();
            String userName = NodeHistory.class.getDeclaredField("userName").getAnnotation(Column.class)
                    .name();
            String providerName = NodeHistory.class.getDeclaredField("providerName").getAnnotation(
                    Column.class).name();
            String nodeUrl = NodeHistory.class.getDeclaredField("nodeUrl").getAnnotation(Column.class).name();

            // counting the time of finished actions 
            // select SUM(endTime-startTime) from History where endTime <> 0 and nodeState = 1 and userName='NAME'
            String wereBusy = "SELECT SUM(" + endTime + "-" + startTime + ") " + "FROM " + history +
                " WHERE " + userName + "='" + user + "' AND " + endTime + " <> 0 AND " + nodeState + " = 1";

            List<?> rows = dbmanager.sqlQuery(wereBusy);
            account.usedNodeTime += aggregateNodeUsageTime(rows);

            String areBusy = "SELECT SUM(" + System.currentTimeMillis() + "-" + startTime + ") " + "FROM " +
                history + " WHERE " + userName + "='" + user + "' AND " + endTime + " = 0 AND " + nodeState +
                " = 1";
            rows = dbmanager.sqlQuery(areBusy);
            account.usedNodeTime += aggregateNodeUsageTime(rows);

            // select SUM(endTime-startTime), COUNT(DISTINCT nodeUrl) from History where endTime <> 0 and nodeState in (0,1,3,6,7) and providerName='rm'
            String wereProvided = "SELECT COUNT(DISTINCT " + nodeUrl + "), SUM(" + endTime + "-" + startTime +
                ") " + "FROM " + history + " WHERE " + providerName + "='" + user + "' AND " + endTime +
                " <> 0 AND " + nodeState + " in (0,1,3,6,7)";
            // select SUM(CURRNET_TIME-startTime), COUNT(DISTINCT nodeUrl) from History where endTime = 0 and nodeState in (0,1,3,6,7) and providerName='rm'
            String areProvided = "SELECT 0, SUM(" + System.currentTimeMillis() + "-" + startTime + ") " +
                "FROM " + history + " WHERE " + providerName + "='" + user + "' AND " + endTime +
                " = 0 AND " + nodeState + " in (0,1,3,6,7)";

            rows = dbmanager.sqlQuery(wereProvided);
            account.providedNodesCount += aggregateProvidedNodesCount(rows);
            account.providedNodeTime += aggregateProvidedNodeTime(rows);

            rows = dbmanager.sqlQuery(areProvided);
            account.providedNodesCount += aggregateProvidedNodesCount(rows);
            account.providedNodeTime += aggregateProvidedNodeTime(rows);

            return account;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int aggregateNodeUsageTime(List<?> rows) {
        int usedNodeTime = 0;
        for (Object row : rows) {
            try {
                if (row != null) {
                    // result could be empty or null
                    usedNodeTime += Long.parseLong(row.toString());
                }
            } catch (RuntimeException e) {
                logger.warn(e.getMessage(), e);
                usedNodeTime = 0;
            }
        }
        return usedNodeTime;
    }

    private int aggregateProvidedNodesCount(List<?> rows) {
        int providedNodesCount = 0;
        for (Object row : rows) {
            Object[] columns = (Object[]) row;
            if (columns.length > 0 && columns[0] != null) {
                try {
                    // result could be empty or null
                    providedNodesCount += Integer.parseInt(columns[0].toString());
                } catch (RuntimeException e) {
                    logger.warn(e.getMessage(), e);
                    providedNodesCount = 0;
                }
            }
        }
        return providedNodesCount;
    }

    private int aggregateProvidedNodeTime(List<?> rows) {
        int providedNodeTime = 0;
        for (Object row : rows) {
            Object[] columns = (Object[]) row;
            if (columns.length > 1 && columns[1] != null) {
                try {
                    // result could be empty or null
                    providedNodeTime += Long.parseLong(columns[1].toString());
                } catch (RuntimeException e) {
                    logger.warn(e.getMessage(), e);
                    providedNodeTime = 0;
                }
            }
        }
        return providedNodeTime;
    }
}
