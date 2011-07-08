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
package org.ow2.proactive.resourcemanager.authentication;

import java.io.Serializable;
import java.security.Permission;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.internalmsg.Heartbeat;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
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

    private static final Heartbeat hb = new Heartbeat();
    /** client's name */
    private String name;

    private Subject subject;

    /** Unique id of the client */
    private UniqueID id;

    /** Defines if this client has to be pinged */
    boolean pingable = false;

    /** Body of the sender of request */
    private transient UniversalBody body;

    /** User connection history stored in the data base*/
    private transient UserHistory history;

    public Client() {
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
            this.id = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
            this.body = PAActiveObject.getContext().getCurrentRequest().getSender();
        }
    }

    /**
     * Gets the name of the client
     * @return the name of the client
     */
    public String getName() {
        return name;
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
     * @param the new client's id
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
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        Client client = (Client) o;
        return name.equals(client.getName());
    }

    /**
     * @return string representation of the client
     */
    public String toString() {
        return "\"" + name + "\"";
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
            if (body == null) {
                throw new RuntimeException("Cannot detect if the client " + this + " is alive");
            }
            try {
                body.receiveFTMessage(hb);
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
            throw new SecurityException(errorMessage);
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

}
