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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;
import org.ow2.proactive.db.Condition;
import org.ow2.proactive.db.ConditionComparator;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.db.DatabaseManager;


/**
 * 
 * This class represents the node history event stored in the data base.
 * Basically for each node we store all state transitions and start/end time of each transition.
 *
 */
@Entity
@Table(name = "NodeHistory")
public class NodeHistory {

    public static final Logger logger = Logger.getLogger(NodeHistory.class);

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
    private String providerName;

    @Column(name = "nodeState")
    private NodeState nodeState;

    @Column(name = "startTime")
    protected long startTime;

    @Column(name = "endTime")
    @Index(name = "endTimeIndex")
    protected long endTime;

    // indicates that new record in the data base will be created for this event
    @Transient
    private boolean createNewRecord;

    /**
     * Default constructor for Hibernate
     */
    public NodeHistory() {
    }

    /**
     * Constructs new history record.
     */
    public NodeHistory(RMNodeEvent event) {

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
        List<NodeHistory> previousRecords = DatabaseManager.getInstance().recover(NodeHistory.class,
                new Condition("nodeUrl", ConditionComparator.EQUALS_TO, this.nodeUrl),
                new Condition("endTime", ConditionComparator.EQUALS_TO, new Long(0)));

        for (NodeHistory prev : previousRecords) {
            prev.endTime = startTime;
            DatabaseManager.getInstance().update(prev);
        }

        logger.debug(nodeUrl + " : updating history");
    }

    /**
     * After the resource manager is terminated some history records may not have end time stamp.
     * We set it at the moment of next RM start taking the time from Alive table.
     */
    public static void recover(Alive alive) {

        List<NodeHistory> records = DatabaseManager.getInstance().recover(NodeHistory.class,
                new Condition("endTime", ConditionComparator.EQUALS_TO, new Long(0)));

        for (NodeHistory record : records) {
            if (record.startTime < alive.getTime()) {
                // alive time bigger than start time of the history record
                // marking the end of this record as last RM alive time
                record.endTime = alive.getTime();
            } else {
                // the event happened after last RM alive time update
                // just put endTime = startTime
                record.endTime = record.startTime;
            }
            DatabaseManager.getInstance().update(record);
        }

        logger.debug("Restoring the node history: " + records.size() + " raws updated");

    }
}
