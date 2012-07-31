package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;

public class PauseCommand extends AbstractCommand implements Command {

    public PauseCommand() {
    }

    @Override
    public void execute() throws CLIException {
        HttpPut request = new HttpPut(resourceUrl("pause"));
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            boolean success = readValue(response, Boolean.TYPE);
            if (success) {
                writeLine("Scheduler successfully paused.");
            } else {
                writeLine("Cannot pause scheduler.");
            }
        } else {
            handleError(
                    "An error occurred while attempting to pause scheduler:",
                    response);
        }
    }

}
