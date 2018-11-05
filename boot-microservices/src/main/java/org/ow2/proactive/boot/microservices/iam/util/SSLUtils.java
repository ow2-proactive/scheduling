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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.oneandone.compositejks.CompositeX509KeyManager;
import com.oneandone.compositejks.CompositeX509TrustManager;
import com.oneandone.compositejks.KeyStoreLoader;
import com.oneandone.compositejks.SslContextUtils;


/**
 * Utility methods for SSL context.
 */
public final class SSLUtils {

    private SSLUtils() {
    }

    /**
     * Configures the default SSL context to use a merged view of the system key
     * store and a custom key store.
     *
     * @throws GeneralSecurityException, IOException
     */
    public static void mergeKeyStoreWithSystem(String sslProtocol, String x509Algorithm, String certificatePath,
            String certificatePassword) throws GeneralSecurityException, IOException {

        SSLContext.setDefault(buildMergedWithSystem(sslProtocol,
                                                    x509Algorithm,
                                                    KeyStoreLoader.fromFile(certificatePath),
                                                    certificatePassword));
    }

    /**
     * Generates an SSL context that uses a merged view of the system key store
     * and a custom key store.
     *
     * @param keyStore The custom key store.
     * @return The SSL context
     * @throws GeneralSecurityException
     */
    private static SSLContext buildMergedWithSystem(String sslProtocol, String x509Algorithm, KeyStore keyStore,
            String password) throws GeneralSecurityException {
        String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

        KeyManager[] keyManagers = { new CompositeX509KeyManager(SslContextUtils.getSystemKeyManager(x509Algorithm,
                                                                                                     keyStore,
                                                                                                     password.toCharArray()),
                                                                 SslContextUtils.getSystemKeyManager(defaultAlgorithm,
                                                                                                     null,
                                                                                                     null)) };

        TrustManager[] trustManagers = { new CompositeX509TrustManager(SslContextUtils.getSystemTrustManager(x509Algorithm,
                                                                                                             keyStore),
                                                                       SslContextUtils.getSystemTrustManager(defaultAlgorithm,
                                                                                                             null)) };

        SSLContext context = SSLContext.getInstance(sslProtocol);
        context.init(keyManagers, trustManagers, null);
        return context;
    }
}
