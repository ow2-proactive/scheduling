package org.ow2.proactive_grid_cloud_portal;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;

@Path("/")
public class SchedulerREST {
    
    
    @GET
    @Path("/jobs")
    public String jobs(@HeaderParam("sessionid") String sessionId) {
        Scheduler s = SessionMapper.getInstance().getSessionsMap().get(sessionId);
        System.out.println("sessionid " + sessionId);
        if (s != null) {
            return "found";
        } else {
            return "lost";
        }
        /*
        try {
            return s.getState().toString();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            e.printStackTrace();
        }
        return "";
        */
    }

}
