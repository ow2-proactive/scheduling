/*
 * Created on Oct 20, 2003
 * author : Matthieu Morel
  */
package nonregressiontest.component;

import org.apache.log4j.Logger;

/**
 * @author Matthieu Morel
 */
public class PrimitiveComponentB implements I2 {
	static Logger logger = Logger.getLogger(PrimitiveComponentB.class.getName());
	public final static String MESSAGE="-->PrimitiveComponentB";


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
