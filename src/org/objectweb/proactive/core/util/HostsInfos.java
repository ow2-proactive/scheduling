/*
 * Created on 25 juil. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.ProActiveConfiguration;


/**
 * @author rquilici
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

//the Unique instance of HostInfos
public class HostsInfos {
    private static HostsInfos hostsInfos = new HostsInfos();
    private static Logger logger = Logger.getLogger(HostsInfos.class.getName());
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
