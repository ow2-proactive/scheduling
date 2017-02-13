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
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Definition of a job identification, this will be used during scheduling to identify your job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface JobId extends Comparable<JobId>, Serializable {

    /**
     * Return the human readable name associated to this id.
     *
     * @return the human readable name associated to this id.
     */
    String getReadableName();

    /**
     * Get the value of the JobId.
     * <p>
     * As the internal implementation of this class can change, It is strongly recommended to use this method
     * to get a literal value of the ID. Use this value if you lost the jobId Object returned by the scheduler.
     * 
     * @return the textual representation of this jobId
     */
    String value();

    /**
     * Returns the current value of the JobId.
     *
     * @return the current value of the JobId as a long.
     */
    long longValue();

}
