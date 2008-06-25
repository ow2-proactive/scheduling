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
package org.ow2.proactive.scheduler.ext.matlab;

import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import java.util.Map;


public class MatlabSplitter extends SimpleMatlab {

    private static AOMatlabSplitter splitterWorker = null;

    private int numberOfChildren;

    public MatlabSplitter() {
    }

    @Override
    public void init(Map<String, String> args) throws Exception {
        super.init(args);

        Object nb = args.get("number_of_children");

        if (nb == null) {
            throw new IllegalArgumentException("\"number_of_children\" must be specified.");
        }

        numberOfChildren = Integer.parseInt((String) nb);
    }

    @Override
    protected Object executeInternal(String uri, TaskResult... results) throws Throwable {

        if (splitterWorker == null) {
            if (logger.isDebugEnabled()) {
                System.out.println("[" + host + " MATLAB TASK] Deploying Worker (MatlabSplitter)");
            }
            splitterWorker = (AOMatlabSplitter) deploy(uri, AOMatlabSplitter.class.getName(), matlabConfig
                    .getMatlabCommandName());
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    splitterWorker.terminate();
                }
            }));
        }
        if (logger.isDebugEnabled()) {
            System.out.println("[" + host + " MATLAB TASK] Executing (MatlabSplitter)");
        }
        splitterWorker.init(inputScript, scriptLines, numberOfChildren);

        Object res = splitterWorker.execute(results);
        res = PAFuture.getFutureValue(res);
        // We don't terminate the worker for subsequent calculations
        //splitterWorker.terminate();

        return res;
    }
}
