package org.ow2.proactive_grid_cloud_portal;

import java.io.IOException;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.ext.filessplitmerge.schedulertools.SchedulerProxyUserInterface;

@Path("/login")
public class Login {

    @POST
   
    public String login(@FormParam("username") String username, @FormParam("password") String password) {
        
        try {
        
            SchedulerProxyUserInterface scheduler =  PAActiveObject.newActive(
                    SchedulerProxyUserInterface.class, new Object[] {});
        
            scheduler.init("rmi://localhost:1099/SCHEDULER", username, password);
        
        return ""+SchedulerSessionMapper.getInstance().add(scheduler);
        
        } catch (Throwable e) {
            e.printStackTrace();
            throw new UnauthorizedException(e);
        }
          
        
       
        
    }
    
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
