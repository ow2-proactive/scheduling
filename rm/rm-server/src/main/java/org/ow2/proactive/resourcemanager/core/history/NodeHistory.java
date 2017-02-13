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
package org.ow2.proactive.resourcemanager.core.history;

import javax.persistence.*;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;


/**
 * This class represents the node history event stored in the data base.
 * Basically for each node we store all state transitions and start/end time of each transition.
 */
@Entity
@Table(name = "NodeHistory", indexes = { @Index(name = "NODE_HISTORY_END_TIME", columnList = "endTime"),
                                         @Index(name = "NODE_HISTORY_NODE_URL", columnList = "nodeUrl"),
                                         @Index(name = "NODE_HISTORY_USER_NAME", columnList = "userName"), })
public class NodeHistory {

    public static final Logger logger = Logger.getLogger(NodeHistory.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NODE_HISTORY_ID_SEQUENCE")
    @SequenceGenerator(name = "NODE_HISTORY_ID_SEQUENCE", sequenceName = "NODE_HISTORY_ID_SEQUENCE")
    @SuppressWarnings("unused")
    protected long id;

    @Column(name = "nodeUrl")
    private String nodeUrl;

    @Column(name = "host")
    private String host;

    @Column(name = "nodeSource")
    private String nodeSource;

    @Column(name = "userName")
    private String userName;

    @Column(name = "providerName")
    private String providerName;

    @Column(name = "nodeState")
    private NodeState nodeState;

    @Column(name = "startTime")
    protected long startTime;

    @Column(name = "endTime")
    protected long endTime;

    // indicates that new record in the data base will be created for this event
    @Transient
    private boolean storeInDataBase;

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

        storeInDataBase = true;

        // do not store TO_BE REMOVED record as it reflects nothing
        //
        // when the node is removed do not create a new record - 
        // just updating the end time of the last state.
        if (NodeState.TO_BE_REMOVED == event.getNodeState() || RMEventType.NODE_REMOVED == event.getEventType()) {
            // new node history event
            storeInDataBase = false;
            logger.debug("Creating new line in the data base for " + event);
        }
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public NodeState getNodeState() {
        return nodeState;
    }

    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isStoreInDataBase() {
        return storeInDataBase;
    }

    public void setStoreInDataBase(boolean storeInDataBase) {
        this.storeInDataBase = storeInDataBase;
    }

}
