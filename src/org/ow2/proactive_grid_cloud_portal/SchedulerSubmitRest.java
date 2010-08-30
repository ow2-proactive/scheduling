package org.ow2.proactive_grid_cloud_portal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;


@Path("/")
public class SchedulerSubmitRest {

    @POST
    @Path("/submit")
    public JobId submit(@HeaderParam("sessionid") String sessionId, MultipartInput multipart) {
        Scheduler s = SchedulerSessionMapper.getInstance().getSessionsMap().get(sessionId);
        System.out.println("sessionid " + sessionId);
        File tmp;
        try {
            tmp = File.createTempFile("prefix", "suffix");
            for (InputPart part : multipart.getParts()) {

                BufferedWriter outf = new BufferedWriter(new FileWriter(tmp));
                outf.write(part.getBodyAsString());
                outf.close();

            }

            Job j = JobFactory.getFactory().createJob(tmp.getAbsolutePath());
            return s.submit(j);
        } catch (JobCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SubmissionClosedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

}
