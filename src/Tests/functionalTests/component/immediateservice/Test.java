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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;

import functionalTests.ComponentTest;


/**
 * Test immediate service with component.
 */
public class Test extends ComponentTest {

    /**
         *
         */
    private static final long serialVersionUID = 4191411789650566643L;

    /**
     * Use immediate service while the loopQueueMethod is serving.
     */
    @org.junit.Test
    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);

        Component comp = cf.newFcInstance(type_factory.createFcType(
                    new InterfaceType[] {
                        type_factory.createFcItfType("itf",
                            Itf.class.getName(), false, false, false)
                    }),
                new ControllerDescription("component", Constants.PRIMITIVE),
                new ContentDescription(A.class.getName(), new Object[] {  }));

        Fractal.getLifeCycleController(comp).startFc();
        // first execute an infinite loop in the component activity
        ((Itf) comp.getFcInterface("itf")).loopQueueMethod();
        // call an immediate service: it is executed concurrently with the activity 
        System.err.println("MAIN: result is '" +
            ((Itf) comp.getFcInterface("itf")).immediateMethod("a ") + "'");
        //Fractal.getLifeCycleController(comp).startFc();
        // call an immediate service to set the condition false and thus terminate the loopQueueMethod
        ((Itf) comp.getFcInterface("itf")).immediateStopLoopMethod();
        //Fractal.getLifeCycleController(comp).stopFc();
    }
}
