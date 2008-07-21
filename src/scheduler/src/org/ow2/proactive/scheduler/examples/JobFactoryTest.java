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
package org.ow2.proactive.scheduler.examples;

import org.ow2.proactive.scheduler.common.job.JobFactory;


/**
 * JobFactoryTest is a class that can be used to test the job factory without starting a scheduler or a whole job process.
 *
 * @author The ProActive Team
 * @date 2 juil. 08
 * @since ProActive 4.0
 *
 */
public class JobFactoryTest {
    /**
     * Start the test with the job factory
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String path = "jobs_descriptors/Job_2_tasks.xml";
        if (args.length > 0) {
            path = args[0];
        }
        JobFactory.getFactory().createJob(path);
    }
}
