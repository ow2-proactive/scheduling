/*
 * Created on Oct 20, 2003
 * author : Matthieu Morel
 */
package nonregressiontest.component;


/**
 * @author Matthieu Morel
 */
public class PrimitiveComponentB implements I2 {
    public final static String MESSAGE = "-->b";

    /**
     *
     */
    public PrimitiveComponentB() {
    }

    /* (non-Javadoc)
     * @see nonregressiontest.component.creation.I2#processOutputMessage(java.lang.String)
     */
    public Message processOutputMessage(Message message) {
        //logger.info("transferring message :" + message.toString());
        return message.append(MESSAGE);
    }
}
