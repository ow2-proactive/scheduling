/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.gui.common;

import java.io.Serializable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Special thread which is used to detect failure of active object instance.
 * <p/>  
 * Active object can fail due to different reasons and three type of ping failure
 * are distinguished:
 * <ul>
 * <li>error: ping call failed with exception
 * <li>timeout: ping call didn't finish in the specified pingTimeout time
 * (this may happen if there are some network problems and remote ping call hangs) 
 * <li>false: ping call returned 'false'
 * </ul>
 * 
 * 
 * @author The ProActive Team
 *
 */
public class ActiveObjectPingerThread extends Thread implements Serializable {

	public static final long DEFAULT_PING_FREQUENCY = 5000;

	public static final long DEFAULT_PING_TIMEOUT = 5000;
	
	public static interface PingListener {
		
		void onPingFalse();
		
		void onPingError();
		
		void onPingTimeout();
	}

	private class PingTimeoutHandler implements Runnable {
		@Override
		@SuppressWarnings("deprecation")
		public void run() {
			pingTimeout();
			/*
			 * it is safe to use 'stop' here since pinger thread
			 * doesn't have any state which can be corrupted after 
			 * thread stopping 
			 */
			stop();
		}
	}
	
	private final ScheduledThreadPoolExecutor pingTimeoutWatchdogTimer;
	
	private final ActiveObjectProxy<?> activeObjectProxy; 
	
	private final long pingFrequency;
	
	private final long pingTimeout;
	
	private final PingListener pingListener;
	
	private volatile boolean stopPing;
	
	public ActiveObjectPingerThread(ActiveObjectProxy<?> activeObjectProxy, 
			long pingFrequency,
			long pingTimeout,
			PingListener pingListener) {
		this.activeObjectProxy = activeObjectProxy;
		this.pingFrequency = pingFrequency;
		this.pingTimeout = pingTimeout;
		this.pingListener = pingListener;
		this.pingTimeoutWatchdogTimer = new ScheduledThreadPoolExecutor(1);
	}

	public ActiveObjectPingerThread(ActiveObjectProxy<?> activeObjectProxy, 
			PingListener pingListener) {
		this(activeObjectProxy, DEFAULT_PING_FREQUENCY, DEFAULT_PING_TIMEOUT, pingListener);
	}
	
	public void stopPinger() {
		stopPing = true;
		interrupt();
	}
	
	public boolean isStopped() {
		return stopPing;
	}
	
	public void run() {
		PingTimeoutHandler pingTimeoutHandler = new PingTimeoutHandler();
        try {
    		while (!stopPing) {
                try {
                    Thread.sleep(pingFrequency);
                } catch (InterruptedException e) {
                    break;
                }
                
                // ping call is synchronous, need to use special time to detect ping timeout
                ScheduledFuture<?> future = pingTimeoutWatchdogTimer.schedule(pingTimeoutHandler, 
                		pingTimeout, 
                		TimeUnit.MILLISECONDS);

                boolean pingResult;
                try {
                	pingResult = activeObjectProxy.syncPingActiveObject(); 
                } catch (Throwable t) {
                	pingError(t);
                	break;
                } finally {
                    if (!future.cancel(false)) {
                    	// timeout handler already was executed
                    	break;
                    }
                }

                if (!pingResult) {
                	pingFalse();
                	break;
                }
            }
        } finally {
            pingTimeoutWatchdogTimer.shutdown();
        }
        // System.out.println("Pinger is finishing");
	}

	private synchronized void pingError(Throwable t) {
		if (!stopPing) {
			stopPing = true;
			System.out.println("Ping error: " + t);
			pingListener.onPingError();
		}
	}
	private synchronized void pingFalse() {
		if (!stopPing) {
			stopPing = true;
			System.out.println("Ping false");
			pingListener.onPingFalse();
		}
	}

	private synchronized void pingTimeout() {
		if (!stopPing) {
			stopPing = true;
			System.out.println("Ping timeout");
			pingListener.onPingTimeout();
		}
	}
	
}
