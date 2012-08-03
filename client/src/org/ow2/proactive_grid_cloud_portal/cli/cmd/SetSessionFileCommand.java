package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import java.io.File;

import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;

public class SetSessionFileCommand extends AbstractCommand implements Command {
    private String sessionFile;

    public SetSessionFileCommand(String sessionFile) {
        this.sessionFile = sessionFile;
    }

    @Override
    public void execute() throws CLIException {
        File file = new File(sessionFile);
        if (file.exists()) {
            context().setSessionId(FileUtility.readFileToString(file));

        } else {
            throw new CLIException(CLIException.REASON_INVALID_ARGUMENTS,
                    String.format("File does not exist: %s", sessionFile));
        }
    }
}
