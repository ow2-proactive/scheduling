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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core.rmproxies;

import java.util.Set;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.RMState;


/**
 * SchedulerRMProxy is the ResourceManager proxy for Scheduler core.
 * It is currently implemented has an active Object that forwards call to Resource Manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class SchedulerRMProxy extends UserRMProxy {

    /**
     * ProActive constructor, DO NOT USE
     */
    public SchedulerRMProxy() {
    }

    public SchedulerRMProxy(Set<String> filters) {
        super(filters);
    }

    /************************ INHERITED FROM RESOURCE-MANAGER **********************/

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public RMState getState() {
        return rm.getState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ImmediateService
    public BooleanWrapper isActive() {
        return rm.isActive();
    }
}
