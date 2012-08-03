package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import org.ow2.proactive_grid_cloud_portal.cli.CLIException;

public class SetSessionCommand extends AbstractCommand implements Command {
    private String session;

    public SetSessionCommand(String session) {
        this.session = session;
    }

    @Override
    public void execute() throws CLIException {
        context().setSessionId(session);
    }

}
