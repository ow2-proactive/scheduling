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
package org.objectweb.proactive.core.util;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is a utility class to perform modifications and operations on urls.
 */
public class URIBuilder {
    private static String[] LOCAL_URLS = {
            "", "localhost.localdomain", "localhost", "127.0.0.1"
        };
    static Logger logger = ProActiveLogger.getLogger(Loggers.UTIL);

    //
    //-------------------Public methods-------------------------
    //

    /**
     * Checks if the given url is well-formed
     * @param url the url to check
     * @return The url if well-formed
     * @throws URISyntaxException if the url is not well-formed
     */
    public static URI checkURI(String url) throws URISyntaxException {
        URI u = new URI(url);

        String hostname;
        try {
            hostname = fromLocalhostToHostname(u.getHost());
            URI u2 = buildURI(hostname, u.getPath(), u.getScheme(), u.getPort());
            return u2;
        } catch (UnknownHostException e) {
            throw new URISyntaxException(url, "host unknow");
        }
    }

    /**
     * Returns an url compliant with RFC 2396 [protocol:][//host][[/]path]
     * loopback address is replaced by a non-loopback address localhost -> [DNS/IP] Address
     * @param host Url's hostname
     * @param name Url's Path
     * @param protocol Url's protocol
     * @throws UnknownProtocolException
     * @returnan url under the form [protocol:][//host][[/]name]
     */
    public static URI buildURI(String host, String name, String protocol) {
        try {
            return buildURI(host, name, protocol,
                RemoteObjectHelper.getDefaultPortForProtocol(protocol));
        } catch (UnknownProtocolException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns an url compliant with RFC 2396[//host][[/]path]
     * loopback address is replaced by a non-loopback address localhost -> [DNS/IP] Address
     * @param host Url's hostname
     * @param name Url's Path
     * @throws UnknownProtocolException
     * @returnan url under the form [//host][[/]name]
     */
    public static URI buildURI(String host, String name) {
        return buildURI(host, name, null);
    }

    /**
     * Returns an url compliant with RFC 2396 [protocol:][//host[:port]][[/]path]
     * loopback address is replaced by a non-loopback address localhost -> [DNS/IP] Address
     * @param host Url's hostname
     * @param name Url's Path
     * @param protocol Url's protocol
     * @param port Url's port
     * @returnan url under the form [protocol:][//host[:port]][[/]name]
     */
    public static URI buildURI(String host, String name, String protocol,
        int port) {
        return buildURI(host, name, protocol, port, true);
    }

    /**
     * Returns an url compliant with RFC 2396 [protocol:][//host[:port]][[/]name]
     * @param host Url's hostname
     * @param name Url's Path
     * @param protocol Url's protocol
     * @param port Url's port
     * @param replaceHost indicate if internal hooks regarding how to resolve the hostname have to be used
     * @see #fromLocalhostToHostname(String localName)
     * @see #getHostNameorIP(InetAddress address)
     * @returnan url under the form [protocol:][//host[:port]][[/]name]
     */
    public static URI buildURI(String host, String name, String protocol,
        int port, boolean replaceHost) {
        //        if (protocol == null) {
        //            protocol = System.getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL);
        //        }
        if (port == 0) {
            port = -1;
        }

        try {
            if (replaceHost) {
                host = fromLocalhostToHostname(host);
            }

            if ((name != null) && (!name.startsWith("/"))) {
                /* URI does not require a '/' at the beginning of the name like URLs. As we cannot use
                 * URL directly (because we do not want to register a URL handler), we do this ugly hook.
                 */
                name = "/" + name;
            }
            return new URI(protocol, null, host, port, name, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static URI setProtocol(URI uri, String protocol) {
        try {
            return new URI(protocol, uri.getUserInfo(), uri.getHost(),
                uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method build an url in the form protocol://host:port/name where the port
     * and protocol are retrieved from system properties
     * @param host
     * @param name
     * @return an Url built from properties
     */
    public static URI buildURIFromProperties(String host, String name) {
        int port = -1;
        String protocol = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
        try {
            // ok, awful hack but ensures that the factory for the given
            // protocol has effectively been loaded by the classloader
            // and that the initialization process has been performed
            port = RemoteObjectHelper.getDefaultPortForProtocol(protocol);
        } catch (UnknownProtocolException e) {
            logger.debug(e.getMessage());
        }
        if (port == -1) {
            return buildURI(host, name, protocol);
        } else {
            return buildURI(host, name, protocol, new Integer(port).intValue());
        }
    }

    /**
     * build a virtual node url from a given url
     * @param url
     * @return
     * @throws java.net.UnknownHostException if no network interface was found
     */
    public static URI buildVirtualNodeUrl(URI uri) {
        String vnName = getNameFromURI(uri);
        vnName = vnName.concat("_VN");
        String host = getHostNameFromUrl(uri);
        String protocol = uri.getScheme();
        int port = uri.getPort();
        return buildURI(host, vnName, protocol, port);
    }

    /**
     * build a virtual node url from a given url
     * @param url
     * @return
     * @throws java.net.UnknownHostException if no network interface was found
     */
    public static URI buildVirtualNodeUrl(String url) {
        return buildVirtualNodeUrl(URI.create(url));
    }

    public static String appendVnSuffix(String name) {
        return name.concat("_VN");
    }

    public static String removeVnSuffix(String url) {
        int index = url.lastIndexOf("_VN");
        if (index == -1) {
            return url;
        }
        return url.substring(0, index);
    }

    /**
     * Returns the name included in the url
     * @param url
     * @return the name included in the url
     */
    public static String getNameFromURI(URI u) {
        String path = u.getPath();
        if ((path != null) && (path.startsWith("/"))) {
            // remove the intial '/'
            return path.substring(1);
        }
        return path;
    }

    public static String getNameFromURI(String url) {
        URI uri = URI.create(url);
        return getNameFromURI(uri);
    }

    /**
     * Return the protocol specified in the string
     * The same convention as in URL is used
     */
    public static String getProtocol(URI uri) {
        String protocol = uri.getScheme();
        if (protocol == null) {
            return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
        }
        return protocol;
    }

    public static String getProtocol(String url) {
        URI uri = URI.create(url);
        return getProtocol(uri);
    }

    /**
     * Returns the url without protocol
     */
    public static URI removeProtocol(URI uri) {
        return buildURI(getHostNameFromUrl(uri), uri.getPath(), null,
            uri.getPort(), false);
    }

    public static URI removeProtocol(String url) {
        URI uri = URI.create(url);
        return removeProtocol(uri);
    }

    public static String getHostNameFromUrl(URI uri) {
        return uri.getHost();
    }

    public static String getHostNameFromUrl(String url) {
        URI uri = URI.create(url);
        return getHostNameFromUrl(uri);
    }

    public static String removePortFromHost(String hostname) {
        try {
            URI uri = new URI(hostname);
            return uri.getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return hostname;
        }
    }

    /**
     * this method returns the hostname or the IP address associated to the InetAddress address parameter.
     * It is possible to set
     *  {@code "proactive.hostname"}  property (evaluated in that order) to override the default java behaviour
     * of resolving InetAddress
     * @param address any InetAddress
     * @return a String matching the corresponding InetAddress
     */
    public static String getHostNameorIP(InetAddress address) {
        //        address = UrlBuilder.getNetworkInterfaces();
        if (PAProperties.PA_HOSTNAME.getValue() != null) {
            return PAProperties.PA_HOSTNAME.getValue();
        }

        String temp = "";

        if (PAProperties.PA_NET_USE_IP_ADDRESS.isTrue()) {
            temp = (address).getHostAddress();
        } else {
            temp = address.getCanonicalHostName();
        }

        return URIBuilder.ipv6withoutscope(temp);
    }

    /**
     * evaluate if localName is a loopback entry, if yes calls {@link getHostNameorIP(InetAddress address)}
     * @param localName
     * @return a remotely accessible host name if exists
     * @throws UnknownHostException if no network interface was found
     * @see getHostNameorIP(InetAddress address)
     */
    public static String fromLocalhostToHostname(String localName)
        throws UnknownHostException {
        if (localName == null) {
            localName = "localhost";
        }

        java.net.InetAddress hostInetAddress = ProActiveInet.getInstance()
                                                            .getInetAddress();
        for (int i = 0; i < LOCAL_URLS.length; i++) {
            if (LOCAL_URLS[i].startsWith(localName.toLowerCase())) {
                return URIBuilder.getHostNameorIP(hostInetAddress);
            }
        }

        return localName;
    }

    /**
     * This method extract the port from a string in the form host:port or host
     * @param url
     * @return port number or 0 if there is no  port
     */
    public static int getPortNumber(String url) {
        URI uri = URI.create(url);
        return getPortNumber(uri);
    }

    public static int getPortNumber(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return 0;
    }

    /**
     * change the port of a given url
     * @param url the url to change the port
     * @param port the new port number
     * @return the url with the new port
     */
    public static URI setPort(URI u, int port) {
        URI u2;
        try {
            u2 = new URI(u.getScheme(), u.getUserInfo(), u.getHost(), port,
                    u.getPath(), u.getQuery(), u.getFragment());
            return u2;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return u;
    }

    public static String ipv6withoutscope(String address) {
        String name = address;
        int indexPercent = name.indexOf('%');
        if (indexPercent != -1) {
            return "[" + name.substring(0, indexPercent) + "]";
        } else {
            return address;
        }
    }

    public static String ipv6withoutscope(InetAddress address) {
        String name = address.getHostAddress();
        int indexPercent = name.indexOf('%');

        if (indexPercent != -1) {
            return "[" + name.substring(0, indexPercent) + "]";
        } else {
            return name;
        }
    }

    public static String removeUsername(String url) {
        //this method is used to extract the username, that might be necessary for the callback
        //it updates the hostable.
        int index = url.indexOf("@");

        if (index >= 0) {
            String username = url.substring(0, index);
            url = url.substring(index + 1, url.length());

            HostsInfos.setUserName(getHostNameFromUrl(url), username);
        }

        return url;
    }
}
