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

import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_DIR;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_FILE_EXT;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;
import org.ow2.proactive_grid_cloud_portal.cli.json.PluginView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpUtility;

public class ApplicationContext {

    private static ApplicationContext instance;

    private String user;
    private String password;
    private String alias;
    private String credFilePathname;
    private String sessionId;
    private AbstractDevice device;
    private boolean termiated = false;
    private String restServerUrl;
    private String resourceType;
    private ObjectMapper objectMapper;
    private boolean insecureAccess = false;
    private boolean newSession = false;
    private Map<String, PluginView> infrastructures;
    private Map<String, PluginView> policies;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private boolean forced;
    private ScriptEngine engine;

    private ApplicationContext() {
    }

    public static synchronized ApplicationContext instance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public static void deleteSession(String user) {
        File sessionFile = new File(DFLT_SESSION_DIR, user
                + DFLT_SESSION_FILE_EXT);
        if (sessionFile.exists()) {
            sessionFile.delete();
        }
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setDevice(AbstractDevice device) {
        this.device = device;
    }

    public void setRestServerUrl(String restServerUrl) {
        this.restServerUrl = restServerUrl;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean isNewSession() {
        return newSession;
    }

    public void setNewSession(boolean newSession) {
        this.newSession = newSession;
    }

    public synchronized void init(String schedulerUrl, AbstractDevice console) {
        this.restServerUrl = schedulerUrl;
    }

    public AbstractDevice getDevice() {
        return device;
    }

    public boolean isTermiated() {
        return termiated;
    }

    public void setTerminated(boolean termiated) {
        this.termiated = termiated;
    }

    public HttpResponse executeClient(HttpUriRequest request) {
        if (sessionId != null) {
            request.setHeader("sessionid", sessionId);
        }
        DefaultHttpClient client = new DefaultHttpClient();
        try {
            if ("https".equals(request.getURI().getScheme())
                    && allowInsecureAccess()) {
                HttpUtility.setInsecureAccess(client);
            }
            return client.execute(request);
        } catch (Exception e) {
            throw new CLIException(CLIException.REASON_OTHER, e);
        }
    }

    public String getRestServerUrl() {
        return restServerUrl;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean logged() {
        return sessionId != null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean allowInsecureAccess() {
        return insecureAccess;
    }

    public void allowInsecureAccess(boolean insecure) {
        this.insecureAccess = insecure;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCredFilePathname() {
        return credFilePathname;
    }

    public void setCredFilePathname(String credFilePathname) {
        this.credFilePathname = credFilePathname;
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public void setEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceType() {
        return resourceType;
    }
    
    public Map<String, PluginView> getInfrastructures() {
        return infrastructures;
    }

    public void setInfrastructures(Map<String, PluginView> infrastructures) {
        this.infrastructures = infrastructures;
    }

    public Map<String, PluginView> getPolicies() {
        return policies;
    }

    public void setPolicies(Map<String, PluginView> policies) {
        this.policies = policies;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public <T> T getProperty(String key, Class<T> type) {
        return type.cast(properties.get(key));
    }
    
    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public void clearSession() {
        String sessionIdentifier = (user != null) ? user : alias;
        if (sessionIdentifier != null) {
            File sessionFile = new File(DFLT_SESSION_DIR, sessionIdentifier
                    + DFLT_SESSION_FILE_EXT);
            if (sessionFile.exists()) {
                sessionFile.delete();
            }
        }
    }

}
