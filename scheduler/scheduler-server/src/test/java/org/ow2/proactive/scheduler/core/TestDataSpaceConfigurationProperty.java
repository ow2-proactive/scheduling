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
package org.ow2.proactive.scheduler.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.ow2.proactive.utils.Tools;
import org.ow2.tests.ProActiveTestClean;


/**
 * TestDataSpaceConfigurationProperty
 *
 * @author The ProActive Team
 **/
public class TestDataSpaceConfigurationProperty extends ProActiveTestClean {

    @Test
    public void testPropertyParsing() throws Throwable {
        assertArrayEquals(new String[0], Tools.dataSpaceConfigPropertyToUrls("  \"\"  "));
        assertArrayEquals(new String[0], Tools.dataSpaceConfigPropertyToUrls("  "));
        assertArrayEquals(new String[] { "a" }, Tools.dataSpaceConfigPropertyToUrls(" \"a\"  "));
        assertArrayEquals(new String[] { "a" }, Tools.dataSpaceConfigPropertyToUrls("a"));
        assertArrayEquals(new String[] { "a" }, Tools.dataSpaceConfigPropertyToUrls(" a  "));
        assertArrayEquals(new String[] { "a b" }, Tools.dataSpaceConfigPropertyToUrls(" \"a b\"  "));
        assertArrayEquals(new String[] { "a", "b" }, Tools.dataSpaceConfigPropertyToUrls(" a b  " + ""));
        assertArrayEquals(new String[] { "a b c" }, Tools.dataSpaceConfigPropertyToUrls(" \"a b c\"  "));
        assertArrayEquals(new String[] { "a", "b", "c" }, Tools.dataSpaceConfigPropertyToUrls("  a b c  "));
        assertArrayEquals(new String[] { "a b c", "d e f" },
                          Tools.dataSpaceConfigPropertyToUrls(" \"a b c\"    \"d e f\"   "));
        assertArrayEquals(new String[] { "a b c d e f" }, Tools.dataSpaceConfigPropertyToUrls("   \"a b c d e f\"   "));
        assertArrayEquals(new String[] { "a", "b", "c", "d", "e", "f" },
                          Tools.dataSpaceConfigPropertyToUrls("   a b c   d e    f "));
    }
}
