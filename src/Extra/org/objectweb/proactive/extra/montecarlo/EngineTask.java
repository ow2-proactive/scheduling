package org.objectweb.proactive.extra.montecarlo;

import java.io.Serializable;


/**
 * EngineTask
 *
 * An engine task represents any general task, which is not Monte-Carlo specific.
 *
 * An engine task have access to two interfaces:
 * <ul>
 *   <li>
 *  The Simulator: provides the ability to submit to the engine a bunch of Monte-Carlo parallel simulations.
 *   </li>
 *   <li>
 *   The Executor: provides the ability to submit to the engine other engine tasks
 *   </li>
 * </ul>
 *
 * A specific case of engine task is the top-level task. This task is the very first one submitted to the engine and should contain the main code of the algorithm.
 *
 * @author The ProActive Team
 */
public interface EngineTask extends Serializable {

    public Serializable run(Simulator simulator, Executor executor);

}
