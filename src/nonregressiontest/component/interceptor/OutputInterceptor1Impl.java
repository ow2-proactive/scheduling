package nonregressiontest.component.interceptor;

import java.lang.reflect.Method;

import nonregressiontest.component.controller.DummyController;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.interception.OutputInterceptor;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

public class OutputInterceptor1Impl extends AbstractProActiveController implements OutputInterceptor1, OutputInterceptor  {
    
    public OutputInterceptor1Impl(Component owner) {
        super(owner);
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(OUTPUT_INTERCEPTOR_1_NAME,
                    OutputInterceptor1.class.getName(), TypeFactory.SERVER,
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


    public void afterOutputMethodInvocation(Method method, Object[] args) {
        setDummyValue(getDummyValue() + OutputInterceptor1.AFTER_INTERCEPTION);
    }

    public void beforeOutputMethodInvocation(Method method, Object[] args) {
        setDummyValue(getDummyValue() + OutputInterceptor1.BEFORE_INTERCEPTION);
    }


}
