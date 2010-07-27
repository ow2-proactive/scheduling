package org.ow2.proactive_grid_cloud_portal;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;

@Path("/")
public class SchedulerREST {
    
    
    @GET
    @Path("/jobs")
    @Produces("application/json")
    public SchedulerState jobs(@HeaderParam("sessionid") String sessionId) {
        Scheduler s = SessionMapper.getInstance().getSessionsMap().get(sessionId);
        System.out.println("sessionid " + sessionId);
        try {
            return s.getState();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
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
