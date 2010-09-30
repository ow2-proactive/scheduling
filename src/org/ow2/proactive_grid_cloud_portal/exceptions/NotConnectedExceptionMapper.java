package org.ow2.proactive_grid_cloud_portal.exceptions;

import java.net.HttpURLConnection;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ow2.proactive.scheduler.common.exception.NotConnectedException;


@Provider
public class NotConnectedExceptionMapper implements ExceptionMapper<NotConnectedException> {

      public Response toResponse(NotConnectedException exception)
      {
        return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED)
        .entity(exception.getMessage()).build();
            
      }

    } 

