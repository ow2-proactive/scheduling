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
package scalabilityTests.framework;

import java.io.File;
import java.io.Serializable;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;


/**
 *
 * The Action of submitting a job to the Scheduler
 * 
 * @author fabratu
 *
 */
public class JobSubmissionAction implements Action<Scheduler, JobId>, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;
	private final Job job;

    public JobSubmissionAction(String jobDescriptorPath) {
        File jobDescriptor = new File(jobDescriptorPath);

        // routine checks of the path
        if (!jobDescriptor.exists())
            throw new IllegalArgumentException("File " + jobDescriptorPath + " does not exist");
        if (!jobDescriptor.isFile())
            throw new IllegalArgumentException("The path " + jobDescriptorPath + " does not point to a file");
        if (!jobDescriptor.canRead())
            throw new IllegalArgumentException("The file " + jobDescriptorPath +
                " cannot be read - maybe check your permissions?");

        // ok, it is valid. Try to create a Job from the xml descriptor
        try {
            this.job = JobFactory.getFactory().createJob(jobDescriptorPath);
        } catch (JobCreationException e) {
            throw new IllegalArgumentException("Cannot create a Scheduler Job from the xml descriptor " +
                jobDescriptorPath + " because " + e.getMessage());
        }
    }

    public JobId execute(Scheduler usi) throws Exception {
        // simple submit, don't care for the result yet
        return usi.submit(this.job);
    }

    @Override
    public String toString() {
        return "Scheduler job submission action";
    }

}
