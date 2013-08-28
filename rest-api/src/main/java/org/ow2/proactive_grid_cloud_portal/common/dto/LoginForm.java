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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.common.dto;

import java.io.InputStream;

import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.multipart.PartType;


/**
 * a class that represent a mean to submit credential through the rest api.
 * A credential can be either 
 *   - a username/password couple
 *   - a file submitted through an HTTP form and whose name is 'credential'
 */
@XmlRootElement
public class LoginForm {
    /**
     * a representation of a serialized Credentials class
     */
    @FormParam("credential")
    @PartType("application/octet-stream")
    private InputStream credential;

    /**
     * username
     */
    @FormParam("username")
    @PartType("text/plain")
    private String username;

    /**
     * password
     */
    @FormParam("password")
    @PartType("text/plain")
    private String password;

    /**
     * ssh key for runAsMe
     */
    @FormParam("sshKey")
    @PartType("application/octet-stream")
    private byte[] sshKey;

    public LoginForm() {
    }

    public InputStream getCredential() {
        return credential;
    }

    public void setCredential(final InputStream filedata) {
        this.credential = filedata;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String login) {
        this.username = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKeyStream) {
        this.sshKey = sshKeyStream.getBytes();
    }

}
