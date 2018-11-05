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
package org.ow2.proactive.authentication;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.Logger;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.boot.microservices.iam.IAMStarter;
import org.ow2.proactive.boot.microservices.iam.util.IAMConfiguration;
import org.ow2.proactive.boot.microservices.iam.util.IAMCredentialsValidator;
import org.ow2.proactive.boot.microservices.iam.util.IAMRestClient;


/**
 * Authentication based on ProActive IAM (Identity and Access Management) micro-service.
 *
 *
 * based on Apereo CAS framework
 *
 *
 * @author The ActiveEon Team
 * @since ProActive Scheduling 8.2
 */
public class IAMLoginModule implements LoginModule {

    /** Logger instance */
    private static final Logger LOG = Logger.getLogger(IAMLoginModule.class.getName());

    private static final String IAM_URL_KEY = "iamServerUrlPrefix";

    /** JAAS authentication subject */
    private Subject subject;

    /** JAAS callback handler */
    private CallbackHandler callbackHandler;

    /** IAM Server URL */
    private String iamServerUrlPrefix;

    /** IAM Ticket/Token validator */
    private String ticketValidatorClass;

    /** IAM Ticket/Token validator extracted from ticketValidatorClass*/
    private TicketValidator ticketValidator;

    /** IAM client service */
    private String service;

    /** Attribute used to designate the user role */
    private String roleAttributeName;

    /** Attribute used to separate many roles assigned to the same user */
    private String roleSeparator;

    /** IAM response marker used to get the SSO ticket */
    private String ssoTicketMarker;

    /** login status */
    private boolean succeeded = false;

