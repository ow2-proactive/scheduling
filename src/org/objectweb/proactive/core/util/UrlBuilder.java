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


/**
 * This class is a utility class to perform modifications and operations on urls.
 */
public class UrlBuilder {
    private static String[] LOCAL_URLS = { "///", "//localhost/", "//127.0.0.1/" };
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
        String noProtocolUrl = internalCheckUrl(url);
        return readHostAndName(noProtocolUrl);
    }

    /**
     * Returns a well-formed url.
     * @param host
     * @param name
     * @return String the well formed url
     */
    public static String buildUrl(String host, String name) {
        return buildUrl(host, name, DEFAULT_REGISTRY_PORT);
    }

    /**
     * Returns a well-formed url.
     * @param host
     * @param name
     * @param port
     * @return String the well formed url
     */
    public static String buildUrl(String host, String name, int port) {
        if (port == DEFAULT_REGISTRY_PORT) {
            return "//" + host + "/" + name;
        } else {
            return "//" + host + ":" + port + "/" + name;
        }
    }

    public static String buildUrl(String host, String name, String protocol) {
        return protocol + "//" + host + "/" + name;
    }

    public static String buildVirtualNodeUrl(String url)
        throws java.net.UnknownHostException {
        String vnName = getNameFromUrl(url);
        vnName = vnName.concat("_VN");
        String host = getHostNameFromUrl(url);
        String protocol = getProtocol(url);
        return buildUrl(host, vnName, protocol);
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
        if (nodeURL == null) {
           return getDefaultProtocole();
           // return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
        }
        int n = nodeURL.indexOf("://");
        if (n <= 0) {
           return getDefaultProtocole();
           // return Constants.DEFAULT_PROTOCOL_IDENTIFIER;
        }
        return nodeURL.substring(0, n + 1);
    }
    
    private static String getDefaultProtocole() {
    	return System.getProperty("proactive.rmi")+":";
    	//return null;
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
        return validUrl.substring(n + 2, m);
    }

    public static String checkProtocol(String protocol) {
        if (protocol.indexOf(":") == -1) {
            return protocol.concat(":");
        }
        return protocol;
    }

    //
    //-----------------Private Static Methods-----------------------------
    //
    private static String internalCheckUrl(String url)
        throws java.net.UnknownHostException {
        String noProtocolUrl = internalCheck(url);
        if (noProtocolUrl.charAt(noProtocolUrl.length() - 1) == '/') {
            noProtocolUrl = noProtocolUrl.substring(0,
                    noProtocolUrl.length() - 1);
        }
        if (!noProtocolUrl.startsWith("//")) {
            throw new java.net.UnknownHostException("Malformed URL = " + url);
        }
        return noProtocolUrl;
    }

    private static String readHostAndName(String urlToRead)
        throws java.net.UnknownHostException {
        java.net.InetAddress hostInetAddress = java.net.InetAddress.getLocalHost();
        for (int i = 0; i < LOCAL_URLS.length; i++) {
            if (urlToRead.toLowerCase().startsWith(LOCAL_URLS[i])) {
                // local url
                String name = urlToRead.substring(LOCAL_URLS[i].length());
                return buildUrl(hostInetAddress.getHostName(), name);
            }
        }

        // non local url
        int n = urlToRead.indexOf('/', 2); // looking for the end of the host
        if (n < 3) {
            throw new java.net.UnknownHostException(
                "Cannot determine the name of the host in this url=" +
                urlToRead);
        }

        // get the host
        String hostname = getHostFromUrl(urlToRead.substring(2, n));

        // get the real host name
        hostInetAddress = java.net.InetAddress.getByName(hostname);
        //get the port 
        int portNumber = getPortNumber(urlToRead.substring(2, n));
        String name = urlToRead.substring(n + 1);

        //build the url
        if (portNumber > 0) {
            return buildUrl(hostInetAddress.getHostName(), name, portNumber);
        } else {
            return buildUrl(hostInetAddress.getHostName(), name);
        }
    }

    private static int getPortNumber(String url) {
        int portNumber = 0;
        int index = url.lastIndexOf(":");
        if (index > -1) {
            portNumber = Integer.parseInt(url.substring(index + 1, url.length()));
        }
        return portNumber;
    }

    private static String getHostFromUrl(String url) {
        int index = url.lastIndexOf(":");
        if (index > -1) {
            return url.substring(0, index);
        }
        return url;
    }

    private static String internalCheck(String url) {
        String noprotocoUrl = removeProtocol(url, getProtocol(url));
        String tmp = noprotocoUrl;
        if (!tmp.startsWith("//")) {
            tmp = "//" + tmp;
        }
        return tmp;
    }
}
