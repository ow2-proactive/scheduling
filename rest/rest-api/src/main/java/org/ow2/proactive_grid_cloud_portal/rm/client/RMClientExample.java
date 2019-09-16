package org.ow2.proactive_grid_cloud_portal.rm.client;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive_grid_cloud_portal.common.RMRestInterface;

import javax.security.auth.login.LoginException;
import java.security.KeyException;

public class RMClientExample {

    public static void main(String[] args) throws LoginException, KeyException, ActiveObjectCreationException, NodeException, RMException {
        System.out.println("Hello from example.");

//        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().useSystemProperties();
        RMRestClient client = new RMRestClient("http://localhost:8080/rest/", null);//, new ApacheHttpClient4Engine(httpClientBuilder.build()));
        RMRestInterface rm = client.getRm();
        String sessionId = rm.rmConnect("admin", "admin");

        System.out.println("s" + sessionId);
    }
}
