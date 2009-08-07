/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive.resourcemanager.utils;

import java.io.StringReader;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;


/**
 * PAAgentServiceRMStarter wrapper for EC2
 * <p>
 * All the work done in this small launcher class should be done with scripts and not hard java
 * code. Such a class is useful however on systems with very limited scripting capacities, like
 * Windows Server 2003, where launching a Java program as a Service at boot is a complex task that
 * is more easily done with a full java code than with a batch script that runs the actual java
 * code.
 * 
 * 
 * @author ProActive team
 * @since ProActive Scheduling 1.0
 * 
 */
public class WindowsEC2RMStarter {

    private static final String userData = "http://169.254.169.254/1.0/user-data";
    private static final String instanceType = "http://169.254.169.254/2009-04-04/meta-data/instance-type";
    private static final String publicIp = "http://169.254.169.254/2009-04-04/meta-data/public-ipv4";

    /**
     * 
     * Starts a new node that will register to a specific RM.
     * <p>
     * Credentials are extracted from EC2 user-data
     * 
     * @param args
     *            <ul>
     *            <li> args[0] : path to the ProActive Scheduler installation
     *            </ul>
     */
    public static void main(final String args[]) {
        try {
            if (args.length != 1) {
                throw new Exception("You need to specify the PA_SCHEDULER Home as parameter");
            }

            System.setProperty("proactive.home", args[0]);
            System.setProperty("pa.scheduler.home", args[0]);
            System.setProperty("pa.rm.home", args[0]);

            String rmUser = "", rmPass = "", rmUrl = "", rmNodeName = "", rmNsName = "";

            Properties props = new Properties();
            StringReader in = new StringReader(getUrl(userData));
            props.load(in);

            rmUser = props.getProperty("rmLogin");
            rmPass = props.getProperty("rmPass");
            rmUrl = props.getProperty("rmUrl");
            rmNodeName = getNodeName();
            rmNsName = props.getProperty("nodeSource");

            int port = 0;
            try {
                String paPort = rmUrl.split(":")[2].replace("/", "");
                port = Integer.parseInt(paPort);
            } catch (Exception e) {
                throw new Exception("Could not find port number in url: " + rmUrl, e);
            }

            System.setProperty("proactive.communication.protocol", "http");
            System.setProperty("proactive.http.port", "" + port);
            System.setProperty("proactive.hostname", getUrl(publicIp));

            PAAgentServiceRMStarter.main(new String[] { rmUser, rmPass, rmUrl, rmNodeName, rmNsName });

        } catch (Exception e) {
            System.out.println("Exception caught: " + e);
        }
    }

    /**
     * Returns the result of an HTTP GET request from a particular URL
     * 
     * @param url
     *            the url to get
     * @return the result of the get
     * @throws Exception
     */
    public static String getUrl(String url) throws Exception {
        HttpClient cli = new HttpClient();
        GetMethod get = new GetMethod(url);
        try {
            cli.executeMethod(get);
            return get.getResponseBodyAsString();
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * @return a pseudo random name for the new node
     * @throws Exception
     */
    public static String getNodeName() throws Exception {
        Random rand = new Random();
        String rs = "", alpha = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < 16; i++) {
            rs += alpha.charAt(rand.nextInt(alpha.length()));
        }
        String type = getUrl(instanceType).replace('.', '-');
        return "EC2-" + type + "-" + rs;
    }
}
