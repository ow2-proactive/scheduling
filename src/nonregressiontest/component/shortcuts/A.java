package nonregressiontest.component.shortcuts;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * @author Matthieu Morel
 */
public class A implements InitActive, RunActive, EndActive {
    
    protected static Logger logger = ProActiveLogger.getLogger(
    "nonregressiontests.components");


    public A() {}

    
    /*
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug("A : starting activity");
        }

    }
    /*
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug("A : running activity");
        }
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        service.fifoServing();
        }

    /*
     * @see org.objectweb.proactive.EndActive#endActivity(org.objectweb.proactive.Body)
     */
    public void endActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug("A : ending activity");
        }
    }
}
