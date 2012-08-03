package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import java.io.IOException;

import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

public class PrintSessionCommand extends AbstractCommand implements Command {

    public PrintSessionCommand() {
    }

    @Override
    public void execute() throws CLIException {
        String sessionId = context().getSessionId();
        if (!StringUtility.isEmpty(sessionId)) {
            try {
                context().getDevice().writeLine("%s", sessionId);
            } catch (IOException ioe) {
                throw new CLIException(CLIException.REASON_IO_ERROR, ioe);
            }
        }
    }

}
