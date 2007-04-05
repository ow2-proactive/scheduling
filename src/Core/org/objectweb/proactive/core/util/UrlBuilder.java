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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
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
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is a utility class to perform modifications and operations on urls.
 */
public class UrlBuilder {
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
     * @return String the url if well-formed
     * @throws URISyntaxException if the url is not well-formed
     */
    public static String checkUrl(String url) throws URISyntaxException {
        URI u = new URI(url);
        String hostname;
        try {
            hostname = fromLocalhostToHostname(u.getHost());

            URI u2 = new URI(u.getScheme(), null, hostname, u.getPort(),
                    u.getPath(), u.getQuery(), u.getFragment());
            return u2.toString();
        } catch (UnknownHostException e) {
            throw new URISyntaxException(url, "host unknow");
        }
    }

    /**
     * @param host
     * @param name
     * @param protocol
     * @return
     */
    public static String buildUrl(String host, String name, String protocol) {
        return buildUrl(host, name, protocol,
            getDefaultPortForProtocol(protocol));
    }

    /**
     * @param host
     * @param name
     * @return
     */
    public static String buildUrl(String host, String name) {
        return buildUrl(host, name, null);
    }

    /**
     * returns the default port set for the given protocol
     * @param protocol the protocol
     * @return the default port number associated to the protocol
     */
    public static int getDefaultPortForProtocol(String protocol) {
        if (Constants.XMLHTTP_PROTOCOL_IDENTIFIER.equals(protocol)) {
            if (System.getProperty(Constants.PROPERTY_PA_XMLHTTP_PORT) != null) {
                return Integer.parseInt(System.getProperty(
                        Constants.PROPERTY_PA_XMLHTTP_PORT));
            }
        } else if ((Constants.RMI_PROTOCOL_IDENTIFIER.equals(protocol)) ||
                Constants.IBIS_PROTOCOL_IDENTIFIER.equals(protocol)) {
            return Integer.parseInt(System.getProperty(
                    Constants.PROPERTY_PA_RMI_PORT));
        } else if (Constants.RMISSH_PROTOCOL_IDENTIFIER.equals(protocol)) {
            return Integer.parseInt(System.getProperty(
                    Constants.PROPERTY_PA_RMISSH_PORT));
        }

        // default would be to return the RMI default port
        return -1;
    }

