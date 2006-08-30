/*
 * Created on Oct 20, 2003
 * author : Matthieu Morel
 */
package nonregressiontest.component.collectiveitf.multicast;

import java.util.List;

import nonregressiontest.component.Message;


/**
 * @author Matthieu Morel
 */
public class UncompatibleComponentB implements I2Dummy {
    public final static String MESSAGE = "-->b";

    /**
     *
     */
    public UncompatibleComponentB() {
    }

    public List<Message> processOutputMessage(Message message) {

        // TODO Auto-generated method stub
        return null;
    }

}
