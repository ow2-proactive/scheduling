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
package functionalTests.stub.abstractclass;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.URIBuilder;

import functionalTests.FunctionalTest;


/**
 * Stub generation on abstract classes
 */
public class Test extends FunctionalTest {
    String stubClassName;
    byte[] data;

    @org.junit.Test
    public void action() throws Exception {
        Factory f = (Factory) PAActiveObject.newActive(Factory.class.getName(),
                new Object[] {  });
        PAActiveObject.register(f,
            URIBuilder.buildURIFromProperties("localhost", "myFactory")
                      .toString());

        Factory factory = (Factory) PAActiveObject.lookupActive(Factory.class.getName(),
                URIBuilder.buildURIFromProperties("localhost", "myFactory")
                          .toString());
        AbstractClass abstractClass = factory.getWidget(NodeFactory.getDefaultNode());
        abstractClass.foo();
        abstractClass.bar();
        abstractClass.gee();
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