    /**
     * Initialize this IAMLoginModule.
     *
     *
     * @param subject
     *            the Subject not to be authenticated.
     *
     *
     * @param callbackHandler
     *            a CallbackHandler to get the credentials of the
     *            user, must work with NoCallback callbacks.
     *
     * @param sharedState state shared with other configured LoginModules.
     *
     * @param options options specified in the login
     *			Configuration for this particular
     *			IAMLoginModule.
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.iamServerUrlPrefix = IAMStarter.getIamURL();

        Configuration config = IAMStarter.getConfiguration();
        this.ticketValidatorClass = config.getString(IAMConfiguration.TOKEN_VALIDATOR_CLASS);
        this.roleAttributeName = config.getString(IAMConfiguration.ROLE_ATTRIBUTE);
        this.roleSeparator = config.getString(IAMConfiguration.ROLE_SEPARATOR);
        this.ssoTicketMarker = config.getString(IAMConfiguration.SSO_TICKET_MARKER);
        this.service = config.getString(IAMConfiguration.PA_CORE_CLIENT);

        boolean jsonWebTokenSigned = config.getBoolean(IAMConfiguration.IAM_TOKEN_SIGNATURE_ENABLED);
        boolean jsonWebTokenEncrypted = config.getBoolean(IAMConfiguration.IAM_TOKEN_ENCRYPTION_ENABLED);
        String jsonWebTokenSignatureKey = config.getString(IAMConfiguration.IAM_TOKEN_SIGNATURE_KEY);
        String jsonWebTokenEncryptionKey = config.getString(IAMConfiguration.IAM_TOKEN_ENCRYPTION_KEY);

        // Pass configuration parameters (as a Map) to token validator classes
        Map configParams = new HashMap<String, Object>();
        configParams.put(IAM_URL_KEY, iamServerUrlPrefix);
        configParams.put("roleAttributeName", roleAttributeName);
        configParams.put("roleSeparator", roleSeparator);
        configParams.put("service", service);
        configParams.put("jsonWebTokenSigned", String.valueOf(jsonWebTokenSigned));
        configParams.put("jsonWebTokenEncrypted", String.valueOf(jsonWebTokenEncrypted));
        configParams.put("jsonWebTokenSignatureKey", jsonWebTokenSignatureKey);
        configParams.put("jsonWebTokenEncryptionKey", jsonWebTokenEncryptionKey);

        CommonUtils.assertNotNull(ticketValidatorClass, "ticketValidatorClass is required.");
        this.ticketValidator = createTicketValidator(ticketValidatorClass, configParams);
    }

    /**
     * Authenticate the user by getting the user name and password from the
     * CallbackHandler.
     *
     *
     *
     * @return true in all cases since this IAMLoginModule
     *         should not be ignored.
     *
     * @exception FailedLoginException
     *                if the authentication fails.
     *
     *
     * @exception LoginException
     *                if this IAMLoginModule is unable to
     *                perform the authentication.
     */
    @Override
    public boolean login() throws LoginException {

        succeeded = false;

        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available " +
                                     "to garner authentication information from the user");
        }
        try {
            Callback[] callbacks = new Callback[] { new NoCallback() };

            // gets the username, password, group Membership, and group Hierarchy from callback handler
            callbackHandler.handle(callbacks);
            Map<String, Object> params = ((NoCallback) callbacks[0]).get();
            String username = (String) params.get("username");
            char[] password = ((String) params.get("pw")).toCharArray();

            params.clear();
            ((NoCallback) callbacks[0]).clear();

            CommonUtils.assertNotNull(username, "No username has been specified for authentication");
            CommonUtils.assertNotNull(password, "No password has been specified for authentication");

            String iamToken;

            if (ticketValidator instanceof IAMCredentialsValidator) {
                iamToken = validateUserCredentials(username, password);
            } else {
                iamToken = getServiceToken(username, password);
            }

            Assertion assertion = ticketValidator.validate(iamToken, service);

            CommonUtils.assertNotNull(assertion,
                                      "Empty assertion returned by ticket validator " + this.ticketValidatorClass);

            String authenticator = assertion.getPrincipal().getName();
            String role = (String) assertion.getPrincipal().getAttributes().get(roleAttributeName);

            CommonUtils.assertNotNull(authenticator,
                                      "Authenticator returned by ticket validator " + this.ticketValidatorClass +
                                                     " is null");
            CommonUtils.assertNotNull(role,
                                      "User role returned by ticket validator " + this.ticketValidatorClass +
                                            " is null");

            //Authentication
            subject.getPrincipals().add(new UserNamePrincipal(authenticator));
            LOG.debug("adding principal '" + authenticator);

            //Authorization
            String[] groups = role.split(roleSeparator);
            for (String group : groups) {
                subject.getPrincipals().add(new GroupNamePrincipal(group.trim()));
                LOG.debug("adding group '" + group + "' to principal '" + authenticator + "'");
            }

            //Successful login
            succeeded = true;
            LOG.debug("authentication succeeded for user '" + authenticator + "'");

            return succeeded;

        } catch (IOException | UnsupportedCallbackException | TicketValidationException e) {
            LOG.error("", e);
            throw new LoginException(e.toString());
        }
    }

    /**
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    @Override
    public boolean commit() throws LoginException {
        return succeeded;
    }

    @Override
    public boolean abort() throws LoginException {
        boolean result = succeeded;
        succeeded = false;
        return result;
    }

    @Override
    public boolean logout() throws LoginException {
        succeeded = false;
        return true;
    }

    /**
     * Creates a ServiceToken given a valid user credentials.
     * @param username login name of the authenticator.
     * @param password password of the authenticator.
     * @return Service token including authentication information.
     */
    private String getServiceToken(String username, char[] password) {

        // Acquire SSO ticket from IAM
        String ssoTicket = new IAMRestClient().getSSOTicket(iamServerUrlPrefix + IAMConfiguration.IAM_TICKET_REQUEST,
                                                            username,
                                                            password,
                                                            ssoTicketMarker,
                                                            false);
        // Acquire Service Token (i.e., JWT or CAS ST) based on SSO ticket
        String serviceToken = new IAMRestClient().getServiceToken(iamServerUrlPrefix +
                                                                  IAMConfiguration.IAM_TICKET_REQUEST + "/" + ssoTicket,
                                                                  service);

        CommonUtils.assertNotNull(serviceToken, "no service token produced by IAM");

        return serviceToken;

    }

    /**
     * Creates a TicketValidator instance from a class name and map of property name/value pairs.
     * @param className Fully-qualified name of TicketValidator concrete class.
     * @param propertyMap Map of property name/value pairs to set on validator instance.
     * @return Ticket validator with properties set.
     */
    private TicketValidator createTicketValidator(String className, Map<String, ?> propertyMap) {

        Class<TicketValidator> validatorClass = ReflectUtils.loadClass(className);
        TicketValidator validator;

        if (className.equals(Cas30ServiceTicketValidator.class.getName())) {
            CommonUtils.assertTrue(propertyMap.containsKey(IAM_URL_KEY),
                                   "Required property " + IAM_URL_KEY + " not found.");

            validator = ReflectUtils.newInstance(validatorClass, propertyMap.get(IAM_URL_KEY));
        } else {
            validator = ReflectUtils.newInstance(validatorClass);
        }

        try {
            BeanInfo info = Introspector.getBeanInfo(validatorClass);

            for (Map.Entry entry : propertyMap.entrySet()) {

                String property = (String) entry.getKey();
                String value = (String) entry.getValue();

                LOG.debug("Attempting to set TicketValidator property " + property);
                PropertyDescriptor propertyDescriptor = ReflectUtils.getPropertyDescriptor(info, property);
                if (propertyDescriptor != null) {
                    ReflectUtils.setProperty(property, convertIfNecessary(propertyDescriptor, value), validator, info);
                    LOG.debug("Set " + property + " = " + value);
                } else {
                    LOG.debug("Cannot find property " + property + " on " + className);
                }
            }

        } catch (IntrospectionException e) {
            LOG.error("Error getting bean info for " + validatorClass + e.getMessage());
            return null;
        }

        return validator;
    }

    /**
     * Attempts to do simple type conversion from a string value to the type expected
     * by the given property.
     *
     * Currently only conversion to int, long, and boolean are supported.
     *
     * @param propertyDescriptor Property descriptor of target property to set.
     * @param value Property value as a string.
     * @return Value converted to type expected by property if a conversion strategy exists.
     */
    private static Object convertIfNecessary(final PropertyDescriptor propertyDescriptor, final String value) {
        if (String.class.equals(propertyDescriptor.getPropertyType())) {
            return value;
        } else if (boolean.class.equals(propertyDescriptor.getPropertyType())) {
            return Boolean.valueOf(value);
        } else if (int.class.equals(propertyDescriptor.getPropertyType())) {
            return new Integer(value);
        } else if (long.class.equals(propertyDescriptor.getPropertyType())) {
            return new Long(value);
        } else {
            throw new IllegalArgumentException("No conversion strategy exists for property " +
                                               propertyDescriptor.getName() + " of type " +
                                               propertyDescriptor.getPropertyType());
        }
    }

    private String validateUserCredentials(String username, char[] password) {
        return new IAMRestClient().validateUserCredentials(IAMStarter.getIamURL() + IAMConfiguration.IAM_USERS_REQUEST,
                                                           username,
                                                           password);
    }
}
