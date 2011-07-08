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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.ext.matsci.client.MatSciJobPermanentInfo;
import org.ow2.proactive.scheduler.ext.matsci.client.Pair;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;


/**
 * This class is a Java object handling the interaction between Scilab c/jni code and the ActiveObject AOScilabEnvironment
 *
 * @author The ProActive Team
 */
public class ScilabSolver {

    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.SCILAB);

    private static AOScilabEnvironment scilabSolver;

    static {
        scilabSolver = null;
    }

    public ScilabSolver() {

    }

    public static ArrayList<ScilabResultsAndLogs> solve(PASolveScilabGlobalConfig config,
            PASolveScilabTaskConfig[][] taskConfigs) throws Throwable {
        Pair<MatSciJobPermanentInfo, ArrayList<ScilabResultsAndLogs>> results = scilabSolver.solve(config,
                taskConfigs);
        results = (Pair<MatSciJobPermanentInfo, ArrayList<ScilabResultsAndLogs>>) PAFuture
                .getFutureValue(results);
        ArrayList<ScilabResultsAndLogs> flist = results.getY();
        ArrayList<ScilabResultsAndLogs> answer = new ArrayList<ScilabResultsAndLogs>();
        for (ScilabResultsAndLogs resf : flist) {
            ScilabResultsAndLogs res = PAFuture.getFutureValue(resf);
            answer.add(res);
        }
        return answer;
    }

    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public static String createConnection(String url) throws Exception {
        try {
            if (scilabSolver == null) {

                scilabSolver = (AOScilabEnvironment) PAActiveObject.newActive(AOScilabEnvironment.class
                        .getName(), new Object[] {});

            }

            if (!scilabSolver.isJoined()) {
                scilabSolver.join(url);
            }

            if (!scilabSolver.isLoggedIn()) {
                scilabSolver.startLogin();
            }

            while (!scilabSolver.isLoggedIn()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;

        } catch (Throwable e) {
            return getStackTrace(e);

        }
    }
}
