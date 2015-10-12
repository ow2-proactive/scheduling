/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_INVALID_ARGUMENTS;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;
import static org.ow2.proactive_grid_cloud_portal.cli.HttpResponseStatus.OK;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.utils.FileUtility;
import org.ow2.proactive_grid_cloud_portal.cli.utils.HttpResponseWrapper;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;


public class LoginWithCredentialsCommand extends AbstractLoginCommand implements Command {
    public static final String CRED_FILE = "org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginWithCredentials.credFile";

    private String pathname;
    private boolean warn;

    public LoginWithCredentialsCommand(String pathname, boolean warn) {
        this(pathname);
        this.warn = warn;
    }

    public LoginWithCredentialsCommand(String pathname) {
        this.pathname = pathname;
    }

    @Override
    protected String login(ApplicationContext currentContext) throws CLIException {
        File credentials = new File(pathname);
        if (!credentials.exists()) {
            throw new CLIException(REASON_INVALID_ARGUMENTS, String.format("File does not exist: %s",
                    credentials.getAbsolutePath()));
        }
        if (warn) {
            writeLine(currentContext, "Using the default credentials file: %s", credentials.getAbsolutePath());
        }
        HttpPost request = new HttpPost(currentContext.getResourceUrl("login"));
        MultipartEntity entity = new MultipartEntity();
        entity.addPart("credential", new ByteArrayBody(FileUtility.byteArray(credentials),
            APPLICATION_OCTET_STREAM.getMimeType()));
        request.setEntity(entity);
        HttpResponseWrapper response = execute(request, currentContext);
        if (statusCode(OK) == statusCode(response)) {
            return StringUtility.responseAsString(response).trim();
        } else {
            handleError("An error occurred while logging: ", response, currentContext);
            throw new CLIException(REASON_OTHER, "An error occurred while logging.");
        }
    }

    @Override
    protected String getAlias(ApplicationContext currentContext) {
        String pathname = currentContext.getProperty(CRED_FILE, String.class);
        return FileUtility.md5Checksum(new File(pathname));
    }

    @Override
    protected void setAlias(ApplicationContext currentContext) {
        currentContext.setProperty(CRED_FILE, pathname);
    }
}
