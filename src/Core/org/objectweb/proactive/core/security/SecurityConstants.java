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


/**
 * @author ProActive Team
 * Defines usefull constants for security
 *
 */
public abstract class SecurityConstants {
    //    public static final String XML_CERTIFICATE = "/Policy/Certificate";
    //    public static final String XML_PRIVATE_KEY = "/Policy/PrivateKey";
    //    public static final String XML_TRUSTED_CERTIFICATION_AUTHORITY = "/Policy/TrustedCertificationAuthority/CertificationAuthority";
    //    public static final String XML_CERTIFICATION_AUTHORITY_CERTIFICATE = "Certificate";
    public static final int MAX_SESSION_VALIDATION_WAIT = 30;
    public enum EntityType {UNKNOWN,
        ENTITY,
        OBJECT,
        NODE,
        RUNTIME,
        APPLICATION,
        USER,
        DOMAIN;
        public static EntityType fromString(String string) {
            for (EntityType value : EntityType.values()) {
                if (value.toString().equalsIgnoreCase(string)) {
                    return value;
                }
            }
            return EntityType.UNKNOWN;
        }

        public EntityType getParentType() {
            switch (this) {
            case ENTITY:
            case OBJECT:
            case NODE:
            case RUNTIME:
                return APPLICATION;
            case APPLICATION:
                return USER;
            case USER:
            case DOMAIN:
                return DOMAIN;
            default:
                return UNKNOWN;
            }
        }

        public boolean match(EntityType that) {
            if (this == that) {
                return true;
            }

            if ((this == UNKNOWN) || (that == UNKNOWN)) {
                return true;
            }

            if ((this == ENTITY) &&
                    ((that == RUNTIME) || (that == NODE) || (that == OBJECT))) {
                return true;
            }

            if ((that == ENTITY) &&
                    ((this == RUNTIME) || (this == NODE) || (this == OBJECT))) {
                return true;
            }

            return false;
        }
    }
}
