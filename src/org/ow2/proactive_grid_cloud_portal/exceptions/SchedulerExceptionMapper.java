package org.ow2.proactive_grid_cloud_portal.exceptions;

import java.net.HttpURLConnection;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ow2.proactive.scheduler.common.exception.SchedulerException;


@Provider
public class SchedulerExceptionMapper implements ExceptionMapper<SchedulerException> {

      public Response toResponse(SchedulerException exception)
      {
        return Response.status(HttpURLConnection.HTTP_NOT_FOUND)
        .entity(exception.getMessage()).build();

      }

    }
