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

import scalabilityTests.framework.listeners.JobResultSchedulerListener;


/**
 * 
 * In this implementation, listeners are not decorated
 * 
 * @author fabratu
 *
 */
@ActiveObject
public class SchedulerUser<V> extends AbstractSchedulerUser<V> {

    public SchedulerUser() {
        super();
    }

    public SchedulerUser(String schedulerURL, Credentials userCreds) {
        super(schedulerURL, userCreds);
    }

    public SchedulerUser(Action<Scheduler, V> defaultAction, String schedulerURL, Credentials userCreds) {
        super(defaultAction, schedulerURL, userCreds);
    }

    @Override
    protected SchedulerEventListener createEventListener(String listenerClazzName)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        logger.trace("Trying to load the listener class " + listenerClazzName);
        Class listenerClazz = Class.forName(listenerClazzName);
        logger.trace("Trying to instantiate a listener of type " + listenerClazzName);
        SchedulerEventListener ret = (SchedulerEventListener) listenerClazz.newInstance();
        // ugly but necessary; getJobResult MUST be called from the SchedulerUser AO !!
        if (ret instanceof JobResultSchedulerListener)
            ((JobResultSchedulerListener) ret).setResultFetcher((SchedulerUser<JobId>) PAActiveObject
                    .getStubOnThis());
        return ret;
    }

}
