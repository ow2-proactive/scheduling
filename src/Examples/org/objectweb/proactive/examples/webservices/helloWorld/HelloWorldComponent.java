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
package org.objectweb.proactive.examples.webservices.helloWorld;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.extensions.webservices.WebServices;


/**
 * A simple example to expose an active object as a web service.
 * A web server must be installed first to run this example
 * @author vlegrand
 */
public class HelloWorldComponent implements HelloWorldItf {
    public HelloWorldComponent() {
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.examples.webservices.helloWorld.HelloWorldItf#helloWorld()
     */
    public String helloWorld() {
        return "Hello world !";
    }

    public static void main(String[] args) {
        String url;
        if (args.length == 0) {
            url = "http://localhost:8080";
        } else {
            url = args[0];
        }
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        Component boot = null;
        Component comp = null;
        try {
            boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();

            TypeFactory tf = Fractal.getTypeFactory(boot);
            GenericFactory cf = Fractal.getGenericFactory(boot);

            // type of server component
            ComponentType sType = tf.createFcType(new InterfaceType[] { tf.createFcItfType("hello-world",
                    HelloWorldItf.class.getName(), false, false, false) });
            // create server component
            comp = cf.newFcInstance(sType, new ControllerDescription("server", Constants.PRIMITIVE),
                    new ContentDescription(HelloWorldComponent.class.getName()));
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }

        System.out.println("Deploy an hello world service on : " + url);

        WebServices.exposeComponentAsWebService(comp, url, "server");
    }
}
