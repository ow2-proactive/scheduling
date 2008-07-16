/**
 *
 */
package org.ow2.proactive.scheduler.common.exception;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Exceptions Generated if a ProActive task is sent to a java or native task dependence list.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 */
@PublicAPI
public class DependenceFailedException extends RuntimeException {

    /**  */

    /**
     * Create a new instance of JobCreationException with the given messag
     *
     * @param msg the message to attach.
     */
    public DependenceFailedException(String msg) {
        super(msg);
    }
}
