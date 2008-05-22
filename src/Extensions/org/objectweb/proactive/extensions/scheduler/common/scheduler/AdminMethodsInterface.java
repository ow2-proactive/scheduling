/**
 * 
 */
package org.objectweb.proactive.extensions.scheduler.common.scheduler;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.policy.PolicyInterface;


/**
 * AdminMethodsInterface describe the methods that an administrator
 * should do in addition to the user methods.<br>
 * This interface represents what a scheduler administrator should do.
 *
 * @author The ProActive Team
 * @date 20 févr. 08
 * @version 3.9
 * @since ProActive 3.9
 *
 */
@PublicAPI
public interface AdminMethodsInterface {
    /**
     * Change the policy of the scheduler.<br>
     * This method will immediately change the policy and so the whole scheduling process.
     *
     * @param newPolicyFile the new policy file as a string.
     * @return true if the policy has been correctly change, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper changePolicy(Class<? extends PolicyInterface> newPolicyFile)
            throws SchedulerException;

    /**
     * Start the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper start() throws SchedulerException;

    /**
     * Stop the scheduler.<br>
     * Once done, you won't be able to submit job, and the scheduling will be stopped.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper stop() throws SchedulerException;

    /**
     * Pause the scheduler by terminating running jobs.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper pause() throws SchedulerException;

    /**
     * Freeze the scheduler by terminating running tasks.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper freeze() throws SchedulerException;

    /**
     * Resume the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper resume() throws SchedulerException;

    /**
     * Shutdown the scheduler.<br>
     * It will terminate every submitted jobs but won't accept new submit.<br>
     * Use {@link #kill()} if you want to stop the scheduling and exit the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper shutdown() throws SchedulerException;

    /**
     * kill the scheduler.<br>
     * Will stop the scheduling, and shutdown the scheduler.
     *
     * @return true if success, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper kill() throws SchedulerException;

    /**
     * Reconnect a new Resource Manager to the scheduler.<br>
     * Can be used if the resource manager has crashed.
     * 
     * @param imp the URL of the new Resource Manager to link to the scheduler.
     * @return true if success, false otherwise.
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException;
}
