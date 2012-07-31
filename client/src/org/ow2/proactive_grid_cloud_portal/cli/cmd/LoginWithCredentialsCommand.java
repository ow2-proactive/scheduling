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

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_DIR;
import static org.ow2.proactive_grid_cloud_portal.cli.RestConstants.DFLT_SESSION_FILE_EXT;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;

public class LoginWithCredentialsCommand extends AbstractCommand implements
        Command {
    private String pathname;

    public LoginWithCredentialsCommand(String pathname) {
        this.pathname = pathname;
    }

    @Override
    public void execute() throws Exception {
        File credentialFile = new File(pathname);
        String alias = FileUtility.md5Checksum(credentialFile);
        File userSessionFile = userSessionFile(alias);
        ApplicationContext context = context();
        if (userSessionFile.exists()) {
            context.setAlias(alias);
            context.setCredFilePathname(pathname);
            context.setSessionId(FileUtility.read(userSessionFile));
            context.setNewSession(false);
            return;
        }

        HttpPost request = new HttpPost(resourceUrl("login"));
        MultipartEntity entity = new MultipartEntity();
        entity.addPart("credential",
                new ByteArrayBody(FileUtility.byteArray(credentialFile),
                        APPLICATION_OCTET_STREAM.getMimeType()));
        request.setEntity(entity);

        HttpResponse response = context.executeClient(request);
        if (statusCode(OK) == statusCode(response)) {
            String sessionId = StringUtility.string(response).trim();
            context.setSessionId(sessionId);
            context.setNewSession(true);
            FileUtility.write(userSessionFile, sessionId);
            if (!setOwnerOnly(userSessionFile)) {
                writeLine(
                        "Warning! Possible security risk: unable to limit access rights of session-id file '%s'",
                        userSessionFile.getAbsoluteFile());
            }
            writeLine("Session id successfully renewed.");

        } else {

            handleError("An error occurred while logging: ", response);
        }
    }

    private File userSessionFile(String username) {
        return new File(DFLT_SESSION_DIR, username + DFLT_SESSION_FILE_EXT);
    }

    private boolean setOwnerOnly(File file) {
        // effectively set file permission to 600
        return file.setReadable(false, false) && file.setReadable(true, true)
                && file.setWritable(false, false)
                && file.setWritable(true, true) && file.setExecutable(false);
    }

}
