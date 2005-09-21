/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.examples.components.c3d;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.examples.c3d.Dispatcher;
import org.objectweb.proactive.examples.c3d.RenderingEngine;


/**
 * This example is a C3D Component version.
 */
public class Main {
    public static void main(final String[] args) throws Exception {
        Component boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();
        TypeFactory tf = Fractal.getTypeFactory(boot);
        Component rootComp = null;

        // type of root component
        ComponentType rootType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("root2user", Runnable.class.getName(),
                        false, false, false)
                });

        // type of user component
        ComponentType userType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("root2user", Runnable.class.getName(),
                        false, false, false),
                    tf.createFcItfType("user2dispatcher",
                        Dispatcher.class.getName(), true, false, false),
                });

        // type of dispatcher component
        ComponentType dispatcherType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("user2dispatcher",
                        Dispatcher.class.getName(), false, false, false),
                    tf.createFcItfType("attribute-controller",
                        DispatcherAttributes.class.getName(), false, false,
                        false),
                    tf.createFcItfType("dispatcher2engine",
                        RenderingEngine.class.getName(), true, false, false),
                });

        // type of engine component
        ComponentType engineType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("dispatcher2engine",
                        RenderingEngine.class.getName(), false, false, false),
                    tf.createFcItfType("attribute-controller",
                        EngineAttributes.class.getName(), false, false, false),
                });

        GenericFactory cf = Fractal.getGenericFactory(boot);

        // create root component
        rootComp = cf.newFcInstance(rootType,
                new ControllerDescription("root", Constants.COMPOSITE), null);

        // create user component
        Component userComp = cf.newFcInstance(userType,
                new ControllerDescription("user", Constants.PRIMITIVE),
                new ContentDescription(UserImpl.class.getName())); // other properties could be added (activity for example)

        // create dispatcher component
        Component dispatcherComp = cf.newFcInstance(dispatcherType,
                new ControllerDescription("dispatcher", Constants.PRIMITIVE),
                //new ContentDescription(DispatcherImpl.class.getName(), parameters));
            new ContentDescription(DispatcherImpl.class.getName()));

        // create engine component
        Component engineComp1 = cf.newFcInstance(engineType,
                new ControllerDescription("engine", Constants.PRIMITIVE),
                new ContentDescription(EngineImpl.class.getName()));
        Component engineComp2 = cf.newFcInstance(engineType,
                new ControllerDescription("engine", Constants.PRIMITIVE),
                new ContentDescription(EngineImpl.class.getName()));
        Component engineComp3 = cf.newFcInstance(engineType,
                new ControllerDescription("engine", Constants.PRIMITIVE),
                new ContentDescription(EngineImpl.class.getName()));

        // non functionnal interface, ie change "constants" in the component 
        //        ((DispatcherAttributes) Fractal.getAttributeController(dispatcherComp)).setCount(1);
        // component assembly
        Fractal.getContentController(rootComp).addFcSubComponent(userComp);
        Fractal.getContentController(rootComp).addFcSubComponent(dispatcherComp);
        Fractal.getContentController(rootComp).addFcSubComponent(engineComp1);
        Fractal.getContentController(rootComp).addFcSubComponent(engineComp2);
        Fractal.getContentController(rootComp).addFcSubComponent(engineComp3);
        Fractal.getBindingController(rootComp).bindFc("root2user",
            userComp.getFcInterface("root2user"));
        Fractal.getBindingController(userComp).bindFc("user2dispatcher",
            dispatcherComp.getFcInterface("user2dispatcher"));
        Fractal.getBindingController(dispatcherComp).bindFc("dispatcher2engine1",
            engineComp1.getFcInterface("dispatcher2engine"));
        Fractal.getBindingController(dispatcherComp).bindFc("dispatcher2engine2",
            engineComp2.getFcInterface("dispatcher2engine"));
        Fractal.getBindingController(dispatcherComp).bindFc("dispatcher2engine3",
            engineComp3.getFcInterface("dispatcher2engine"));

        // start root component
        Fractal.getLifeCycleController(rootComp).startFc();

        // call main method
        ((Runnable) rootComp.getFcInterface("root2user")).run();
        
    }
}
