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
package functionalTests.component.immediateservice;

import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class A implements Itf, ComponentInitActive {
    private boolean condition = true;

    /**
     * Initialize the immediate service method in the initComponentActivity
     */
    public void initComponentActivity(Body body) {
        ProActiveObject.setImmediateService("immediateMethod",
            new Class[] { String.class });
        //        ProActive.setImmediateService("startFc",
        //                new Class[] { });
        ProActiveObject.setImmediateService("immediateStopLoopMethod");
        //ProActive.setImmediateService("startFc");
    }

    public StringWrapper immediateMethod(String arg) {
        System.err.println("COMPONENT: immediateMethod: " + arg);
        StringWrapper res = new StringWrapper(arg + "success");
        condition = false;
        return res;
    }

    /**
     * This method never terminate, unless that the immediateStopLoopMethod (an immediate service) is called.
     */
    public void loopQueueMethod() {
        System.err.println("COMPONENT: loopQueueMethod: BEGINNING");
        while (condition)
            ;
        System.err.println("COMPONENT: loopQueueMethod: END");
    }

    public void immediateStopLoopMethod() {
        System.err.println("COMPONENT: immediateStopLoopMethod");
        condition = false;
    }

    public void startFc() throws IllegalLifeCycleException {
        System.err.println("MY_startFc");
    }
}
