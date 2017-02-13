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
package org.ow2.proactive.authentication.crypto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Used to properly separate login, password and key:
 * will be serialized and encrypted
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
@PublicAPI
public class CredData implements Serializable {

    // fix for #2456 : Credential Data and TaskLogs contain serialVersionUID based on scheduler server version
    private static final long serialVersionUID = 1L;

    /**
     * thirdPartyCredentials can contain the SSH key used for runAsMe tasks under this specific key
     */
    public static final String SSH_PRIVATE_KEY = "SSH_PRIVATE_KEY";

    private String login = null;

    private String pass = null;

    // Windows domain name, optional
    private String domain = null;

    // Optional ssh key
    private byte[] key = null;

    private Map<String, String> thirdPartyCredentials = new HashMap<>();

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
            if ("".equals(domain.trim())) {
                return null;
            }
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

    public CredData() {
    }

    public CredData(String login, String pass) {
        this.login = login;
        this.pass = pass;
    }

    public CredData(String login, String password, Map<String, String> thirdPartyCredentials) {
        this(login, password);
        this.thirdPartyCredentials = thirdPartyCredentials;
    }

    /**
     * @since Scheduling 3.0.1
     */
    public CredData(String login, String domain, String pass) {
        this.login = login;
        this.pass = pass;
        this.domain = domain;
    }

    public CredData(String login, String pass, byte[] key) {
        this.login = login;
        this.pass = pass;
        this.key = key;
    }

    /**
     * @since Scheduling 3.0.1
     */
    public CredData(String login, String domain, String pass, byte[] key) {
        this.login = login;
        this.pass = pass;
        this.key = key;
        this.domain = domain;
    }

    /**
     * Get the login
     *
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Set the login value to the given login value
     *
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Get the password
     *
     * @return the password
     */
    public String getPassword() {
        return pass;
    }

    /**
     * Set the password value to the given pass value
     *
     * @param pass the password to set
     */
    public void setPassword(String pass) {
        this.pass = pass;
    }

    /**
     * Get the key
     *
     * @return the key
     */
    public byte[] getKey() {
        if (key == null && thirdPartyCredentials != null && thirdPartyCredentials.containsKey(SSH_PRIVATE_KEY)) {
            return thirdPartyCredentials.get(SSH_PRIVATE_KEY).getBytes();
        }
        return key;
    }

    /**
     * Set the key value to the given key value
     *
     * @param key the key to set
     */
    public void setKey(byte[] key) {
        this.key = key;
    }

    /**
     * Return the domain of this user or null if no domain has been specified.
     * @return the domain of this user or null if no domain has been specified.
     * @since Scheduling 3.0.1
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set a domain for this user. Domain is optionnal.
     * @param domain the domain to set
     * @since Scheduling 3.0.1
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Return the login and password as a string array.
     * where element at index 0 is login and element at index 1 is password.
     *
     * @return the login and password as a string array.
     */
    public String[] getLoginPassword() {
        return new String[] { login, pass };
    }

    public Map<String, String> getThirdPartyCredentials() {
        return thirdPartyCredentials;
    }

    public void addThirdPartyCredential(String key, String decryptedValue) {
        thirdPartyCredentials.put(key, decryptedValue);
    }
}
