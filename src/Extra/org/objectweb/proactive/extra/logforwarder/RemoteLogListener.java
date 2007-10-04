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

public class RemoteLogListener {
    public static final int LISTEN_PORT = 1988;

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        /*

               try {
        //            SchedulerFake sf = (SchedulerFake)(ProActive.lookupActive(SchedulerFake.class.getName(), args[0]));

                        // start the log server
                   SimpleLoggerServer slf = new SimpleLoggerServer(LISTEN_PORT);
                   Thread slft  = new Thread(slf);
                   slft.start();

                       System.out.println("Connecting to " + args[0]);
                       UserScheduler sf = UserScheduler.connectTo(args[0]);
                       System.out.println("Connected to " + args[0]);

                       // new job log view
                   Appender a = new AppenderTest();
                   Logger test1 = Logger.getLogger(Scheduler.LOGGER_PREFIX + args[1]);
                   test1.removeAllAppenders();
                   test1.addAppender(a);




                   String hostname = java.net.InetAddress.getLocalHost().getHostName();

                   sf.listenLog(Integer.parseInt(args[1]),hostname, LISTEN_PORT);

               } catch (IOException e) {
                   e.printStackTrace();
               } catch (Exception e) {
                               e.printStackTrace();
                       }
               */

        //        System.out.println("Starting listener");
        //        Appender a = new AppenderTest();
        //        Logger test1 = Logger.getLogger("logger.test.id1");
        //        test1.addAppender(a);
        //        Logger test2 = Logger.getLogger("logger.test.id2");
        //        test2.addAppender(a);
        //        
        //        SimpleLoggerServer slf = new SimpleLoggerServer(1338);
        //        Thread slft  = new Thread(slf);
        //        slft.start();
    }
}
