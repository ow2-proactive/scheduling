/*
 * Created on Oct 20, 2003
 * author : Matthieu Morel
  */
package nonregressiontest.component;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.group.ProActiveGroup;


/**
 * @author Matthieu Morel
 */
public class PrimitiveComponentD implements I1, BindingController {
    static Logger logger = Logger.getLogger(PrimitiveComponentA.class.getName());
    public final static String MESSAGE = "-->PrimitiveComponentD";

    //public final static Message MESSAGE = new Message("-->PrimitiveComponentD");
    public final static String I2_ITF_NAME = "i2";

    // typed collective interface
    I2 i2 = (I2) Fractive.createCollectiveClientInterface(I2_ITF_NAME,
            I2.class.getName());

    /**
     *
     */
    public PrimitiveComponentD() {
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.UserBindingController#addFcBinding(java.lang.String, java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf) {
        if (clientItfName.equals(I2_ITF_NAME)) {
            ProActiveGroup.getGroup(i2).add(serverItf);
        } else {
            logger.error("Binding impossible : wrong client interface name");
        }
    }

    /* (non-Javadoc)
     * @see nonregressiontest.component.creation.Input#processInputMessage(java.lang.String)
     */
    public Message processInputMessage(Message message) {
        //		/logger.info("transferring message :" + message.toString());
        if (i2 != null) {
            //Message msg = new Message(((i2Group.processOutputMessage(message.append(MESSAGE))).append(MESSAGE)));
            Message msg = i2.processOutputMessage(message.append(MESSAGE));
            return msg.append(MESSAGE);
            //return (i2Group.processOutputMessage(message.append(MESSAGE))).append(MESSAGE);
            //return msg;
        } else {
            logger.error("cannot forward message (binding missing)");
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        return new String[] { I2_ITF_NAME };
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItf) throws NoSuchInterfaceException {
        if (clientItf.equals(I2_ITF_NAME)) {
            return i2;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("cannot find " + I2_ITF_NAME + " interface");
            }
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItf)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        if (clientItf.equals(I2_ITF_NAME)) {
            ProActiveGroup.getGroup(i2).clear();
            if (logger.isDebugEnabled()) {
                logger.debug(I2_ITF_NAME + " interface unbound");
            }
        } else {
            logger.error("client interface not found");
        }
    }
}
