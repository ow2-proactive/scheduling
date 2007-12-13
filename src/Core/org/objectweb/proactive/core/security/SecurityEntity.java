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

import java.io.IOException;
import java.io.Serializable;
import java.security.AccessControlException;
import java.security.PublicKey;

import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;


/**
 *
 * SecureEntity defines all security related method a secure generic object must
 * implement. An entity can be an active object, a runtime, a domain.
 */
public interface SecurityEntity extends Serializable {

    /**
     * entity certificate
     * @return returns entity certificate
     * @throws SecurityNotAvailableException if security is not available
     * @throws java.io.IOException if communication fails
     */
    public TypedCertificate getCertificate() throws SecurityNotAvailableException, IOException;

    /**
     * start an unvalidated empty session
     * @param policy policy associated to the session
     * @return session ID
     * @throws SecurityNotAvailableException if security is not available
     * @throws RenegotiateSessionException if the session immediatly expires
     * @throws SessionException
     * @throws SecurityNotAvailableException
     */
    public long startNewSession(long distantSessionID, SecurityContext policy,
            TypedCertificate distantCertificate) throws SessionException, SecurityNotAvailableException,
            IOException;

    /**
     * entity public key
     * @return returns entity public key
     * @throws SecurityNotAvailableException
     */
    public PublicKey getPublicKey() throws SecurityNotAvailableException, IOException;

    /**
     * Exchange random value between client and server entity
     * @param sessionID the session ID
     * @param clientRandomValue client random value
     * @return server random value
     * @throws SecurityNotAvailableException if the security is not available
     * @throws RenegotiateSessionException if the session has expired
     */
    public byte[] randomValue(long sessionID, byte[] clientRandomValue) throws SecurityNotAvailableException,
            RenegotiateSessionException, IOException;

    /**
     * exchange entity certificate and/or public key if certificate are not available
     * @param sessionID the session ID
     * @param signature encoded signature of previous paramaters
     * @return an array containing :
     *           - server certificate and/or server public key
     *           - encoded signature of these parameters
     * @throws SecurityNotAvailableException if the security is not available
     * @throws RenegotiateSessionException if the session has expired
     * @throws KeyExchangeException if a key data/length/algorithm is not supported
     */
    public byte[] publicKeyExchange(long sessionID, byte[] signature) throws SecurityNotAvailableException,
            RenegotiateSessionException, KeyExchangeException, IOException;

    /**
     * this method sends encoded secret parameters to the target entity
     * @param sessionID session ID
     * @param encodedAESKey the AES key use to exchange secret message
     * @param encodedIVParameters Initilization parameters for the AES key
     * @param encodedClientMacKey MAC key for checking signature of future messages
     * @param encodedLockData random value to prevent message replays by an external attacker
     * @param parametersSignature encoded signature of the previous parameters
     * @return an array containing  :
     *             - encoded server AES key
     *             - encoded IV parameters
     *             - encoded server MAC key
     *             - encoded lock data to prevent message replays
     *             - encoded signature of previous parameters
     * @throws SecurityNotAvailableException if this entity does not support security
     * @throws RenegotiateSessionException if the session has expired or has been cancelled during this exchange
     * @throws java.io.IOException if communication fails
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey, byte[] encodedIVParameters,
            byte[] encodedClientMacKey, byte[] encodedLockData, byte[] parametersSignature)
            throws SecurityNotAvailableException, RenegotiateSessionException, IOException;

    /**
     * Ask the entity to fill the securityContext parameters with its own policy
     * according to the communication details contained in the given securityContext
     * @param securityContext communication details allowing the entity to
     * look for a matching policy
     * @return securityContext filled with this entity's policy
     * @throws SecurityNotAvailableException thrown the entity doest not support the security
     */
    public SecurityContext getPolicy(Entities local, Entities distant) throws SecurityNotAvailableException,
            IOException;

    /**
     * Retrieves all the entity's ID which contain this entity plus this entity ID.
     * @return returns all the entity's ID which contain this entity plus this entity ID.
     * @throws SecurityNotAvailableException if the target entity does not support security
     */
    public Entities getEntities() throws SecurityNotAvailableException, IOException;

    /**
     * terminate a given session
     * @param sessionID
     * @throws SecurityNotAvailableException id security is not available
     */
    public void terminateSession(long sessionID) throws SecurityNotAvailableException, IOException;

    /**
     * Returns this entity's security manager
     *
     * @param user
     *            an entity representing the user asking for the security
     *            manager
     * @throws SecurityNotAvailableException
     *             if security is not available
     * @throws AccessControlException
     *             if the user does not have the right to see the security
     *             manager
     */
    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
            throws SecurityNotAvailableException, AccessControlException, IOException;

    /**
     * Modifiy this entity's security manager
     *
     * @param user
     *            an entity representing the user modifying for the security
     *            manager
     * @param policyServer
     *            The new policy server of the security manager
     * @throws SecurityNotAvailableException
     *             if security is not available
     * @throws AccessControlException
     *             if the user does not have the right to see the security
     *             manager
     * @throws IOException
     */
    public void setProActiveSecurityManager(Entity user, PolicyServer policyServer)
            throws SecurityNotAvailableException, AccessControlException, IOException;
}
