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
package org.ow2.proactive.scheduler.ext.matsci.client;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matsci.common.DummyJVMProcess;
import org.ow2.proactive.scheduler.ext.matsci.common.JVMSpawnHelper;
import org.ow2.proactive.scheduler.ext.matsci.common.ProcessInitializer;
import org.ow2.proactive.scheduler.ext.matsci.common.ProcessListener;

import java.io.*;
import java.util.Map;


/**
 * DataspaceHelper
 *
 * @author The ProActive Team
 */
public class DataspaceHelper implements ProcessInitializer, ProcessListener {

    static DataspaceHelper instance = null;

    /**
     * Node name where this task is being executed
     */
    protected static String nodeName = "MatSciDataSpaceHelper";

    protected PrintStream outDebug;

    protected File nodeTmpDir;

    protected JVMSpawnHelper helper;

    protected long semtimeout = 2;

    protected int retries = 30;

    protected boolean debug;

    protected String inbasename;

    protected String outbasename;

    protected int deployID;

    protected Node node;

    protected Process process;

    protected Integer childPid;

    protected static OperatingSystem os = OperatingSystem.getOperatingSystem();

    protected AODataspaceRegistry registry;

    private DataspaceHelper(String oldregistryurl, String inbasename, String outbasename, boolean debug)
            throws Throwable {

        this.debug = debug;

        this.inbasename = inbasename;

        this.outbasename = outbasename;

        // system temp dir
        String tmpPath = System.getProperty("java.io.tmpdir");

        // log file writer used for debugging
        File tmpDirFile = new File(tmpPath);
        nodeTmpDir = new File(tmpDirFile, nodeName);
        if (!nodeTmpDir.exists()) {
            nodeTmpDir.mkdirs();
        }
        File logFile = new File(tmpPath, "" + this.getClass().getSimpleName() + ".log");
        if (!logFile.exists()) {

            logFile.createNewFile();

        }

        outDebug = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)));

        if (oldregistryurl != null) {

            registry = (AODataspaceRegistry) PAActiveObject.lookupActive(AODataspaceRegistry.class.getName(),
                    oldregistryurl);
            node = PAActiveObject.getActiveObjectNode(registry);
            childPid = registry.getPID();

        }
        if (registry == null) {
            try {
                helper = new JVMSpawnHelper(debug, outDebug, nodeTmpDir, nodeName, semtimeout, retries);
                process = helper.startProcess(nodeName, this, this);

                IOTools.LoggingThread lt1;
                if (debug) {
                    lt1 = new IOTools.LoggingThread(process, "[DS]", System.out, System.err, outDebug);

                } else {
                    lt1 = new IOTools.LoggingThread(process);
                }
                Thread t1 = new Thread(lt1, "OUT DS");
                t1.setDaemon(true);
                t1.start();

                helper.waitForRegistration(process);

                deploy();

                helper.unsubscribeJMXRuntimeEvent();
            } catch (Throwable e) {
                outDebug.flush();
                outDebug.close();
                throw e;
            }
        }

    }

    /**
     * Tells if this DataspaceHelper object is properly connected to a running AODataspaceRegistry active object
     * @return
     */
    public static boolean isConnected() {
        if (instance == null) {
            return false;
        }
        if (instance.registry == null) {
            return false;
        }
        try {
            instance.registry.getPID();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initialize this DataspaceHelper
     * @param url if url is provided, it tries to reconnect to an existing running AODataspaceRegistry, if not it will spawn a new JVM to store a new AODataspaceRegistry active object
     * @param inbasename Input Space Base Name which will be prepended to any created DataSpace
     * @param outbasename Output Space Base Name which will be prepended to any created DataSpace
     * @param debug debug mode will print a lot of debugging information to standard output
     * @throws Throwable
     */
    public static void init(String url, String inbasename, String outbasename, boolean debug)
            throws Throwable {
        if (instance == null) {
            instance = new DataspaceHelper(url, inbasename, outbasename, debug);
        }
    }

    public static DataspaceHelper getInstance() throws Throwable {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    /**
     * Returns the URL of the remote AODataspaceRegistry active object
     * @return
     */
    public String getUrl() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return PAActiveObject.getUrl(registry);
    }

    /**
     * Asks the remote AODataspaceRegistry to create a DataSpace on the given path. If a dataspace already exists for this path, it will reuse it.
     * @param path path to create a new dataspace
     * @return
     */
    public Pair<String, String> createDataSpace(String path) {
        Pair<String, String> answer = registry.createDataSpace(path);
        answer = PAFuture.getFutureValue(answer);
        return answer;
    }

    /**
     * Shuts down the spawned JVM and remote AODataspaceRegistry
     */
    public void shutdown() {
        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + this.getClass().getSimpleName() +
                "] Shutting down handler");
            outDebug.println("[" + new java.util.Date() + " " + this.getClass().getSimpleName() +
                "] Shutting down handler");
        }
        try {
            node.killAllActiveObjects();
        } catch (NodeException e) {

        } catch (IOException e) {

        }
        if (process != null) {
            process.destroy();
        } else if (os.equals(OperatingSystem.windows)) {
            if (debug) {
                System.out.println("Killing process " + childPid);
                outDebug.println("Killing process " + childPid);
            }
            try {
                Runtime.getRuntime().exec("taskkill /PID " + childPid + " /T");
                Runtime.getRuntime().exec("tskill " + childPid);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            if (debug) {
                System.out.println("Killing process " + childPid);
                outDebug.println("Killing process " + childPid);
            }
            try {
                Runtime.getRuntime().exec("kill -9 " + childPid);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        outDebug.close();

    }

    private void deploy() throws NodeException, ActiveObjectCreationException {
        ProActiveException ex = null;

        if (debug) {
            System.out.println("[" + new java.util.Date() + " " + this.getClass().getSimpleName() +
                "] Deploying Worker");
            outDebug.println("[" + new java.util.Date() + " " + this.getClass().getSimpleName() +
                "] Deploying Worker");
        }

        registry = (AODataspaceRegistry) PAActiveObject.newActive(AODataspaceRegistry.class.getName(),
                new Object[] { inbasename, outbasename, nodeName, debug }, node);

    }

    public void initProcess(DummyJVMProcess jvmprocess, Map<String, String> env) throws Throwable {
        // do nothing
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void setDeployID(Integer deployID) {
        this.deployID = deployID;
    }

    public Integer getDeployID() {
        return deployID;
    }
}
