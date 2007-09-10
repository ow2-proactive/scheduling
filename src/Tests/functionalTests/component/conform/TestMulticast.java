package functionalTests.component.conform;

import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;

import functionalTests.component.binding.SlaveImpl;
import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.I;
import functionalTests.component.conform.components.MasterImpl;
import functionalTests.component.conform.components.Slave;
import functionalTests.component.conform.components.SlaveMulticast;


public class TestMulticast extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected ProActiveTypeFactory ptf;
    protected GenericFactory gf;
    protected ComponentType tMaster;
    protected ComponentType tSlave;
    protected final static String serverRun = "server/" +
        Runnable.class.getName() + "/false,false,false";
    protected final static String serverSlave = "server-multicast/" + PKG +
        ".Slave/false,false,false";
    protected final static String clientSlaveMulticast = "client-multicast/" +
        PKG + ".SlaveMulticast/true,false,false";

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
                    tf.createFcItfType("server", Runnable.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                    ptf.createFcItfType("client-multicast",
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
    public void testPrimitiveWithCollection() throws Exception {
        Component master = gf.newFcInstance(tMaster, "primitive",
                MasterImpl.class.getName());
        checkComponent(master,
            new HashSet(Arrays.asList(
                    new Object[] {
                        COMP, BC, LC, SC, NC, CP, MCC, GC, MC, serverRun,
                        clientSlaveMulticast
                    })));
        Component slave = gf.newFcInstance(tSlave, "primitive",
                SlaveImpl.class.getName());
        checkComponent(slave,
            new HashSet(Arrays.asList(
                    new Object[] { COMP, LC, SC, NC, CP, MCC, GC, MC, serverSlave })));
    }

    @Test
    public void testCompositeWithCollection() throws Exception {
        Component master = gf.newFcInstance(tMaster, "composite", null);
        checkComponent(master,
            new HashSet(Arrays.asList(
                    new Object[] {
                        COMP, BC, CC, LC, SC, NC, CP, MCC, GC, MC, serverRun,
                        clientSlaveMulticast
                    })));
        Component slave = gf.newFcInstance(tSlave, "composite", null);
        checkComponent(slave,
            new HashSet(Arrays.asList(
                    new Object[] {
                        COMP, BC, CC, LC, SC, NC, CP, MCC, GC, MC, serverSlave
                    })));
    }
}
