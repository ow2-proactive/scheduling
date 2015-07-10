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

    void setDevice(AbstractDevice device);

    AbstractDevice getDevice();

    void setRestServerUrl(String restServerUrl);

    String getRestServerUrl();

    String getResourceUrl(String resource);

    void setObjectMapper(ObjectMapper mapper);

    ObjectMapper getObjectMapper();

    String getSessionId();

    void setSessionId(String sessionId);

    boolean canInsecureAccess();

    void setInsecureAccess(boolean allow);

    ScriptEngine getEngine();

    void setResourceType(String resourceType);

    String getResourceType();

    Map<String, PluginView> getInfrastructures();

    void setInfrastructures(Map<String, PluginView> infrastructures);

    Map<String, PluginView> getPolicies();

    void setPolicies(Map<String, PluginView> policies);

    void setProperty(String key, Object value);

    <T> T getProperty(String key, Class<T> type);

    <T> T getProperty(String key, Class<T> type, T dflt);

    boolean isForced();

    void setForced(boolean forced);

    boolean isSilent();

    void setSilent(boolean silent);

    @SuppressWarnings("rawtypes")
    Stack resultStack();

    SchedulerRestClient getRestClient();
}
