package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import java.io.File;

import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.json.TaskResultView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;

public class OutputCommand extends AbstractCommand implements Command {
    private String pathname;

    public OutputCommand(String pathname) {
        this.pathname = pathname;
    }

    @Override
    public void execute() throws CLIException {
        File outFile = new File(pathname);
        if (outFile.exists()) {
            outFile.delete();
        }
        if (!context().emptyResultStack()) {
            Object result = resultStack().peek();
            if (result instanceof String) {
                FileUtility.writeStringToFile(outFile, (String) result);
            } else if (result instanceof TaskResultView) {
                FileUtility
                        .writeByteArrayToFile(
                                ((TaskResultView) result).getSerializedValue(),
                                outFile);
            } else {
                FileUtility.writeObjectToFile(result, outFile);
            }
        } else {
            writeLine("No result available to write.");
        }
    }

}
