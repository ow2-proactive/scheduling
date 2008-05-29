package org.objectweb.proactive.extra.p2p.service.exception;

/**
 * @author The ProActive Team
 *
 * Created on Mar 31, 2008
 */

public class PeerDoesntExist extends Exception {

    /**
    *
    */
    public PeerDoesntExist() {
        super();
    }

    /**
     * @param message
     */
    public PeerDoesntExist(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public PeerDoesntExist(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public PeerDoesntExist(Throwable cause) {
        super(cause);
    }

}
