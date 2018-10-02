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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import com.microsoft.azure.storage.blob.BlobRange;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.ListBlobsOptions;
import com.microsoft.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse;
import com.microsoft.rest.v2.util.FlowableUtil;

import io.reactivex.Single;


/**
 * Import file(s) from an Azure Blob Storage using an AzureBlobDownloader task.
 * This task can be launched with parameters.
 *
 * @author ActiveEon Team
 * @since 13/09/2018
 */
public class AzureBlobDownloader extends JavaExecutable {

    private String outputPath;

    private String containerName;

    Optional<String> optionalBlobName;

    private String storageAccount;

    private String accountKey;

    private static final String OUTPUT_PATH = "outputPath";

    private static final String CONTAINER_NAME = "containerName";

    private static final String BLOB_NAME = "blobName";

    private ContainerURL containerURL;

    private static final String STORAGE_ACCOUNT = "storageAccount";

    @Override
    public void init(Map<String, Serializable> args) throws Exception {

        containerName = (String) Optional.ofNullable(args.get(CONTAINER_NAME))
                                         .filter(container -> !container.toString().isEmpty())
                                         .orElseThrow(() -> new IllegalArgumentException("You have to specify a container name. Empty value is not allowed."));

        optionalBlobName = Optional.ofNullable(args.get(BLOB_NAME).toString()).filter(name -> !name.isEmpty());

        //Default value is getLocalSpace() because it will always be writable and moreover can be used to transfer files to another data space (global, user)
        outputPath = (String) Optional.ofNullable(args.get(OUTPUT_PATH))
                                      .filter(output -> !output.toString().isEmpty())
                                      .orElse(getLocalSpace());

        storageAccount = (String) Optional.ofNullable(args.get(STORAGE_ACCOUNT))
                                          .filter(storage -> !storage.toString().isEmpty())
                                          .orElseThrow(() -> new IllegalArgumentException("You have to specify a storage account name. Empty value is not allowed."));

        // Retrieve the credential
        accountKey = getThirdPartyCredential("ACCOUNT_KEY");
        if (accountKey == null) {
            throw new IllegalArgumentException("You first need to add your account key to 3rd-party credentials under the key: " +
                                               storageAccount);
        }
    }

    /**
     * @see JavaExecutable#execute(TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws IOException {

        File file = new File(outputPath);
        containerURL = AzureStorageConnectorUtils.createContainerURL(storageAccount, accountKey, containerName);
        //download a single blob
        if (optionalBlobName.isPresent()) {
            String azureBlobLocalRelativePath = Paths.get(outputPath,
                                                          Paths.get(optionalBlobName.get()).getFileName().toString())
                                                     .toString();
            //check that the outputPath is either a path to a directory terminated by / or a path for the local space or a path for a file.
            if (SchedulerExamplesUtils.isDirectoryPath(outputPath)) {
                SchedulerExamplesUtils.createDirIfNotExists(file);
                downloadBlob(new File(azureBlobLocalRelativePath), optionalBlobName.get());
            } else if (outputPath.equals(getLocalSpace())) {
                downloadBlob(new File(azureBlobLocalRelativePath), optionalBlobName.get());
            } else {
                downloadBlob(file, optionalBlobName.get());
            }
        } else { //download the whole container
            SchedulerExamplesUtils.createDirIfNotExists(file);
            downloadContainerBlobs(containerURL);
        }

        return (Serializable) Arrays.asList(outputPath);
    }

    private void downloadContainerBlobs(ContainerURL containerURL) {
        // Each ContainerURL.listBlobsFlatSegment call return up to maxResults (maxResults=10 passed into ListBlobOptions below).
        // To list all Blobs, we are creating a helper static method called downloadAllBlobs,
        // and calling it after the initial listBlobsFlatSegment call
        ListBlobsOptions options = new ListBlobsOptions(null, null, 1);

        containerURL.listBlobsFlatSegment(null, options)
                    .flatMap(containerListBlobFlatSegmentResponse -> downloadAllBlobs(containerURL,
                                                                                      containerListBlobFlatSegmentResponse))
                    .blockingGet();
        getOut().println("Downloading of the container blobs completed.");
    }

    private Single<ContainerListBlobFlatSegmentResponse> downloadAllBlobs(ContainerURL url,
            ContainerListBlobFlatSegmentResponse response) {
        // Process the blobs returned in this result segment (if the segment is empty, blobs() will be null.
        if (response.body().segment() != null) {
            for (BlobItem b : response.body().segment().blobItems()) {
                File azureBlobFile = new File(Paths.get(outputPath, b.name()).toString());
                if (!azureBlobFile.getParentFile().exists()) {
                    azureBlobFile.getParentFile().mkdirs();
                }
                getOut().println("blob name: " + b.name());
                downloadBlob(azureBlobFile, b.name());
            }
        } else {
            getOut().println("There are no more blobs to list off.");
        }

        // If there is not another segment, return this response as the final response.
        if (response.body().nextMarker() == null) {
            return Single.just(response);
        } else {
            /*
             * IMPORTANT: ListBlobsFlatSegment returns the start of the next segment; you MUST use
             * this to get the next
             * segment (after processing the current result segment
             */

            String nextMarker = response.body().nextMarker();

            /*
             * The presence of the marker indicates that there are more blobs to list, so we make
             * another call to
             * listBlobsFlatSegment and pass the result through this helper function.
             */

            return url.listBlobsFlatSegment(nextMarker, new ListBlobsOptions(null, null, 1))
                      .flatMap(containersListBlobFlatSegmentResponse -> downloadAllBlobs(url,
                                                                                         containersListBlobFlatSegmentResponse));
        }
    }

    private void downloadBlob(File destinationFile, String blobName) {
        getOut().println("Downloading " + blobName + " blob into the file: " + destinationFile);

        // Create a BlockBlobURL to run operations on Blobs
        final BlockBlobURL blobURL = containerURL.createBlockBlobURL(blobName);
        try {
            // Get the blob using the low-level download method in BlockBlobURL type
            // com.microsoft.rest.v2.util.FlowableUtil is a static class that contains helpers to work with Flowable
            blobURL.download(new BlobRange(0, Long.MAX_VALUE), null, false)
                   .flatMapCompletable(response -> {
                       AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get(destinationFile.getPath()),
                                                                                      StandardOpenOption.CREATE,
                                                                                      StandardOpenOption.WRITE);
                       return FlowableUtil.writeFile(response.body(), channel);
                   })
                   .doOnComplete(() -> getOut().println("The blob was downloaded to " +
                                                        destinationFile.getAbsolutePath()))
                   // To call it synchronously add .blockingAwait()
                   .blockingAwait();
        } catch (Exception ex) {

            getErr().println(ex.toString());
            System.exit(1);
        }
    }
}
