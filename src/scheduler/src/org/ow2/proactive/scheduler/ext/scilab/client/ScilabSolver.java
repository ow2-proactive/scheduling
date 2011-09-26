/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.scilab.client;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.ext.matsci.client.MatSciJobPermanentInfo;
import org.ow2.proactive.scheduler.ext.matsci.client.LoginFrame;
import org.ow2.proactive.scheduler.ext.matsci.client.Pair;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;


/**
 * This class is a Java object handling the interaction between Scilab c/jni code and the ActiveObject AOScilabEnvironment
 *
 * @author The ProActive Team
 */
public class ScilabSolver {

    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCILAB);

    private static AOScilabEnvironment scilabEnv;

    private static Node node;

    static {
        scilabEnv = null;
    }

    public ScilabSolver() {

    }

    public static Pair<MatSciJobPermanentInfo, ArrayList<ScilabResultsAndLogs>> solve(
            PASolveScilabGlobalConfig config, PASolveScilabTaskConfig[][] taskConfigs) throws Throwable {
        Pair<MatSciJobPermanentInfo, ArrayList<ScilabResultsAndLogs>> results = scilabEnv.solve(config,
                taskConfigs);
        results = (Pair<MatSciJobPermanentInfo, ArrayList<ScilabResultsAndLogs>>) PAFuture
                .getFutureValue(results);
        return results;
    }

    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public static AOScilabEnvironment getEnvironment() {
        return scilabEnv;
    }

    public static boolean isConnected() {
        if (scilabEnv == null) {
            return false;
        }
        try {
            return scilabEnv.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static void disconnect() {
        if (scilabEnv == null) {
            throw new IllegalStateException("This session is not connected to a Scheduler.");
        }
        scilabEnv.disconnect();
    }

    public static boolean isLoggedIn() {
        return isConnected() && scilabEnv.isLoggedIn();
    }

    public static String createConnection(String url) throws Exception {
        try {
            if (scilabEnv == null) {
                if (CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue().equals("rmi")) {
                    int rmiport = CentralPAPropertyRepository.PA_RMI_PORT.getValue();
                    try {
                        LocateRegistry.createRegistry(rmiport);
                    } catch (Exception e) {
                        System.out
                                .println("WARNING: could not create a RMI registry at port " +
                                    rmiport +
                                    ", maybe the registry is hosted by another Java process, this can lead to unexpected error. Try to change the " +
                                    CentralPAPropertyRepository.PA_RMI_PORT.getName() + " property.");
                    }
                }
                node = NodeFactory.createLocalNode("ScilabNode", true, null, null);
                scilabEnv = (AOScilabEnvironment) PAActiveObject.newActive(AOScilabEnvironment.class
                        .getName(), new Object[] {}, node);

            } else if (!isConnected()) {
                scilabEnv.terminate();
                Thread.sleep(1000);
                NodeFactory.killNode(node.getNodeInformation().getURL());
                node = NodeFactory.createLocalNode("ScilabNode", true, null, null);
                scilabEnv = (AOScilabEnvironment) PAActiveObject.newActive(AOScilabEnvironment.class
                        .getName(), new Object[] {}, node);
            }

            if (!scilabEnv.isJoined()) {
                scilabEnv.join(url);
            }

            if (!scilabEnv.isLoggedIn()) {
                scilabEnv.startLogin();
            }

            while (!scilabEnv.isLoggedIn() && scilabEnv.getNbAttempts() <= LoginFrame.MAX_NB_ATTEMPTS) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (scilabEnv.getNbAttempts() > LoginFrame.MAX_NB_ATTEMPTS) {
                throw new Exception("Maximum number of Login attempts reached.");
            }
            return null;

        } catch (Throwable e) {
            return getStackTrace(e);

        }
    }
}
