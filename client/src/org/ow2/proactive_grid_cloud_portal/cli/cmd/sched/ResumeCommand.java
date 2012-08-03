package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;

public class ResumeCommand extends AbstractCommand implements Command {

    public ResumeCommand() {
    }

    @Override
    public void execute() throws CLIException {
        HttpPut request = new HttpPut(resourceUrl("resume"));
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            boolean success = readValue(response, Boolean.TYPE);
            resultStack().push(success);
            if (success) {
                writeLine("Scheduler successfully resumed.");
            } else {
                writeLine("Cannot resume scheduler.");
            }
        } else {
            handleError(
                    "An error occurred while attempting to resume scheduler:",
                    response);
        }
    }

}
