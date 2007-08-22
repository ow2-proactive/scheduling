package unitTests.deployment.pathElement;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.PathElement.PathBase;
import org.objectweb.proactive.extra.gcmdeployment.process.commandbuilder.CommandBuilderProActive;
import org.objectweb.proactive.extra.gcmdeployment.process.hostinfo.HostInfoImpl;
import org.objectweb.proactive.extra.gcmdeployment.process.hostinfo.Tool;
import org.objectweb.proactive.extra.gcmdeployment.process.hostinfo.Tools;


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
