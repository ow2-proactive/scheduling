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
 * This class represents security attributes granted to a targeted communication
 *
 */
public class Communication implements Serializable {

    /**
     *
     */
    private final Authorization authentication;
    private final Authorization confidentiality;
    private final Authorization integrity;

    // public static final int REQUIRED = 1;
    // public static final int DENIED = -1;
    // public static final int OPTIONAL = 0;

    // public static final String STRING_REQUIRED = "required";
    // public static final String STRING_OPTIONAL = "optional";
    // public static final String STRING_DENIED = "denied";

    // /* indicates if authentication is required,optional or denied */
    // private int authentication;
    //
    // /* indicates if confidentiality is required,optional or denied */
    // private int confidentiality;
    //
    // /* indicates if integrity is required,optional or denied */
    // private int integrity;

    /* indicates if communication between active objects is allowed or not */
    private final boolean communication;

    /**
     * Default constructor, initialize a policy with communication attribute
     * sets to allowed and authentication,confidentiality and integrity set to
     * optional
     */
    public Communication() {
        this.authentication = Authorization.REQUIRED;
        this.confidentiality = Authorization.REQUIRED;
        this.integrity = Authorization.REQUIRED;
        this.communication = false;
    }

    /**
     * Copy constructor
     */
    public Communication(Communication com) {
        this.authentication = com.getAuthentication();
        this.confidentiality = com.getConfidentiality();
        this.integrity = com.getIntegrity();
        this.communication = com.getCommunication();
    }

    /**
     * This method specifies if communication is allowed
     *
     * @param authentication
     *            specifies if authentication is required, optional, or denied
     * @param confidentiality
     *            specifies if confidentiality is required, optional, or denied
     * @param integrity
     *            specifies if integrity is required, optional, or denied
     */
    public Communication(boolean allowed, Authorization authentication, Authorization confidentiality,
            Authorization integrity) {
        this.communication = allowed;
        this.authentication = authentication;
        this.confidentiality = confidentiality;
        this.integrity = integrity;
    }

    /**
     * Method isAuthenticationEnabled.
     *
     * @return boolean true if authentication is required
     */
    public boolean isAuthenticationEnabled() {
        return this.authentication == Authorization.REQUIRED;
    }

    /**
     * Method isConfidentialityEnabled.
     *
     * @return boolean true if confidentiality is required
     */
    public boolean isConfidentialityEnabled() {
        return this.confidentiality == Authorization.REQUIRED;
    }

    /**
     * Method isIntegrityEnabled.
     *
     * @return boolean true if integrity is required
     */
    public boolean isIntegrityEnabled() {
        return this.integrity == Authorization.REQUIRED;
    }

    // /**
    // * Method isAuthenticationForbidden.
    // * @return boolean true if confidentiality is forbidden
    // */
    // public boolean isAuthenticationForbidden() {
    // return this.authentication == Authorization.DENIED;
    // }
    //
    // /**
    // * Method isConfidentialityForbidden.
    // * @return boolean true if confidentiality is forbidden
    // */
    // public boolean isConfidentialityForbidden() {
    // return this.confidentiality == Authorization.DENIED;
    // }
    //
    // /**
    // * Method isIntegrityForbidden.
    // * @return boolean true if integrity is forbidden
    // */
    // public boolean isIntegrityForbidden() {
    // return this.integrity == Authorization.DENIED;
    // }

    /**
     * Method isCommunicationAllowed.
     *
     * @return boolean true if confidentiality is allowed
     */
    public boolean isCommunicationAllowed() {
        return this.communication;
    }

    @Override
    public String toString() {
        return "\n\tCom : " + this.communication + "\n\tAuth : " + this.authentication + "\n\tConf : " +
            this.confidentiality + "\n\tIntegrity : " + this.integrity + "\n";
    }

    /**
     * Method computePolicy.
     *
     * @param from
     *            the client policy
     * @param to
     *            the server policy
     * @return Policy returns a computation of the from and server policies
     * @throws IncompatiblePolicyException
     *             policies are incomptables, conflicting communication
     *             attributes
     */
    public static Communication computeCommunication(Communication from, Communication to)
            throws IncompatiblePolicyException {
        return new Communication(from.communication && to.communication, Authorization.compute(
                from.authentication, to.authentication), Authorization.compute(from.confidentiality,
                to.confidentiality), Authorization.compute(from.integrity, to.integrity));
    }

    /**
     * @return communication
     */
    public boolean getCommunication() {
        return this.communication;
    }

    // /**
    // * @param i
    // */
    // public void setCommunication(boolean i) {
    // this.communication = i;
    // }
    public Authorization getAuthentication() {
        return this.authentication;
    }

    public Authorization getConfidentiality() {
        return this.confidentiality;
    }

    public Authorization getIntegrity() {
        return this.integrity;
    }
}
