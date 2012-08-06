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
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_DIR;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_FILE_EXT;

import java.io.File;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;

public abstract class AbstractLoginCommand extends AbstractCommand implements
        Command {

    public static final String RETRY_LOGIN = "org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.retryLogin";
    public static final String RENEW_SESSION = "org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.renew";
    private static final String PERSIST_SESSION = "org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.default";

    @Override
    public void execute() throws CLIException {
        ApplicationContext context = context();
        Boolean renewSession = context.getProperty(RENEW_SESSION, Boolean.TYPE,
                false);
        Boolean persistSession = context.getProperty(PERSIST_SESSION,
                Boolean.TYPE, false);
        String sessionId = context.getSessionId();

        if (sessionId != null && !renewSession) {
            // session id is set explicitly and no update request. But in case
            // of a failure we can always retry.
            context.setProperty(RETRY_LOGIN, true);
            return;
        }

        if (sessionId == null) {
            File sessionFile = sessionFile();
            if (sessionFile.exists()) {
                sessionId = FileUtility.readFileToString(sessionFile);
                context.setProperty(RETRY_LOGIN, true);

            } else {
                sessionId = login();
                context.setProperty(RETRY_LOGIN, false);
                writeToSessionFile(sessionFile, sessionId);
            }
            context.setProperty(PERSIST_SESSION, true);

        } else {
            if (renewSession) {
                sessionId = login();
                context.setProperty(RETRY_LOGIN, false);
                if (persistSession) {
                    File sessionFile = sessionFile();
                    if (sessionFile.exists()) {
                        sessionFile.delete();
                    }
                    writeToSessionFile(sessionFile, sessionId);
                    writeLine("Session id successfully renewed.");
                }
            }
        }
        context.setSessionId(sessionId);
        resultStack().push(sessionId);
    }

    protected abstract String login() throws CLIException;

    protected abstract String alias();

    private void writeToSessionFile(File sessionFile, String sessionId) {
        File parentFile = sessionFile.getParentFile();
        if (parentFile.exists()) {
            parentFile.mkdir();
        }
        FileUtility.writeStringToFile(sessionFile, sessionId);
        if (!setOwnerOnly(sessionFile)) {
            writeLine(
                    "Warning! Possible security risk: unable to limit access rights of session-id file '%s'",
                    sessionFile.getAbsoluteFile());
        }

    }

    private File sessionFile() {
        String filename = (new StringBuilder()).append(alias()).append('-')
                .append(context().getResourceType())
                .append(DFLT_SESSION_FILE_EXT).toString();
        return new File(DFLT_SESSION_DIR, filename);
    }

    private boolean setOwnerOnly(File file) {
        // effectively set file permission to 600
        return file.setReadable(false, false) && file.setReadable(true, true)
                && file.setWritable(false, false)
                && file.setWritable(true, true) && file.setExecutable(false);
    }

}
