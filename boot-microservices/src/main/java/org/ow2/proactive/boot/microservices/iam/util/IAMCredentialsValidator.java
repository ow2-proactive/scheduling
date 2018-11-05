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

import java.util.Map;

import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.ow2.proactive.boot.microservices.iam.exceptions.IAMException;


public class IAMCredentialsValidator implements TicketValidator {

    /** Logger instance */
    private Logger logger = Logger.getLogger(IAMCredentialsValidator.class);

    /** Attribute used to extract user role from IAM response */
    private String roleAttributeName;

    public void setRoleAttributeName(String roleAttributeName) {
        this.roleAttributeName = roleAttributeName;
    }

    /**
     * parses and validates a JSON string containing authentication and user information acquired
     * after validating user credentials against IAM microservice.
     *
     * @param iamToken JSON string containing authentication and user information
     * @param service (not used). It is only added to fit the TicketValidator interface.
     * @return Assertion containing parsed authentication and user information
     * @throws TicketValidationException if a problem occurs during the validation process
     * @since version 8.3.0
     */
    public Assertion validate(String iamToken, String service) throws TicketValidationException {

        IAMJsonUtils iamJsonUtils = new IAMJsonUtils();

        try {
            iamJsonUtils.parseAuthenticationJson(iamToken);

            // validate user id
            String userId = iamJsonUtils.getUserId();
            CommonUtils.assertNotNull(userId, "No user is found in Json response acquired from IAM");
            CommonUtils.isNotBlank(userId);

            // validate user attributes
            Map userAttributes = iamJsonUtils.getUserAttributes();
            String userRole = (String) userAttributes.get(roleAttributeName);
            CommonUtils.assertNotNull(userRole, "No user group/role is found in Json response acquired from IAM");
            CommonUtils.isNotBlank(userRole);

            // Create authentication assertion
            AttributePrincipal principal = new AttributePrincipalImpl(userId);
            Assertion assertion = new AssertionImpl(new AttributePrincipalImpl(principal.getName(), userAttributes));

            logger.debug("Assertion generated for principal " + principal.getName());

            return assertion;

        } catch (ParseException e) {
            throw new IAMException("Malformed Json response acquired from IAM [" + iamToken + "]");
        }
    }
}
