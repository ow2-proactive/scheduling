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
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
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

    private static final String S3_LOCAL_RELATIVE_PATH = "s3LocalRelativePath";

    private static final String BUCKET_NAME_ARG = "bucketName";

    private static final String REGION_ARG = "region";

    private static final String REMOTE_PREFIX_ARG = "s3RemoteRelativePath";

    private static final String PAUSE = "pause";

    private static final String ACCESS_KEY = "accessKey";

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

        if (args.containsKey(REGION_ARG) && !args.get(REGION_ARG).toString().isEmpty() &&
            RegionUtils.getRegion(args.get(REGION_ARG).toString()) != null) {
            region = args.get(REGION_ARG).toString();
        } else {
            throw new IllegalArgumentException("You have to specify a valid region \"" +
                                               args.get(REGION_ARG).toString() + "\" is not allowed.");
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
        if (args.containsKey(ACCESS_KEY) && !args.get(ACCESS_KEY).toString().isEmpty()) {
            accessKey = args.get(ACCESS_KEY).toString();
        } else {
            throw new IllegalArgumentException("You have to specify a your access key. Empty value is not allowed.");
        }

        // Retrieve the credential
        secretKey = getThirdPartyCredential(accessKey);
        if (secretKey == null) {
            throw new IllegalArgumentException("You first need to add your Secret Key to 3rd-party credentials under the key: " +
                                               accessKey);
        }
    }

    /**
     * @see JavaExecutable#execute(TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws IOException {
        List<String> filesRelativePathName = new ArrayList<>();

        File file = new File(s3LocalRelativePath);
        AmazonS3 amazonS3 = S3ConnectorUtils.getS3Client(accessKey, secretKey, region);

        // Create Bucket if it does not exist
        S3ConnectorUtils.createBucketIfNotExists(bucketName, amazonS3);

        // If the path does not exists, raise an exception.
        if (file.exists()) {
            if (file.isDirectory()) {
                uploadDir(s3LocalRelativePath, bucketName, s3RemoteRelativePath, true, false, amazonS3);
            } else {
                uploadFile(s3LocalRelativePath, bucketName, s3RemoteRelativePath, false, amazonS3);
            }
        } else {
            throw new FileNotFoundException("The input file cannot be found at " + s3LocalRelativePath);
        }

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
     * Upload a local file to S3. <br>
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

}
