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
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.ReflectUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.ow2.proactive.authentication.iam.IAMRestClient;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;


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

    /** IAM REST request for tickets */
    private String iamTicketRequest;

    /** IAM Ticket/Token validator */
    private String ticketValidatorClass;

    /** IAM Ticket/Token validator extracted from ticketValidatorClass*/
    private TicketValidator ticketValidator;

    /** IAM client service */
    private String service;

    /** IAM assertion */
    private Assertion assertion;

    /** Name of the attribute in the CAS assertion that should be used for the username */
    private String userAttributeName;

    /** character used to separate many roles assigned to the user */
    private String roleSeparator;

    /** Name of the attribute in the CAS assertion that should be used for user role data */
    private String roleAttributeName;

    /** IAM response header used to get the SSO ticket */
    private String ssoTicketHeader;

    /** Boolean mentioning whether the JWT is signed */
    private boolean jsonWebTokenSigned;

    /** Boolean mentioning whether the JWT encrypted */
    private boolean jsonWebTokenEncrypted;

    /** JWT signature key */
    private String jsonWebTokenSignatureKey;

    /** JWT encryption key */
    private String jsonWebTokenEncryptionKey;

    /** login status */
    private boolean succeeded = false;

    /**
     * Initialize this <code>IAMLoginModule</code>.
     *
     * <p>
     *
     * @param subject
     *            the <code>Subject</code> not to be authenticated.
     *            <p>
     *
     * @param callbackHandler
     *            a <code>CallbackHandler</code> to get the credentials of the
     *            user, must work with <code>NoCallback</code> callbacks.
     *            <p>
     * @param sharedState state shared with other configured LoginModules. <p>
     *
     * @param options options specified in the login
     *			<code>Configuration</code> for this particular
     *			<code>IAMLoginModule</code>.
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.assertion = null;
        this.ticketValidatorClass = null;

        for (final Map.Entry entry : options.entrySet()) {
            LOG.trace("Processing option " + entry.getKey());
            if (IAM_URL_KEY.equals(entry.getKey())) {
                this.iamServerUrlPrefix = (String) entry.getValue();
                LOG.debug("Set " + IAM_URL_KEY + "=" + this.iamServerUrlPrefix);
            } else if ("iamTicketRequest".equals(entry.getKey())) {
                this.iamTicketRequest = (String) entry.getValue();
                LOG.debug("Set iamTicketRequest=" + this.iamTicketRequest);
            } else if ("service".equals(entry.getKey())) {
                this.service = (String) entry.getValue();
                LOG.debug("Set service=" + this.service);
            } else if ("ticketValidatorClass".equals(entry.getKey())) {
                this.ticketValidatorClass = (String) entry.getValue();
                LOG.debug("Set ticketValidatorClass=" + ticketValidatorClass);
            } else if ("userAttributeName".equals(entry.getKey())) {
                this.userAttributeName = (String) entry.getValue();
                LOG.trace("Got userAttributeName value" + userAttributeName);
            } else if ("roleAttributeName".equals(entry.getKey())) {
                this.roleAttributeName = (String) entry.getValue();
                LOG.trace("Got roleAttributeName value" + roleAttributeName);
            } else if ("roleSeparator".equals(entry.getKey())) {
                this.roleSeparator = (String) entry.getValue();
                LOG.trace("Got roleSeparator value" + roleSeparator);
            } else if ("ssoTicketHeader".equals(entry.getKey())) {
                this.ssoTicketHeader = (String) entry.getValue();
                LOG.debug("Set ssoTicketHeader=" + ssoTicketHeader);
            } else if ("jsonWebTokenSigned".equals(entry.getKey())) {
                this.jsonWebTokenSigned = Boolean.parseBoolean((String) entry.getValue());
                LOG.debug("Set jsonWebTokenSigned=" + jsonWebTokenSigned);
            } else if ("jsonWebTokenEncrypted".equals(entry.getKey())) {
                this.jsonWebTokenEncrypted = Boolean.parseBoolean((String) entry.getValue());
                LOG.debug("Set jsonWebTokenEncrypted=" + jsonWebTokenEncrypted);
            } else if ("jsonWebTokenSignatureKey".equals(entry.getKey())) {
                this.jsonWebTokenSignatureKey = (String) entry.getValue();
                LOG.debug("Set jsonWebTokenSignatureKey=" + jsonWebTokenSignatureKey);
            } else if ("jsonWebTokenEncryptionKey".equals(entry.getKey())) {
                this.jsonWebTokenEncryptionKey = (String) entry.getValue();
                LOG.debug("Set jsonWebTokenEncryptionKey=" + jsonWebTokenEncryptionKey);
            }

        }

        CommonUtils.assertNotNull(ticketValidatorClass, "ticketValidatorClass is required.");
        this.ticketValidator = createTicketValidator(ticketValidatorClass, options);
    }

    /**
     * Authenticate the user by getting the user name and password from the
     * CallbackHandler.
     *
     * <p>
     *
     * @return true in all cases since this <code>IAMLoginModule</code>
     *         should not be ignored.
     *
     * @exception FailedLoginException
     *                if the authentication fails.
     *                <p>
     *
     * @exception LoginException
     *                if this <code>IAMLoginModule</code> is unable to
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
            String password = (String) params.get("pw");

            params.clear();
            ((NoCallback) callbacks[0]).clear();

            CommonUtils.assertNotNull(username, "No username has been specified for authentication");
            CommonUtils.assertNotNull(password, "No password has been specified for authentication");

            assertion = ticketValidator.validate(getServiceToken(username, password), service);

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
     * <p>
     * This method is called if the LoginContext's overall authentication
     * succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * LoginModules succeeded).
     * <p>
     *
     * @exception LoginException
     *                if the commit fails.
     *
     * @return true if this IAMLoginModule's own login and commit attempts
     *         succeeded, or false otherwise.
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
    private String getServiceToken(String username, String password) {

        // Acquire SSO ticket from IAM
        String ssoTicket = IAMRestClient.getSSOTicket(iamServerUrlPrefix + iamTicketRequest,
                                                      username,
                                                      password,
                                                      ssoTicketHeader);
        // Acquire JWT based on SSO ticket
        String serviceToken = IAMRestClient.getServiceToken(iamServerUrlPrefix + iamTicketRequest + "/" + ssoTicket,
                                                            service);

        CommonUtils.assertNotNull(serviceToken, "no service token produced by IAM");

        return serviceToken;

    }

    /**
     * Creates a {@link TicketValidator} instance from a class name and map of property name/value pairs.
     * @param className Fully-qualified name of {@link TicketValidator} concrete class.
     * @param propertyMap Map of property name/value pairs to set on validator instance.
     * @return Ticket validator with properties set.
     */
    private TicketValidator createTicketValidator(final String className, final Map<String, ?> propertyMap) {
        CommonUtils.assertTrue(propertyMap.containsKey(IAM_URL_KEY),
                               "Required property " + IAM_URL_KEY + " not found.");

        final Class<TicketValidator> validatorClass = ReflectUtils.loadClass(className);
        final TicketValidator validator = ReflectUtils.newInstance(validatorClass, propertyMap.get(IAM_URL_KEY));

        try {
            final BeanInfo info = Introspector.getBeanInfo(validatorClass);

            for (final Map.Entry entry : propertyMap.entrySet()) {
                if (!IAM_URL_KEY.equals(entry.getKey())) {
                    final String property = (String) entry.getKey();
                    final String value = (String) entry.getValue();

                    LOG.debug("Attempting to set TicketValidator property " + property);
                    final PropertyDescriptor pd = ReflectUtils.getPropertyDescriptor(info, property);
                    if (pd != null) {
                        ReflectUtils.setProperty(property, convertIfNecessary(pd, value), validator, info);
                        LOG.debug("Set " + property + " = " + value);
                    } else {
                        LOG.debug("Cannot find property " + property + " on " + className);
                    }
                }
            }

        } catch (final IntrospectionException e) {
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
     * @param pd Property descriptor of target property to set.
     * @param value Property value as a string.
     * @return Value converted to type expected by property if a conversion strategy exists.
     */
    private static Object convertIfNecessary(final PropertyDescriptor pd, final String value) {
        if (String.class.equals(pd.getPropertyType())) {
            return value;
        } else if (boolean.class.equals(pd.getPropertyType())) {
            return Boolean.valueOf(value);
        } else if (int.class.equals(pd.getPropertyType())) {
            return new Integer(value);
        } else if (long.class.equals(pd.getPropertyType())) {
            return new Long(value);
        } else {
            throw new IllegalArgumentException("No conversion strategy exists for property " + pd.getName() +
                                               " of type " + pd.getPropertyType());
        }
    }
}
