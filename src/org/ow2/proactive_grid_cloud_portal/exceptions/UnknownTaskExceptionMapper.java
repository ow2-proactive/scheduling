package org.ow2.proactive_grid_cloud_portal.exceptions;

import java.net.HttpURLConnection;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;


@Provider
public class UnknownTaskExceptionMapper implements ExceptionMapper<UnknownTaskException> {

      public Response toResponse(UnknownTaskException exception)
      {
        return Response.status(HttpURLConnection.HTTP_NOT_FOUND)
                .entity(exception.getMessage()).build();
      }

    } 
