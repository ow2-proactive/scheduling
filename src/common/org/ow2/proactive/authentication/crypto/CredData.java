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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.authentication.crypto;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Dummy class used to properly separate login, password and key:
 * will be serialized and encrypted
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
@PublicAPI
public class CredData implements Serializable {
    /**  */
    private static final long serialVersionUID = 31L;
    private String login = null;
    private String pass = null;
    // windows domain name, optionnal
    private String domain = null;
    //Optionnal ssh key
    private byte[] key = null;

    /**
     * Extract the Windows domain name from the full login
     * parseDomain("domain\\user") returns domain
	 * parseDomain("user") returns null
	 * @param fullLogin the login to parse
	 * @return the domain name, null if no domain is specified
	 * @since Scheduling 3.0.1
	 */
    public static final String parseDomain(String fullLogin) {
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
    public static final String parseLogin(String fullLogin) {
        if (fullLogin.contains("\\")) {
            String login = fullLogin.substring(fullLogin.indexOf("\\") + 1, fullLogin.length());
            return login;
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

}
