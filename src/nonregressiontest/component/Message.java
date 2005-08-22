/*
 * Created on Oct 21, 2003
 * author : Matthieu Morel
 */
package nonregressiontest.component;

import java.io.PrintStream;
import java.io.Serializable;


/**
 * @author Matthieu Morel
 */
public class Message implements Serializable {
    String message;
    boolean valid = true;

    public Message() {
    }

    public Message(String string) {
        message = string;
    }

    public Message append(String string) {
        message = message + string;
        return this;
    }

    public Message append(Message message) {
        if (isValid()) {
            this.message = message + message.toString();
        }
        return this;
    }

    public String toString() {
        return message;
    }

    public void setInvalid() {
        message = null;
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public void printToStream(PrintStream out) {
        out.println(message);
    }

    public String getMessage() {
        return message;
    }
}
