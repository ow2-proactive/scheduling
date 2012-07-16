package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.ow2.proactive_grid_cloud_portal.cli.ResponseStatus.OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;

public class PauseSchedulerCommand extends AbstractCommand implements Command {

    public PauseSchedulerCommand() {
    }

    @Override
    public void execute() throws Exception {
        HttpPut request = new HttpPut(resourceUrl("pause"));
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            boolean success = readValue(response, Boolean.TYPE);
            if (success) {
                writeLine("Scheduler is paused");
            } else {
                writeLine("Scheudler cannot be paused");
            }
        } else {
            handleError(
                    "An error occured while attempting to pause the scheduler ..",
                    response);
        }
    }

}
