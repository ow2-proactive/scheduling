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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
