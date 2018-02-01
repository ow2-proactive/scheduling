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
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeDescriptor;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * @author ActiveEon Team
 * @since 05/06/2017
 */
@Entity
@NamedQueries({ @NamedQuery(name = "deleteAllRMNodeData", query = "delete from RMNodeData"),
                @NamedQuery(name = "getAllRMNodeData", query = "from RMNodeData"),
                @NamedQuery(name = "deleteRMNodeDataByUrl", query = "delete from RMNodeData where nodeUrl=:url"),
                @NamedQuery(name = "getRMNodeDataByNodeSource", query = "from RMNodeData where nodeSource.name=:name") })
@Table(name = "RMNodeData")
public class RMNodeData implements Serializable {

    private String name;

    private String nodeUrl;

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

    private boolean locked;

    private Client lockedBy;

    private long lockTime;

    /** Command line of deploying node. For RMDeployingNode only */
    private String commandLine;

    /** Description of a deploying or lost node. For RMDeployingNode only */
    private String description;

    /**
     * Create an instance of {@link RMNodeData} and populate its fields with the content of a node.
     * @param rmNode the object to take the values from
     * @return a new instance of {@link RMNodeData} with the values of the given {@link RMNode}
     */
    public static RMNodeData createRMNodeData(RMNode rmNode) {
        Builder builder = new Builder().name(rmNode.getNodeName())
                                       .nodeUrl(rmNode.getNodeURL())
                                       .provider(rmNode.getProvider())
                                       .state(rmNode.getState())
                                       .stateChangeTime(rmNode.getStateChangeTime())
                                       .locked(rmNode.isLocked())
                                       .lockedBy(rmNode.getLockedBy())
                                       .lockTime(rmNode.getLockTime());
        if (!rmNode.getState().equals(NodeState.DEPLOYING) && !rmNode.getState().equals(NodeState.LOST)) {
            builder.owner(rmNode.getOwner())
                   .userPermission(rmNode.getUserPermission())
                   .hostname(rmNode.getHostName())
                   .jmxUrls(rmNode.getJmxUrls())
                   .jvmName(rmNode.getDescriptorVMName());
        } else {
            builder.commandLine(((RMDeployingNode) rmNode).getCommandLine())
                   .description(((RMDeployingNode) rmNode).getDescription());
        }

        return builder.build();
    }

    public RMNodeData() {
        // Required empty constructor
    }

    private RMNodeData(String name, String nodeUrl, Client owner, Client provider, Permission permission,
            NodeState state, long stateChangeTime, String hostName, String[] jmxUrls, String jvmName, boolean locked,
            Client lockedBy, long lockTime, String commandLine, String description) {
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
        this.locked = locked;
        this.lockedBy = lockedBy;
        this.lockTime = lockTime;
        this.commandLine = commandLine;
        this.description = description;
    }

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

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Client getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(Client lockedBy) {
        this.lockedBy = lockedBy;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "text")
    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public String getDescription() {
        return description;
    }

    public void setdescription(String description) {
        this.description = description;
    }

    public RMNodeEvent createNodeEvent(String nodeSourceName) {

        RMNodeDescriptor rmNodeDescriptor = new RMNodeDescriptor();
        rmNodeDescriptor.setNodeURL(nodeUrl);
        rmNodeDescriptor.setDefaultJMXUrl(jmxUrls == null ? null : jmxUrls[JMXTransportProtocol.RMI.ordinal()]);
        rmNodeDescriptor.setDescriptorVMName(jvmName);
        rmNodeDescriptor.setHostName(hostname);
        rmNodeDescriptor.setLocked(locked);
        rmNodeDescriptor.setLockedBy(getLockedBy() == null ? null : getLockedBy().getName());
        rmNodeDescriptor.setLockTime(getLockTime());
        rmNodeDescriptor.setNodeSourceName(nodeSourceName);
        rmNodeDescriptor.setOwnerName(getOwner() == null ? null : getOwner().getName());
        rmNodeDescriptor.setProactiveJMXUrl(jmxUrls == null ? null : jmxUrls[JMXTransportProtocol.RO.ordinal()]);
        rmNodeDescriptor.setProviderName(getProvider() == null ? null : getProvider().getName());
        rmNodeDescriptor.setState(getState());
        rmNodeDescriptor.setStateChangeTime(getStateChangeTime());
        rmNodeDescriptor.setVNodeName(jvmName);

        return new RMNodeEvent(rmNodeDescriptor, null, null, null);
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
        return node.getNodeInformation().getURL().equals(nodeUrl);
    }

    @Override
    public int hashCode() {
        return nodeUrl.hashCode();
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
            return other.getNodeUrl().equals(nodeUrl);
        } else {
            return false;
        }
    }

    public static class Builder {

        private String name;

        private String nodeUrl;

        private Client owner;

        private Client provider;

        private Permission userPermission;

        private NodeState state;

        private long stateChangeTime;

        private String hostname;

        private String[] jmxUrls;

        private String jvmName;

        private boolean locked;

        private Client lockedBy;

        private long lockTime;

        private String commandLine;

        private String description;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder nodeUrl(String nodeUrl) {
            this.nodeUrl = nodeUrl;
            return this;
        }

        public Builder owner(Client owner) {
            this.owner = owner;
            return this;
        }

        public Builder provider(Client provider) {
            this.provider = provider;
            return this;
        }

        public Builder userPermission(Permission userPermission) {
            this.userPermission = userPermission;
            return this;

        }

        public Builder state(NodeState state) {
            this.state = state;
            return this;

        }

        public Builder stateChangeTime(long stateChangeTime) {
            this.stateChangeTime = stateChangeTime;
            return this;

        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder jmxUrls(String[] jmxUrls) {
            this.jmxUrls = jmxUrls;
            return this;
        }

        public Builder jvmName(String jvmName) {
            this.jvmName = jvmName;
            return this;
        }

        public Builder locked(boolean locked) {
            this.locked = locked;
            return this;
        }

        public Builder lockedBy(Client lockedBy) {
            this.lockedBy = lockedBy;
            return this;
        }

        public Builder lockTime(long lockTime) {
            this.lockTime = lockTime;
            return this;
        }

        public Builder commandLine(String commandLine) {
            this.commandLine = commandLine;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public RMNodeData build() {
            return new RMNodeData(name,
                                  nodeUrl,
                                  owner,
                                  provider,
                                  userPermission,
                                  state,
                                  stateChangeTime,
                                  hostname,
                                  jmxUrls,
                                  jvmName,
                                  locked,
                                  lockedBy,
                                  lockTime,
                                  commandLine,
                                  description);
        }

    }

}
