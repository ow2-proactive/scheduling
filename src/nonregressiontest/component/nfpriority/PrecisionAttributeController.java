package nonregressiontest.component.nfpriority;

import org.objectweb.fractal.api.control.AttributeController;


public interface PrecisionAttributeController extends AttributeController {
    public static final String PRECISION_ATTRIBUTE_CONTROLLER_NAME = "precision-attribute-controller";

    public void setPrecision(int nbDecimals);

    public int getPrecision();
}
