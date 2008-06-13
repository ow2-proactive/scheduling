package functionalTests.component.collectiveitf.unicast;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class RunnerImpl implements RunnerItf, org.objectweb.fractal.api.control.BindingController {

    public static final String PARAMETER_2 = "parameter 2";
    public static final String PARAMETER_1 = "parameter 1";
    RequiredService services;

    public boolean runTest() {

        // run unicast test

        List<String> parameters = new ArrayList<String>();
        parameters.add(RunnerImpl.PARAMETER_1);
        //		parameters.add("parameter 2");

        // first dispatch
        Assert.assertEquals("server " + 1 + " received parameter 1", services.method1(parameters)
                .stringValue());

        parameters.clear();
        parameters.add(RunnerImpl.PARAMETER_2);

        // second dispatch
        Assert.assertEquals("server 2 received parameter 2", services.method1(parameters).stringValue());

        // has been executed
        return true;
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals("requiredServiceItf")) {
            services = (RequiredService) serverItf;
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
        if ("requiredServiceItf".equals(clientItfName)) {
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
