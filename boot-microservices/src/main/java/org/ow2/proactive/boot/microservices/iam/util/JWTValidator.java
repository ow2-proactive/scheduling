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
package org.ow2.proactive.boot.microservices.iam.util;

import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.ow2.proactive.boot.microservices.iam.exceptions.IAMException;


public class JWTValidator implements TicketValidator {

    /** Logger instance */
    private Logger logger = Logger.getLogger(JWTValidator.class);

    /** IAM Server URL */
    protected String iamServerUrlPrefix;

    /** IAM client service */
    protected String service;

    /** Boolean mentioning whether the JWT is signed */
    protected boolean jsonWebTokenSigned;

    /** Boolean mentioning whether the JWT encrypted */
    protected boolean jsonWebTokenEncrypted;

    /** JWT signature key */
    protected String jsonWebTokenSignatureKey;

    /** JWT encryption key */
    protected String jsonWebTokenEncryptionKey;

    // List of setters used by IAMLoginModule
    public void setIamServerUrlPrefix(String iamServerUrlPrefix) {
        this.iamServerUrlPrefix = iamServerUrlPrefix;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setJsonWebTokenSigned(boolean jsonWebTokenSigned) {
        this.jsonWebTokenSigned = jsonWebTokenSigned;
    }

    public void setJsonWebTokenEncrypted(boolean jsonWebTokenEncrypted) {
        this.jsonWebTokenEncrypted = jsonWebTokenEncrypted;
    }

    public void setJsonWebTokenSignatureKey(String jsonWebTokenSignatureKey) {
        this.jsonWebTokenSignatureKey = jsonWebTokenSignatureKey;
    }

    public void setJsonWebTokenEncryptionKey(String jsonWebTokenEncryptionKey) {
        this.jsonWebTokenEncryptionKey = jsonWebTokenEncryptionKey;
    }

    /**
     * parses and validates a Json Web Token (JWT) acquired from IAM microservice.
     *
     * @param jsonWebToken  Json Web Token containing authentication and user information
     * @param service for which the JWT is generated
     * @return Assertion containing parsed authentication and user information
     * @throws TicketValidationException if a problem occurs during the validation process
     * @since version 8.3.0
     */
    public Assertion validate(String jsonWebToken, String service) throws TicketValidationException {

        // Parse JWT and get attributes
        JwtClaims claims = JWTUtils.parseJWT(jsonWebToken,
                                             jsonWebTokenSigned,
                                             jsonWebTokenSignatureKey,
                                             jsonWebTokenEncrypted,
                                             jsonWebTokenEncryptionKey,
                                             iamServerUrlPrefix,
                                             service);

        try {
            // Extract JWT principal from attributes
            AttributePrincipal principal = new AttributePrincipalImpl(claims.getSubject());

            // Create authentication assertion
            Assertion assertion = new AssertionImpl(new AttributePrincipalImpl(principal.getName(),
                                                                               claims.getClaimsMap()));

            logger.debug("Assertion generated for principal " + principal.getName());

            return assertion;
        } catch (MalformedClaimException e) {
            throw new IAMException("Malformed Json Web Token acquired from IAM [" + jsonWebToken + "]");
        }

    }

}
