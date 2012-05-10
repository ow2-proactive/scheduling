/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.test;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;


public class OneClient {

    public OneClient() {
    }

    public long test(URI uri) throws HttpException, IOException {

        long result;

        // login

        PostMethod methodLogin = new PostMethod(uri.toString() + "/login");
        methodLogin.addParameter("username", "demo");
        methodLogin.addParameter("password", "demo");

        HttpClient client = new HttpClient();
        String sessionId = "";

        client.executeMethod(methodLogin);
        sessionId = methodLogin.getResponseBodyAsString();
        //        System.out.println(sessionId);

        GetMethod method = new GetMethod(uri.toString() + "/state");
        method.addRequestHeader("sessionid", sessionId);
        client = new HttpClient();
        client.executeMethod(method);
        result = method.getResponseBodyAsString().length();
        System.out.println(method.getResponseBodyAsString());

        PutMethod dismethod = new PutMethod(uri.toString() + "/disconnect");
        dismethod.addRequestHeader("sessionid", sessionId);
        client = new HttpClient();
        client.executeMethod(dismethod);
        //        System.out.println(method.getResponseBodyAsString());

        return result;
        /*
         * SchedulerRestInterface proxy = ProxyFactory.create(SchedulerRestInterface.class,
         * uri.toString());
         * 
         * try { String sessionId = proxy.login("demo", "demo");
         * 
         * List<String> jobs = proxy.jobs(sessionId, -1, -1);
         * 
         * // select a random jobs
         * 
         * int index = new SecureRandom().nextInt(jobs.size());
         * 
         * if (index < 0) { System.out.println("no jobs found, aborting"); return; }
         * 
         * String jobId = jobs.get(index);
         * 
         * // get the elected job
         * 
         * List<String> tasknames = proxy.getJobTasksIds(sessionId, jobId);
         * 
         * int taskindex = new SecureRandom().nextInt(tasknames.size());
         * 
         * System.out.println(proxy.tasklog(sessionId, jobId, tasknames.get(taskindex)));
         * 
         * } catch (Exception e) { // TODO: handle exception e.printStackTrace(); }
         */
    }

    public static void main(String[] args) {
        try {
            new OneClient().test(URI.create("http://localhost:8080/proactive_grid_cloud_portal/scheduler"));
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
