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
package unitTests;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfoImpl;


@RunWith(Suite.class)
/**
 * All in-place Unit tests must be declared here otherwise they will not be run.
 * 
 * Please use the following convention:
 * <ul>
 * <li>Add a static inner class to the class. Use <b>UnitTest</b> a prefix the for classname</li>
 * <li>Add this class to the following <b>SuiteClasses</b> annotation</li>
 * </ul>
 */
@SuiteClasses( { org.objectweb.proactive.core.util.CircularArrayList.UnitTestCircularArrayList.class,
        HostInfoImpl.UnitTestHostInfoImpl.class })
public class UnitTests {
    static final public Logger logger = Logger.getLogger("testsuite");
}
