package nonregressiontest.component.collectiveitf.multicast;

import java.util.List;

import nonregressiontest.component.Message;


public interface I2Dummy {

    public List<Message> processOutputMessage(Message message);
}
