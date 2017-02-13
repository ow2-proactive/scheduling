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

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.CA_CERTS;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.CA_CERTS_PASSWORD;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.COMMON_HELP;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.CREDENTIALS;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.DEBUG;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.INFRASTRUCTURE;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.INSECURE;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.LOGIN;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.OUTPUT;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.PASSWORD;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.POLICY;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.RM_HELP;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.SCHEDULER_HELP;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.SESSION_ID;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.SESSION_ID_FILE;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.SILENT;
import static org.ow2.proactive_grid_cloud_portal.cli.CommandSet.URL;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DEFAULT_CREDENTIALS_PATH;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_DIR;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.ow2.proactive.utils.ClasspathUtils;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.ImodeCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginWithCredentialsCommand;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HierarchicalMap;


/**
 * {@link CommandFactory} builds an ordered {@link Command} list for a given
 * user argument set.
 */
public abstract class CommandFactory {

    public static enum Type {
        SCHEDULER,
        RM,
        ALL;
    }

    private static final Map<String, CommandSet.Entry> commonCmdMap;

    static {
        commonCmdMap = new HashMap<>();
        for (CommandSet.Entry entry : CommandSet.COMMON_COMMANDS) {
            commonCmdMap.put(opt(entry), entry);
        }
    }

    protected final Map<String, CommandSet.Entry> cmdMap = new HierarchicalMap<>(CommandFactory.supportedCommandMap());

    public static CommandFactory getCommandFactory(Type type) {
        switch (type) {
            case SCHEDULER:
                return new SchedulerCommandFactory();
            case RM:
                return new RmCommandFactory();
            case ALL:
                return new CommonCommandFactory();
            default:
                throw new CLIException(REASON_INVALID_ARGUMENTS,
                                       String.format("Unknown AbstractCommandFactory type['%s']", type));
        }
    }

    static void put(CommandSet.Entry entry, Map<String, CommandSet.Entry> map) {
        map.put(opt(entry), entry);
    }

    static Map<String, CommandSet.Entry> supportedCommandMap() {
        return commonCmdMap;
    }

    static String opt(CommandSet.Entry entry) {
        return entry.opt();
    }

    public Options supportedOptions() {
        return createOptions(cmdMap.values());
    }

    public List<Command> getCommandList(CommandLine cli, ApplicationContext currentContext) {
        throw new UnsupportedOperationException();
    }

    public void addCommandEntry(CommandSet.Entry entry) {
        cmdMap.put(opt(entry), entry);
    }

    public Command commandForOption(Option option) {
        return getCommandForOption(option, cmdMap);
    }

    public CommandSet.Entry[] supportedCommandEntries() {
        Collection<CommandSet.Entry> entries = cmdMap.values();
        return entries.toArray(new CommandSet.Entry[entries.size()]);
    }

    /**
     * Returns an ordered {@link Command} list for specified user arguments.
     *
     * @param cli the command-line arguments
     * @return an ordered {@link Command} list.
     */
    protected List<Command> getCommandList(CommandLine cli, Map<String, Command> map,
            ApplicationContext currentContext) {
        LinkedList<Command> list = new LinkedList<>();

        if (map.containsKey(opt(COMMON_HELP))) {
            list.add(map.remove(opt(COMMON_HELP)));
            return list;
        }

        if (map.containsKey(opt(RM_HELP))) {
            list.add(map.remove(opt(RM_HELP)));
            return list;
        }

        if (map.containsKey(opt(SCHEDULER_HELP))) {
            list.add(map.remove(opt(SCHEDULER_HELP)));
            return list;
        }

        if (map.containsKey(opt(SILENT))) {
            list.add(map.remove(opt(SILENT)));
        }

        if (map.containsKey(opt(DEBUG))) {
            list.add(map.remove(opt(DEBUG)));
        }

        if (map.containsKey(opt(URL))) {
            list.addFirst(map.remove(opt(URL)));
        }

        if (map.containsKey(opt(INSECURE))) {
            list.add(map.remove(opt(INSECURE)));

        } else if (map.containsKey(opt(CA_CERTS))) {
            list.add(map.remove(opt(CA_CERTS)));

            if (map.containsKey(opt(CA_CERTS_PASSWORD))) {
                list.add(map.remove(opt(CA_CERTS_PASSWORD)));
            }
        }

        if (map.containsKey(opt(SESSION_ID))) {
            list.add(map.remove(opt(SESSION_ID)));

        } else if (map.containsKey(opt(SESSION_ID_FILE))) {
            list.add(map.remove(opt(SESSION_ID_FILE)));
        }

        if (map.containsKey(opt(PASSWORD))) {
            list.add(map.remove(opt(PASSWORD)));
        }

        if (map.containsKey(opt(LOGIN))) {
            list.add(map.remove(opt(LOGIN)));

        } else if (map.containsKey(opt(CREDENTIALS))) {
            list.add(map.remove(opt(CREDENTIALS)));

        } else {
            // auto login
            String resourceType = currentContext.getResourceType();
            String filename = resourceType + ".cc";
            File credFile = new File(DFLT_SESSION_DIR, filename);
            if (credFile.exists()) {
                list.add(new LoginWithCredentialsCommand(credFile.getAbsolutePath(), true));
            } else {
                String schedulerHome = ClasspathUtils.findSchedulerHome();
                File defaultCredentials = new File(schedulerHome, DEFAULT_CREDENTIALS_PATH);
                if (defaultCredentials.exists()) {
                    list.add(new LoginWithCredentialsCommand(defaultCredentials.getAbsolutePath(), true));
                }
            }
        }

        if (map.containsKey(opt(INFRASTRUCTURE))) {
            list.add(map.remove(opt(INFRASTRUCTURE)));
        }

        if (map.containsKey(opt(POLICY))) {
            list.add(map.remove(opt(POLICY)));
        }

        if (map.isEmpty()) {
            list.add(new ImodeCommand());
        } else {
            Command output = map.remove(opt(OUTPUT));
            list.addAll(map.values());
            if (output != null) {
                list.add(output);
            }
        }

        return list;
    }

    protected Map<String, Command> commandMapInstance(CommandLine cli,
            final Map<String, CommandSet.Entry> supportedCommandEntryMap) {
        Map<String, Command> cliCommands = new HashMap<>();
        for (Option o : cli.getOptions()) {
            cliCommands.put(o.getOpt(), getCommandForOption(o, supportedCommandEntryMap));
        }
        return cliCommands;
    }

    protected Command getCommandForOption(Option opt, final Map<String, CommandSet.Entry> commandMap) {
        return commandMap.get(opt.getOpt()).commandObject(opt);
    }

    protected Options createOptions(Collection<CommandSet.Entry> entries) {
        Options options = new Options();

        for (CommandSet.Entry entry : entries) {
            Option option = new Option(entry.opt(), entry.longOpt(), entry.hasArgs(), entry.description());

            option.setArgName(entry.argNames());
            option.setArgs(entry.numOfArgs());
            option.setOptionalArg(entry.hasOptionalArg());

            options.addOption(option);
        }
        return options;
    }

}
