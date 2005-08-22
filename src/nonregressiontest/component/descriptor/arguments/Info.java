package nonregressiontest.component.descriptor.arguments;

import org.objectweb.fractal.api.control.AttributeController;


/**
 * @author Matthieu Morel
 *
 */
public interface Info extends AttributeController {
    public String getInfo();

    public void setInfo(String info);
}
