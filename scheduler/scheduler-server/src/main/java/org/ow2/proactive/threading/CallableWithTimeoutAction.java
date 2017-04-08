/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
