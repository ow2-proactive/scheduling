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
package functionalTests.component.conform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import functionalTests.component.conform.components.BadSlaveMulticast;
import functionalTests.component.conform.components.Master;
import functionalTests.component.conform.components.MasterImpl;
import functionalTests.component.conform.components.Slave;
import functionalTests.component.conform.components.SlaveImpl;
import functionalTests.component.conform.components.SlaveMulticast;

import junit.framework.Assert;


public class TestMulticast extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected ProActiveTypeFactory ptf;
    protected GenericFactory gf;
    protected ComponentType tMaster;
    protected ComponentType tBadMaster;
    protected ComponentType tSlave;
    protected final static String serverMaster = "server/" +
        Master.class.getName() + "/false,false,false";
    protected final static String serverSlave = "server-multicast/" + PKG +
        ".Slave/false,false,false";
    protected final static String clientSlaveMulticast = MasterImpl.ITF_CLIENTE_MULTICAST +
        "/" + PKG + ".SlaveMulticast/true,false,false";

    // -------------------------------------------------------------------------
    // Constructor ans setup
    // -------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = Fractal.getTypeFactory(boot);
        ptf = (ProActiveTypeFactory) tf;
        gf = Fractal.getGenericFactory(boot);
        tMaster = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("server", Master.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                    ptf.createFcItfType(MasterImpl.ITF_CLIENTE_MULTICAST,
                        SlaveMulticast.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY,
                        ProActiveTypeFactory.MULTICAST_CARDINALITY)
                });
        tSlave = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("server-multicast",
                        Slave.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                });
    }

    // -------------------------------------------------------------------------
    // Test component instantiation
    // -------------------------------------------------------------------------
    @Test
    public void testPrimitiveWithMulticast() throws Exception {
        Component master = gf.newFcInstance(tMaster, "primitive",
                MasterImpl.class.getName());
        checkComponent(master,
            new HashSet<Object>(Arrays.asList(
                    new Object[] {
                        COMP, BC, LC, SC, NC, CP, MCC, GC, MC, serverMaster,
                        clientSlaveMulticast
                    })));
        Component slave = gf.newFcInstance(tSlave, "primitive",
                SlaveImpl.class.getName());
        checkComponent(slave,
            new HashSet<Object>(Arrays.asList(
                    new Object[] { COMP, LC, SC, NC, CP, MCC, GC, MC, serverSlave })));
    }

    @Test
    public void testCompositeWithMulticast() throws Exception {
        Component master = gf.newFcInstance(tMaster, "composite", null);
        checkComponent(master,
            new HashSet<Object>(Arrays.asList(
                    new Object[] {
                        COMP, BC, CC, LC, SC, NC, CP, MCC, GC, MC, serverMaster,
                        clientSlaveMulticast
                    })));
        Component slave = gf.newFcInstance(tSlave, "composite", null);
        checkComponent(slave,
            new HashSet<Object>(Arrays.asList(
                    new Object[] {
                        COMP, BC, CC, LC, SC, NC, CP, MCC, GC, MC, serverSlave
                    })));
    }

    @Test(expected = InstantiationException.class)
    public void testItfTypeWithBadMulticastItf() throws Exception {
        tBadMaster = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("server", Master.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                    ptf.createFcItfType("client-multicast",
                        BadSlaveMulticast.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY,
                        ProActiveTypeFactory.MULTICAST_CARDINALITY)
                });
    }

    // -------------------------------------------------------------------------
    // Test multicast interface with different dispatch of parameters
    // -------------------------------------------------------------------------
    @Test
    public void testRoundRobinParameterDispatch() throws Exception {
        Component master = gf.newFcInstance(tMaster, "primitive",
                MasterImpl.class.getName());
        Component slave1 = gf.newFcInstance(tSlave, "primitive",
                SlaveImpl.class.getName());
        Component slave2 = gf.newFcInstance(tSlave, "primitive",
                SlaveImpl.class.getName());

        Fractal.getBindingController(master)
               .bindFc(MasterImpl.ITF_CLIENTE_MULTICAST,
            slave1.getFcInterface("server-multicast"));
        Fractal.getBindingController(master)
               .bindFc(MasterImpl.ITF_CLIENTE_MULTICAST,
            slave2.getFcInterface("server-multicast"));

        Fractal.getLifeCycleController(master).startFc();
        Fractal.getLifeCycleController(slave1).startFc();
        Fractal.getLifeCycleController(slave2).startFc();

        Master masterItf = (Master) master.getFcInterface("server");

        List<List<String>> listOfParameters = generateParameter();
        for (List<String> stringList : listOfParameters) {
            masterItf.computeOneWay(stringList, "OneWay call");
        }
        for (List<String> stringList : listOfParameters) {
            List<StringWrapper> results = masterItf.computeAsync(stringList,
                    "Asynchronous call");
            ArrayList<String> resultsAL = new ArrayList<String>();
            for (StringWrapper sw : results) {
                Assert.assertNotNull("One result is null", sw);
                resultsAL.add(sw.stringValue());
            }
            checkResult(stringList, "Asynchronous call", resultsAL);
            System.err.println("TM: async call" + results);
        }
        for (List<String> stringList : listOfParameters) {
            //FIXME
            //List<GenericTypeWrapper<String>> results = masterItf.computeAsyncGenerics(stringList,
            //        "Asynchronous call");
            //System.err.println("TM: async gen call" + results);
        }
        for (List<String> stringList : listOfParameters) {
            //FIXME
            //List<String> results = masterItf.computeSync(stringList,
            //        "With non reifiable return type call");
            //System.err.println("TM: sync call" +  results);
        }
    }

    private static List<List<String>> generateParameter() {
        List<List<String>> multicastArgsList = new ArrayList<List<String>>();
        for (int i = 0; i < 6; i++) {
            multicastArgsList.add(i, new ArrayList<String>());

            for (int j = 0; j < i; j++) {
                multicastArgsList.get(i).add("arg " + j);
            }
        }
        return multicastArgsList;
    }

    private static void checkResult(List<String> args, String other,
        ArrayList<String> results) {
        Slave mySlave = new SlaveImpl();
        ArrayList<String> expectedResults = new ArrayList<String>(args.size());

        for (String string : args) {
            expectedResults.add(mySlave.computeSync(string, other));
        }
        Assert.assertEquals("Result aren't equals", expectedResults, results);
    }
}
