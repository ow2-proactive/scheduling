/*
 * Created on Jul 23, 2004
 *
 */
package org.objectweb.proactive.core.rmi;


/**
 * @author sbeucler
 *
 */
public class MSG {
    private byte[] message;
    private String action;

    /**
     * @param action
     * @param message
     */
    public MSG(byte[] message, String action) {
        super();
        this.action = action;
        this.message = message;
    }

    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }

    /**
     * @param message The message to set.
     */
    public void setMessage(byte[] message) {
        this.message = message;
    }

    /**
     * @return Returns the message.
     */
    public byte[] getMessage() {
        return message;
    }
}
