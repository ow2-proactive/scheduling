package org.ow2.proactive_grid_cloud_portal.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ow2.proactive.scheduler.common.exception.PermissionException;

@Provider
public class PermissionExceptionExceptionMapper implements ExceptionMapper<PermissionException> {

      public Response toResponse(PermissionException permissionException)
      {
        return Response.status(403)
            .entity(permissionException.getMessage())
            .build();
      }

    } 

