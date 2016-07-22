/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.authentication;

import org.objectweb.proactive.annotation.PublicAPI;

import java.io.File;

/**
 * Composite object storing information to connect to the scheduler/rm
 *
 * @author The ProActive Team
 */
@PublicAPI
public class ConnectionInfo {
    private String url;
    private String login;
    private String password;
    private File credentialFile;
    private boolean insecure;

    /**
     * @param url            the REST server URL
     * @param login          the login
     * @param password       the password
     * @param credentialFile path to a file containing encrypted credentials
     * @param insecure       if true the server certificate will not be verified
     */
    public ConnectionInfo(String url, String login, String password, File credentialFile, boolean insecure) {
        this.url = url;
        this.login = login;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCredentialFile(File credentialFile) {
        this.credentialFile = credentialFile;
    }

    public void setInsecure(boolean insecure) {
        this.insecure = insecure;
    }
}
