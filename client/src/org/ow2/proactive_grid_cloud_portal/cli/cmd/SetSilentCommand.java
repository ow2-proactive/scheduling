package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import org.ow2.proactive_grid_cloud_portal.cli.CLIException;

public class SetSilentCommand extends AbstractCommand implements Command {

    @Override
    public void execute() throws CLIException {
        context().setSilent(true);
    }

}
