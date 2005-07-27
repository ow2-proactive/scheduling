package nonregressiontest.component.interceptor;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

public class A implements FooItf, BindingController {
    FooItf b;
    
    public A() {
    }
    
    public void foo() {
        b.foo();
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals(FooItf.CLIENT_ITF_NAME)) {
            b = (FooItf) serverItf;
            //logger.debug("MotorImpl : added binding on a wheel");
        } else {
            throw new IllegalBindingException("no such binding is possible : client interface name does not match");
        }

    }

    public String[] listFc() {
        return null;
    }

    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        return null;
    }

    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
    }
    
    
}
