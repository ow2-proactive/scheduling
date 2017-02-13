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
package org.ow2.proactive_grid_cloud_portal.cli;

import java.util.Map;
import java.util.Stack;

import javax.script.ScriptEngine;

import org.codehaus.jackson.map.ObjectMapper;
import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;
import org.ow2.proactive_grid_cloud_portal.cli.json.PluginView;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;


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
