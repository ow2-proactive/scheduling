package nonregressiontest.component.interceptor;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.interception.InputInterceptor;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.mop.MethodCall;

import nonregressiontest.component.controller.DummyController;


/**
 * @author Matthieu Morel
 *
 */
public class InputInterceptor1Impl extends AbstractProActiveController
    implements InputInterceptor {
    private int beforeInvocationCounter = 0;
    private int afterInvocationCounter = 0;

    /**
     * @param owner
     */
    public InputInterceptor1Impl(Component owner) {
        super(owner);
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(InputInterceptor1.INPUT_INTERCEPTOR1_NAME,
                    InputInterceptor1.class.getName(), TypeFactory.SERVER,
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

    public void afterInputMethodInvocation(MethodCall methodCall) {
        //System.out.println("after method invocation");
        setDummyValue(getDummyValue() + InputInterceptor1.AFTER_INTERCEPTION);
        afterInvocationCounter++;
    }

    public void beforeInputMethodInvocation(MethodCall methodCall) {
        //        System.out.println("before method invocation");
        setDummyValue(getDummyValue() + InputInterceptor1.BEFORE_INTERCEPTION);
        beforeInvocationCounter++;
    }
}
