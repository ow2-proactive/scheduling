/*
 * Created on Jul 8, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.examples.hello2;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;

import java.io.IOException;


/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HelloClient implements Runnable {
    private HelloServer server;

    public HelloClient() {
        try {
            this.server = (HelloServer) ProActive.lookupActive(HelloServer.class.getName(),
                    "//localhost/helloServer");
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //try {
        //HelloClient client = (HelloClient)ProActive.newActive(HelloClient.class.getName(), new Object []{});
        HelloClient client = new HelloClient();
        Thread t = new Thread(client);
        t.start();
        //		} catch (ActiveObjectCreationException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		} catch (NodeException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		}
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        System.out.println(server.sayHello());
    }
}
