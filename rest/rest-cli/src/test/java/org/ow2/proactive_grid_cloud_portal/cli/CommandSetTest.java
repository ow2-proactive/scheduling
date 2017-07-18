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
package org.ow2.proactive_grid_cloud_portal.cli;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.collect.ImmutableList;


/**
 * The class is in charge to test that all
 * {@link org.ow2.proactive_grid_cloud_portal.cli.CommandSet.Entry} defined in
 * {@link CommandSet} class have their number of expected parameters that match
 * the number of tokens used the argNames.
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
        String regex = "\\[(.*?)\\]";
        Pattern pattern = Pattern.compile(regex);
        String optionalArgNames = "";
        int optionals = 0;

        if (entry.argNames() != null) {
            String argNames = entry.argNames();
            Matcher matcher = pattern.matcher(argNames);

            while (matcher.find()) {
                optionals = matcher.group().split(" ").length;
                optionalArgNames = matcher.group();
            }

            if (!argNames.trim().isEmpty()) {
                nbArgsBasedOnName = argNames.split(" ").length;
                if (entry.hasArgs()) {
                    nbArgsBasedOnName = nbArgsBasedOnName - optionals;
                } else if (entry.hasOptionalArg()) {
                    nbArgsBasedOnName = optionals;
                }
            }

            if (argNames.contains("...") && !optionalArgNames.contains("|") && !optionalArgNames.contains("...")) {
                nbArgsBasedOnName = Option.UNLIMITED_VALUES;
            }

        }

        if (entry.numOfArgs() != nbArgsBasedOnName) {
            nbArgsBasedOnName = numberComplexOptionalArgs(entry);
        }

        Assert.assertEquals("Option '" + entry.longOpt() + "' does not have argNames matching number of args",
                            entry.numOfArgs(),
                            nbArgsBasedOnName);
    }

    public int numberComplexOptionalArgs(CommandSet.Entry entry) {
        int numberArgs = 0;
        final String regex = "(\\\\w+)=\\\"([^\\\"]+)\\\"|([^\\\\s]+)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(entry.argNames());

        while (matcher.find()) {
            numberArgs = matcher.group().split(" ").length;
        }
        return numberArgs;

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
