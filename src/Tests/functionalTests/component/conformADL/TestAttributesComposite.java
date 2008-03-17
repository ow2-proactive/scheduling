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
package functionalTests.component.conformADL;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.adl.FactoryFactory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.util.Fractal;

import functionalTests.ComponentTest;
import functionalTests.component.conformADL.components.CAttributes;


public class TestAttributesComposite extends ComponentTest {
    protected Factory factory;

    @Before
    public void setUp() throws Exception {
        factory = FactoryFactory.getFactory(FactoryFactory.FRACTAL_BACKEND);
    }

    // -----------------------------------------------------------------------------------
    // Full test
    // -----------------------------------------------------------------------------------
    @Test
    public void testCompositeWithAttributeController() throws Exception {

        // ----------------------------------------------------------        
        // Load the ADL definition
        // ----------------------------------------------------------        
        Component root = (Component) factory.newComponent(
                "functionalTests.component.conformADL.components.CAttributesComposite",
                new HashMap<Object, Object>());

        // ----------------------------------------------------------
        // Start the Root component
        // ----------------------------------------------------------
        Fractal.getLifeCycleController(root).startFc();

        // ----------------------------------------------------------
        // Call the attributes methods
        // ----------------------------------------------------------
        CAttributes ca = (CAttributes) root.getFcInterface("attribute-controller");
        ca.setX1(true);
        assertEquals(true, ca.getX1());
        ca.setX2((byte) 1);
        assertEquals((byte) 1, ca.getX2());
        ca.setX3((char) 1);
        assertEquals((char) 1, ca.getX3());
        ca.setX4((short) 1);
        assertEquals((short) 1, ca.getX4());
        ca.setX5(1);
        assertEquals(1, ca.getX5());
        ca.setX6(1);
        assertEquals((long) 1, ca.getX6());
        ca.setX7(1);
        assertEquals(1, ca.getX7(), 0);
        ca.setX8(1);
        assertEquals(1, ca.getX8(), 0);
        ca.setX9("1");
        assertEquals("1", ca.getX9());
    }

    // -----------------------------------------------------------------------------------
    // Test composite with content do not extends AttributeController
    // -----------------------------------------------------------------------------------
    @Test(expected = InstantiationException.class)
    @Ignore
    public void testCompositeWithContentError() throws Exception {
        // ----------------------------------------------------------        
        // Load the ADL definition
        // ----------------------------------------------------------        
        factory.newComponent("functionalTests.component.conformADL.components.CAttributesCompositeError",
                new HashMap<Object, Object>());

    }
}
