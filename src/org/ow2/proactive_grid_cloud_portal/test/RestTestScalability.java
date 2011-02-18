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

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;
import org.ow2.proactive.scripting.InvalidScriptException;


public class RestTestScalability {

    public static void main(String[] args) {
        try {

//            String restApiUrl = "http://dalek:8080/proactive_grid_cloud_portal/scheduler";
            String restApiUrl = "https://node0.cloud.sophia.inria.fr:8080/proactive_grid_cloud_portal/scheduler";
//            CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.setValue("pamr");
            PAMRConfig.PA_NET_ROUTER_ADDRESS.setValue("node0.cloud.sophia.inria.fr");
            PAMRConfig.PA_NET_ROUTER_PORT.setValue(8090);
            
            PAMRConfig.PA_PAMR_SOCKET_FACTORY.setValue("ssh");
            
            PAMRConfig.PA_PAMRSSH_KEY_DIR.setValue(System.getProperty("user.home") + "/.ssh/cloud");
            PAMRConfig.PA_PAMRSSH_REMOTE_USERNAME.setValue("ac");
            
//            String scheduler_url = "pamr://1/";
            String scheduler_url = "rmi://tagada.activeeon.com:1099/";
            TaskFlowJob job = new TaskFlowJob();
            job.setName("sparkle deploy");

            JavaTask rtask = new JavaTask();
            rtask.setName("deploy");
            rtask.setExecutableClassName(TaskReplicator.class.getName());
            FlowScript rep = FlowScript.createReplicateFlowScript("runs = 4");
            rtask.setFlowScript(rep);

            job.addTask(rtask);

            //create a Java Task with the default constructor
            JavaTask aTask = new JavaTask();
            //add executable class or instance
            aTask.setExecutableClassName(OneTestTask.class.getName());
            //then, set the desired options
            aTask.setName("Sparkle");
            //        aTask.setFlowBlock(FlowBlock.START);

            aTask.setDescription("perform some lookup on existing jobs in the scheduler");
            aTask.setRestartTaskOnError(RestartMode.ELSEWHERE);
            aTask.setCancelJobOnError(false);
            aTask.addArgument("rest_api_url", restApiUrl);
            aTask.addDependence(rtask);

            // add the task to the job
            job.addTask(aTask);

            JavaTask join = new JavaTask();
            join.setName("join");
            join.addDependence(aTask);
            join.setExecutableClassName(TaskReplicator.class.getName());

            job.addTask(join);

            JobEnvironment je = new JobEnvironment();
            je.setJobClasspath(new String[] { "/home/acontes/ws_git/proactive_grid_cloud_portal.git/build/classes" });
            //                    "/home/acontes/ws_git/proactive_grid_cloud_portal.git/WebContent/WEB-INF/lib"});
            job.setEnvironment(je);

            System.out.println("the job is ready, we now contact the scheduler");
            
            SchedulerProxyUserInterface s = PAActiveObject.newActive(SchedulerProxyUserInterface.class, null);

            s.init(scheduler_url, "demo", "demo");

            System.out.println("trying to submit the job");
            
            s.submit(job);


            System.out.println("job submitted");
            
        } catch (UserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidScriptException e) {
            // TODO Auto-generated catch block
            e.printStackTrace(); 
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
