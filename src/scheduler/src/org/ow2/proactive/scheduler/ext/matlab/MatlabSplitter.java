/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matlab;

import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import java.io.Serializable;
import java.util.Map;


/**
 * A Scheduler Task, specific to Matlab, which handles splitting of results in matlab workflows
 *
 * @author The ProActive Team
 */
public class MatlabSplitter extends MatlabTask {

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
    protected Serializable executeInternal(TaskResult... results) throws Throwable {

        if (splitterWorker == null) {
            if (debug) {
                System.out.println("[" + host + " MATLAB TASK] Deploying Worker (MatlabSplitter)");
            }
            splitterWorker = (AOMatlabSplitter) deploy(AOMatlabSplitter.class.getName());
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    splitterWorker.terminate();
                }
            }));
        }
        if (debug) {
            System.out.println("[" + host + " MATLAB TASK] Executing (MatlabSplitter)");
        }
        splitterWorker.init(inputScript, scriptLines, debug, numberOfChildren);

        Serializable res = splitterWorker.execute(results);
        res = (Serializable) PAFuture.getFutureValue(res);
        // We don't terminate the worker for subsequent calculations
        //splitterWorker.terminate();

        return res;
    }
}
