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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;


/**
 * Import/Export file(s) from/to an AWS S3 server using an S3Connector task.
 * This task can be launched with parameters.
 *
 * @author The ProActive Team
 */
public class S3ConnectorUploader extends JavaExecutable {

    private String s3LocalRelativePath;

    private String bucketName = null;

    private String s3RemoteRelativePath = null;

    private String region = null;

    private String accessKey;

    private String secretKey;

    private boolean pathStyle = true;

    private static final String S3_LOCAL_RELATIVE_PATH = "s3LocalRelativePath";

    private static final String BUCKET_NAME_ARG = "bucketName";

    private static final String REGION_ARG = "region";

    private static final String REMOTE_PREFIX_ARG = "s3RemoteRelativePath";

    private static final String PAUSE = "pause";

    /**
     * @see JavaExecutable#init(Map)
     */
    @Override
    public void init(Map<String, Serializable> args) throws Exception {

        if (args.containsKey(BUCKET_NAME_ARG) && !args.get(BUCKET_NAME_ARG).toString().isEmpty()) {
            bucketName = args.get(BUCKET_NAME_ARG).toString();
        } else {
            throw new IllegalArgumentException("You have to specify a valid bucket name. Empty value is not allowed.");
        }

        if (args.containsKey(REGION_ARG) && !args.get(REGION_ARG).toString().isEmpty()) {
            region = args.get(REGION_ARG).toString();
        } else {
            throw new IllegalArgumentException("You have to specify a valid region. Empty value is not allowed.");
        }

        if (args.containsKey(REMOTE_PREFIX_ARG) && !args.get(REMOTE_PREFIX_ARG).toString().isEmpty()) {
            s3RemoteRelativePath = args.get(REMOTE_PREFIX_ARG).toString();
        }

        if (args.containsKey(S3_LOCAL_RELATIVE_PATH) && !args.get(S3_LOCAL_RELATIVE_PATH).toString().isEmpty()) {
            s3LocalRelativePath = args.get(S3_LOCAL_RELATIVE_PATH).toString();
        } else {
            //Default value is getLocalSpace() because it will always be writable and moreover can be used to transfer files to another data space (global, user)
            s3LocalRelativePath = getLocalSpace();
        }

        accessKey = getThirdPartyCredential("S3_ACCESS_KEY");
        secretKey = getThirdPartyCredential("S3_SECRET_KEY");
        if (accessKey == null || secretKey == null) {
            throw new IllegalArgumentException("You first need to add your s3 username and password (S3_ACCESS_KEY, S3_SECRET_KEY) to the third party credentials");
        }
    }

    /**
     * @see JavaExecutable#execute(TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws IOException {
        List<String> filesRelativePathName = new ArrayList<>();

        File f = new File(s3LocalRelativePath);

        // Any remaining args are assumed to be local paths to copy.
        // They may be directories, arrays, or a mix of both.
        ArrayList<String> dirsToCopy = new ArrayList<>();
        ArrayList<String> filesToCopy = new ArrayList<>();

        // If the path already exists, print a warning.
        if (f.exists()) {
            if (f.isDirectory()) {
                dirsToCopy.add(s3LocalRelativePath);
            } else {
                filesToCopy.add(s3LocalRelativePath);
            }
        } else {
            throw new FileNotFoundException("The input file cannot be found at " + s3LocalRelativePath);
        }

        AmazonS3 amazonS3 = getS3Client(region);

        // Create Bucket if it does not exist
        createBucketIfNotExists(bucketName, amazonS3);

        // Upload any directories in the list.
        for (String dirPath : dirsToCopy) {
            uploadDir(dirPath, bucketName, s3RemoteRelativePath, true, false, amazonS3);
            filesRelativePathName = SchedulerExamplesUtils.listDirectoryContents(f, new ArrayList<>());
        }

        // If there's more than one file in the list, upload it as a file list.
        // Otherwise, upload it as a single file.
        if (filesToCopy.size() > 1) {
            uploadFileList(filesToCopy.toArray(new String[0]), bucketName, s3RemoteRelativePath, false, amazonS3);
            filesRelativePathName.addAll(filesToCopy);
        } else if (filesToCopy.size() == 1) {
            uploadFile(filesToCopy.get(0), bucketName, s3RemoteRelativePath, false, amazonS3);
            filesRelativePathName.add(filesToCopy.get(0));

        } // else: nothing to do.

        return (Serializable) filesRelativePathName;
    }

    /**
     * Upload a local directory to S3. <br>
     * Requires a bucket name. <br>
     * If recursive is set to true, upload all subdirectories recursively.
     *
     * @param dirPath local directory to upload
     * @param bucketName
     * @param keyPrefix
     * @param recursive
     * @param pause
     * @param s3Client
     */
    private void uploadDir(String dirPath, String bucketName, String keyPrefix, boolean recursive, boolean pause,
            AmazonS3 s3Client) {
        getOut().println("directory: " + dirPath + (recursive ? " (recursive)" : "") +
                         (pause ? " (" + PAUSE + ")" : ""));

        File folder = new File(dirPath);
        String keyName = (keyPrefix != null) ? Paths.get(keyPrefix, folder.getName()).toString() : folder.getName();

        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        try {
            MultipleFileUpload uploader = transferManager.uploadDirectory(bucketName, keyName, folder, recursive);
            // loop with Transfer.isDone()
            SchedulerExamplesUtils.showTransferProgress(uploader);
            // or block with Transfer.waitForCompletion()
            SchedulerExamplesUtils.waitForCompletion(uploader);
        } catch (AmazonServiceException e) {
            getErr().println(e.getErrorMessage());
            System.exit(1);
        }
        transferManager.shutdownNow();
    }

