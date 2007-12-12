/**
 *
 */
package org.objectweb.proactive.extensions.scheduler.common.exception;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Exceptions Generated if a ProActive task is sent to a java or native task dependence list.
 *
 * @author jlscheef - ProActive Team
 * @version 3.9, Dec 12, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public class DependenceFailedException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = -2407982905465733418L;

    /**
    * Create a new instance of JobCreationException with the given messag
    *
    * @param msg the message to attach.
    */
    public DependenceFailedException(String string) {
        super(string);
    }
}
