package nonregressiontest.component;

import org.objectweb.fractal.api.control.AttributeController;

/**
 * @author Matthieu Morel
 *
 */
public interface MessageAttributes extends AttributeController {
    
    public void setMessage(String message);

}
