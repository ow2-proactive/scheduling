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
package org.ow2.proactive.resourcemanager.authentication;

import java.io.Serializable;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.resourcemanager.core.history.UserHistory;


/**
 * This class represents a client of the resource manager(RM).
 * It could be an internal service or connected remote user.
 *
 * The class is used to track and associate all activities inside the RM to particular user.
 *
 * It also provides capabilities to detect if the client is still alive.
 *
 * NOTE: The pinger functionality has some drawbacks and limitations. For instance it cannot be used
 * after serialization/deserialization of the class. It relies on ProActive internals and
 * probably will be replaced in the future.
 *
 */
public class Client implements Serializable {

    // This serial version uid is meant to prevent issues when restoring Resource Manager database from a previous version.
    // any addition to this class (new method, field, etc) should imply to change this uid.
    private static final long serialVersionUID = 1L;

    /** The security entity that represents this client */
    private final Subject subject;

    /** Defines if this client has to be pinged */
    private final boolean pingable;

    /** Client's name */
    private String name;

    /** Unique id of the client */
    private UniqueID id;

    /**
     * URL of the client
     */
    private String url;

    /** Body of the sender of request */
    private transient UniversalBody body;

    /** User connection history stored in the data base*/
    private transient UserHistory history;

    private Credentials credentials;

    public Client() {
        this.subject = null;
        this.pingable = false;
    }

    /**
     * Constructs the client object from given client subject.
     * @param subject with the name of the client authenticated in the resource manager (can be null)
     * @param pingable defines if client has to be pinged
     */
    public Client(Subject subject, boolean pingable) {
        this.subject = subject;
        this.pingable = pingable;

        if (subject != null) {
            UserNamePrincipal unPrincipal = subject.getPrincipals(UserNamePrincipal.class).iterator().next();
            this.name = unPrincipal.getName();
        }
        if (pingable) {
            Request r = PAActiveObject.getContext().getCurrentRequest();
            this.id = r.getSourceBodyID();
            this.url = r.getSender().getNodeURL() + "/" + this.id.shortString();
            this.body = r.getSender();
        }
    }

    /**
     * Gets the name of the client
     * @return the name of the client
     */
    public String getName() {
        return name;
    }

    public Set<String> getGroups() {
        Set<String> answer = new HashSet<>();
        Set<GroupNamePrincipal> groupPrincipals = subject.getPrincipals(GroupNamePrincipal.class);
        for (GroupNamePrincipal principal : groupPrincipals) {
            answer.add(principal.getName());
        }
        return answer;
    }

    /**
     * Gets the id of the client
     * @return the id of the client
     */
    public UniqueID getId() {
        return id;
    }

    /**
     * Sets the id of the client
     * @param id new client's id
     */
    public void setId(UniqueID id) {
        this.id = id;
    }

    /**
     * Defines if the client has to be pinged.
     * Client of the core could be an internal service
     * or connected active object. We heed to ping only connected
     * ones.
     *
     * @return true if ping is requires
     */
    public boolean isPingable() {
        return pingable;
    }

    /**
     * Redefined equals method based on client's name
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Client client = (Client) o;

        if (name != null ? !name.equals(client.name) : client.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * @return string representation of the client
     */
    public String toString() {
        return "\"" + name + "\"" + (id != null ? " (" + url + ")" : "");
    }

    /**
     * Checks if the client is alive by sending the message to it.
     * There is a blocking network call inside so it should be used carefully.
     *
     * Throws an exception if the client body is not available which is
     * always the case after serialization.
     *
     * @return true if the client is alive, false otherwise
     */
    public boolean isAlive() {
        if (pingable) {
            if (this.body == null) {
                throw new RuntimeException("Cannot detect if the client " + this + " is alive");
            }
            try {
                return PAActiveObject.pingActiveObject(this.body);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extract the body id from an active object.
     * TODO find more straightforward way to do that
     *
     * @param service a target active object
     * @return an active object body id
     */
    public static UniqueID getId(Object service) {
        if (service instanceof StubObject && ((StubObject) service).getProxy() != null) {
            Proxy proxy = ((StubObject) service).getProxy();

            if (proxy instanceof BodyProxy) {
                return ((BodyProxy) proxy).getBodyID();
            }
        }

        return null;
    }

    /**
     * @return the subject of the client
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Checks that client has the specified permission.
     *
     * @return true if it has, throw {@link SecurityException} otherwise with specified error message
     */
    public boolean checkPermission(final Permission permission, String errorMessage) {
        try {
            Subject.doAsPrivileged(subject, new PrivilegedAction<Object>() {
                public Object run() {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(permission);
                    }
                    return null;
                }
            }, null);
        } catch (SecurityException ex) {
            throw new SecurityException(errorMessage, ex);
        }

        return true;
    }

    /**
     * Sets the history DB object associated to the current user connection.
     * @param history is an object to set
     */
    public void setHistory(UserHistory history) {
        this.history = history;
    }

    /**
     * Returns the connection history object linked to the data base
     * @return the connection history object linked to the data base
     */
    public UserHistory getHistory() {
        return history;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
