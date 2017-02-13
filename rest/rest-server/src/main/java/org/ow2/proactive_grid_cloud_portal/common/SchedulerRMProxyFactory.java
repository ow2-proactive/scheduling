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
package org.ow2.proactive_grid_cloud_portal.common;

import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;


public class SchedulerRMProxyFactory {

    public RMProxyUserInterface connectToRM(CredData credData)
            throws ActiveObjectCreationException, NodeException, RMException, KeyException, LoginException {
        RMProxyUserInterface rm = PAActiveObject.newActive(RMProxyUserInterface.class, new Object[] {});
        rm.init(getUrl(PortalConfiguration.rm_url), credData);
        return rm;
    }

    public RMProxyUserInterface connectToRM(Credentials credentials)
            throws ActiveObjectCreationException, NodeException, RMException, KeyException, LoginException {
        RMProxyUserInterface rm = PAActiveObject.newActive(RMProxyUserInterface.class, new Object[] {});
        rm.init(getUrl(PortalConfiguration.rm_url), credentials);
        return rm;
    }

    public SchedulerProxyUserInterface connectToScheduler(Credentials credentials)
            throws LoginException, SchedulerException, ActiveObjectCreationException, NodeException {
        SchedulerProxyUserInterface scheduler = PAActiveObject.newActive(SchedulerProxyUserInterface.class,
                                                                         new Object[] {});
        scheduler.init(getUrl(PortalConfiguration.scheduler_url), credentials);
        return scheduler;
    }

    public SchedulerProxyUserInterface connectToScheduler(CredData credData)
            throws ActiveObjectCreationException, NodeException, LoginException, SchedulerException {
        SchedulerProxyUserInterface scheduler = PAActiveObject.newActive(SchedulerProxyUserInterface.class,
                                                                         new Object[] {});
        scheduler.init(getUrl(PortalConfiguration.scheduler_url), credData);
        return scheduler;
    }

    private String getUrl(String urlKey) {
        return PortalConfiguration.getProperties().getProperty(urlKey);
    }
}
