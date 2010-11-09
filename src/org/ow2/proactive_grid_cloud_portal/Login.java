package org.ow2.proactive_grid_cloud_portal;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.security.auth.login.LoginException;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.CachingSchedulerProxyUserInterface;



public class Login {



    public static void main(String[] args) {
        GetMethod method = new GetMethod("http://localhost:8080/proactive_grid_cloud_portal/jobs");
        method.addRequestHeader("sessionid", "1");
        HttpClient client = new HttpClient();
        try {
            client.executeMethod(method);
            System.out.println(method.getResponseBodyAsString());
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
