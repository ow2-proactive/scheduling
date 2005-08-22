package org.objectweb.proactive.examples.chat;

import java.io.Serializable;


/**
 * This Class defines the Message (exchanged by the users)
 *
 *  @author Laurent Baduel
 */
public class Message implements Serializable {

    /** The message */
    private String s;

    /**
     * Constructor : builds a message from a String
     * @param source - the string
     */
    public Message(String source) {
        s = source;
        if (!(s.endsWith("\n"))) {
            s += "\n";
        }
    }

    /**
     * Constructor : builds a message from a String. The message will contain the name of the user and the date of writting.
     * @param author
     * @param source
     */
    public Message(String author, String source) {
        s = "<" + (new java.util.Date(System.currentTimeMillis())).toString() +
            "> <" + author + "> " + source;
        if (!(s.endsWith("\n"))) {
            s += "\n";
        }
    }

    /**
     * Returns the String corresponding to the Message
     */
    public String toString() {
        return s;
    }
}