    /**
     * Upload a list of files to S3. <br>
     * Requires a bucket name. <br>
     *
     * @param filePaths
     * @param bucketName
     * @param keyPrefix
     * @param pause
     * @param s3Client
     */
    private void uploadFileList(String[] filePaths, String bucketName, String keyPrefix, boolean pause,
            AmazonS3 s3Client) {
        getOut().println("file list: " + Arrays.toString(filePaths) + (pause ? " (" + PAUSE + ")" : ""));
        // convert the file paths to a list of File objects (required by the
        // uploadFileList method)
        ArrayList<File> files = new ArrayList<>();
        for (String path : filePaths) {
            files.add(new File(path));
        }

        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        try {
            MultipleFileUpload uploader = transferManager.uploadFileList(bucketName, keyPrefix, new File("."), files);
            // loop with Transfer.isDone()
            SchedulerExamplesUtils.showTransferProgress(uploader);
            // or block with Transfer.waitForCompletion()
            SchedulerExamplesUtils.waitForCompletion(uploader);
        } catch (AmazonServiceException e) {
            getErr().println(e.getErrorMessage());
            System.exit(1);
        }
        transferManager.shutdownNow();
    }

    /**
     * Upload a local directory to S3. <br>
     * Requires a bucket name. <br>
     *
     * @param filePath
     * @param bucketName
     * @param keyPrefix
     * @param pause
     * @param s3Client
     */
    private void uploadFile(String filePath, String bucketName, String keyPrefix, boolean pause, AmazonS3 s3Client) {
        getOut().println("file: " + filePath + (pause ? " (" + PAUSE + ")" : ""));

        File file = new File(filePath);

        String keyName = (keyPrefix != null) ? Paths.get(keyPrefix, file.getName()).toString() : file.getName();

        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        try {
            Upload uploader = transferManager.upload(bucketName, keyName, file);
            // loop with Transfer.isDone()
            SchedulerExamplesUtils.showTransferProgress(uploader);
            //  or block with Transfer.waitForCompletion()
            SchedulerExamplesUtils.waitForCompletion(uploader);
        } catch (AmazonServiceException e) {
            getErr().println(e.getErrorMessage());
            System.exit(1);
        }
        transferManager.shutdownNow();
    }

    /**
     * Get or initialize the S3 client.
     * Note: this method must be synchronized because we're accessing the
     * field and we're calling this method from a worker thread.
     *
     * @return the S3 client
     */
    private synchronized AmazonS3 getS3Client(String region) {

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                                                             .withCredentials(new AWSStaticCredentialsProvider(credentials));
        builder = builder.withRegion(region);
        builder = builder.withPathStyleAccessEnabled(pathStyle);
        return builder.build();
    }

    /**
     * Check if an S3 bucket exists and returns its name if it does exist or null otherwise.
     * @param bucketName
     * @param s3
     * @return bucket name if it exists or null otherwise
     */
    private Bucket getBucket(String bucketName, AmazonS3 s3) {
        Bucket namedBucket = null;
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucketName)) {
                namedBucket = b;
            }
        }
        return namedBucket;
    }

    /**
     * Creates an S3 bucket if it does not exist and returns its name.
     * @param bucketName
     * @param s3
     * @return
     */
    private Bucket createBucketIfNotExists(String bucketName, AmazonS3 s3) {
        Bucket b;
        if (s3.doesBucketExistV2(bucketName)) {
            b = getBucket(bucketName, s3);
        } else {
            getOut().println("Bucket " + bucketName + " does not exist. Creating bucket ...");
            b = s3.createBucket(bucketName);
            getOut().println("Bucket " + bucketName + " created successfully!");
        }
        return b;
    }
}
