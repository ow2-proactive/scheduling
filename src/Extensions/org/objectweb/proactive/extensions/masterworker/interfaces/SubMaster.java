package org.objectweb.proactive.extensions.masterworker.interfaces;

import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.annotation.PublicAPI;

import java.io.Serializable;
import java.util.List;


/**
 * Interface describing the aspects of task submission and results collection only
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface SubMaster<T extends Task<R>, R extends Serializable> {

    /**
    * Reception order mode. Results can be received in Completion Order (the default) or Submission Order
    * @author The ProActive Team
    *
    */
    public enum OrderingMode {
        /**
         * Results of tasks are received in the same order as tasks were submitted
         */
        SubmitionOrder,
        /**
         * Results of tasks are received in the same order as tasks are completed (unspecified)
         */
        CompletionOrder
    }

    /**
    * Results of tasks are received in the same order as tasks were submitted
    */
    public OrderingMode SUBMISSION_ORDER = OrderingMode.SubmitionOrder;

    /**
     * Results of tasks are received in the same order as tasks are completed (unspecified)
     */
    public OrderingMode COMPLETION_ORDER = OrderingMode.CompletionOrder;

    //@snippet-start masterworker_order
    /**
     * Sets the current ordering mode <br/>
     * If reception mode is switched while computations are in progress,<br/>
     * then subsequent calls to waitResults methods will be done according to the new mode.<br/>
     * @param mode the new mode for result gathering
     */
    void setResultReceptionOrder(OrderingMode mode);

    //@snippet-end masterworker_order

    //@snippet-start masterworker_solve
    /**
     * Adds a list of tasks to be solved by the master <br/>
     * <b>Warning</b>: the master keeps a track of task objects that have been submitted to it and which are currently computing.<br>
     * Submitting two times the same task object without waiting for the result of the first computation is not allowed.
     * @param tasks list of tasks
     * @throws org.objectweb.proactive.extensions.masterworker.TaskAlreadySubmittedException if a task is submitted twice
     */
    void solve(List<T> tasks);

    //@snippet-end masterworker_solve
    //@snippet-start masterworker_collection
    /**
     * Wait for all results, will block until all results are computed <br>
     * The ordering of the results depends on the result reception mode in use <br>
     * @return a collection of objects containing the result
     * @throws org.objectweb.proactive.extensions.masterworker.TaskException if a task threw an Exception
     */
    List<R> waitAllResults() throws TaskException;

    /**
     * Wait for the first result available <br>
     * Will block until at least one Result is available. <br>
     * Note that in SubmittedOrder mode, the method will block until the next result in submission order is available<br>
     * @return an object containing the result
     * @throws TaskException if the task threw an Exception
     */
    R waitOneResult() throws TaskException;

    /**
     * Wait for a number of results<br>
     * Will block until at least k results are available. <br>
     * The ordering of the results depends on the result reception mode in use <br>
     * @param k the number of results to wait for
     * @return a collection of objects containing the results
     * @throws TaskException if the task threw an Exception
     */
    List<R> waitKResults(int k) throws TaskException;

    /**
     * Tells if the master is completely empty (i.e. has no result to provide and no tasks submitted)
     * @return the answer
     */
    boolean isEmpty();

    /**
     * Returns the number of available results <br/>
     * @return the answer
     */
    int countAvailableResults();

    //@snippet-end masterworker_collection
}
