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
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import static org.apache.http.entity.ContentType.APPLICATION_XML;
import static org.ow2.proactive_grid_cloud_portal.cli.ResponseStatus.OK;

import java.io.File;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.ow2.proactive_grid_cloud_portal.cli.json.JobIdView;

public class SubmitJobCommand extends AbstractCommand implements Command {
    private String pathname;

    public SubmitJobCommand(String pathname) {
        this.pathname = pathname;
    }

    @Override
    public void execute() throws Exception {
        HttpPost request = new HttpPost(resourceUrl("submit"));

        File jobFile = new File(pathname);
        if (!jobFile.exists()) {
            throw new IllegalArgumentException(pathname + " does not exist ..");
        }
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(
                pathname);
        MultipartEntity multipartEntity = new MultipartEntity();
        if (APPLICATION_XML.getMimeType().equals(contentType)) {
            multipartEntity.addPart("descriptor", new FileBody(jobFile,
                    APPLICATION_XML.getMimeType()));
        } else {
            multipartEntity.addPart("archive", new FileBody(jobFile,
                    APPLICATION_OCTET_STREAM.getMimeType()));
        }
        request.setEntity(multipartEntity);
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            JobIdView jobId = readValue(response, JobIdView.class);
            writeLine("Job('%s') submitted successfully .. => job('%d')",
                    pathname, jobId.getId());
        } else {
            handleError("An error occured while submitting job(" + pathname
                    + ")", response);
        }
    }
}
