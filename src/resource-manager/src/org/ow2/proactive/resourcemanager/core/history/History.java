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
package org.ow2.proactive.resourcemanager.core.history;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.db.DatabaseManager;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * 
 * This class represents the node history event stored in the data base.
 * Basically for each node we store all state transitions and start/end time of each transition.
 *
 */
@Entity
@Table(name = "History")
public class History {

    public static final Logger logger = ProActiveLogger.getLogger(RMLoggers.DATABASE);

    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    protected long id;

    @Column(name = "nodeUrl")
    @Index(name = "urlIndex")
    private String nodeUrl;

    @Column(name = "host")
    private String host;

    @Column(name = "nodeSource")
    private String nodeSource;

    @Column(name = "userName")
    @Index(name = "userIndex")
    private String userName;

    @Column(name = "providerName")
    @Index(name = "providerIndex")
    private String providerName;

    @Column(name = "nodeState")
    private NodeState nodeState;

    @Column(name = "startTime")
    protected long startTime;

    @Column(name = "endTime")
    protected long endTime;

    // indicates that new record in the data base will be created for this event
    @Transient
    private boolean createNewRecord;

    /**
     * Constructs new history record.
     */
    public History(RMNodeEvent event) {

        this.nodeUrl = event.getNodeUrl();
        this.host = event.getHostName();
        this.nodeSource = event.getNodeSource();

        this.userName = event.getNodeOwner();
        this.providerName = event.getNodeProvider();
        this.nodeState = event.getNodeState();
        this.startTime = event.getTimeStamp();

        createNewRecord = true;
        // do not store TO_BE REMOVED record as it reflects nothing
        //
        // when the node is removed do not create a new record - 
        // just updating the end time of the last state.
        if (NodeState.TO_BE_REMOVED == event.getNodeState() ||
            RMEventType.NODE_REMOVED == event.getEventType()) {
            // new node history event
            createNewRecord = false;
            logger.debug("Creating new line in the data base for " + event);
        }
    }

    /**
     * Updates the previous history record and creates a new one if necessary
     */
    public void save() {
        // updating the previous history record
        updatePreviousItem();

        if (createNewRecord) {
            // registering the new history record		
            DatabaseManager.getInstance().register(this);
        }
    }

    /**
     * Updates the previous history record, namely sets the end time of the previous node state
     */
    private void updatePreviousItem() {
        String sql = "UPDATE " + this.getClass().getAnnotation(Table.class).name() + " SET ";
        try {
            String endTimeColumnName = this.getClass().getDeclaredField("endTime")
                    .getAnnotation(Column.class).name();
            String nodeUrlColumnName = this.getClass().getDeclaredField("nodeUrl")
                    .getAnnotation(Column.class).name();

            sql += endTimeColumnName + " = " + startTime + " ";
            sql += "WHERE " + nodeUrlColumnName + " = '" + nodeUrl + "' and " + endTimeColumnName + " = 0";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int result = DatabaseManager.getInstance().sqlUpdateQuery(sql);
        logger.debug("Updating previous item for node " + nodeUrl + ": " + result + " raws affected");
    }

    /**
     * After the resource manager is terminated some history records may not have end time stamp.
     * We set it at the moment of next RM start taking the time from Alive table.
     */
    public static void recover(Alive alive) {

        String historyTableName = History.class.getAnnotation(Table.class).name();

        // first updating the end time of each record with the last up time of the resource manager
        // where it's bigger than start time
        String sql = "UPDATE " + historyTableName + " SET ";
        try {
            String endTimeColumnName = History.class.getDeclaredField("endTime").getAnnotation(Column.class)
                    .name();
            String startTimeColumnName = History.class.getDeclaredField("startTime").getAnnotation(
                    Column.class).name();

            sql += endTimeColumnName + " = " + alive.getTime() + " ";
            sql += "WHERE " + startTimeColumnName + " < " + alive.getTime() + " AND " + endTimeColumnName +
                " = 0";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int result = DatabaseManager.getInstance().sqlUpdateQuery(sql);
        logger
                .debug("Restoring the node history: " + result +
                    " raws updated with the time from Alive table");

        // now put start time into endTime for other history records with 0 end time
        sql = "UPDATE " + historyTableName + " SET ";
        try {
            String endTimeColumnName = History.class.getDeclaredField("endTime").getAnnotation(Column.class)
                    .name();
            String startTimeColumnName = History.class.getDeclaredField("startTime").getAnnotation(
                    Column.class).name();

            sql += endTimeColumnName + " = " + startTimeColumnName + " ";
            sql += "WHERE " + endTimeColumnName + " = 0";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        result = DatabaseManager.getInstance().sqlUpdateQuery(sql);
        logger.debug("Restoring the node history: " + result +
            " raws updated putting the start time into the end time");
    }
}
