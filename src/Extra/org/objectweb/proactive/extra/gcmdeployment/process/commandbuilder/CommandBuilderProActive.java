package org.objectweb.proactive.extra.gcmdeployment.process.commandbuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.PathElement.PathBase;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeInternal;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;


public class CommandBuilderProActive implements CommandBuilder {
    public final static String DEFAULT_JAVAPATH = System.getProperty(
            "java.home") + File.separator + "bin" + File.separator + "java";
    public static String DEFAULT_POLICY_FILE = System.getProperty(
            "java.security.policy");
    public static String DEFAULT_LOG4J_FILE = System.getProperty(
            "log4j.configuration");

    /** Path to the ProActive installation */
    private PathElement proActivePath;

    /** Declared Virtual nodes*/
    private Map<String, VirtualNodeInternal> vns;

    /** Path to ${java.home}/bin/java */
    private PathElement javaPath = new PathElement(DEFAULT_JAVAPATH);

    /** Arguments to be passed to java */
    private List<String> javaArgs;

    /** ProActive classpath
     *
     *  If not set, then the default classpath is used
     */
    private List<PathElement> proactiveClasspath;

    /** Application classpath */
    private List<PathElement> applicationClasspath;

    /** File transfers to perform before starting the command */
    // FIXME cmathieu Not really sure fts are needed here
    private List<FileTransferBlock> fts;

    /** Security Policy file*/
    private PathElement securityPolicy = new PathElement(DEFAULT_POLICY_FILE);

    /** Log4j configuration file */
    private PathElement log4jProperties = new PathElement(DEFAULT_LOG4J_FILE);

    static {
        if (DEFAULT_POLICY_FILE != null) {
            DEFAULT_POLICY_FILE = getAbsolutePath(DEFAULT_POLICY_FILE);
        }
        if (DEFAULT_LOG4J_FILE != null) {
            DEFAULT_LOG4J_FILE = getAbsolutePath(DEFAULT_LOG4J_FILE);
        }
    }

    public CommandBuilderProActive() {
        vns = new HashMap<String, VirtualNodeInternal>();
        fts = new ArrayList<FileTransferBlock>();
        javaArgs = new ArrayList<String>();
    }

    public void addVirtualNode(VirtualNodeInternal vn) {
        addVirtualNode(vn.getName(), vn);
    }

    public void addVirtualNode(String id, VirtualNodeInternal vn) {
        vns.put(id, vn);
    }

    synchronized public void addProActivePath(PathElement pe) {
        if (proactiveClasspath == null) {
            proactiveClasspath = new ArrayList<PathElement>();
        }

        proactiveClasspath.add(pe);
    }

    synchronized public void setProActiveClasspath(List<PathElement> pe) {
        proactiveClasspath = pe;
    }

    synchronized public void addApplicationPath(PathElement pe) {
        if (applicationClasspath == null) {
            applicationClasspath = new ArrayList<PathElement>();
        }

        applicationClasspath.add(pe);
    }

    synchronized public void setApplicationClasspath(List<PathElement> pe) {
        applicationClasspath = pe;
    }

    public void setVirtualNodes(Map<String, VirtualNodeInternal> vns) {
        this.vns = vns;
    }

    public void addFileTransferBlock(FileTransferBlock ftb) {
        fts.add(ftb);
    }

    public void addJavaArg(String arg) {
        javaArgs.add(arg);
    }

    public void setJavaPath(PathElement pe) {
        javaPath = pe;
    }

    public void setLog4jProperties(PathElement pe) {
        log4jProperties = pe;
    }

    public void setSecurityPolicy(PathElement pe) {
        securityPolicy = pe;
    }

    public String buildCommand(HostInfo hostInfo) {
        if (proactiveClasspath == null) {
            proactiveClasspath = getDefaultProActiveClassPath();
        }

        if (log4jProperties == null) {
            log4jProperties = getDefaultLog4jProperties();
        }

        if (securityPolicy == null) {
            securityPolicy = getDefaultSecurityPolicy();
        }

        // TODO Build the command here
        return "java -version";
    }

    private PathElement getDefaultSecurityPolicy() {
        // TODO Return the default PathElement for Security Policy
        return null;
    }

    private List<PathElement> getDefaultProActiveClassPath() {
        // TODO Return the default PathElements for ProActive ClassPath
        return null;
    }

    private PathElement getDefaultLog4jProperties() {
        // TODO Return the default PathElemen for log4jProperties
        return null;
    }

    public void setProActivePath(String proActivePath) {
        this.proActivePath = new PathElement(proActivePath, PathBase.HOME);
    }

    public String getPath(HostInfo hostInfo) {
        if (proActivePath != null) {
            return proActivePath.getFullPath(hostInfo, this);
        }

        return null;
    }

    private static String getAbsolutePath(String path) {
        if (path.startsWith("file:")) {
            //remove file part to build absolute path
            path = path.substring(5);
        }
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.error(e.getMessage());
            return path;
        }
    }
}
