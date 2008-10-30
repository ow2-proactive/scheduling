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
package org.ow2.proactive.scheduler.common.job;

import org.ow2.proactive.scheduler.common.exception.JobCreationException;


/**
 * JobFactory is used to parse XML Job descriptor.
 * It can use different implementation of parsing.
 *
 * @author The ProActive Team
 * @date 2 July 07
 * @since ProActive Scheduling 0.9
 *
 */
public abstract class JobFactory {

    private static final String CURRENT_IMPL = "org.ow2.proactive.scheduler.common.job.JobFactory_stax";

    /**
     * Singleton Pattern
     */
    private static JobFactory factory = null;

    /**
     * Return the instance of the jobFactory.
     *
     * @return the instance of the jobFactory.
     */
    public static JobFactory getFactory() {
        if (factory == null) {
            try {
                factory = (JobFactory) Class.forName(CURRENT_IMPL).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instanciate this factory : " + CURRENT_IMPL, e);
            }
        }
        return factory;
    }

    /**
     * Creates a job using the given job descriptor.
     * 
     * @param filePath the path to an XML job descriptor.
     * @return a Job instance created with the given XML file.
     * @throws JobCreationException if an exception occurred during job creation.
     */
    public abstract Job createJob(String filePath) throws JobCreationException;

}
