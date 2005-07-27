package nonregressiontest.component.interceptor;

import java.lang.reflect.Method;

import nonregressiontest.component.controller.DummyController;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;


/**
 * @author Matthieu Morel
 *
 */
public class InputOutputInterceptorImpl extends AbstractProActiveController
    implements InputOutputInterceptor{

    /**
     * @param owner
     */
    public InputOutputInterceptorImpl(Component owner) {
        super(owner);
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(InputOutputInterceptor.INPUT_OUTPUT_INTERCEPTOR_NAME,
                    InputOutputInterceptor.class.getName(), TypeFactory.SERVER,
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

    public void afterInputMethodInvocation(Method method, Object[] args) {
        //System.out.println("after method invocation");
        setDummyValue(getDummyValue() + InputOutputInterceptor.AFTER_INPUT_INTERCEPTION);
    }

    public void beforeInputMethodInvocation(Method method, Object[] args) {
        //        System.out.println("before method invocation");
        setDummyValue(getDummyValue() + InputOutputInterceptor.BEFORE_INPUT_INTERCEPTION);
    }
    
    public void afterOutputMethodInvocation(Method method, Object[] args) {
        setDummyValue(getDummyValue() + InputOutputInterceptor.AFTER_OUTPUT_INTERCEPTION);
    }

    public void beforeOutputMethodInvocation(Method method, Object[] args) {
        setDummyValue(getDummyValue() + InputOutputInterceptor.BEFORE_OUTPUT_INTERCEPTION);
    }

}
