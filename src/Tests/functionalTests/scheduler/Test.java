/*
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

/**
 *
 *
 * @author walzouab
 *
 */
package functionalTests.scheduler;

import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.AdminScheduler;
import org.objectweb.proactive.extra.scheduler.Info;
import org.objectweb.proactive.extra.scheduler.ProActiveTask;
import org.objectweb.proactive.extra.scheduler.SchedulerUserAPI;
import org.objectweb.proactive.extra.scheduler.Status;
import org.objectweb.proactive.extra.scheduler.UserResult;
import org.objectweb.proactive.extra.scheduler.exception.UserException;
import org.objectweb.proactive.extra.scheduler.resourcemanager.SimpleResourceManager;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Launches the scheduler and adds deletes tasks then shutsdown
 */
public class Test extends FunctionalTest {
    private AdminScheduler adminAPI;
    private SchedulerUserAPI userAPI;
    private final String xmlURL = Test.class.getResource(
            "/functionalTests/scheduler/test.xml").getPath();
    private final String SNode = "//localhost/SCHEDULER_NODE";
    private String userName;
    private SimpleResourceManager rm;

    @org.junit.Test
    public void action() throws Exception {
        boolean sucessfulTryCatch = true;
        Vector<UserResult> results = null;
        Vector<Info> info = null;

        //test kill all running and getresult
        results = submit(2, 120);

        while (adminAPI.status(results.get(1).getTaskID()) != Status.RUNNNING)
            Thread.sleep(1000);
        adminAPI.killAllRunning();

        info = adminAPI.info_all();
        if (info.size() != 2) {
            throw new Exception("kill all running has a bug");
        }
        for (int i = 0; i < info.size(); i++) {
            if (info.get(i).getStatus() != Status.KILLED) {
                throw new Exception("kill all running has a bug");
            }
        }

        for (int i = 0; i < results.size(); i++) {
            try {
                sucessfulTryCatch = true;
                //since all are killed, 
                results.get(i).getResult();
                sucessfulTryCatch = false;
            } catch (Exception e) {
                //do nothing here , as a matter of fact it must enter here and do nothing
            }
            if (!sucessfulTryCatch) {
                throw new Exception("bug in get result");
            }
        }

        //test exceptions in the usercode
        A withException = new A();
        withException.setThrowException(true);
        UserResult result = userAPI.submit(withException, userName);
        while (!result.isFinished())
            Thread.sleep(1000);
        if (userAPI.status(result.getTaskID()) != Status.FINISHED) {
            throw new Exception("error exucting a task with exception");
        }

        try {
            sucessfulTryCatch = true;
            result.getResult();
            sucessfulTryCatch = false;
        } catch (java.lang.ArithmeticException e) { //this exception must be thrown
        }
        if (!sucessfulTryCatch) {
            throw new Exception("bug in exception handling mechanism");
        }

        //test flushquue, delete, normal getresult
        results = submit(7, 9);
        userAPI.del(results.get(6).getTaskID(), userName);
        adminAPI.flushqueue();
        results.get(0).getResult();
        results.get(1).getResult();
        results.get(2).getResult();
        results.get(3).getResult();
        for (int i = 4; i < results.size(); i++) {
            try {
                sucessfulTryCatch = true;
                //since all are killed, 
                results.get(i).getResult();
                sucessfulTryCatch = false;
            } catch (Exception e) {
                //do nothing here , as a matter of fact it must enter here and do nothing
            }
            if (!sucessfulTryCatch) {
                throw new Exception("bug in get result");
            }
        }

        //testin the start and stop
        results = submit(2, 4);
        results.get(0).getResult();
        ProActive.waitFor(adminAPI.stop());

        try {
            sucessfulTryCatch = true;
            results.get(1).getResult();
            sucessfulTryCatch = false;
        } catch (UserException e) { //this exception must be thrown
        }
        if (!sucessfulTryCatch) {
            throw new Exception("bug in exception handling mechanism");
        }

        ProActive.waitFor(adminAPI.start());

        if (userAPI.del(results.get(1).getTaskID(), "wrong id").booleanValue() == true) {
            throw new Exception("bug in delete");
        }

        if (userAPI.getResult(results.get(1).getTaskID(), "wrong id")
                       .getErrorMessage().equals("")) {
            throw new Exception("bug in getresult");
        }

        results.get(1).getResult();

        submit(5000, 1);

        assertTrue(adminAPI.info_all().size() == 5000);
    }

    @After
    public void endTest() throws Exception {
        BooleanWrapper shutDownResult = adminAPI.shutdown(new BooleanWrapper(
                    false));
        if (shutDownResult.booleanValue() == false) {
            throw new Exception("error shutting down the scheduler");
        }

        BooleanWrapper rmStopResult = rm.stopRM();
        if (rmStopResult.booleanValue() == false) {
            throw new Exception("error shutting down the resource manager");
        }
    }

    @Before
    public void initTest() throws Exception {
        //get the path of the file
        rm = (SimpleResourceManager) ProActive.newActive(SimpleResourceManager.class.getName(),
                null);
        adminAPI = AdminScheduler.createLocalScheduler(rm, SNode);
        rm.addNodes(xmlURL);
        userAPI = SchedulerUserAPI.connectTo(SNode);
        userName = System.getProperty("user.name");

        assertTrue(adminAPI.start().booleanValue());
    }

    Vector<UserResult> submit(int no, int sleepTime) throws Exception {
        Vector<ProActiveTask> tasks = new Vector<ProActiveTask>();
        for (int i = 0; i < no; i++) {
            A a = new A();
            a.setSleepTime(sleepTime);
            tasks.add(a);
        }
        return (userAPI.submit(tasks, userName));
    }
}
