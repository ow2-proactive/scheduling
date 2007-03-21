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


package nonregressiontest.scheduler;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.scheduler.AdminScheduler;
import org.objectweb.proactive.extra.scheduler.SchedulerUserAPI;
import org.objectweb.proactive.extra.scheduler.resourcemanager.SimpleResourceManager;

import testsuite.test.FunctionalTest;



public class Test extends FunctionalTest {

	private AdminScheduler adminAPI;
	private SchedulerUserAPI userAPI;
	private final String xmlURL = Test.class.getResource("/nonregressiontest/scheduler/testDeployment.xml").getPath();
	private final String SNode="//localhost/SCHEDULER_NODE";
	private SimpleResourceManager rm;
	public Test() {
		super("Scheduler","Launches the scheduler and adds deletes tasks then shutsdown");
	}





	public void action() throws Exception {

		userAPI=SchedulerUserAPI.connectTo(SNode);
	}


	public void endTest() throws Exception {
		adminAPI.shutdown(new BooleanWrapper(true));
		rm.stopRM();

	}


	public void initTest() throws Exception {
		//get the path of the file
		
		rm=(SimpleResourceManager)ProActive.newActive(SimpleResourceManager.class.getName(),null);
		adminAPI=AdminScheduler.createLocalScheduler(rm,SNode);
        rm.addNodes(xmlURL);
		
	}
	public boolean preConditions() throws Exception {return adminAPI.start().booleanValue();	}
	public boolean postConditions() throws Exception {return true;	}	
	public static void main(String[] args) {
        Test test = new Test();
        
        try
        {
        	test.initTest();
        	test.action();
        	test.endTest();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}


}
