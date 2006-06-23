/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
import java.net.UnknownHostException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author rquilici
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

//the Unique instance of HostInfos
public class HostsInfos {
    private static HostsInfos hostsInfos = new HostsInfos();
    static Logger logger = ProActiveLogger.getLogger(Loggers.UTIL);
    private static Hashtable hostsTable;

    private HostsInfos() {
        ProActiveConfiguration.load();
        hostsTable = new Hashtable();
        hostsTable.put("all", new Hashtable());
        loadProperties();
    }

    public static Hashtable getHostsInfos() {
        return hostsTable;
    }

    public static Hashtable getHostInfos(String hostname) {
        return getHostInfos(hostname, true);
    }

    public static String getUserName(String hostname) {
        Hashtable host_infos = getHostInfos(hostname, true);
        return (String) host_infos.get("username");
    }

    public static String getSecondaryName(String hostname) {
        Hashtable host_infos = getHostInfos(hostname, true);
        String secondary = (String) host_infos.get("secondary");
        if (secondary != null) {
            return secondary;
        } else {
            return hostname;
        }
    }

    public static void setUserName(String hostname, String username) {
        Hashtable host_infos = getHostInfos(hostname, false);
        if (host_infos.get("username") == null) {
            host_infos.put("username", username);
            //        	try {
            //        		System.out.println(hostname+" --> "+username+ " on host "+InetAddress.getLocalHost().getHostName());
            //        	} catch (UnknownHostException e) {
            //        		// TODO Auto-generated catch block
            //        		e.printStackTrace();
            //        	}
        }
    }

    public static void setSecondaryName(String hostname, String secondary_name) {
        Hashtable host_infos = getHostInfos(hostname, false);
        if (host_infos.get("secondary") == null) {
            host_infos.put("secondary", secondary_name);
        }
    }

    protected static Hashtable getHostInfos(String hostname, boolean common) {
        Hashtable host_infos = (Hashtable) findHostInfos(hostname);
        if (host_infos == null) {
            if (common) {
                return (Hashtable) hostsTable.get("all");
            } else {
                host_infos = new Hashtable();
                hostsTable.put(hostname, host_infos);
                return host_infos;
            }
        }
        return host_infos;
    }

    /**
     *
     */
    private void loadProperties() {
        String userNames = System.getProperty("proactive.ssh.username");

        if (userNames == null) {
            userNames = System.getProperty("user.name");
        }
        parseUserNames(userNames);
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

    private static Hashtable findHostInfos(String hostname) {
        Hashtable host_infos = (Hashtable) hostsTable.get(hostname);
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
                return (Hashtable) hostsTable.get(host);
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
