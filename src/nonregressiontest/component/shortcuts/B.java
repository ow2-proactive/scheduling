package nonregressiontest.component.shortcuts;

import nonregressiontest.component.I2;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentB;

/**
 * @author Matthieu Morel
 */
public class B implements I2 {

    private B next = null;

    public B() {}

    public B(B next) {
        this.next = next;
    }


    public Message processOutputMessage(Message message) {
        if (next != null) {
            return next.processOutputMessage(message);
        }
        return message.append(PrimitiveComponentB.MESSAGE);
    }
}
