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
package functionalTests;

import org.junit.BeforeClass;
import org.objectweb.proactive.core.config.PAProperties;

import functionalTests.FunctionalTest;


/**
 * @author The ProActive Team
 *
 */
public abstract class ComponentTest extends FunctionalTest {

    /**
     * @param name
     */
    public ComponentTest() {
        //  super("[COMPONENTS] " + name);
    }

    /**
     * @param name
     * @param description
     */
    public ComponentTest(String name, String description) {
        //  super("Components : " + name, description);
    }

    @BeforeClass
    public static void componentPreConditions() throws Exception {
        if (!PAProperties.PA_FUTURE_AC.isTrue()) {
            throw new Exception(
                "The components framework needs the automatic continuations (system property 'proactive.future.ac' set to 'enable') to be operative");
        }

        //-Dfractal.provider=org.objectweb.proactive.core.component.Fractive
        PAProperties.FRACTAL_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
    }
}
