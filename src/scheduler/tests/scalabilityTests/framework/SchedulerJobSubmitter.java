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

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobId;

import scalabilityTests.framework.listeners.JobResultDecorator;


/**
 * A {@link AbstractSchedulerUser} who is interested in 
 *   the results of the jobs she submits
 * 
 * @author fabratu
 *
 */
@ActiveObject
public class SchedulerJobSubmitter extends AbstractSchedulerUser<JobId> {

    private JobResultDecorator jrListener = null;

    // job id is in this.result
    public SchedulerJobSubmitter() {
        super();
    }

    public SchedulerJobSubmitter(String schedulerURL, Credentials userCreds) {
        super(schedulerURL, userCreds);
    }

    @Override
    public void doAction(Action<Scheduler, JobId> action) {
        // listener is needed in this case
        if (this.slExposer == null)
            throw new IllegalStateException(
                "No listeners were registered to the Scheduler. A listener is needed to know when the job finishes. "
                    + "Consider calling the registerListeners() method first");
        super.doAction(action);
        logger.trace("Submitted job with ID " + this.result);
        // register job ID
        this.jrListener.startMonitoring(this.result);
    }

    @Override
    protected SchedulerEventListener createEventListener(String listenerClazzName)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        logger.trace("Trying to load the listener class " + listenerClazzName);
        Class listenerClazz = Class.forName(listenerClazzName);
        logger.trace("Trying to instantiate a listener of type " + listenerClazzName);
        SchedulerEventListener schedulerListener = (SchedulerEventListener) listenerClazz.newInstance();
        logger.trace("Decorating the event listener with " + JobResultDecorator.class.getName());
        this.jrListener = new JobResultDecorator(schedulerListener,
            (AbstractSchedulerUser<JobId>) PAActiveObject.getStubOnThis());
        return this.jrListener;
    }

}