    /**
    * Returns an url under the form protocol://host:port/name
    * @param host
    * @param name
    * @param protocol
    * @param port
    * @return an url under the form protocol://host:port/name
    */
    public static String buildUrl(String host, String name, String protocol,
        int port) {
        //                if (protocol == null) {
        //                    protocol = System.getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL);
        //                }
        if (port == 0) {
            port = -1;
        }

        try {
            host = fromLocalhostToHostname(host);

            if ((name != null) && (!name.startsWith("/"))) {
                /* URI does not require a '/' at the beginning of the name like URLs. As we cannot use
                 * URL directly (because we do not want to register a URL handler), we do this ugly hook.
                 */
                name = "/" + name;
            }
            return new URI(protocol, null, host, port, name, null, null).toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method build an url in the form protocol://host:port/name where the port
     * is given from system propeties, except when the protocol is jini. In that case the url
     * looks like jini://host/name.
     * @param host
     * @param name
     * @return an Url built from properties
     */
    public static String buildUrlFromProperties(String host, String name) {
        String port = null;
        String protocol = System.getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL);
        if (protocol.equals(Constants.RMI_PROTOCOL_IDENTIFIER) ||
                protocol.equals(Constants.IBIS_PROTOCOL_IDENTIFIER)) {
            port = System.getProperty(Constants.PROPERTY_PA_RMI_PORT);
        }
        if (protocol.equals(Constants.XMLHTTP_PROTOCOL_IDENTIFIER)) {
            port = System.getProperty(Constants.PROPERTY_PA_XMLHTTP_PORT);
        }
        if (protocol.equals(Constants.JINI_PROTOCOL_IDENTIFIER) ||
                (port == null)) {
            return buildUrl(host, name, protocol);
        } else {
            return buildUrl(host, name, protocol, new Integer(port).intValue());
        }
    }

    public static String buildVirtualNodeUrl(String url)
        throws java.net.UnknownHostException {
        String vnName = getNameFromUrl(url);
        vnName = vnName.concat("_VN");
        String host = getHostNameFromUrl(url);
        String protocol = getProtocol(url);
        int port = getPortFromUrl(url);
        return buildUrl(host, vnName, protocol, port);
    }

    public static String appendVnSuffix(String name) {
        return name.concat("_VN");
    }

    public static String removeVnSuffix(String url) {
        //		System.out.println("########vn url "+url);
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
    public static String getNameFromUrl(String url) {
        int n = url.lastIndexOf("/");
        String name = url.substring(n + 1);
        return name;
    }

    /**
     * Return the protocol specified in the string
     * The same convention as in URL is used
     */
    public static String getProtocol(String nodeURL) {
        String protocol = URI.create(nodeURL).getScheme();
        if (protocol == null) {
            return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
        }
        return protocol;
    }

    /**
     * Returns the url without protocol
     */
    public static String removeProtocol(String url, String protocol) {
        if (url.startsWith(protocol)) {
            return url.substring(protocol.length() + 1);
        }
        return url;
    }

    public static String getHostNameFromUrl(String url) {
        URI uri = URI.create(url);
        return uri.getHost();
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
     * Returns port number included in the url or the default port for the specified protocol
     * @param url
     * @return the port number included in the url or  the default port for the specified protocol
     */
    public static int getPortFromUrl(String url) {
        try {
            URI uri = new URI(url);
            if (uri.getPort() != -1) {
                return uri.getPort();
            } else {
                return getDefaultPortForProtocol(uri.getScheme());
            }
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
            return -1;
        }
    }

    /**
     * this method returns the hostname or the IP address associate to the InetAddress address parameter.
     * It is possible to set "proactive.runtime.ipaddress" or
     * "proactive.hostname" or the "proactive.useIPaddress" property (evaluated in that order) to override the default java behaviour
     * of resolving InetAddress
     * @param address any InetAddress
     * @return a String matching the corresponding InetAddress
     */
    public static String getHostNameorIP(InetAddress address) {
        if (System.getProperty(Constants.PROPERTY_PA_RUNTIME_IPADDRESS) != null) {
            return System.getProperty(Constants.PROPERTY_PA_RUNTIME_IPADDRESS);
        }

        if (System.getProperty(Constants.PROPERTY_PA_HOSTNAME) != null) {
            return System.getProperty(Constants.PROPERTY_PA_HOSTNAME);
        }
        if ("true".equals(System.getProperty(
                        Constants.PROPERTY_PA_USE_IP_ADDRESS))) {
            return address.getHostAddress();
        } else {
            return address.getCanonicalHostName();
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

    public static String fromLocalhostToHostname(String localName)
        throws UnknownHostException {
        java.net.InetAddress hostInetAddress = java.net.InetAddress.getLocalHost();
        for (int i = 0; i < LOCAL_URLS.length; i++) {
            if (LOCAL_URLS[i].startsWith(localName.toLowerCase())) {
                return UrlBuilder.getHostNameorIP(hostInetAddress);
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
        try {
            URI uri = new URI(url);
            if (uri.getPort() != -1) {
                return uri.getPort();
            }
            return 0;
        } catch (URISyntaxException e) {
            return 0;
        }
    }

    /**
     * change the port of a given url
     * @param url the url to change the port
     * @param port the new port number
     * @return the url with the new port
     */
    public static String setPort(String url, int port) {
        URI u = URI.create(url);
        URI u2;
        try {
            u2 = new URI(u.getScheme(), u.getUserInfo(), u.getHost(), port,
                    u.getPath(), u.getQuery(), u.getFragment());
            return u2.toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return url;
    }
}
