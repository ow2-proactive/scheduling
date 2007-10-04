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
package org.objectweb.proactive.extra.scheduler.task;

import org.objectweb.proactive.extra.scheduler.job.TaskDescriptor;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalTask;


/**
 * This class represents an elligible task for the policy.
 * It is a sort of tag class that will avoid user from giving non-eligible task to the scheduler.
 * In fact policy will handle LightTask and EligibleLightTask but
 * will only be allowed to send EligibleLightTask to the scheduler
 * @see org.objectweb.proactive.extra.scheduler.job.TaskDescriptor
 *
 * @author ProActive Team
 * @version 1.0, Jul 9, 2007
 * @since ProActive 3.2
 */
public class EligibleTaskDescriptor extends TaskDescriptor {

    /** Serial version UID */
    private static final long serialVersionUID = 8461969956605719440L;

    /**
     * Get a new eligible light task using a taskDescriptor.
     * Same constructor as LightTask
     *
     * @param td the taskDescriptor to shrink.
     */
    public EligibleTaskDescriptor(InternalTask td) {
        super(td);
    }
}
