/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.examples;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;

import org.apache.log4j.Logger;

import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.rest.v2.RestException;


/**
 * @author ActiveEon Team
 * @since 13/09/2018
 */
public class AzureStorageConnectorUtils {

    private static final Logger logger = Logger.getLogger(AzureStorageConnectorUtils.class);

    private AzureStorageConnectorUtils() {

    }

    public static ContainerURL createContainerURL(String accountName, String accountKey, String containerName)
            throws MalformedURLException {
        // Create a ServiceURL to call the Blob service. We will also use this to construct the ContainerURL
        SharedKeyCredentials creds = null;
        try {
            creds = new SharedKeyCredentials(accountName, accountKey);
        } catch (InvalidKeyException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }
        // We are using a default pipeline here, you can learn more about it at https://github.com/Azure/azure-storage-java/wiki/Azure-Storage-Java-V10-Overview
        final ServiceURL serviceURL = new ServiceURL(new URL("http://" + accountName + ".blob.core.windows.net"),
                                                     StorageURL.createPipeline(creds, new PipelineOptions()));

        // Let's create a container using a blocking call to Azure Storage
        // If container exists, we'll catch and continue
        ContainerURL containerURL = serviceURL.createContainerURL(containerName);

        try {
            containerURL.create(null, null).blockingGet();
            logger.info("The " + containerName + " container is created");
        } catch (RestException e) {
            if (e.response().statusCode() != 409) {
                logger.error(e.getMessage());
                System.exit(1);
            } else {
                logger.info("The " + containerName + " container already exists, resuming...");
            }
        }
        return containerURL;
    }
}
