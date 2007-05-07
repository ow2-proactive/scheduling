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

import org.objectweb.proactive.extra.scheduler.ProActiveTask;

public class A implements ProActiveTask {

	private int sleepTime=0;
	private boolean throwException=false;
	
	
	 //PA noArg contructor
    public A(){}
	public Object run() {
        if(throwException) {int a=2;int b=1; b--;a=a/b;}
        	
		String message;
        try {
            message = java.net.InetAddress.getLocalHost().toString();
            Thread.sleep(sleepTime * 1000);
        } catch (Exception e) {
            message = "crashed";
            e.printStackTrace();
        }

        return (" hi from " + message + "\t slept for " +
        sleepTime + "Seconds");
    }
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}

}
