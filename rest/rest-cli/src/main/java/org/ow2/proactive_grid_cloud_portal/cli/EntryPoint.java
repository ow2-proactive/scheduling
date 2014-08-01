/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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

package org.ow2.proactive_grid_cloud_portal.cli;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_UNAUTHORIZED_ACCESS;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_REST_SCHEDULER_URL;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.PROP_RENEW_SESSION;
import static org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.PROP_PERSISTED_SESSION;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;
import org.ow2.proactive_grid_cloud_portal.cli.console.JLineDevice;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

import com.google.common.collect.ObjectArrays;


public abstract class EntryPoint {

    protected abstract String resourceType();

    protected void run(String... args) {

        CommandFactory commandFactory = null;
        CommandLine cli = null;
        AbstractDevice console = null;

        ApplicationContext currentContext = ApplicationContextImpl.currentContext();

        try {
            commandFactory = getCommandFactory();
            console = AbstractDevice.getConsole(AbstractDevice.JLINE);
            ((JLineDevice) console).setCommands(ObjectArrays.concat(commandFactory.supportedCommandEntries(),
                    CommandSet.INTERACTIVE_COMMANDS, CommandSet.Entry.class));
            currentContext.setDevice(console);

            Options options = commandFactory.supportedOptions();
            cli = (new GnuParser()).parse(options, args);

        } catch (IOException ioe) {
            writeError(new PrintWriter(System.err, true), ioe.getMessage(), ioe);
            System.exit(1);

        } catch (ParseException pe) {
            writeError((PrintWriter) console.getWriter(), pe.getMessage(), pe);
            // print usage
            Command help = commandFactory.commandForOption(new Option("h", null));
            if (help != null) {
                help.execute(currentContext);
            }
            System.exit(1);
        }

        currentContext.setObjectMapper(new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false));
        currentContext.setRestServerUrl(DFLT_REST_SCHEDULER_URL);
        currentContext.setResourceType(resourceType());

        // retrieve the (ordered) command list corresponding to command-line
        // arguments
        List<Command> commands = null;
        try {
            commands = commandFactory.getCommandList(cli, currentContext);
        } catch (CLIException e) {
            writeError(writer(currentContext), "", e);
            System.exit(1);
        }

        boolean retryLogin = false;

        try {
            executeCommandList(commands, currentContext);
        } catch (CLIException error) {
            if (REASON_UNAUTHORIZED_ACCESS == error.reason() && hasLoginCommand(commands)) {
                retryLogin = true;
            } else {
                writeError(writer(currentContext), "An error occurred.", error);
                System.exit(1);
            }
        } catch (Throwable e) {
            writeError(writer(currentContext), "An error occurred.", e);
            System.exit(1);
        }

        /*
         * in case of an existing session-id, the REST CLI reuses it without
         * obtaining a new session-id even if a login with credentials
         * specified. However if the REST server responds with an authorization
         * error (e.g. due to session timeout), it re-executes the commands list
         * with AbstractLoginCommand.PROP_RENEW_SESSION property set to 'true'.
         * This will effectively re-execute the user command with a new
         * session-id from server.
         */
        if (retryLogin && currentContext.getProperty(PROP_PERSISTED_SESSION, Boolean.TYPE, false)) {
            try {
                currentContext.setProperty(PROP_RENEW_SESSION, true);
                executeCommandList(commands, currentContext);
            } catch (Throwable error) {
                writeError(writer(currentContext), "An error occurred while execution:", error);
                System.exit(1);
            }
        }
    }

    private void executeCommandList(List<Command> commandList, ApplicationContext currentContext)
            throws CLIException {
        for (Command command : commandList) {
            command.execute(currentContext);
        }
    }

    private void writeError(PrintWriter writer, String errorMsg, Throwable cause) {
        writer.printf("%n%s", errorMsg);
        if (cause != null) {
            if (cause.getMessage() != null) {
                writer.printf("%n%nError Message: %s", cause.getMessage());
            }
            if (cause.getStackTrace() != null) {
                writer.printf("%n%nStackTrace: %s", StringUtility.stackTraceAsString(cause));
            }
        }
    }

    private PrintWriter writer(ApplicationContext context) {
        return new PrintWriter(context.getDevice().getWriter(), true);
    }

    private CommandFactory getCommandFactory() {
        return CommandFactory.getCommandFactory(CommandFactory.Type.ALL);
    }

    private boolean hasLoginCommand(List<Command> commandList) {
        for (Command c : commandList) {
            if (c instanceof AbstractLoginCommand) {
                return true;
            }
        }
        return false;
    }
}
