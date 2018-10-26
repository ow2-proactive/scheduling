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

import java.io.File;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;


public class IAMConfiguration {

    public static final String PROPERTIES_FILE = "application-proactive.properties";

    public static final String ARCHIVE_NAME = "iam.archive.name";

    public static final String STARTUP_TIMEOUT = "iam.startup.timeout";

    public static final String READY_MARKER = "iam.ready.marker";

    public static final String ERROR_MARKER = "iam.error.marker";

    public static final String SSO_TICKET_MARKER = "iam.sso.ticket.marker";

    public static final String JVM_ARGS = "iam.jvm.args";

    public static final String SSL_PROTOCOL = "iam.ssl.protocol";

    public static final String SSL_X509_ALGORITHM = "iam.ssl.x509.algorithm";

    public static final String SSL_CERTTIFICATE = "server.ssl.key-store";

    public static final String SSL_CERTTIFICATE_PASS = "server.ssl.key-store-password";

    public static final String ROLE_ATTRIBUTE = "iam.user.role.attribute";

    public static final String ROLE_SEPARATOR = "iam.user.role.separator";

    public static final String PA_CORE_CLIENT = "iam.pa.core.client";

    public static final String PA_REST_SESSION = "iam.pa.rest.session.id";

    public static final String TOKEN_VALIDATOR_CLASS = "iam.ticket.validator.class";

    public static final String SPRING_PROACTIVE_ENV_PROFILE = "proactive";

    public static final String IAM_PROTOCOL = "https://";

    public static final String IAM_HOST = "cas.host.name";

    public static final String IAM_PORT = "server.port";

    public static final String IAM_CONTEXT = "server.context-path";

    public static final String IAM_TOKEN_SIGNATURE_KEY = "cas.authn.token.crypto.signing.key";

    public static final String IAM_TOKEN_ENCRYPTION_KEY = "cas.authn.token.crypto.encryption.key";

    public static final String IAM_TOKEN_SIGNATURE_ENABLED = "cas.authn.token.crypto.signingEnabled";

    public static final String IAM_TOKEN_ENCRYPTION_ENABLED = "cas.authn.token.crypto.encryptionEnabled";

    public static final String IAM_TICKET_REQUEST = "/v1/tickets";

    public static final String IAM_USERS_REQUEST = "/v1/users";

    public static final String IAM_LOGIN_PAGE = "/login";

    public static final String IAM_URL = "casServerUrlPrefix";

    public static final String IAM_LOGIN = "casServerLoginUrl";

    public static final String PA_SERVER_NAME = "serverName";

    public static final String PA_HOME_PLACEHOLDER = "${pa.scheduler.home}";

    private static final Logger LOGGER = Logger.getLogger(IAMConfiguration.class);

    private static final ListDelimiterHandler DELIMITER = new DefaultListDelimiterHandler(';');

    private IAMConfiguration() {

    }

    public static Configuration loadConfig(File configFile) throws ConfigurationException {

        Configuration config;

        PropertiesBuilderParameters propertyParameters = new Parameters().properties();
        propertyParameters.setFile(configFile);
        propertyParameters.setThrowExceptionOnMissing(true);
        propertyParameters.setListDelimiterHandler(DELIMITER);

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);

        builder.configure(propertyParameters);

        config = builder.getConfiguration();

        LOGGER.debug("IAM configuration loaded");

        return config;
    }
}
