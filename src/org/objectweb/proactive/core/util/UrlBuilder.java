/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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

import org.objectweb.proactive.core.Constants;

import java.net.UnknownHostException;


/**
 * This class is a utility class to perform modifications and operations on urls.
 */
public class UrlBuilder {
    private static String[] LOCAL_URLS = { "///", "//localhost", "//127.0.0.1" };
    private final static int DEFAULT_REGISTRY_PORT = 1099;

    //
    //------------Constructor--------------------------------
    //
    public UrlBuilder() {
    }

    //
    //-------------------Public methods-------------------------
    //

    /**
     * Checks if the given url is well-formed
     * @param url the url to check
     * @return String the url if well-formed
     * @throws UnknownHostException if the url is not well-formed
     */
    public static String checkUrl(String url)
        throws java.net.UnknownHostException {
        String protocol = getProtocol(url);
        String noProtocolUrl = internalCheck(removeProtocol(url, protocol));
        return readHostAndName(noProtocolUrl, protocol);
    }

    public static String buildUrl(String host, String name, String protocol) {
        return buildUrl(host, name, protocol, DEFAULT_REGISTRY_PORT);
    }

    //	
    public static String buildUrl(String host, String name, String protocol,
        int port) {
        protocol = checkProtocol(protocol);
        String noProtocolUrl = buildUrl(host, name, port);
        if (protocol.equals(Constants.DEFAULT_PROTOCOL_IDENTIFIER)) {
            return noProtocolUrl;
        } else {
            return protocol + noProtocolUrl;
        }
    }

