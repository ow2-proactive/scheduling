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
package org.objectweb.proactive.examples.hello;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;


/** A stripped-down Active Object example.
 * The object has only one public method, sayHello()
 * The object does nothing but reflect the host its on. */
public class TinyHello implements java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private final String message = "Hello World!";

    /** ProActive compulsory no-args constructor */
    public TinyHello() {
    }

    /** The Active Object creates and returns information on its location
     * @return a StringWrapper which is a Serialized version, for asynchrony */
    public StringMutableWrapper sayHello() {
        return new StringMutableWrapper(this.message + "\n from " +
            getHostName() + "\n at " +
            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(
                new java.util.Date()));
    }

    /** finds the name of the local machine */
    static String getHostName() {
        try {
            return URIBuilder.getLocalAddress().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /** The call that starts the Acive Objects, and displays results.
     * @param args must contain the name of an xml descriptor */
    public static void main(String[] args) throws Exception {
        // Creates an active instance of class Tiny on the local node
        TinyHello tiny = (TinyHello) ProActiveObject.newActive(TinyHello.class.getName(), // the class to deploy
                null // the arguments to pass to the constructor, here none
            ); // which jvm should be used to hold the Active Object

        // get and display a value 
        StringMutableWrapper received = tiny.sayHello(); // possibly remote call
        logger.info("On " + getHostName() + ", a message was received: " +
            received); // potential wait-by-necessity
                       // quitting

        ProActive.exitSuccess();
    }
}
