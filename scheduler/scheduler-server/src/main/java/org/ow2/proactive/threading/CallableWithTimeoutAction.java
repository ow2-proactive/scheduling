/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.threading;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;


/**
 * Generic CallableWithTimeoutAction interface can be used to execute a task in an {@link ExecutorService} thread pool.
 * This interface provides method to do a particular action when timeout expires during an execution.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public interface CallableWithTimeoutAction<V> extends Callable<V> {

    /**
     * Executed if the timeout for this task expires.
     * <b>Warning</b> : As this method is not called by threads provided by the {@link ExecutorService},
     * this method must be non-blocking.<br>
     * Note that this method should also be called while the {@link CallableWithTimeoutAction#call()} method is being executed.
     */
    void timeoutAction();
}
