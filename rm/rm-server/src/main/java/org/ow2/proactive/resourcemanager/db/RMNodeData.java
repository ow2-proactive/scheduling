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
package org.ow2.proactive.resourcemanager.db;

import java.io.Serializable;
import java.security.Permission;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * @author ActiveEon Team
 * @since 05/06/2017
 */
@Entity
@NamedQueries({ @NamedQuery(name = "getAllRMNodeData", query = "from RMNodeData"),
                @NamedQuery(name = "getRMNodeDataByNameAndUrl", query = "from RMNodeData where name=:name and nodeUrl=:url"),
                @NamedQuery(name = "getRMNodeDataByNodeSource", query = "from RMNodeData where nodeSource.name=:name") })
@Table(name = "RMNodeData")
public class RMNodeData implements Serializable {

    private String nodeUrl;

    private String name;

    /** client taken the node for computations */
    private Client owner;

    /** client registered the node in the resource manager */
    private Client provider;

    /** Node access permission */
    private Permission userPermission;

    /** Current node state */
    private NodeState state;

    /** Time stamp of the latest state change */
    private long stateChangeTime;

    /** Node Source containing the node **/
    private NodeSourceData nodeSource;

    /** Name of the machine hosting the node */
    private String hostname;

    /** the jmx protocols that can be used */
    private String[] jmxUrls;

    /** Name identifying the JVM in which the node runs,
      * e.g. following the pattern: pnp://192.168.1.104:59357/PA_JVM[0-9]* */
    private String jvmName;

    /**
     * Create an instance of {@link RMNodeData} and populate its fields with the content of a node.
     * @param rmNode the object to take the values from
     * @return a new instance of {@link RMNodeData} with the values of the given {@link RMNode}
     */
    public static RMNodeData createRMNodeData(RMNode rmNode) {
        RMNodeData rmNodeData = new RMNodeData(rmNode.getNodeName(),
                                               rmNode.getNodeURL(),
                                               rmNode.getOwner(),
                                               rmNode.getProvider(),
                                               rmNode.getUserPermission(),
                                               rmNode.getState(),
                                               rmNode.getStateChangeTime(),
                                               rmNode.getHostName(),
                                               rmNode.getJmxUrls(),
                                               rmNode.getDescriptorVMName());
        return rmNodeData;
    }

    public RMNodeData() {
        // Required empty constructor
    }

    public RMNodeData(String name, String nodeUrl, Client owner, Client provider, Permission permission,
            NodeState state, long stateChangeTime, String hostName, String[] jmxUrls, String jvmName) {
        this.name = name;
        this.nodeUrl = nodeUrl;
        this.owner = owner;
        this.provider = provider;
        this.userPermission = permission;
        this.state = state;
        this.stateChangeTime = stateChangeTime;
        this.hostname = hostName;
        this.jmxUrls = jmxUrls;
        this.jvmName = jvmName;
    }

    @Id
    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Id
    @Column(nullable = false)
    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Client getOwner() {
        return owner;
    }

    public void setOwner(Client owner) {
        this.owner = owner;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Client getProvider() {
        return provider;
    }

    public void setProvider(Client provider) {
        this.provider = provider;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Permission getUserPermission() {
        return userPermission;
    }

    public void setUserPermission(Permission nodeAccessPermission) {
        this.userPermission = nodeAccessPermission;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public long getStateChangeTime() {
        return stateChangeTime;
    }

    public void setStateChangeTime(long stateChangeTime) {
        this.stateChangeTime = stateChangeTime;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NODESOURCEDATA_NAME", nullable = false, updatable = false)
    public NodeSourceData getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(NodeSourceData nodeSource) {
        this.nodeSource = nodeSource;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public String[] getJmxUrls() {
        return jmxUrls;
    }

    public void setJmxUrls(String[] jmxUrls) {
        this.jmxUrls = jmxUrls;
    }

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
    }

    /**
     * Say whether the current node data structure reflects a particular
     * instance of {@link Node}.
     * @param node the node to compare to
     * @return true if the node data has the same name and url as the given node
     */
    public boolean equalsToNode(Node node) {
        if (node == null) {
            return false;
        }
        return node.getNodeInformation().getName().equals(name) && node.getNodeInformation().getURL().equals(nodeUrl);
    }

    @Override
    public int hashCode() {
        return name.hashCode() + nodeUrl.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (obj instanceof RMNodeData) {
            RMNodeData other = (RMNodeData) obj;
            return other.getName().equals(name) && other.getNodeUrl().equals(nodeUrl);
        } else {
            return false;
        }
    }

}
