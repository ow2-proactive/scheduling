package nonregressiontest.component;

import java.util.List;

import nonregressiontest.component.Message;

public interface I2Multicast {
	
    public List<Message> processOutputMessage(Message message);

}
