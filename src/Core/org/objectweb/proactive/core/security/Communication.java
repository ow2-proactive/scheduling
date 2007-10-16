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

import org.objectweb.proactive.core.security.exceptions.IncompatiblePolicyException;


/**
 *  This class represents security attributes granted to a targeted communication
 *
 */
public class Communication implements Serializable {
    public static int REQUIRED = 1;
    public static int DENIED = -1;
    public static int OPTIONAL = 0;
    public static int ALLOWED = 1;

    /* indicates if authentication is required,optional or denied */
    private int authentication;

    /* indicates if confidentiality is required,optional or denied */
    private int confidentiality;

    /* indicates if integrity is required,optional or denied */
    private int integrity;

    /* indicates if communication between active objects is allowed or not */
    private int communication;
    private int migration;
    private int aoCreation;

    /**
     * Default constructor, initialize a policy with communication attribute sets to allowed and
     * authentication,confidentiality and integrity set to optional
     */
    public Communication() {
        authentication = 0;
        confidentiality = 0;
        integrity = 0;
        communication = 1;
        migration = 1;
        aoCreation = 1;
    }

    /**
     * This method specifies if communication is allowed
     * @param authentication specifies if authentication is required, optional, or denied
     * @param confidentiality specifies if confidentiality is required, optional, or denied
     * @param integrity specifies if integrity is required, optional, or denied
     */
    public Communication(int authentication, int confidentiality, int integrity) {
        this.authentication = authentication;
        this.confidentiality = confidentiality;
        this.integrity = integrity;
    }

    /**
     * Method isAuthenticationEnabled.
     * @return boolean true if authentication is required
     */
    public boolean isAuthenticationEnabled() {
        return authentication == 1;
    }

    /**
     * Method isConfidentialityEnabled.
     * @return boolean true if confidentiality is required
     */
    public boolean isConfidentialityEnabled() {
        return confidentiality == 1;
    }

    /**
     * Method isIntegrityEnabled.
     * @return boolean true if integrity is required
     */
    public boolean isIntegrityEnabled() {
        return integrity == 1;
    }

    /**
     * Method isAuthenticationForbidden.
     * @return boolean true if confidentiality is forbidden
     */
    public boolean isAuthenticationForbidden() {
        return authentication == -1;
    }

    /**
     * Method isConfidentialityForbidden.
     * @return boolean true if confidentiality is forbidden
     */
    public boolean isConfidentialityForbidden() {
        return confidentiality == -1;
    }

    /**
     * Method isIntegrityForbidden.
     * @return boolean true if integrity is forbidden
     */
    public boolean isIntegrityForbidden() {
        return integrity == -1;
    }

    /**
     * Method isCommunicationAllowed.
     * @return boolean true if confidentiality is allowed
     */
    public boolean isCommunicationAllowed() {
        return communication == 1;
    }

    @Override
    public String toString() {
        return "Com : " + communication + " Auth : " + authentication +
        " Conf : " + confidentiality + " Integrity : " + integrity + "\n";
    }

    /**
     * @param i
     */
    public void setMigration(int i) {
        migration = i;
    }

    /**
     * @return migration
     */
    public int getMigration() {
        return migration;
    }

    /**
     * Method computePolicy.
     * @param from the client policy
     * @param to the server policy
     * @return Policy returns a computation of the from and server policies
     * @throws IncompatiblePolicyException policies are incomptables, conflicting communication attributes
     */
    public static Communication computeCommunication(Communication from,
        Communication to) throws IncompatiblePolicyException {
        if (from.isCommunicationAllowed() && to.isCommunicationAllowed()) {
            if (((from.authentication == REQUIRED) &&
                    (to.authentication == DENIED)) ||
                    ((from.confidentiality == REQUIRED) &&
                    (to.confidentiality == DENIED)) ||
                    ((from.integrity == REQUIRED) && (to.integrity == DENIED)) ||
                    ((from.authentication == DENIED) &&
                    (to.authentication == REQUIRED)) ||
                    ((from.confidentiality == DENIED) &&
                    (to.confidentiality == REQUIRED)) ||
                    ((from.integrity == DENIED) && (to.integrity == REQUIRED))) {
                throw new IncompatiblePolicyException("incompatible policies");
            }
        }

        return new Communication(from.authentication + to.authentication,
            from.confidentiality + to.confidentiality,
            from.integrity + to.integrity);
    }

    /**
     * @param aocreation
     */
    public void setAOCreation(int aocreation) {
        this.aoCreation = aocreation;
    }

    /**
     *
     */
    public int getAOCreation() {
        return this.aoCreation;
    }

    /**
     * @return communication
     */
    public int getCommunication() {
        return communication;
    }

    /**
     * @param i
     */
    public void setCommunication(int i) {
        communication = i;
    }
}
