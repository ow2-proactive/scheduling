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
package org.ow2.proactive.authentication;

import java.io.File;
import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Composite object storing information to connect to the scheduler/rm
 *
 * @author The ProActive Team
 */
@PublicAPI
public class ConnectionInfo implements Serializable {
    private String url;

    private String login;

    private String domain;

    private String password;

    private File credentialFile;

    private boolean insecure;

    /**
     * @param url            the REST server URL
     * @param login          the login
     * @param domain         the user domain
     * @param password       the password
     * @param credentialFile path to a file containing encrypted credentials
     * @param insecure       if true the server certificate will not be verified
     */
    public ConnectionInfo(String url, String login, String domain, String password, File credentialFile,
            boolean insecure) {
        this.url = url;
        this.login = login;
        this.domain = domain;
        this.password = password;
        this.credentialFile = credentialFile;
        this.insecure = insecure;
    }

    /**
     * @param url            the REST server URL
     * @param login          the login, can be in the form domain_name\\user_name
     * @param password       the password
     * @param credentialFile path to a file containing encrypted credentials
     * @param insecure       if true the server certificate will not be verified
     */
    public ConnectionInfo(String url, String login, String password, File credentialFile, boolean insecure) {
        this.url = url;
        this.login = parseLogin(login);
        this.domain = parseDomain(domain);
        this.password = password;
        this.credentialFile = credentialFile;
        this.insecure = insecure;
    }

    public String getUrl() {
        return url;
    }

    public String getLogin() {
        return login;
    }

    public String getDomain() {
        return domain;
    }

    public String getPassword() {
        return password;
    }

    public File getCredentialFile() {
        return credentialFile;
    }

    public boolean isInsecure() {
        return insecure;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCredentialFile(File credentialFile) {
        this.credentialFile = credentialFile;
    }

    public void setInsecure(boolean insecure) {
        this.insecure = insecure;
    }

    /**
     * Extract the Windows domain name from the full login
     * parseDomain("domain\\user") returns domain
     * parseDomain("user") returns null
     * @param fullLogin the login to parse
     * @return the domain name, null if no domain is specified
     * @since Scheduling 3.0.1
     */
    public static String parseDomain(String fullLogin) {
        if (fullLogin.contains("\\")) {
            String domain = fullLogin.substring(0, fullLogin.indexOf("\\"));
            return "".equals(domain.trim()) ? null : domain;
        } else {
            return null;
        }
    }

    /**
     * Extract the user name from the full login
     * parseDomain("domain\\user") returns user
     * parseDomain("user") returns user
     * @param fullLogin the login to parse
     * @return the user name
     * @since Scheduling 3.0.1
     */
    public static String parseLogin(String fullLogin) {
        if (fullLogin.contains("\\")) {
            return fullLogin.substring(fullLogin.indexOf("\\") + 1, fullLogin.length());
        } else {
            return fullLogin;
        }
    }
}
