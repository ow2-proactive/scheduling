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
import static org.ow2.proactive_grid_cloud_portal.cli.ResponseStatus.FORBIDDEN;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_REST_SCHEDULER_URL;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_DIR;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_FILE_EXT;
import static org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice.STARDARD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.digest.DigestUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.HelpCommand;
import org.ow2.proactive_grid_cloud_portal.cli.console.AbstractDevice;

/**
 * {@link Main} is the entry-point of stand-alone REST CLI application.
 * 
 */
public class Main {

	public static void main(String... args) {
		GnuParser parser = new GnuParser();
		CommandLine cli = null;
		try {
			cli = parser.parse(options(), args);
		} catch (ParseException pe) {
			try {
				writeError(new PrintWriter(System.err, true), pe.getMessage(),
						null);
				ApplicationContext.instance().setDevice(
						AbstractDevice.getConsole(STARDARD));
				(new HelpCommand()).execute();
			} catch (Exception e) {
				e.printStackTrace();
				// Ignore
			}
			System.exit(1);
		}

		AbstractDevice console = null;
		try {
			console = AbstractDevice.getConsole(AbstractDevice.JLINE);
		} catch (IOException ioe) {
			try {
				writeError(
						new PrintWriter(System.err, true),
						"An error occured when obtaining the console device ..",
						ioe);
			} catch (IOException ioe2) {
				// ignore
			}
			System.exit(1);
		}

		// obtain the session sigleton an initialize it
		ApplicationContext applicationContext = ApplicationContext.instance();
		applicationContext.setDevice(console);
		applicationContext.setObjectMapper(new ObjectMapper().configure(
				FAIL_ON_UNKNOWN_PROPERTIES, false));
		applicationContext.setSchedulerUrl(DFLT_REST_SCHEDULER_URL);

		// retrieve the (ordered) command list corresponding to command-line
		// arguments
		List<Command> commands = null;
		try {
			commands = CommandFactory.getCommandList(cli);
		} catch (Exception e) {
			try {
				writeError(writer(applicationContext), "", e);
			} catch (IOException ioe) {
				// Ignore
			}
			System.exit(1);
		}

		boolean authorizationError = false;

		try {
			executeCommandList(commands);
		} catch (RestCliException e) {
			if (FORBIDDEN.statusCode() == e.errorCode()
					&& !applicationContext.isNewSession()) {
				authorizationError = true;
			}
		} catch (Throwable error) {
			try {
				writeError(writer(applicationContext),
						"An error occured while executing ..", error);
			} catch (IOException e) {
				// Ignore
			}
		}

		/*
		 * in case of an existing session-id, the REST CLI reuses it without
		 * obtaining a new session-id even if a login with credentials
		 * specified. However if the REST server responds with an authorization
		 * error (e.g. due to session timeout), it re-executes the commands list
		 * after clearing the existing session. This will effectively re-execute
		 * the user command after with a new session-id.
		 */
		if (authorizationError) {
			try {

				String sessionIdentifier = (applicationContext.getUser() != null) ? applicationContext
						.getUser() : applicationContext.getAlias();

				if (sessionIdentifier != null) {
					File sessionFile = sessionFile(sessionIdentifier);
					if (sessionFile.exists()) {
						sessionFile.delete(); // clear session ..
					}
				}
				executeCommandList(commands);
			} catch (Throwable error) {
				try {
					writeError(writer(applicationContext),
							"An error occured while executing ..", error);
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	public static Options options() {
		Options options = new Options();
		for (RestCommand command : RestCommand.values()) {
			options.addOption((command.getArgsNum() == 0) ? createOption(
					command.getOpt(), command.getLongOpt(),
					command.getDescription()) : createOption(command.getOpt(),
					command.getLongOpt(), command.getDescription(),
					command.getArgsName(), command.getArgsNum(),
					command.isArgsRequired()));
		}
		return options;
	}

	private static void executeCommandList(List<Command> commands)
			throws Exception {
		for (Command c : commands) {
			c.execute();
		}
	}

	@SuppressWarnings("static-access")
	private static Option createOption(String opt, String longOpt,
			String description) {
		return OptionBuilder.withLongOpt(longOpt).withDescription(description)
				.hasArg(false).create(opt);
	}

	@SuppressWarnings("static-access")
	private static Option createOption(String opt, String longOpt,
			String description, String argsName, int argsNum,
			boolean isArgsRequired) {
		return OptionBuilder.withLongOpt(longOpt).withDescription(description)
				.hasArgs(argsNum).withArgName(argsName)
				.isRequired(isArgsRequired).create(opt);
	}

	private static File sessionFile(String username) {
		return new File(DFLT_SESSION_DIR, username + DFLT_SESSION_FILE_EXT);
	}

	private static void writeError(PrintWriter writer, String errorMsg,
			Throwable cause) throws IOException {
		writer.printf("%s%n", errorMsg);
		if (cause != null) {
			writer.printf("Error Message: %s%n", cause.getMessage());
			if (cause.getStackTrace() != null) {
				writer.printf("%s%n", "StackTrace:");
				cause.printStackTrace(writer);
			}
		}
	}

	private static PrintWriter writer(ApplicationContext context) {
		return new PrintWriter(context.getDevice().getWriter(), true);
	}

}
