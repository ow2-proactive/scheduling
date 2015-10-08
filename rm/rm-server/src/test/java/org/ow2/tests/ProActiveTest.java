/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.tests;

import java.security.Policy;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

/**
 * Just to set the security manager without a system property in the command
 * line.
 */
public class ProActiveTest {
	static {
		configureSecurityManager();
		configurePAHome();
		configureLog4j();
	}

	private static void configureLog4j() {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
	}

	private static void configureSecurityManager() {
		if (System.getProperty("java.security.policy") == null) {
			System.setProperty("java.security.policy", System.getProperty(PAResourceManagerProperties.RM_HOME.getKey())
					+ "/config/security.java.policy-server");

			Policy.getPolicy().refresh();
		}
	}

	private static void configurePAHome() {
		String rmHome = System.getProperty(PAResourceManagerProperties.RM_HOME.getKey());
		if (System.getProperty(CentralPAPropertyRepository.PA_HOME.getName()) == null && rmHome != null) {
			System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(), rmHome);
		}
	}

}
