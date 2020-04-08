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
package org.ow2.proactive.core.properties;

import static org.jasypt.commons.CommonUtils.STRING_OUTPUT_TYPE_BASE64;

import java.util.Properties;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.NoIvGenerator;
import org.jasypt.properties.EncryptableProperties;
import org.jasypt.salt.RandomSaltGenerator;


public class PropertyDecrypter {

    public static final String ENCRYPTION_PREFIX = "ENC(";

    public static final String ENCRYPTION_SUFFIX = ")";

    private static StandardPBEStringEncryptor encryptor = null;

    public static synchronized StringEncryptor getDefaultEncryptor() {
        if (encryptor == null) {
            encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(PASharedProperties.PROPERTIES_CRYPT_KEY.getValueAsString());
            // This system property is used by spring boot configurations
            System.setProperty("jasypt.encryptor.password", PASharedProperties.PROPERTIES_CRYPT_KEY.getValueAsString());
            // default jasypt-springboot configuration to greatly simplify configuration
            encryptor.setAlgorithm("PBEWithMD5AndDES");
            encryptor.setProviderName("SunJCE");
            encryptor.setStringOutputType(STRING_OUTPUT_TYPE_BASE64);
            encryptor.setSaltGenerator(new RandomSaltGenerator());
            encryptor.setIvGenerator(new NoIvGenerator());
            encryptor.setKeyObtentionIterations(1000);
        }
        return encryptor;
    }

    public static String encryptData(String data) {
        StringEncryptor encryptor = getDefaultEncryptor();
        if (encryptor == null || data == null || data.isEmpty() || data.startsWith(ENCRYPTION_PREFIX)) {
            return data;
        } else {
            return ENCRYPTION_PREFIX + encryptor.encrypt(data) + ENCRYPTION_SUFFIX;
        }
    }

    public static String decryptData(String data) {
        StringEncryptor encryptor = getDefaultEncryptor();
        if ((encryptor != null) && (data != null) && data.startsWith(ENCRYPTION_PREFIX)) {
            return encryptor.decrypt(data.substring(ENCRYPTION_PREFIX.length(),
                                                    data.length() - ENCRYPTION_SUFFIX.length()));
        }
        return data;
    }

    public static Properties getDecryptableProperties() {
        StringEncryptor encryptor = getDefaultEncryptor();
        return new EncryptableProperties(encryptor);
    }
}
