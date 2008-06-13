package functionalTests.component.collectiveitf.reduction.primitive;

import junit.framework.Assert;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class ReductionImpl implements RunnerItf, org.objectweb.fractal.api.control.BindingController {
    Reduction services;

    public boolean runTest() {

        // run unicast test

        IntWrapper rval = services.doIt();
        Assert.assertEquals(new IntWrapper(30), rval);

        rval = services.doItInt(new IntWrapper(100));
        Assert.assertEquals(new IntWrapper(230), rval);

        services.voidDoIt();

        // has been executed
        return true;
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals("mcast")) {
            services = (Reduction) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("mcast".equals(clientItfName)) {
            return services;
        }
        throw new NoSuchInterfaceException(clientItfName);
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        throw new RuntimeException("not implemented");
    }
}
