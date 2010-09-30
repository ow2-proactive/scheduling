package org.ow2.proactive_grid_cloud_portal.exceptions;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class IOExceptionMapper implements ExceptionMapper<IOException> {

      public Response toResponse(IOException exception)
      {
        return Response.status(HttpURLConnection.HTTP_NOT_FOUND)
        .entity(exception.getMessage()).build();
            
      }

    } 

