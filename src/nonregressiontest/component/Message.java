/*
 * Created on Oct 21, 2003
 * author : Matthieu Morel
  */
package nonregressiontest.component;

import java.io.Serializable;


/**
 * @author Matthieu Morel
 */
public class Message implements Serializable {

	String message;

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
		this.message = message + message.toString();
		return this;
	}

	public String toString() {
		return message;
	}

}
