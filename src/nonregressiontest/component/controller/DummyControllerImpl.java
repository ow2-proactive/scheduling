package nonregressiontest.component.controller;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.ProActiveController;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

/**
 * @author Matthieu Morel
 *
 */
public class DummyControllerImpl extends ProActiveController implements DummyController {
    
    private String dummyValue = null;
    
    /**
     * @param owner
     */
    public DummyControllerImpl(Component owner) {
        super(owner);
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(DummyController.DUMMY_CONTROLLER_NAME,
                    DummyController.class.getName(),
                    TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " +
                this.getClass().getName());
        }

    }
    
    public void setDummyValue(String value) {
        dummyValue = value;
    }
    
    public String getDummyValue() {
        return dummyValue;
    }

}
