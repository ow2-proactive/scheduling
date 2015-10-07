/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.cli;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * The class is in charge to test that all {@link org.ow2.proactive_grid_cloud_portal.cli.CommandSet.Entry}
 * defined in {@link CommandSet} class have their number of expected parameters that match the number of
 * tokens used the argNames.
 */
@RunWith(Parameterized.class)
public class CommandSetTest {

    private CommandSet.Entry entry;

    // field used by reflection with Junit
    private String testName;

    public CommandSetTest(CommandSet.Entry entry, String testName) {
        this.entry = entry;
        this.testName = testName;
    }

    @Test
    public void testThatArgNamesMatchNumberOfArgs() throws ParseException, IllegalAccessException {
        int nbArgsBasedOnName = Option.UNINITIALIZED;

        if (entry.argNames() != null) {
            String argNames = entry.argNames();

            if (entry.hasOptionalArg()) {
                // argNames description of optional arguments is assumed to start with '['
                argNames = argNames.substring(0, argNames.indexOf("["));
            }

            if (!argNames.trim().isEmpty()) {
                nbArgsBasedOnName = argNames.split(" ").length;
            }

            if (argNames.contains("...")) {
                nbArgsBasedOnName = Option.UNLIMITED_VALUES;
            }
        }

        Assert.assertEquals(
                "Option '" + entry.longOpt() + "' does not have argNames matching number of args",
                entry.numOfArgs(), nbArgsBasedOnName);
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() throws IllegalAccessException {
        List<CommandSet.Entry> availableCommands = getAvailableCommands();

        Object[][] result = new Object[availableCommands.size()][2];

        for (int i = 0; i < availableCommands.size(); i++) {
            CommandSet.Entry command = availableCommands.get(i);
            String name = command.longOpt();

            if (name == null) {
                name = command.jsCommand();
            }

            result[i][0] = command;
            result[i][1] = "testThatArgNamesMatchNumberOfArgsForOption" + name;
        }

        return ImmutableList.copyOf(result);
    }

    private static ImmutableList<CommandSet.Entry> getAvailableCommands() throws IllegalAccessException {
        Class<CommandSet> commandSetClass = CommandSet.class;

        Field[] declaredFields = commandSetClass.getFields();

        ImmutableList.Builder<CommandSet.Entry> builder = ImmutableList.builder();

        for (Field field : declaredFields) {
            if (field.getType().isAssignableFrom(CommandSet.Entry.class)) {
                builder.add((CommandSet.Entry) field.get(null));
            }
        }

        return builder.build();
    }

}
