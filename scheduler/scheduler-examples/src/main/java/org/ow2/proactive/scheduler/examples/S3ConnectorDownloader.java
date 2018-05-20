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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.internal.ServiceUtils;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.util.AwsHostNameUtils;


/**
 * Import file(s) from an S3 server using the Import_from_S3 task.
 * This task can be launched with parameters.
 *
 * @author The ProActive Team
 */
public class S3ConnectorDownloader extends JavaExecutable {

    private String s3LocalRelativePath;

    private String s3Url;

    private String bucketName = null;

    private String keyName = null;

    private String host = null;

    private String scheme;

    private String accessKey;

    private String secretKey;

    private boolean pathStyle = true;

    private static final String S3_LOCAL_RELATIVE_PATH = "s3LocalRelativePath";

    private static final String S3_URI = "s3Url";

    /**
     * @see JavaExecutable#init(Map)
     */
    @Override
    public void init(Map<String, Serializable> args) throws Exception {

        if (args.containsKey(S3_URI) && !args.get(S3_URI).toString().isEmpty()) {
            s3Url = args.get(S3_URI).toString();
            parseAmazonS3URI(s3Url);
        } else {
            throw new IllegalArgumentException("You have to specify a valid s3URI. Empty value is not allowed.");
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

        boolean s3PathIsPrefix = (keyName.lastIndexOf('/') == keyName.length() - 1);

        File f = new File(s3LocalRelativePath);

        // If the path already exists, print a warning.
        if (f.exists()) {
            getOut().println("The local path already exists");
            if (s3PathIsPrefix) {
                try {
                    f.mkdir();
                } catch (Exception e) {
                    getOut().println("Couldn't create destination directory!");
                    System.exit(1);
                }
            }
        }
        AmazonS3 amazonS3 = getS3Client(host, scheme);

        // Assume that the path exists, do the download.
        if (s3PathIsPrefix) {
            downloadDir(bucketName, keyName, s3LocalRelativePath, false, amazonS3);
            filesRelativePathName = SchedulerExamplesUtils.listDirectoryContents(f, new ArrayList<>());
        } else {
            s3LocalRelativePath = Paths.get(s3LocalRelativePath, Paths.get(s3Url).getFileName().toString()).toString();
            downloadFile(bucketName, keyName, s3LocalRelativePath, false, amazonS3);
            filesRelativePathName.add(s3LocalRelativePath);

        }

        return (Serializable) filesRelativePathName;
    }

    /**
     * Download a list of files from S3. <br>
     * Requires a bucket name. <br>
     * Requires a key prefix. <br>
     * @param bucketName
     * @param keyPrefix
     * @param dirPath
     * @param pause
     * @param s3Client
     */
    private void downloadDir(String bucketName, String keyPrefix, String dirPath, boolean pause, AmazonS3 s3Client) {
        getOut().println("downloading to directory: " + dirPath + (pause ? " (pause)" : ""));
        TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();

        try {
            MultipleFileDownload xfer = transferManager.downloadDirectory(bucketName, keyPrefix, new File(dirPath));
            // loop with Transfer.isDone()
            SchedulerExamplesUtils.showTransferProgress(xfer);
            // or block with Transfer.waitForCompletion()
            SchedulerExamplesUtils.waitForCompletion(xfer);
        } catch (AmazonServiceException e) {
            getErr().println(e.getMessage());
            System.exit(1);
        }
        transferManager.shutdownNow();
    }

    /**
     * Download a file from S3. <br>
     * Requires a bucket name. <br>
     * Requires a key prefix. <br>
     * @param bucketName
     * @param keyName
     * @param filePath
     * @param pause
     * @param s3Client
     */
    private void downloadFile(String bucketName, String keyName, String filePath, boolean pause, AmazonS3 s3Client) {
        getOut().println("Downloading to file: " + filePath + (pause ? " (pause)" : ""));

        File f = new File(filePath);
        TransferManager xferMgr = TransferManagerBuilder.standard().withS3Client(s3Client).build();
        try {
            Download xfer = xferMgr.download(bucketName, keyName, f);
            // loop with Transfer.isDone()
            SchedulerExamplesUtils.showTransferProgress(xfer);
            // or block with Transfer.waitForCompletion()
            SchedulerExamplesUtils.waitForCompletion(xfer);
        } catch (AmazonServiceException e) {
            getErr().println(e.getMessage());
            System.exit(1);
        }
        xferMgr.shutdownNow();
    }

    /**
     * Get or initialize the S3 client.
     * Note: this method must be synchronized because we're accessing the
     * field and we're calling this method from a worker thread.
     *
     * @return the S3 client
     */
    private synchronized AmazonS3 getS3Client(String host, String scheme) {

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                                                             .withCredentials(new AWSStaticCredentialsProvider(credentials));
        String endpoint = scheme + "://" + host;
        String clientRegion = null;
        if (!ServiceUtils.isS3USStandardEndpoint(endpoint)) {
            clientRegion = AwsHostNameUtils.parseRegion(host, AmazonS3Client.S3_SERVICE_NAME);
        }
        builder = builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, clientRegion));
        builder = builder.withPathStyleAccessEnabled(pathStyle);
        return builder.build();
    }

    /**
     * Parse an Amazon S3 Uri to extact four elements: scheme, host, bucket name and key name.
     * @param s3Uri
     */
    private void parseAmazonS3URI(String s3Uri) {
        AmazonS3URI amazonS3URI = new AmazonS3URI(s3Uri);
        if ((scheme = amazonS3URI.getURI().getScheme()) == null) {
            throw new IllegalArgumentException("You have to specify a valid scheme in the provided s3 uri. Empty value is not allowed.");
        }
        if ((host = amazonS3URI.getURI().getHost()) == null) {
            throw new IllegalArgumentException("You have to specify a valid host in the provided s3 uri. Empty value is not allowed.");
        }
        if ((bucketName = amazonS3URI.getBucket()) == null) {
            throw new IllegalArgumentException("You have to specify a valid bucket name in the provided s3 uri. Empty value is not allowed.");
        }
        if ((keyName = amazonS3URI.getKey()) == null) {
            throw new IllegalArgumentException("You have to specify a valid key name in the provided s3 uri. Empty value is not allowed.");
        }

    }

}
