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
import java.net.UnknownHostException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * TODO hostsTable should be HashTable<InetAddress, Hashtable<String, String>>
 * but the key "all" need to be handled separately since a null key is not allowed
 * inside a HashTable
 */

//the Unique instance of HostInfos
public class HostsInfos {
    private static HostsInfos hostsInfos = new HostsInfos();
    static Logger logger = ProActiveLogger.getLogger(Loggers.UTIL);
    private static Hashtable<String, Hashtable<String, String>> hostsTable;

    private HostsInfos() {
        ProActiveConfiguration.load();
        hostsTable = new Hashtable<String, Hashtable<String, String>>();
        hostsTable.put("all", new Hashtable<String, String>());
        loadProperties();
    }

    public static Hashtable getHostsInfos() {
        return hostsTable;
    }

    public static String hostnameToIP(String hostname) {
        if ((hostname == null) || hostname.equals("all")) {
            return hostname;
        }

        try {
            return InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException e) {
            return hostname;
        }
    }

    public static Hashtable getHostInfos(String _hostname) {
        String hostname = hostnameToIP(_hostname);
        return getHostInfos(hostname, true);
    }

    public static String getUserName(String _hostname) {
        String hostname = hostnameToIP(_hostname);
        Hashtable host_infos = getHostInfos(hostname, true);
        return (String) host_infos.get("username");
    }

    public static String getSecondaryName(String _hostname) {
        String hostname = hostnameToIP(_hostname);
        Hashtable host_infos = getHostInfos(hostname, true);
        String secondary = (String) host_infos.get("secondary");
        if (secondary != null) {
            return secondary;
        } else {
            return hostname;
        }
    }

    public static void setUserName(String _hostname, String username) {
        String hostname = hostnameToIP(_hostname);
        Hashtable<String, String> host_infos = getHostInfos(hostname, false);
        if (host_infos.get("username") == null) {
            host_infos.put("username", username);
            //        	try {
            //        		System.out.println(hostname+" --> "+username+ " on host "+URIBuilder.getLocalAddress().getHostName());
            //        	} catch (UnknownHostException e) {
            //        		// TODO Auto-generated catch block
            //        		e.printStackTrace();
            //        	}
        }
    }

    public static void setSecondaryName(String _hostname, String secondary_name) {
        String hostname = hostnameToIP(_hostname);
        Hashtable<String, String> host_infos = getHostInfos(hostname, false);
        if (host_infos.get("secondary") == null) {
            host_infos.put("secondary", secondary_name);
        }
    }

    protected static Hashtable<String, String> getHostInfos(String _hostname,
        boolean common) {
        String hostname = hostnameToIP(_hostname);
        Hashtable<String, String> host_infos = findHostInfos(hostname);
        if (host_infos == null) {
            if (common) {
                return hostsTable.get("all");
            } else {
                host_infos = new Hashtable<String, String>();
                hostsTable.put(hostname, host_infos);
                return host_infos;
            }
        }
        return host_infos;
    }

    /**
     *
     */
    final static String REGEXP_KEYVAL = "(([0-9]{1,3}\\.){3}[0-9]{1,3}:([0-9]{1,3}\\.){3}[0-9]{1,3})";

    private void loadProperties() {
        String userNames = PAProperties.PA_SSH_USERNAME.getValue();

        if (userNames == null) {
            userNames = System.getProperty("user.name");
        }
        parseUserNames(userNames);

        String secondaryNames = PAProperties.PA_SECONDARYNAMES.getValue();
        if (secondaryNames != null) {
            if (secondaryNames.matches(REGEXP_KEYVAL + "(," + REGEXP_KEYVAL +
                        ")?")) {
                for (String keyval : secondaryNames.split(",")) {
                    String[] tmp = keyval.split(":");
                    setSecondaryName(tmp[0], tmp[1]);
                }
            } else {
                logger.error("Invalid value for proactive.secondaryNames: " +
                    secondaryNames);
            }
        }
    }

    /**
     * @param userNames
     */
    private void parseUserNames(String userNames) {
        String[] unames = userNames.split(";");
        for (int i = 0; i < unames.length; i++) {
            int index = unames[i].indexOf("@");
            if (index < 0) {
                if (unames.length > 1) {
                    logger.error(
                        "ERROR: malformed usernames. Should be username1@host1;username2@host2");
                } else {
                    setUserName("all", unames[0]);
                }
            } else {
                String username = unames[i].substring(0, index);
                String hostname = unames[i].substring(index + 1,
                        unames[i].length());
                setUserName(hostname, username);
            }
        }
    }

    private static Hashtable<String, String> findHostInfos(String _hostname) {
        String hostname = hostnameToIP(_hostname);
        Hashtable<String, String> host_infos = hostsTable.get(hostname);
        if (host_infos == null) {
            try {
                //we have to identify the mapping between host and IP
                //we try with the canonical hostname
                String host = InetAddress.getByName(hostname)
                                         .getCanonicalHostName();
                if (!hostsTable.containsKey(host)) {
                    //we try with the IP
                    host = InetAddress.getByName(hostname).getHostAddress();
                }
                return hostsTable.get(host);
            } catch (UnknownHostException e) {
                //same as return null
                return host_infos;
            }
        }
        return host_infos;
    }

    public static void main(String[] args) {
        System.out.println(HostsInfos.getUserName("138.96.90.12"));
    }
}
