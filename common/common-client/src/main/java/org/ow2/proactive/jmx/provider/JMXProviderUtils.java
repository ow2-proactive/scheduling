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
package org.ow2.proactive.jmx.provider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.PrivilegedActionException;

import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;


/**
 * Various utilities and constants.
 * 
 * @author The ProActive Team 
 */
public final class JMXProviderUtils {
    /** A string representation of the Remote Object protocol */
    public static final String RO_PROTOCOL = "ro";

    /** The package that contains the classes of the JMX Remote Object provider */
    public static final String RO_PROVIDER_PKGS = "org.ow2.proactive.jmx.provider";

    // JNDI radix
    private static final String JNDI_CONTEXT = "/jndi/";

    /**
     * Construct a new IOException with a nested exception.
     */
    public static IOException newIOException(final String message, final Throwable cause) {
        final IOException x = new IOException(message);
        x.initCause(cause);
        return x;
    }

    /**
     * Iterate until we extract the real exception from a stack of
     * PrivilegedActionExceptions.
     * 
     * @param e the initial PrivilegedException (top of the stack)
     * @return the first non-PrivilegedException contained in e
     */
    public static Exception extractException(Exception e) {
        while (e instanceof PrivilegedActionException) {
            e = ((PrivilegedActionException) e).getException();
        }
        return e;
    }

    /**
     * Extracts an URI from a JMXServiceURL address such as:
     * <code>service:jmx:<em>protocol</em>:///jndi/<em>uri</em></code>
     * 
     * @param jmxServiceURL the address that contains the uri
     * @return an URI extracted from the address
     * @throws MalformedURLException If the address is incorrect
     */
    public static URI extractURI(final JMXServiceURL jmxServiceURL) throws MalformedURLException {
        final String path = jmxServiceURL.getURLPath();
        if (!path.startsWith(JMXProviderUtils.JNDI_CONTEXT)) {
            throw new MalformedURLException("Incorrect URL form " + path);
        }
        return URI.create(path.substring(JMXProviderUtils.JNDI_CONTEXT.length()));
    }

    /**
     * Returns the default base URI for all remote objects.
     * 
     * @return the default base URI for all remote objects
     * @throws Exception if the default protocol is unknown
     */
    public static URI getBaseURI() throws Exception {
        return AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory().getBaseURI();
    }
}
