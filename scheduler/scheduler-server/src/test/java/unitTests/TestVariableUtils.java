/*
 * ################################################################
 *
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package unitTests;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.ow2.proactive.scheduler.common.util.VariablesUtil.filterAndUpdate;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.util.VariablesUtil;

public class TestVariableUtils {
    private static final String testString = "A${foo}C";

    @Test
    public void testFilterAndUpdateWithVariableMap() {
        Map<String, String> variables = singletonMap("foo", "B");
        variables.put("foo", "B");
        String updated = filterAndUpdate(testString, variables);
        Assert.assertEquals("ABC", updated);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilterAndUpdateWithUnknownVariable() {
        VariablesUtil.filterAndUpdate(testString, emptyMap());
    }

    @Test
    public void testFilterAndUpdateWithSystemProperties() {
        System.setProperty("bar", "B");
        Map<String, String> variables = new HashMap<String, String>();
        String updated = VariablesUtil.filterAndUpdate("A${bar}C", variables);
        Assert.assertEquals("ABC", updated);
        Assert.assertEquals("B", variables.get("bar"));
    }
}
