package org.objectweb.proactive.examples.minidescriptor;

import java.io.Serializable;


/**
 * This Class defines the Message (exchanged by the users)
 *
 *  @author Laurent Baduel
 */
public class Message implements Serializable {

    /** The message */
    private String s;

    public Message() {
    }

    /**
     * Constructor : builds a message from a String
     * @param source - the string
     */
    public Message(String source) {
        s = new String(source);
    }

    /**
     * Returns the String corresponding to the Message
     */
    public String toString() {
        return s;
    }
}
