/*
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
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest;

import java.util.List;

import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;


public abstract class ClientBase implements ISchedulerClient {
    private static final int calling_method_stack_index = 3;

    @Override
    public SchedulerState addEventListener(SchedulerEventListener arg0, boolean arg1, boolean arg2,
            SchedulerEvent... arg3) throws NotConnectedException, PermissionException {
        throw newUnsupportedOperationException();

    }

    @Override
    public boolean changePolicy(String arg0) throws NotConnectedException, PermissionException {
        throw newUnsupportedOperationException();
    }

    @Override
    public List<String> getGlobalSpaceURIs() throws NotConnectedException, PermissionException {
        throw newUnsupportedOperationException();
    }

    @Override
    public SchedulerState getState() throws NotConnectedException, PermissionException {
        throw newUnsupportedOperationException();
    }

    @Override
    public SchedulerState getState(boolean arg0) throws NotConnectedException, PermissionException {
        throw newUnsupportedOperationException();
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(JobId arg0, String arg1, int arg2)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        throw newUnsupportedOperationException();
    }

    @Override
    public TaskResult getTaskResultFromIncarnation(String arg0, String arg1, int arg2)
            throws NotConnectedException, UnknownJobException, UnknownTaskException, PermissionException {
        throw newUnsupportedOperationException();
    }

    @Override
    public boolean killTask(JobId arg0, String arg1) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean killTask(String arg0, String arg1) throws NotConnectedException, UnknownJobException,
            UnknownTaskException, PermissionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void listenJobLogs(JobId arg0, AppenderProvider arg1) throws NotConnectedException,
            UnknownJobException, PermissionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void listenJobLogs(String arg0, AppenderProvider arg1) throws NotConnectedException,
            UnknownJobException, PermissionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean reloadPolicyConfiguration() throws NotConnectedException, PermissionException {
        throw new UnsupportedOperationException();
    }

    private UnsupportedOperationException newUnsupportedOperationException() {
        return new UnsupportedOperationException(String.format("%s does not implements %s(...).",
                className(), methodName()));
    }

    private String className() {
        return this.getClass().getSimpleName();
    }

    private String methodName() {
        return Thread.currentThread().getStackTrace()[calling_method_stack_index].getMethodName();
    }

}
