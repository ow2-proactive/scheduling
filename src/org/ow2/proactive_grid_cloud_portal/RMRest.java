package org.ow2.proactive_grid_cloud_portal;

import java.net.HttpURLConnection;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;


@Path("/rm")
public class RMRest {

    public ResourceManager checkAccess(String sessionId) throws WebApplicationException {
        ResourceManager s = RMSessionMapper.getInstance().getSessionsMap().get(sessionId);

        if (s == null) {
            throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_UNAUTHORIZED)
                    .entity("you are not connected, try to log in first").build());
        }

        return s;
    }
    
    
    @POST
    @Path("login")
    public String rmConnect(@FormParam("username") String username, @FormParam("password") String password) {

        try {

            RMProxy rm = PAActiveObject.newActive(RMProxy.class, new Object[] {});


//            rm.init(Config.getProperty("rm.url"), username, password);
          rm.init("rmi://localhost:1099", username, password);

            return "" + RMSessionMapper.getInstance().add(rm);

        } catch (Throwable e) {
            e.printStackTrace();
            throw new UnauthorizedException(e);
        }

    }

    @GET
    @Path("state")
    public RMState getState(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getState());
    }

    @GET
    @Path("monitoring")
    public RMInitialState getInitialState(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        return PAFuture.getFutureValue(rm.getMonitoring().getState());
    }

    @GET
    @Path("isactive")
    public boolean isActive(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.isActive().booleanValue();
    }
    
    @POST
    @Path("node")
    public boolean addNode(@HeaderParam("sessionid") String sessionId, @FormParam("nodeurl") String nodeUrl) {
        ResourceManager rm = checkAccess(sessionId);
        return rm.addNode(nodeUrl).booleanValue();
    }

    
    @POST
    @Path("disconnect")
    public boolean disconnect(@HeaderParam("sessionid") String sessionId) {
        ResourceManager rm = checkAccess(sessionId);
        RMSessionMapper.getInstance().getSessionsMap().remove(rm);
        return rm.disconnect().booleanValue();
    }
    
}
