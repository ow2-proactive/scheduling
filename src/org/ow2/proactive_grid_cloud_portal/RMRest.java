package org.ow2.proactive_grid_cloud_portal;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

@Path("/rm")
public class RMRest {

    @POST
    @Path("login")
    public String rmConnect(@FormParam("username") String username, 
                          @FormParam("password") String password) {
        
        try {
            
            RMProxy rm =  PAActiveObject.newActive(
                    RMProxy.class, new Object[] {});
        
            rm.init("rmi://localhost:1099/RM", username, password);
        
        return ""+RMSessionMapper.getInstance().add(rm);
        
        } catch (Throwable e) {
            e.printStackTrace();
            throw new UnauthorizedException(e);
        }
       

    }
    
    
    @GET
    @Path("state")
    public RMState getState(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = RMSessionMapper.getInstance().getSessionsMap().get(sessionId);
        if (rm == null) {
            throw new UnauthorizedException();
        }
        
        return  PAFuture.getFutureValue(rm.getState());
    }
    
    @GET
    @Path("monitoring")
    public RMInitialState getInitialState(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = RMSessionMapper.getInstance().getSessionsMap().get(sessionId);
        if (rm == null) {
            throw new UnauthorizedException();
        }
        return PAFuture.getFutureValue(rm.getMonitoring().getState());
    }
 
    @POST
    @Path("node")
    public boolean  addNode(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurl") String nodeUrl) {
        ResourceManager rm = RMSessionMapper.getInstance().getSessionsMap().get(sessionId);
        if (rm == null) {
            throw new UnauthorizedException();
        }
        return rm.addNode(nodeUrl).booleanValue();
    }
    
    
    
    
}
