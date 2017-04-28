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
package scalabilityTests.framework.listeners;

import java.net.URI;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;


/**
 * This class is responsible for exposing 
 * {@link SchedulerEventListener}s as Remote Objects
 * 
 * @author fabratu
 *
 */
public class SchedulerListenerExposer {

    private static final Logger logger = Logger.getLogger(SchedulerListenerExposer.class);

    private final SchedulerEventListener schedulerListener;

    private RemoteObjectExposer<SchedulerEventListener> roe = null;

    private URI uri = null;

    public SchedulerListenerExposer(SchedulerEventListener listener) {
        this.schedulerListener = listener;
    }

    public SchedulerEventListener createRemoteReference() throws ProActiveException {
        roe = PARemoteObject.newRemoteObject(schedulerListener.getClass().getName(), schedulerListener);
        uri = RemoteObjectHelper.generateUrl(SimpleSchedulerListener.class.getSimpleName());
        return PARemoteObject.bind(roe, uri);
    }

    public void destroyRemoteReference() throws ProActiveException {
        roe.unexport(uri);
    }

}
