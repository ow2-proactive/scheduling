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
package org.objectweb.proactive.examples.components.helloworld;

/***
 *
 * Author: Eric Bruneton
 * Modified by: Matthieu Morel
 */
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.Launcher;


/**
 * This example is a mix from the helloworld examples in the Fractal distribution : the example from Julia, and the one from the FractalADL.<br>
 * The differences are the following : <br>
 *     - from FractalADL : <br>
 *             * this one uses a custom parser, based on the standard FractalADL, but it is able to add cardinality to virtual nodes and
 * allows the composition of virtual nodes.<br>
 *             * there are 4 .fractal files corresponding to definitions of the system in the current vm, in distributed vms (this uses
 * the ProActive deployment capabilities), a version with wrapping composite components and a version without wrapping components.
 *
 * Use the "parser" parameter to make it work.<br>
 *     - from Julia :
 *
 *
 * Sections involving templates have been removed, because this implementation does not provide templates. <br>
 * A functionality offered by ProActive is the automatic deployment of components onto remote locations.<br>
 * TODO change comment
 * When using the "distributed" option with the "parser" option, the ADL loader will load the "helloworld-distributed.xml" ADL,
 * which affects virtual nodes to components, and the "deployment.xml" file, which maps the virtual nodes to real nodes.<br>
 * If other cases, all components are instantiated locally, in the current virtual machine. <br>
 *
 *
 */
public class HelloWorld {
    public static void main(final String[] args) throws Exception {
        boolean useParser = false;
        boolean useTemplates = false;
        boolean useWrapper = false;
        boolean distributed = false;

        for (int i = 0; i < args.length; ++i) {
            useParser |= args[i].equals("parser");
            useTemplates |= args[i].equals("templates");
            useWrapper |= args[i].equals("wrapper");
            distributed |= args[i].equals("distributed");
        }

        if (useParser) {
            //      // -------------------------------------------------------------------
            //      // OPTION 1 : USE THE (custom) FRACTAL ADL
            //      // -------------------------------------------------------------------
            String arg0 = "-fractal"; // using the fractal component model
            String arg1; // which component definition to load
            String arg2 = "r";
            String arg3 = HelloWorld.class.getResource(
                    "/org/objectweb/proactive/examples/components/helloworld/deployment.xml").toString(); // the deployment descriptor for proactive

            if (distributed) {
                if (useWrapper) {
                    arg1 = "org.objectweb.proactive.examples.components.helloworld.helloworld-distributed-wrappers";
                } else {
                    arg1 = "org.objectweb.proactive.examples.components.helloworld.helloworld-distributed-no-wrappers";
                }
            } else {
                if (useWrapper) {
                    arg1 = "org.objectweb.proactive.examples.components.helloworld.helloworld-local-wrappers";
                } else {
                    arg1 = "org.objectweb.proactive.examples.components.helloworld.helloworld-local-no-wrappers";
                }
            }
            Launcher.main(new String[] { arg0, arg1, arg2, arg3 });
        } else {
            // -------------------------------------------------------------------
            // OPTION 2 : DO NOT USE THE FRACTAL ADL
            // -------------------------------------------------------------------
            Component boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();
            TypeFactory tf = Fractal.getTypeFactory(boot);
            Component rComp = null;

            // type of root component
            ComponentType rType = tf.createFcType(new InterfaceType[] { tf.createFcItfType("r",
                    Runnable.class.getName(), false, false, false) });

            // type of client component
            ComponentType cType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("r", Runnable.class.getName(), false, false, false),
                    tf.createFcItfType("s", Service.class.getName(), true, false, false) });

            // type of server component
            ComponentType sType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("s", Service.class.getName(), false, false, false),
                    tf.createFcItfType("attribute-controller", ServiceAttributes.class.getName(), false,
                            false, false) });

            GenericFactory cf = Fractal.getGenericFactory(boot);

            if (!useTemplates) {
                // -------------------------------------------------------------------
                // OPTION 2.1 : CREATE COMPONENTS DIRECTLY
                // -------------------------------------------------------------------
                // create root component
                rComp = cf.newFcInstance(rType, new ControllerDescription("root", Constants.COMPOSITE), null);
                // create client component
                Component cComp = cf.newFcInstance(cType, new ControllerDescription("client",
                    Constants.PRIMITIVE), new ContentDescription(ClientImpl.class.getName())); // other properties could be added (activity for example)

                // create server component
                Component sComp = cf.newFcInstance(sType, new ControllerDescription("server",
                    Constants.PRIMITIVE), new ContentDescription(ServerImpl.class.getName()));

                ((ServiceAttributes) Fractal.getAttributeController(sComp)).setHeader("--------> ");
                ((ServiceAttributes) Fractal.getAttributeController(sComp)).setCount(1);

                if (useWrapper) {
                    sType = tf.createFcType(new InterfaceType[] { tf.createFcItfType("s", Service.class
                            .getName(), false, false, false) });
                    // create client component "wrapper" component
                    Component CComp = cf.newFcInstance(cType, new ControllerDescription("client-wrapper",
                        Constants.COMPOSITE), null);

                    // create server component "wrapper" component
                    Component SComp = cf.newFcInstance(sType, new ControllerDescription("server-wrapper",
                        Constants.COMPOSITE), null);

                    // component assembly
                    Fractal.getContentController(CComp).addFcSubComponent(cComp);
                    Fractal.getContentController(SComp).addFcSubComponent(sComp);
                    Fractal.getBindingController(CComp).bindFc("r", cComp.getFcInterface("r"));
                    Fractal.getBindingController(cComp).bindFc("s",
                            Fractal.getContentController(CComp).getFcInternalInterface("s"));
                    //Fractal.getBindingController(cComp).bindFc("s", CComp.getFcInterface("s"));
                    Fractal.getBindingController(SComp).bindFc("s", sComp.getFcInterface("s"));
                    // replaces client and server components by "wrapper" components
                    // THIS CHANGES REFERENCES (STUBS)
                    cComp = CComp;
                    sComp = SComp;
                }

                // component assembly
                Fractal.getContentController(rComp).addFcSubComponent(cComp);
                Fractal.getContentController(rComp).addFcSubComponent(sComp);
                Fractal.getBindingController(rComp).bindFc("r", cComp.getFcInterface("r"));
                Fractal.getBindingController(cComp).bindFc("s", sComp.getFcInterface("s"));
            }

            // start root component
            Fractal.getLifeCycleController(rComp).startFc();

            // call main method
            ((Runnable) rComp.getFcInterface("r")).run();
        }
    }
}
