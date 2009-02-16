/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.authentication;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;

/**
 * Class implements connection to the service with authentication. It attempts lookup authentication 
 * active object and checks that system is up and running (in another words authentication object is activated).
 * Provides an ability to connect in blocking and non blocking manner. 
 */
public abstract class Connection<T extends Authentication> implements Loggable, Serializable {

	private static final String ERROR_CANNOT_LOOKUP_AUTH = "Cannot lookup authentication active object.";
	private static final String ERROR_NOT_ACTIVATED = "System is initializing. Try to connect later.";
	private static final String ERROR_CONNECTION_INTERRUPTED = "Connection is interrupted.";
	
    private static final int PERIOD = 1000; // 1 sec	

	private Logger logger = getLogger();	
	private Class<? extends Authentication> clazz = null;
	
	public Connection(Class<? extends Authentication> clazz) {
		this.clazz = clazz;
	}

	/**
	 * Lookup of authentication active object
	 */
	private T lookupAuthentication(String url) throws Exception {

		logger.debug("Looking up authentication interface '" + url + "'");

		return (T) (PAActiveObject.lookupActive(clazz.getName(), url));
	}

	/**
	 * Connect to the service with given url. If it is not available or initializing throws an exception.
	 * 
	 * @param url the URL of the service to join.
	 * @return the service authentication interface at the specified URL.
	 */
	public T connect(String url) throws Exception {
		T authentication = lookupAuthentication(url);
		if (authentication.isActivated()) {
			return authentication;
		} else {
			throw new Exception(ERROR_NOT_ACTIVATED);
		}
	}

	/**
	 * Connects to the service using given URL. The current thread will be block until
	 * connection established or an error occurs.
	 */
	public T waitAndConnect(String url) throws Exception {
		return waitAndConnect(url, 0);
	}

	/**
	 * Connects to the service with a specified timeout value. A timeout of
	 * zero is interpreted as an infinite timeout. The connection will then
	 * block until established or an error occurs.
	 */
	public T waitAndConnect(String url, long timeout) throws Exception {

		T authentication = null;
		long leftTime = timeout == 0 ? Long.MAX_VALUE : timeout;

		// obtaining authentication active object
		while (leftTime > 0) {
			long startTime = System.currentTimeMillis();
			try {
				authentication = lookupAuthentication(url);
				if (authentication == null) {
					// strange situation : should not be here
					// simulating an exception during lookup
					throw new Exception(ERROR_CANNOT_LOOKUP_AUTH);
				}
				// success
				break;
			} catch (Exception e) {

				leftTime -= (System.currentTimeMillis() - startTime);
				if (leftTime < 0) {
					throw e;
				}
			}
			try {
				Thread.sleep(Math.min(PERIOD, leftTime));
			} catch (InterruptedException e) {
				throw new Exception(ERROR_CONNECTION_INTERRUPTED);
			}
		}

		// waiting until scheduling is initialized
		while (leftTime > 0) {
			long startTime = System.currentTimeMillis();

			if (authentication.isActivated()) {
				// success
				break;
			} else {

				leftTime -= (System.currentTimeMillis() - startTime);
				if (leftTime < 0) {
					throw new Exception(ERROR_NOT_ACTIVATED);
				}
			}
			try {
				Thread.sleep(Math.min(PERIOD, leftTime));
			} catch (InterruptedException e) {
				throw new Exception(ERROR_CONNECTION_INTERRUPTED);
			}
		}
		// TODO two cycles has the same pattern => the code can be unified
		return authentication;
	}
}
