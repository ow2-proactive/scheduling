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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common.backwardcompatibility;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.policy.Policy;


/**
 * SchedulerAdminAdapter is temporarily used to force compatibility with previous version !
 * This class reproduce the previous User interface behavior !
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class SchedulerAdminAdapter extends SchedulerUserAdapter implements AdminSchedulerInterface {

    public SchedulerAdminAdapter(Scheduler frontend) {
        super(frontend);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper changePolicy(Class<? extends Policy> newPolicyFile) throws SchedulerException {
        return new BooleanWrapper(frontend.changePolicy(newPolicyFile));
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper freeze() throws SchedulerException {
        return new BooleanWrapper(frontend.freeze());
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper kill() throws SchedulerException {
        return new BooleanWrapper(frontend.kill());
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException {
        return new BooleanWrapper(frontend.linkResourceManager(rmURL));
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper pause() throws SchedulerException {
        return new BooleanWrapper(frontend.pause());
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper resume() throws SchedulerException {
        return new BooleanWrapper(frontend.resume());
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper shutdown() throws SchedulerException {
        return new BooleanWrapper(frontend.shutdown());
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper start() throws SchedulerException {
        return new BooleanWrapper(frontend.start());
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper stop() throws SchedulerException {
        return new BooleanWrapper(frontend.stop());
    }

}
