/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.utils;

import java.security.Policy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.SecurityManagerConfigurator;


public class SecurityPolicyLoader {

    private static final Logger logger = Logger.getLogger(SecurityPolicyLoader.class);

    public static void configureSecurityManager(String securityFilePath, long refreshPeriod) {
        SecurityManagerConfigurator.configureSecurityManager(securityFilePath);
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                reloadPolicy();
            }
        }, 0, refreshPeriod, TimeUnit.SECONDS);

    }

    public static ReentrantLock lock = new ReentrantLock();

    public static void reloadPolicy() {
        try {
            lock.lockInterruptibly();
            Policy.getPolicy().refresh();
        } catch (InterruptedException e) {
            logger.warn("Load policy thread interrupted", e);
        } finally {
            lock.unlock();
        }
    }

}
