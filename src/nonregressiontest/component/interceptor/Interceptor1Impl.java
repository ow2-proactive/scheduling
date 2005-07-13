package nonregressiontest.component.interceptor;

import nonregressiontest.component.controller.DummyController;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.interception.Interceptor;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;


/**
 * @author Matthieu Morel
 *
 */
public class Interceptor1Impl extends AbstractProActiveController
    implements Interceptor1, Interceptor {
    private int beforeInvocationCounter = 0;
    private int afterInvocationCounter = 0;

    /**
     * @param owner
     */
    public Interceptor1Impl(Component owner) {
        super(owner);
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(Interceptor1.INTERCEPTOR1_NAME,
                    Interceptor1.class.getName(), TypeFactory.SERVER,
                    TypeFactory.MANDATORY, TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " +
                this.getClass().getName());
        }
    }

    public void setDummyValue(String value) {
        try {
            ((DummyController) getFcItfOwner().getFcInterface(DummyController.DUMMY_CONTROLLER_NAME)).setDummyValue(value);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }
    }

    public String getDummyValue() {
        try {
            return ((DummyController) getFcItfOwner().getFcInterface(DummyController.DUMMY_CONTROLLER_NAME)).getDummyValue();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void afterMethodInvocation() {
        //System.out.println("after method invocation");
        setDummyValue(getDummyValue() + Test.SEPARATOR + Test.AFTER_1);
        afterInvocationCounter++;
    }

    public void beforeMethodInvocation() {
        //        System.out.println("before method invocation");
        setDummyValue(getDummyValue() + Test.SEPARATOR + Test.BEFORE_1);
        beforeInvocationCounter++;
    }
}
