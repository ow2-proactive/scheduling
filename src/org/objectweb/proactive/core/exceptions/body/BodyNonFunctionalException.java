package org.objectweb.proactive.core.exceptions.body;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;

public abstract class BodyNonFunctionalException extends NonFunctionalException {
	public BodyNonFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
