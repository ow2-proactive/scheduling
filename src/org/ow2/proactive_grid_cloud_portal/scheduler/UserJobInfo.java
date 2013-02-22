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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.ow2.proactive.scheduler.common.job.JobInfo;


/**
 * A class that contains a subset of the information available
 * in a scheduler state.
 * It is mostly used to provide a fast access to meaningful
 * data within the scheduler state without having to manage the 
 * complete state
 *
 */
@XmlRootElement
public class UserJobInfo implements Serializable {
    
    private JobInfo jobInfo;

    public UserJobInfo() {
    }

    public UserJobInfo(JobInfo jobinfo) {
        this.jobInfo = jobinfo;
    }

    public String getJobid() {
        return jobInfo.getJobId().value();
    }

    public String getJobOwner() {
        return jobInfo.getJobOwner();
    }

    public JobInfo getJobinfo() {
        return jobInfo;
    }

}
