/*
 * Created on Jul 8, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.examples.hello2;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.node.NodeException;

import java.io.IOException;


/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HelloServer {
    public String sayHello() {
        return "hello from ProActive";
    }

    public String getNodeURL() {
        UniversalBody b = ProActive.getBodyOnThis();
        System.out.println("classe de b = " + b.getClass());
        String s = b.getNodeURL();

        return s;
    }

    public static void main(String[] args) {
        try {
            HelloServer server = (HelloServer) ProActive.newActive(HelloServer.class.getName(),
                    new Object[] {  });
            String url = server.getNodeURL();
            System.out.println("url === " + url);

            ProActive.register(server, "//localhost/helloServer");
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