    /**
     * This method build an url in the form protocol://host:port/name where the port
     * is given from system propeties, except when the protocol is jini. In that case the url
     * looks like jini://host/name.
     * @param host
     * @param name
     * @param protocol
     * @return an Url built from properties
     */
    public static String buildUrlFromProperties(String host, String name) {
        String port = null;
        String protocol = System.getProperty("proactive.communication.protocol");
        if(protocol.equals("rmi") || protocol.equals("ibis")){
            port = System.getProperty("proactive.rmi.port");
        }
        if(protocol.equals("http")){
            port = System.getProperty("proactive.http.port");
        }
        if (checkProtocol(protocol).equals("jini:") || (port == null)) {
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
     * Returns the name included in the url under the for hostname:port
     * @param url
     * @return the name included in the url
     * @throws UnknownHostException
     */
    public static String getHostNameAndPortFromUrl(String url)
        throws UnknownHostException {
        String validUrl = checkUrl(url);
        int n = validUrl.indexOf("//");
        int m = validUrl.lastIndexOf("/"); // looking for the end of the host
        if (m == (n + 1)) {
            //the url has no name i.e it is a host url
            // 
            return validUrl.substring(n + 2, validUrl.length());
        } else {
            //check if there is a port
            return validUrl.substring(n + 2, m);
        }
    }

    /**
     * Return the protocol specified in the string
     * The same convention as in URL is used
     */
    public static String getProtocol(String nodeURL) {
        if (nodeURL == null) {
            return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
        }
        int n = nodeURL.indexOf("://");
        if (n <= 0) {
            return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
        }
        return nodeURL.substring(0, n + 1);
    }

    /**
     * Returns the url without protocol
     */
    public static String removeProtocol(String url, String protocol) {
        if (url.startsWith(protocol)) {
            return url.substring(protocol.length());
        }
        return url;
    }

    public static String getHostNameFromUrl(String url)
        throws java.net.UnknownHostException {
        String validUrl = checkUrl(url);
        int n = validUrl.indexOf("//");
        int m = validUrl.lastIndexOf("/"); // looking for the end of the host
        if (m == (n + 1)) {
            //the url has no name i.e it is a host url
            // 
            return getHostFromUrl(validUrl.substring(n + 2, validUrl.length()));
        } else {
            //check if there is a port
            return getHostFromUrl(validUrl.substring(n + 2, m));
        }
    }

    public static String checkProtocol(String protocol) {
        if (protocol.indexOf(":") == -1) {
            return protocol.concat(":");
        }
        return protocol;
    }

    public static String removePortFromHost(String hostname) {
        int n = hostname.lastIndexOf(":");
        if (n > -1) {
            return hostname.substring(0, n);
        }
        return hostname;
    }

    /**
     * Returns port number included in the url or 1099 if no port is specified
     * @param url
     * @return the port number included in the url or 1099 if no port is specified
     */
    public static int getPortFromUrl(String url) {
        try {
            String validUrl = checkUrl(url);
            int n = validUrl.indexOf("//");
            int m = validUrl.lastIndexOf("/");
            if (m == (n + 1)) {
                return getPortNumber(validUrl.substring(n + 2));
            } else {
                return getPortNumber(validUrl.substring(n + 2, m));
            }
        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
            return DEFAULT_REGISTRY_PORT;
        }
    }

    //
    //-----------------Private Static Methods-----------------------------
    //
    //	private static String internalCheckUrl(String url) throws java.net.UnknownHostException{
    //		String validlUrl = internalCheck(url);
    //		String noProtocolUrl
    //    if (noProtocolUrl.charAt(noProtocolUrl.length()-1) == '/')
    //      noProtocolUrl = noProtocolUrl.substring(0,noProtocolUrl.length()-1);
    //    if (! noProtocolUrl.startsWith("//"))
    //      throw new java.net.UnknownHostException("Malformed URL = "+url);
    //    return noProtocolUrl;
    //	}
    private static String buildUrl(String host, String name, int port) {
        if (port == DEFAULT_REGISTRY_PORT) {
            if (name.equals("")) {
                return "//" + host;
            } else {
                return "//" + host + "/" + name;
            }
        } else {
            if (name.equals("")) {
                return "//" + host + ":" + port;
            } else {
                return "//" + host + ":" + port + "/" + name;
            }
        }
    }

    private static String readHostAndName(String urlToRead, String protocol)
        throws java.net.UnknownHostException {
        java.net.InetAddress hostInetAddress = java.net.InetAddress.getLocalHost();
        for (int i = 0; i < LOCAL_URLS.length; i++) {
            if (urlToRead.toLowerCase().startsWith(LOCAL_URLS[i])) {
                // local url
                String name = urlToRead.substring(LOCAL_URLS[i].length());
                if (name.startsWith("/")) {
                    //there is no port and the name starts with /
                    return buildUrl(hostInetAddress.getCanonicalHostName(),
                        name.substring(1), protocol);
                } else {
                    //there is no port and only the name 
                    if (name.indexOf(":") < 0) {
                        return buildUrl(hostInetAddress.getCanonicalHostName(),
                            name, protocol);
                    } else if (name.indexOf("/") < 0) {
                        // there is a port and no name, it is a host url
                        return buildHostUrl("//" +
                            hostInetAddress.getCanonicalHostName() + name,
                            protocol);
                    }

                    //port is define, extract port and build url
                    return buildUrl(hostInetAddress.getCanonicalHostName(),
                        name.substring(name.lastIndexOf("/") + 1, name.length()),
                        protocol,
                        new Integer(name.substring(1, name.lastIndexOf("/"))).intValue());
                }
            }
        }

        // non local url
        int n = urlToRead.indexOf('/', 2); // looking for the end of the host
        if (n < 3) {
            if (n == -1) {
                //It means that there is no name, so return just the complete url of the host
                return buildHostUrl(urlToRead, protocol);
            } else {
                throw new java.net.UnknownHostException(
                    "Cannot determine the name of the host in this url=" +
                    urlToRead);
            }
        }

        // get the host
        String hostname = getHostFromUrl(urlToRead.substring(2, n));

        // get the real host name
        hostInetAddress = java.net.InetAddress.getByName(hostname);
        //get the port 
        int portNumber = getPortNumber(urlToRead.substring(2, n));

        String name = urlToRead.substring(n + 1);

        return buildUrl(hostInetAddress.getCanonicalHostName(), name, protocol,
            portNumber);
    }

    /**
     * @param urlToRead
     * @return An url of the form protocol://host:port
     * @throws UnknownHostException
     */
    private static String buildHostUrl(String urlToRead, String protocol)
        throws UnknownHostException {
        protocol = checkProtocol(protocol);
        String urlTemp;
        String hostname = getHostFromUrl(urlToRead.substring(2,
                    urlToRead.length()));
        java.net.InetAddress hostInetAddress = java.net.InetAddress.getByName(hostname);
        int portNumber = getPortNumber(urlToRead.substring(2, urlToRead.length()));
        if (portNumber == DEFAULT_REGISTRY_PORT) {
            urlTemp = "//" + hostname;
        } else {
            urlTemp = "//" + hostname + ":" + portNumber;
        }
        if (protocol.equals(Constants.DEFAULT_PROTOCOL_IDENTIFIER)) {
            return urlTemp;
        } else {
            return protocol + urlTemp;
        }
    }

    /**
     * This method extract the port from a string in the form host:port or host
     * @param url
     * @return port number or 0 if there is no  port
     */
    private static int getPortNumber(String url) {
        int portNumber = DEFAULT_REGISTRY_PORT;
        int index = url.lastIndexOf(":");
        if (index > -1) {
            portNumber = Integer.parseInt(url.substring(index + 1, url.length()));
        }
        return portNumber;
    }

    /**
     * This method extract the host name from a string in the form host:port or host
     * @param url
     * @return
     */
    private static String getHostFromUrl(String url) {
        int index = url.lastIndexOf(":");

        //there is a port
        if (index > -1) {
            return url.substring(0, index);
        }

        //there is no port
        return url;
    }

    private static String internalCheck(String url) {
        if (!url.startsWith("//")) {
            url = "//" + url;
        }
        if (url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
