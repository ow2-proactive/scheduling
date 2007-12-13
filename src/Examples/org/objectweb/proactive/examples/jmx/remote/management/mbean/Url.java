/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.examples.jmx.remote.management.mbean;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.objectweb.proactive.examples.jmx.remote.management.exceptions.JMXException;
import org.objectweb.proactive.examples.jmx.remote.management.jmx.IJmx;
import org.objectweb.proactive.examples.jmx.remote.management.utils.Constants;


public class Url implements UrlMBean, IJmx {
    private String url;
    private ObjectName on;

    public Url(String url) {
        this.url = url;
        try {
            this.on = new ObjectName(Constants.ON_URL);
            register();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
        } catch (MBeanRegistrationException e) {
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            e.printStackTrace();
        } catch (JMXException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return this.url;
    }

    public void register() throws InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, JMXException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(this, this.on);
    }

    public void unregister() throws InstanceNotFoundException, MBeanRegistrationException, JMXException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.on);
    }
}
