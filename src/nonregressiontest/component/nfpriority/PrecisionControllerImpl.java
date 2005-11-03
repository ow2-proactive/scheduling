package nonregressiontest.component.nfpriority;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.controller.ProActiveController;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;


public class PrecisionControllerImpl extends AbstractProActiveController
    implements PrecisionController {
    public PrecisionControllerImpl(Component owner) {
        super(owner);
    }

    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(PrecisionController.PRECISION_CONTROLLER_NAME,
                    PrecisionController.class.getName(), TypeFactory.SERVER,
                    TypeFactory.MANDATORY, TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " +
                this.getClass().getName());
        }
	}

	public void increasePrecision() {
        try {
            PrecisionAttributeController pac = (PrecisionAttributeController) getFcItfOwner()
                                                                                  .getFcInterface(PrecisionAttributeController.PRECISION_ATTRIBUTE_CONTROLLER_NAME);
            (((PrecisionAttributeController) getFcItfOwner().getFcInterface(PrecisionAttributeController.PRECISION_ATTRIBUTE_CONTROLLER_NAME))).setPrecision(2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decreasePrecision() {
    }
}
