package functionaltests.rm;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.utils.NodeSet;

public class PolicyWhichThrowsExceptions extends ExtendedSchedulerPolicy{

    static final Logger logger = Logger.getLogger(PolicyWhichThrowsExceptions.class);

    private int counter = 0;


    @Override
    public boolean isTaskExecutable(NodeSet selectedNodes, EligibleTaskDescriptor task) {
        logger.info("PolicyWhichThrowsExceptions::isTaskExecutable is called " + counter);
        if(1 <= counter && counter <= 4){
            ++counter;
            throw new Error("This error is thrown to perform reconnection to RM");
        }else{
            ++counter;
            return super.isTaskExecutable(selectedNodes, task);
        }
    }

}