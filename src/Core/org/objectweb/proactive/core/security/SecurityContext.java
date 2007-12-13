/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.security;

import java.io.Serializable;

import org.objectweb.proactive.core.security.securityentity.Entities;


/**
 * This classe represents a security context associated with a particular
 * session
 *
 */
public class SecurityContext implements Serializable {

    /**
     *
     */
    private final Entities entitiesLocal;
    private final Entities entitiesDistant;
    private final Communication sendRequest;
    private final Communication sendReply;
    private final boolean migration;
    private final boolean aoCreation;

    //	public SecurityContext() {
    //		// serializable
    //	}
    public SecurityContext(Entities local, Entities distant, Communication sendRequest,
            Communication sendReply, boolean aoCreation, boolean migration) {
        this.entitiesLocal = local;
        this.entitiesDistant = distant;
        this.sendReply = sendReply;
        this.sendRequest = sendRequest;
        this.aoCreation = aoCreation;
        this.migration = migration;
    }

    /**
     * @return entities of the 'from' objects
     */
    public Entities getEntitiesLocal() {
        return this.entitiesLocal;
    }

    /**
     * @return entities of the 'to' objects
     */
    public Entities getEntitiesDistant() {
        return this.entitiesDistant;
    }

    /**
     * @return true if migration is granted
     */
    public boolean isMigration() {
        return this.migration;
    }

    /**
     * @return true if object can send replies
     */
    public Communication getSendReply() {
        return this.sendReply;
    }

    /**
     * @return true if object can send requests
     */
    public Communication getSendRequest() {
        return this.sendRequest;
    }

    public Communication getReceiveRequest() {
        return this.sendReply;
    }

    public Communication getReceiveReply() {
        return this.sendRequest;
    }

    public boolean isAoCreation() {
        return this.aoCreation;
    }

    public boolean isEverythingForbidden() {
        return !this.sendReply.getCommunication() && !this.sendRequest.getCommunication() &&
            !this.aoCreation && !this.migration;
    }

    public SecurityContext otherSideContext() {
        return new SecurityContext(this.getEntitiesDistant(), this.getEntitiesLocal(), this.getSendReply(),
            this.getSendRequest(), this.isAoCreation(), this.isMigration());
    }

    public static SecurityContext computeContext(SecurityContext from, SecurityContext to) {
        return new SecurityContext(from.getEntitiesLocal(), from.getEntitiesDistant(), Communication
                .computeCommunication(from.getSendRequest(), to.getReceiveRequest()), Communication
                .computeCommunication(from.getSendReply(), to.getReceiveReply()), from.isAoCreation() &&
            to.isAoCreation(), from.isMigration() && to.isMigration());
    }

    public static SecurityContext mergeContexts(SecurityContext thees, SecurityContext that) {
        return new SecurityContext(thees.getEntitiesLocal(), thees.getEntitiesDistant(), Communication
                .computeCommunication(thees.getSendRequest(), that.getSendRequest()), Communication
                .computeCommunication(thees.getSendReply(), that.getSendReply()), thees.isAoCreation() &&
            that.isAoCreation(), thees.isMigration() && that.isMigration());
    }

    @Override
    public String toString() {
        String s = new String();
        s += "Context :";
        s += ("\n\tLocal : " + this.entitiesLocal.toString());
        s += ("\n\tDistant : " + this.entitiesDistant.toString());
        s += ("\n\tRequest : " + this.sendRequest.toString());
        s += ("\n\tReply : " + this.sendReply.toString());
        s += ("\n\tAOCreation : " + this.aoCreation);
        s += ("\n\tMigration : " + this.migration);

        return s;
    }
}
