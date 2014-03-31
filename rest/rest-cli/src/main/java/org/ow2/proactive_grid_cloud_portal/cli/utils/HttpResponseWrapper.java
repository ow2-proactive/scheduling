package org.ow2.proactive_grid_cloud_portal.cli.utils;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_IO_ERROR;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;

public class HttpResponseWrapper {
    private byte[] buffer = null;
    private int statusCode = -1;

    public HttpResponseWrapper(HttpResponse response) throws CLIException {
        statusCode = response.getStatusLine().getStatusCode();
        InputStream inputStream = null;
        try {
            inputStream = response.getEntity().getContent();
            if (inputStream != null) {
                buffer = IOUtils.toByteArray(inputStream);
            }
        } catch (IllegalStateException e) {
            throw new CLIException(REASON_OTHER, e);
        } catch (IOException e) {
            throw new CLIException(REASON_IO_ERROR, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public byte[] getContent() {
        return buffer;
    }
}
