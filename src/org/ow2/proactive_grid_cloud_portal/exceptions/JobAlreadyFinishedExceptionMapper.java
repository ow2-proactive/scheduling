package org.ow2.proactive_grid_cloud_portal.exceptions;

import java.net.HttpURLConnection;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ow2.proactive.scheduler.common.exception.JobAlreadyFinishedException;


@Provider
public class JobAlreadyFinishedExceptionMapper implements ExceptionMapper<JobAlreadyFinishedException> {

      public Response toResponse(JobAlreadyFinishedException exception)
      {
        return Response.status(HttpURLConnection.HTTP_NOT_FOUND)
        .entity(exception.getMessage()).build();
            
      }

    } 

