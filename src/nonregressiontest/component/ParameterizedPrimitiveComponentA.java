/*
 * Created on Oct 20, 2003
 * author : Matthieu Morel
 */
package nonregressiontest.component;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author Matthieu Morel
 */
public class ParameterizedPrimitiveComponentA extends PrimitiveComponentA implements AttributeController {
    public String message;
    public final static String I2_ITF_NAME = "i2";
    private static Logger logger = ProActiveLogger.getLogger(
            "nonregressiontests.components");
    I2 i2;

    /**
     *
     */
    public ParameterizedPrimitiveComponentA() {
    }

    // attribute for use by AttributeController 
    public void setMessage(String message) {
        this.message = message;
    }
    
    
    public Message processInputMessage(Message message) {
        //      /logger.info("transferring message :" + message.toString());
        if (i2 != null) {
            return (i2.processOutputMessage(message.append(message))).append(message);
        } else {
            logger.error("cannot forward message (binding missing)");
            message.setInvalid();
            return message;
        }
    }
}
