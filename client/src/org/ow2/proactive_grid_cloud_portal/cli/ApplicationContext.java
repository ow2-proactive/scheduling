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
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli;

import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_DIR;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_FILE_EXT;

import java.io.File;

import javax.script.ScriptEngine;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpClientUtil;

public class ApplicationContext {

    private static ApplicationContext instance;

    private String user;
    private String password;
    private String alias;
    private String credFilePathname;
    private String sessionId;
    private AbstractDevice device;
    private boolean termiated = false;
    private String schedulerUrl;
    private ObjectMapper objectMapper;
    private boolean insecureAccess = false;
    private boolean newSession = false;
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

    public void setSchedulerUrl(String schedulerUrl) {
        this.schedulerUrl = schedulerUrl;
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
        this.schedulerUrl = schedulerUrl;
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

    public HttpResponse executeClient(HttpUriRequest request) throws Exception {
        if (sessionId != null) {
            request.setHeader("sessionid", sessionId);
        }
        DefaultHttpClient client = new DefaultHttpClient();
        if ("https".equals(request.getURI().getScheme())
                && allowInsecureAccess()) {
            HttpClientUtil.setInsecureAccess(client);
        }
        return client.execute(request);
    }

    public String getSchedulerUrl() {
        return schedulerUrl;
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
}
