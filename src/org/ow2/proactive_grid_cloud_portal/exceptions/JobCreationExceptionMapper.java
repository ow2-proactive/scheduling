package org.ow2.proactive_grid_cloud_portal.exceptions;

import java.net.HttpURLConnection;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ow2.proactive.scheduler.common.exception.JobCreationException;


@Provider
public class JobCreationExceptionMapper implements ExceptionMapper<JobCreationException> {

      public Response toResponse(JobCreationException exception)
      {
        return Response.status(HttpURLConnection.HTTP_NOT_FOUND)
        .entity(exception.getMessage()).build();
            
      }

    } 

