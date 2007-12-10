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

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreInterface;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;


public class SchedulerFake {
    public static final String LOGGER_PREFIX = "logger.test.";

    // loggers associated to each tasks
    private java.util.Hashtable<String, Logger> loggers = new java.util.Hashtable<String, Logger>();
    private SimpleLoggerServer slf;
    private String hostname;
    private int port;

    public SchedulerFake() {
    }

    // test method...
    public void scheduleTasks() {
        try {
            this.hostname = URIBuilder.getLocalAddress().getHostName();
        } catch (UnknownHostException e2) {
            e2.printStackTrace();
        }

        PAActiveObject.setImmediateService("listenLog");

        // resources
        //        SimpleResourceManager srm = null;
        //        try {
        //            srm = (SimpleResourceManager) ProActive.newActive(SimpleResourceManager.class.getName(),
        //                    null);
        //            srm.addNodes("/user/cdelbe/home/ProActiveStd/ProActive/descriptors/helloRemote.xml");
        //        } catch (ActiveObjectCreationException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        } catch (NodeException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }
        IMUser imu = null;
        NodeSet nodes = null;

        try {
            IMCoreInterface imc = (IMCoreInterface) (PAActiveObject.lookupActive(IMCoreInterface.class.getName(),
                    "//localhost/IMCORE"));
            imu = imc.getUser();

            //            imu = IMFactory.getUser(new URI("rmi://duff.inria.fr:1099/"));
        } catch (ActiveObjectCreationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        //        catch (URISyntaxException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }

        //Init logs
        this.initLogServer();

        // create tasks
        RemoteTask t1 = null;

        // create tasks
        RemoteTask t2 = null;

        try {
            nodes = imu.getExactlyNodes(new IntWrapper(2), null);

            System.out.println("**********" + nodes.size());

            // Creates an active instance of class Hello2 on the local node
            t1 = (RemoteTask) PAActiveObject.newActive(RemoteTask.class.getName(), // the class to deploy
                    null, // the arguments to pass to the constructor, here none
                    nodes.get(0)); // which jvm should be used to hold the Active Object
            t2 = (RemoteTask) PAActiveObject.newActive(RemoteTask.class.getName(), // the class to deploy
                    null, // the arguments to pass to the constructor, here none
                    nodes.get(1)); // which jvm should be used to hold the Active Object
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        }

        // regsiter loggers
        this.addLogger(t1, "1");
        this.addLogger(t2, "2");

        // schedule tasks
        for (int i = 0; i < 40; i++) {
            t1.doTask();
            t2.doTask();
        }

        // end tasks
        t1.terminateTask();
        t2.terminateTask();

        PAActiveObject.terminateActiveObject(t1, false);
        PAActiveObject.terminateActiveObject(t2, false);

        // release nodes
        imu.freeNodes(nodes);

        Category.shutdown();
        //        System.out.println("CLOOOOOOOOOOOOOOSE");
        LogManager.shutdown();
    }

    private void initLogServer() {
        // incoming logs from scheduled tasks
        try {
            slf = SimpleLoggerServer.createLoggerServer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.port = slf.getPort();
    }

    private void addLogger(RemoteTask t, String id) {
        Logger l = Logger.getLogger(LOGGER_PREFIX + id);
        this.loggers.put(id, l);
        t.initLogger(LOGGER_PREFIX + id, this.hostname, this.port);
    }

    //    private void removeLogger(RemoteTask t, String id){
    //        slf.
    //    }
    public void listenLog(int id, String hostname, int port) {
        Logger.getLogger(LOGGER_PREFIX + id)
              .addAppender(new SocketAppender(hostname, port));

        //        this.loggers.get(id).addAppender(new SocketAppender(hostname,port));
    }

    public static void main(String[] args) {
        try {
            SchedulerFake sf = (SchedulerFake) (PAActiveObject.newActive(SchedulerFake.class.getName(),
                    null));
            PAActiveObject.register(sf, "rmi://duff/scheduler");
            sf.scheduleTasks();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
