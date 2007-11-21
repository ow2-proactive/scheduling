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
package org.objectweb.proactive.extra.logforwarder;

import java.io.PrintStream;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;


public class RemoteTask {
    SocketAppender out;
    Logger logger;

    public RemoteTask() {
    }

    public void initLogger(String loggerName, String host, int port) {
        out = new SocketAppender(host, port);
        logger = Logger.getLogger(loggerName);
        //        logger.removeAllAppenders();
        logger.addAppender(EmptyAppender.SINK);
        logger.addAppender(out);

        //test
        //logger.removeAllAppenders();

        // redirect stdout
        System.setOut(new PrintStream(
                new LoggingOutputStream(logger, Level.INFO), true));

        //        System.setErr(new PrintStream(new LoggingOutputStream(logger, Level.ERROR), true));
    }

    public void doTask() {
        System.out.println(" Message info 1 from " + this);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.err.println(" Message error 2 from " + this);
    }

    public int terminateTask() {
        System.out.println(" Terminating logger on " + this);
        //logger.removeAllAppenders();
        LogManager.shutdown();

        return 0;
    }
}
