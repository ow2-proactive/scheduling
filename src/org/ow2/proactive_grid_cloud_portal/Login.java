package org.ow2.proactive_grid_cloud_portal;

import java.io.IOException;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;

@Path("/login")
public class Login {

    @POST
   
    public String login(@FormParam("username") String username, @FormParam("password") String password) {
        
        try {
        SchedulerAuthenticationInterface sai = SchedulerConnection.join("rmi://localhost:1099");
        Credentials cred = Credentials.createCredentials(username, password, sai.getPublicKey());
        Scheduler scheduler = sai.login(cred);
        return ""+SessionMapper.getInstance().add(scheduler);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new UnauthorizedException(e);
        }
          
        
       
        
    }
    
    public static void main(String[] args) {
        GetMethod method = new GetMethod("http://localhost:8080/proactive_grid_cloud_portal/jobs");
        method.addRequestHeader("sessionid", "23423432");
        HttpClient client = new HttpClient();
        try {
            client.executeMethod(method);
        } catch (HttpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }
}
