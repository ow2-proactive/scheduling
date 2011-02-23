package org.ow2.proactive_grid_cloud_portal.exceptions;

import java.net.HttpURLConnection;

import java.security.KeyException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.objectweb.proactive.core.util.log.ProActiveLogger;

@Provider
public class KeyExceptionMapper implements ExceptionMapper<KeyException> {

	public Response toResponse(KeyException exception) {
		ExceptionToJson js = new ExceptionToJson();
		js.setErrorMessage(exception.getMessage());
		js.setHttpErrorCode(HttpURLConnection.HTTP_NOT_FOUND);
		js.setStackTrace(ProActiveLogger.getStackTraceAsString(exception));
		return Response.status(HttpURLConnection.HTTP_NOT_FOUND).entity(js)
				.build();
	}

}
