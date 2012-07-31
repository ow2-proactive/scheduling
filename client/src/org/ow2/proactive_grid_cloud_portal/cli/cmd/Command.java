package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import org.ow2.proactive_grid_cloud_portal.cli.CLIException;

/**
 * {@link Command} represents an object whose responsibility is to execute a
 * specific REST CLI command. If necessary it uses the REST API, creates the
 * appropriate REST server request and process the response.
 */
public interface Command {

    /**
     * Executes the specific REST CLI command.
     * 
     * @throws Exception
     *             if an error occurred while executing the command.
     */
    public void execute() throws CLIException;

}
