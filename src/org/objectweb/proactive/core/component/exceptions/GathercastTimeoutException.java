package org.objectweb.proactive.core.component.exceptions;

import org.objectweb.proactive.core.ProActiveRuntimeException;

/**
 * Gathercast interface transform several invocations into a single invocation,
 * by gathering the invocation parameters and redistributing the results.
 * <p>
 * It is possible to configure a maximum time to wait until all connected client
 * interfaces have sent a request. If this timeout is reached before, a runtime
 * GathercastTimeoutException is thrown.
 * <p>
 * This exception is declared as a runtime exception so that invocations on
 * gathercast interfaces are <b>asynchronous</b><br>
 * 
 * @author Matthieu Morel
 * 
 */
public class GathercastTimeoutException extends ProActiveRuntimeException {

	public GathercastTimeoutException() {
		super();
	}

	public GathercastTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public GathercastTimeoutException(String message) {
		super(message);
	}

	public GathercastTimeoutException(Throwable cause) {
		super(cause);
	}

}
