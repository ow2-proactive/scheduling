/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli;

import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;
import org.ow2.proactive_grid_cloud_portal.cli.json.PluginView;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;

import java.util.Map;
import java.util.Stack;

import javax.script.ScriptEngine;

import org.codehaus.jackson.map.ObjectMapper;

public interface ApplicationContext {

    public void setDevice(AbstractDevice device);
    
    public AbstractDevice getDevice();

    public void setRestServerUrl(String restServerUrl);

    public String getRestServerUrl();

    public String getResourceUrl(String resource);

    public void setObjectMapper(ObjectMapper mapper);

    public ObjectMapper getObjectMapper();

    public String getSessionId();

    public void setSessionId(String sessionId);

    public boolean canInsecureAccess();

    public void setInsecureAccess(boolean allow);

    public ScriptEngine getEngine();

    public void setResourceType(String resourceType);
    
    public String getResourceType();

    public Map<String, PluginView> getInfrastructures();

    public void setInfrastructures(Map<String, PluginView> infrastructures);

    public Map<String, PluginView> getPolicies();

    public void setPolicies(Map<String, PluginView> policies);

    public void setProperty(String key, Object value);

    public <T> T getProperty(String key, Class<T> type);

    public <T> T getProperty(String key, Class<T> type, T dflt);

    public boolean isForced();

    public void setForced(boolean forced);

    public boolean isSilent();

    public void setSilent(boolean silent);

    @SuppressWarnings("rawtypes")
    public Stack resultStack();

    SchedulerRestClient getRestClient();
}
