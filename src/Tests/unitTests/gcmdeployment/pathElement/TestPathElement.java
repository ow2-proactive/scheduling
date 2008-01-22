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
package unitTests.gcmdeployment.pathElement;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderProActive;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.hostinfo.HostInfoImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.hostinfo.Tool;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.hostinfo.Tools;
import org.objectweb.proactive.extra.gcmdeployment.PathElement.PathBase;


public class TestPathElement {
    final String path = "/zzzz/plop";
    final String homeDir = "/user/barbie";
    final String proactiveDir = "/bin/proactive";
    final String toolDir = "/tools/proactive";

    @Test
    public void testRoot() {
        PathElement pe;

        HostInfoImpl hostInfo = new HostInfoImpl();
        hostInfo.setOs(OperatingSystem.unix);

        pe = new PathElement(path);
        Assert.assertEquals(path, pe.getRelPath());
        Assert.assertEquals(path, pe.getFullPath(hostInfo, null));

        pe = new PathElement(path, PathBase.ROOT);
        Assert.assertEquals(path, pe.getRelPath());
        Assert.assertEquals(path, pe.getFullPath(hostInfo, null));

        pe = new PathElement(path, "root");
        Assert.assertEquals(path, pe.getRelPath());
        Assert.assertEquals(path, pe.getFullPath(hostInfo, null));
    }

    @Test
    public void testHome() {
        HostInfoImpl hostInfo = new HostInfoImpl();
        hostInfo.setHomeDirectory(homeDir);
        hostInfo.setOs(OperatingSystem.unix);

        PathElement pe = new PathElement(path, PathBase.HOME);
        String expected = PathElement.appendPath(homeDir, path, hostInfo);
        Assert.assertEquals(expected, pe.getFullPath(hostInfo, null));
    }

    @Test
    public void testProActive() {
        HostInfoImpl hostInfo = new HostInfoImpl();
        hostInfo.setHomeDirectory(homeDir);
        hostInfo.setOs(OperatingSystem.unix);

        CommandBuilderProActive cb = new CommandBuilderProActive();
        cb.setProActivePath(proactiveDir);

        PathElement pe = new PathElement(path, PathBase.PROACTIVE);
        String expected = PathElement.appendPath(homeDir, proactiveDir, hostInfo);
        expected = PathElement.appendPath(expected, path, hostInfo);
        Assert.assertEquals(expected, pe.getFullPath(hostInfo, cb));
    }

    @Test
    public void testTool() {
        HostInfoImpl hostInfo = new HostInfoImpl();
        hostInfo.setHomeDirectory(homeDir);
        hostInfo.setOs(OperatingSystem.unix);
        hostInfo.addTool(new Tool(Tools.PROACTIVE.id, toolDir));

        CommandBuilderProActive cb = new CommandBuilderProActive();
        cb.setProActivePath(proactiveDir);

        PathElement pe = new PathElement(path, PathBase.PROACTIVE);

        String expected = PathElement.appendPath(homeDir, toolDir, hostInfo);
        expected = PathElement.appendPath(expected, path, hostInfo);
        Assert.assertEquals(expected, pe.getFullPath(hostInfo, cb));
    }

    @Test
    public void testToolException() {
        HostInfoImpl hostInfo = new HostInfoImpl();
        hostInfo.setHomeDirectory(homeDir);
        hostInfo.setOs(OperatingSystem.unix);

        CommandBuilderProActive cb = new CommandBuilderProActive();
        PathElement pe = new PathElement(path, PathBase.PROACTIVE);

        String expected = PathElement.appendPath(homeDir, toolDir, hostInfo);
        expected = PathElement.appendPath(expected, path, hostInfo);
        Assert.assertEquals(null, pe.getFullPath(hostInfo, cb));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        PathElement pe = new PathElement(path, PathBase.PROACTIVE);
        Assert.assertEquals(pe, pe.clone());
    }

    @Test
    public void testAppendPath() {
        String s1;
        String s2;
        String expected;

        HostInfoImpl hostInfo = new HostInfoImpl();
        hostInfo.setOs(OperatingSystem.unix);

        expected = "/toto";
        s1 = "/";
        s2 = "toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo));
        s1 = "/";
        s2 = "/toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo));
        s1 = "";
        s2 = "toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo));
        s1 = "";
        s2 = "toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo));
    }
}
