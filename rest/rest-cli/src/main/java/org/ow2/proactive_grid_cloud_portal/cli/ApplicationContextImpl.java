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

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.ow2.proactive.http.HttpClientBuilder;
import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;
import org.ow2.proactive_grid_cloud_portal.cli.json.PluginView;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;


public class ApplicationContextImpl implements ApplicationContext {

    private static final String DEVICE = "org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl.deviceImpl";

    private static final String SCRIPT_ENGINE = "org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl.scriptEngine";

    private static final String OBJECT_MAPPER = "org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl.objectMapper";

    private static final String INFRASTRUCTURES = "org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl.infrastructures";

    private static final String POLICIES = "org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl.policies";

    private static final String RESULT_STACK = "org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl.resultStack";

    private static ApplicationContextHolder threadLocalContext = new ApplicationContextHolder();

    private String sessionId = "";

    private String restServerUrl;

    private boolean insecureAccess;

    private String resourceType;

    private boolean forced;

    private boolean silent = false;

    private Map<String, Object> properties = new HashMap<>();

    public static ApplicationContext currentContext() {
        return threadLocalContext.get();
    }

    //Only for test purpose
    public static void mockCurrentContext(ApplicationContextHolder mockThreadLocalContext) {
        threadLocalContext = mockThreadLocalContext;
    }

    //Only for test purpose
    public static ApplicationContextHolder newApplicationContextHolder() {
        return new ApplicationContextHolder();
    }

    ApplicationContextImpl() {
    }

    @Override
    public void setDevice(AbstractDevice deviceImpl) {
        setProperty(DEVICE, deviceImpl);
    }

    @Override
    public AbstractDevice getDevice() {
        return getProperty(DEVICE, AbstractDevice.class);
    }

    @Override
    public void setRestServerUrl(String restServerUrl) {
        this.restServerUrl = restServerUrl;
    }

    @Override
    public SchedulerRestClient getRestClient() {
        HttpClientBuilder httpClientBuilder = new HttpClientBuilder().useSystemProperties();
        if (canInsecureAccess()) {
            httpClientBuilder.insecure(true);
        }
        return new SchedulerRestClient(restServerUrl, new ApacheHttpClient4Engine(httpClientBuilder.build()));
    }

    @Override
    public String getResourceUrl(String resource) {
        return (new StringBuilder()).append(getRestServerUrl())
                                    .append('/')
                                    .append(getResourceType())
                                    .append('/')
                                    .append(resource)
                                    .toString();
    }

    @Override
    public String getRestServerUrl() {
        return restServerUrl;
    }

    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        setProperty(OBJECT_MAPPER, objectMapper);
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return getProperty(OBJECT_MAPPER, ObjectMapper.class);
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean canInsecureAccess() {
        return insecureAccess;
    }

    @Override
    public void setInsecureAccess(boolean insecureAccess) {
        this.insecureAccess = insecureAccess;
    }

    @Override
    public ScriptEngine getEngine() {
        ScriptEngine engine = getProperty(SCRIPT_ENGINE, ScriptEngine.class);
        if (engine == null) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            engine = mgr.getEngineByExtension("js");
            if (engine == null) {
                throw new CLIException(REASON_OTHER, "Cannot obtain JavaScript engine instance.");
            }
            engine.getContext().setWriter(getDevice().getWriter());
            setProperty(SCRIPT_ENGINE, engine);
        }
        return engine;
    }

    @Override
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public String getResourceType() {
        return resourceType;
    }

    @Override
    public Map<String, PluginView> getInfrastructures() {
        return getProperty(INFRASTRUCTURES, Map.class);
    }

    @Override
    public void setInfrastructures(Map<String, PluginView> infrastructures) {
        setProperty(INFRASTRUCTURES, infrastructures);
    }

    @Override
    public Map<String, PluginView> getPolicies() {
        return getProperty(POLICIES, Map.class);
    }

    @Override
    public void setPolicies(Map<String, PluginView> policies) {
        setProperty(POLICIES, policies);
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key, Class<T> type) {
        Object object = properties.get(key);
        return (T) object;
    }

    @Override
    public <T> T getProperty(String key, Class<T> type, T dflt) {
        T property = getProperty(key, type);
        return (property == null) ? dflt : property;
    }

    @Override
    public boolean isForced() {
        return forced;
    }

    @Override
    public void setForced(boolean forced) {
        this.forced = forced;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    @Override
    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Stack resultStack() {
        Stack resultStack = getProperty(RESULT_STACK, Stack.class);
        if (resultStack == null) {
            resultStack = new Stack();
            setProperty(RESULT_STACK, resultStack);
        }
        return resultStack;
    }

    private static class ApplicationContextHolder extends ThreadLocal<ApplicationContextImpl> {
        @Override
        protected ApplicationContextImpl initialValue() {
            return new ApplicationContextImpl();
        }
    }
}
