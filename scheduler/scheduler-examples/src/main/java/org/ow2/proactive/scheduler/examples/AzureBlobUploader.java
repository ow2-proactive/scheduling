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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.TransferManager;


/**
 * Export file(s) to an Azure Blob Storage using an AzureBlobUploader task.
 * This task can be launched with parameters.
 *
 * @author ActiveEon Team
 * @since 10/09/2018
 */
public class AzureBlobUploader extends JavaExecutable {

    private String inputPath;

    private String containerName;

    Optional<String> optionalBlobName;

    private String storageAccount;

    private String accountKey;

    private static final String INPUT_PATH = "inputPath";

    private static final String CONTAINER_NAME = "containerName";

    private static final String BLOB_NAME = "blobName";

    private static final String STORAGE_ACCOUNT = "storageAccount";

    private ContainerURL containerURL;

    @Override
    public void init(Map<String, Serializable> args) throws Exception {

        containerName = (String) Optional.ofNullable(args.get(CONTAINER_NAME))
                                         .filter(container -> !container.toString().isEmpty())
                                         .orElseThrow(() -> new IllegalArgumentException("You have to specify a container name. Empty value is not allowed."));

        optionalBlobName = Optional.ofNullable(args.get(BLOB_NAME).toString()).filter(name -> !name.isEmpty());

        //Default value is getLocalSpace() because it will always be writable and moreover can be used to transfer files to another data space (global, user)
        inputPath = (String) Optional.ofNullable(args.get(INPUT_PATH))
                                     .filter(input -> !input.toString().isEmpty())
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
    public Serializable execute(TaskResult... results) throws IOException, ExecutionException, InterruptedException {

        File file = new File(inputPath);
        containerURL = AzureStorageConnectorUtils.createContainerURL(storageAccount, accountKey, containerName);
        if (file.exists()) {
            return (Serializable) uploadResources(file);
        } else {
            throw new FileNotFoundException("The input file cannot be found at " + inputPath);
        }
    }

    /**
     * This method uploads resources (file or folder to an Azure Blob Storage
     * @param file
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */

    private List<String> uploadResources(File file) throws InterruptedException, ExecutionException, IOException {
        List<String> filesRelativePathName = new ArrayList<>();
        if (file.isDirectory()) {
            if (optionalBlobName.isPresent()) {
                filesRelativePathName = recursiveFolderUpload(inputPath, optionalBlobName.get(), true);
            } else {
                filesRelativePathName = recursiveFolderUpload(inputPath, "", false);
            }

        } else {
            if (optionalBlobName.isPresent()) {
                optionalBlobName = Optional.of(optionalBlobName.get().replaceAll("\\s+", ""));
            }
            if (!optionalBlobName.isPresent() || optionalBlobName.get().isEmpty()) {
                uploadFile(file, file.getName());
            } else {
                uploadFile(file, optionalBlobName.get());
            }
            filesRelativePathName.add(file.getPath());
        }
        return filesRelativePathName;
    }

    /**
     * This method uploads a local file to an Azure Storage blob
     *
     * @param file
     * @param blobName
     * @throws IOException
     */
    private void uploadFile(File file, String blobName) throws IOException, ExecutionException, InterruptedException {
        getOut().println("Uploading " + file.getName() + " file into the container: " + containerURL);
        FileChannel fileChannel = FileChannel.open(file.toPath());

        // Create a BlockBlobURL to run operations on Blobs
        final BlockBlobURL blob = containerURL.createBlockBlobURL(blobName);

        // Uploading a file to the blobURL using the high-level methods available in TransferManager class
        // Alternatively call the PutBlob/PutBlock low-level methods from BlockBlobURL type
        TransferManager.uploadFileToBlockBlob(fileChannel, blob, 8 * 1024 * 1024, null).toFuture().get();
        getOut().println("Completed upload request.");

    }

    /**
     * This method recursively uploads a local file/folder to an Azure Storage blob
     *
     * @param sourcePath
     * @param destinationPath
     * @throws IOException
     */
    private List<String> recursiveFolderUpload(String sourcePath, String destinationPath, boolean ignoreRoot)
            throws IOException, ExecutionException, InterruptedException {
        List<String> filesRelativePathName = new ArrayList<>();

        File sourceFile = new File(sourcePath);
        String remoteFilePath = Paths.get(destinationPath, sourceFile.getName()).toString();

        if (sourceFile.isFile()) {
            // copy if it is a file
            getOut().println("Uploading " + sourcePath);
            uploadFile(sourceFile, remoteFilePath);
            getOut().println("File uploaded successfully to " + remoteFilePath);
            filesRelativePathName.add(remoteFilePath);

        } else {
            if (ignoreRoot) {
                remoteFilePath = destinationPath;
            }

            File[] files = sourceFile.listFiles();
            if (!sourceFile.isHidden()) {
                for (File f : files) {
                    filesRelativePathName.addAll(recursiveFolderUpload(f.getAbsolutePath(), remoteFilePath, false));
                }
            }
        }
        return filesRelativePathName;
    }
}
