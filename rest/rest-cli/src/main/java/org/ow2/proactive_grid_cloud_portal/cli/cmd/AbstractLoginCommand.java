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

import java.io.File;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_DIR;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_FILE_EXT;


public abstract class AbstractLoginCommand extends AbstractCommand implements Command {

    public static final boolean RENEW_SESSION_BY_DEFAULT = false;
    public static final boolean PERSIST_SESSION_BY_DEFAULT = true;

    public static final String PROP_PERSISTED_SESSION = "org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.persistedSession";
    public static final String PROP_RENEW_SESSION = "org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.renewSession";
    public static final String PROP_ENABLE_PERSISTENCE = "org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.enablePersistence";

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        setAlias(currentContext);
        boolean renewSession = currentContext.getProperty(PROP_RENEW_SESSION, Boolean.TYPE,
                RENEW_SESSION_BY_DEFAULT);
        boolean persistSession = currentContext.getProperty(PROP_ENABLE_PERSISTENCE, Boolean.TYPE,
                PERSIST_SESSION_BY_DEFAULT);
        String sessionId = currentContext.getSessionId();

        if (!StringUtility.isEmpty(sessionId) && !renewSession) {
            // session id is set explicitly and no update request. But in case
            // of a failure we can always retry.
            currentContext.setProperty(PROP_PERSISTED_SESSION, true);
            currentContext.setProperty(PROP_ENABLE_PERSISTENCE, false);
            return;
        }

        if (renewSession) {
            writeLine(currentContext, "renewing session ...");
            sessionId = getSessionIdFromServer(currentContext);
            writeLine(currentContext, "Session id successfully renewed.");

        } else {
            // at this point, session-id is null
            if (persistSession) {
                sessionId = getSessionIdFromFile(currentContext);
            }
            if (sessionId == null) {
                sessionId = getSessionIdFromServer(currentContext);
            }

        }
        currentContext.setSessionId(sessionId);
        resultStack(currentContext).push(sessionId);
    }

    protected abstract String login(ApplicationContext currentContext) throws CLIException;

    protected abstract String getAlias(ApplicationContext currentContext);

    protected abstract void setAlias(ApplicationContext currentContext);

    private String getSessionIdFromFile(ApplicationContext currentContext) {
        File sessionFile = sessionFile(currentContext);
        if (sessionFile.exists()) {
            currentContext.setProperty(PROP_PERSISTED_SESSION, true);
            return FileUtility.readFileToString(sessionFile);
        }
        return null;
    }

    private String getSessionIdFromServer(ApplicationContext currentContext) {
        currentContext.setProperty(PROP_PERSISTED_SESSION, false);
        String sessionId = login(currentContext);
        if (currentContext.getProperty(PROP_ENABLE_PERSISTENCE, Boolean.TYPE, true)) {
            writeToSessionFile(sessionFile(currentContext), sessionId, currentContext);
        }
        return sessionId;
    }

    private void writeToSessionFile(File sessionFile, String sessionId, ApplicationContext currentContext) {
        if (sessionFile.exists()) {
            sessionFile.delete();
        } else {
            File parentFile = sessionFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
        }
        FileUtility.writeStringToFile(sessionFile, sessionId);
        if (!setOwnerOnly(sessionFile)) {
            writeLine(currentContext,
                    "Warning! Possible security risk: unable to limit access rights of session-id file '%s'",
                    sessionFile.getAbsoluteFile());
        }

    }

    private File sessionFile(ApplicationContext currentContext) {
        String filename = (new StringBuilder()).append(getAlias(currentContext)).append('-').append(
                currentContext.getResourceType()).append(DFLT_SESSION_FILE_EXT).toString();
        return new File(DFLT_SESSION_DIR, filename);
    }

    private boolean setOwnerOnly(File file) {
        // effectively set file permission to 600
        return file.setReadable(false, false) && file.setReadable(true, true) &&
            file.setWritable(false, false) && file.setWritable(true, true) && file.setExecutable(false);
    }

}
