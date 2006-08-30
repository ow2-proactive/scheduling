package nonregressiontest.component.collectiveitf.multicast;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.controller.MulticastBindingController;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import nonregressiontest.component.I1;
import nonregressiontest.component.Message;


public class ClientParamLevelDispatchAnnotationTester implements I1,
    MulticastBindingController {
    public static final String MESSAGE="-pld-";
    protected final static Logger logger = ProActiveLogger.getLogger(
                "nonregressiontests.components");
    MulticastTestItf multicastServerItf = null;


    public Message processInputMessage(Message message) {

        if (multicastServerItf != null) {
            List<Message> msgsToSend = new ArrayList<Message>();
            Message toForward = message.append(MESSAGE);
            msgsToSend.add(toForward);
            msgsToSend.add(toForward);
            List<Message> msgs=null;
            try {
//                msgs = multicastServerItf.processOutputMessage(msgsToSend);
//                msgs = multicastServerItf.processOutputMessage(msgsToSend);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }

            Message result = new Message();

            for (Message msg : msgs) {
                result.append(msg);
            }

            return result.append(MESSAGE);
        } else {
            logger.error("cannot forward message (binding missing)");
            message.setInvalid();
            return message;
        }
    }

    public Object getMulticastFcItfRef(String itfName)
        throws NoSuchInterfaceException {

        if ("i2Multicast".equals(itfName)) {
            return multicastServerItf;
        } else {
            throw new NoSuchInterfaceException("No such multicast interface : " + itfName);
        }
    }

    public void setMulticastFcItfRef(String itfName, Object itfRef) {

        if ("i2Multicast".equals(itfName) && (itfRef instanceof MulticastTestItf)) {
            multicastServerItf = (MulticastTestItf) itfRef;
        }
    }
}
