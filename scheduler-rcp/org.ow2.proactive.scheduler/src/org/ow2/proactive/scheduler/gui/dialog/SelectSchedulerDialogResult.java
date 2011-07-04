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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.dialog;

import java.io.Serializable;


/**
 * @author The ProActive Team 
 */
@SuppressWarnings("serial")
public class SelectSchedulerDialogResult implements Serializable {
    private final String url;
    private final String login;
    private final String password;
    private final byte[] cred;
    private byte[] sshkey = null;

    /**
     * @param url existing scheduler url
     * @param login login or null if cred is not null
     * @param password password or null
     * @param cred cred file path or null if login is provided
     * @param sshkey ssh private key or null
     */
    public SelectSchedulerDialogResult(String url, String login, String password, byte[] cred, byte[] sshkey) {
        this.url = url;
        this.login = login;
        this.password = password;
        this.cred = cred;
        this.sshkey = sshkey;
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

    public byte[] getCred() {
        return cred;
    }

    public byte[] getSshkey() {
        return sshkey;
    }
}
