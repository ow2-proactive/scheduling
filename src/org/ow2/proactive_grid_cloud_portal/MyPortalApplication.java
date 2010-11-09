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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.ow2.proactive_grid_cloud_portal.exceptions.IOExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.JobAlreadyFinishedExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.NotConnectedExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.PermissionExceptionExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.SchedulerExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.SubmissionClosedExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.UnknownJobExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.UnknownTaskExceptionMapper;


public class MyPortalApplication extends Application {
    HashSet<Object> singletons = new HashSet<Object>();

    public MyPortalApplication() {
        //    singletons.add(new NotConnectedExceptionMapper()); 
        //    singletons.add(new PermissionExceptionExceptionMapper());
        //      singletons.add(new LoggingExecutionInterceptor());
    }

    @Override
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> set = new HashSet<Class<?>>();
        set.add(NotConnectedExceptionMapper.class);
        set.add(PermissionExceptionExceptionMapper.class);
        set.add(UnknownTaskExceptionMapper.class);
        set.add(IOExceptionMapper.class);
        set.add(JobAlreadyFinishedExceptionMapper.class);
        set.add(SubmissionClosedExceptionMapper.class);
        set.add(UnknownJobExceptionMapper.class);
        set.add(UpdatablePropertiesConverter.class);
        set.add(SchedulerExceptionMapper.class);
        set.add(LoggingExecutionInterceptor.class);
        return set;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
