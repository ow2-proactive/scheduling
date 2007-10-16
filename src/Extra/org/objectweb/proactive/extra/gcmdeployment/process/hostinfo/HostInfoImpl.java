/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.gcmdeployment.process.hostinfo;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.OperatingSystem;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;


public class HostInfoImpl implements HostInfo {
    private String username;
    private String homeDirectory;
    private String id;
    private int hostCapacity;
    private int vmCapacity;
    private OperatingSystem os;
    private Set<Tool> tools;
    private long deploymentId;

    public HostInfoImpl() {
        username = null;
        homeDirectory = null;
        id = null;
        hostCapacity = 0;
        vmCapacity = 0;
        os = null;
        tools = new HashSet<Tool>();
        deploymentId = 0;
    }

    public HostInfoImpl(String id) {
        this();
        this.id = id;
    }

    /**
     * Checks that all required fields have been set.
     *
     * @throws IllegalStateException If a required field has not been set
     */
    public void check() throws IllegalStateException {
        if (id == null) {
            throw new IllegalStateException(
                "id field is not set in this HostInfo\n" + toString());
        }

        if (homeDirectory == null) {
            throw new IllegalStateException("homeDirectory is not set for id=" +
                id + "\n" + toString());
        }

        if (os == null) {
            throw new IllegalStateException("os is not set for id=" + id +
                "\n" + toString());
        }

        if ((hostCapacity % vmCapacity) != 0) {
            throw new IllegalStateException(
                "hostCapacity is not a multiple of vmCapacity for HostInfo=" +
                id + "\n" + toString());
        }

        // Theses fields are not mandatory
        if (username == null) {
            GCMD_LOGGER.debug("HostInfo is ready but username has not been set");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HostInfo<" + super.toString() + ">, ");
        sb.append(" id=" + id);
        sb.append(" os=" + os);
        sb.append(" username=" + username);
        sb.append(" homeDir=" + homeDirectory);
        sb.append(" VMCapacity=" + vmCapacity);
        sb.append(" HostCapacity=" + hostCapacity);
        sb.append(" deploymentId=" + deploymentId);

        sb.append("\n");
        for (Tool tool : tools) {
            sb.append("\t" + tool + "\n");
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
        return result;
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
        final HostInfoImpl other = (HostInfoImpl) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setHomeDirectory(String homeDirectory) {
        GCMD_LOGGER.trace("HostInfo " + id + ".homeDirectory <-- " +
            homeDirectory);
        this.homeDirectory = homeDirectory;
    }

    public void setId(String id) {
        GCMD_LOGGER.trace("HostInfo " + id + " created");
        this.id = id;
    }

    public void setOs(OperatingSystem os) {
        GCMD_LOGGER.trace("HostInfo " + id + ".os <-- " + os);
        this.os = os;
    }

    public void setHostCapacity(int hostCapacity) {
        GCMD_LOGGER.trace("HostInfo " + id + ".hostCapacity <-- " +
            hostCapacity);
        this.hostCapacity = hostCapacity;
    }

    public void setVmCapacity(int vmCapacity) {
        GCMD_LOGGER.trace("HostInfo " + id + ".vmCapacity <-- " + vmCapacity);
        this.vmCapacity = vmCapacity;
    }

    public void addTool(Tool tool) {
        GCMD_LOGGER.trace("HostInfo " + id + " added tool: " + tool);
        this.tools.add(tool);
    }

    public String getHomeDirectory() {
        return homeDirectory;
    }

    public String getId() {
        return id;
    }

    public OperatingSystem getOS() {
        return os;
    }

    public Tool getTool(String id) {
        for (Tool tool : tools) {
            if (tool.getId().equals(id)) {
                return tool;
            }
        }

        return null;
    }

    public Set<Tool> getTools() {
        return tools;
    }

    public String getUsername() {
        return username;
    }

    public int getHostCapacity() {
        return hostCapacity;
    }

    public int getVmCapacity() {
        return vmCapacity;
    }

    public long getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(long nodeId) {
        GCMD_LOGGER.trace("HostInfo " + id + ".nodeId <-- " + nodeId);
        this.deploymentId = nodeId;
        GCMD_LOGGER.trace(toString());
    }

    public boolean isCapacitiyValid() {
        if ((hostCapacity == 0) && (vmCapacity == 0)) {
            return true;
        }
        if ((hostCapacity != 0) && (vmCapacity != 0)) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("unused")
    static public class UnitTestHostInfoImpl {
        HostInfoImpl notInitialized;
        HostInfoImpl halfInitialized;
        HostInfoImpl fullyInitialized;

        @Before
        public void before() {
            notInitialized = new HostInfoImpl();

            halfInitialized = new HostInfoImpl();
            halfInitialized.setId("toto");
            halfInitialized.addTool(new Tool("tool", "//path"));

            fullyInitialized = new HostInfoImpl();
            fullyInitialized.setId("id");
            fullyInitialized.setOs(OperatingSystem.unix);
            fullyInitialized.setHomeDirectory("//homeidr");
            fullyInitialized.setUsername("usermane");
            fullyInitialized.addTool(new Tool("tool", "//path"));
        }

        @Test
        public void getTool1() {
            Assert.assertNotNull(fullyInitialized.getTool("tool"));
            Assert.assertNull(fullyInitialized.getTool("tool2"));
        }

        @Test
        public void equality() {
            HostInfoImpl tmp = new HostInfoImpl();
            tmp.setId("id");
            Assert.assertTrue(tmp.equals(fullyInitialized));

            tmp = new HostInfoImpl();
            tmp.setId("xxxxxxx");
            Assert.assertFalse(tmp.equals(fullyInitialized));
        }

        @Test(expected = IllegalStateException.class)
        public void checkReadygetHalfInitialized() {
            halfInitialized.check();
        }

        @Test(expected = IllegalStateException.class)
        public void checkReadygetHomeDirectory() {
            notInitialized.check();
        }
    }
}
