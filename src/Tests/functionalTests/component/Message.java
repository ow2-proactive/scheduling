/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.component;

import java.io.PrintStream;
import java.io.Serializable;


/**
 * @author Matthieu Morel
 */
public class Message implements Serializable {

    /**
         *
         */
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

    @Override
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
