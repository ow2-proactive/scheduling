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

import java.util.Optional;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.ServiceUtils;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.util.AwsHostNameUtils;


/**
 * @author ActiveEon Team
 * @since 21/05/2018
 */
public class S3ConnectorUtils {

    private static final Logger logger = Logger.getLogger(S3ConnectorUtils.class);

    private S3ConnectorUtils() {
    }

    /**
     * Get or initialize the S3 client.
     * Note: this method must be synchronized because we're accessing the
     * field and we're calling this method from a worker thread.
     *
     * @return the S3 client
     */
    protected static synchronized AmazonS3 getS3Client(String accessKey, String secretKey, String... args) {

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                                                             .withCredentials(new AWSStaticCredentialsProvider(credentials));

        if (args.length == 1) {
            builder = builder.withRegion(args[0]);

        } else {
            String endpoint = args[0] + "://" + args[1];
            String clientRegion = null;
            if (!ServiceUtils.isS3USStandardEndpoint(endpoint) &&
                (clientRegion = AwsHostNameUtils.parseRegion(args[1], AmazonS3Client.S3_SERVICE_NAME)) == null) {
                throw new IllegalArgumentException("Invalid region in " + args[1]);
            }
            builder = builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint,
                                                                                                   clientRegion));
        }
        builder = builder.withPathStyleAccessEnabled(true);
        return builder.build();
    }

    /**
     * Check if an S3 bucket exists and returns its name if it does exist or null otherwise.
     *
     * @param bucketName
     * @param s3
     * @return bucket name if it exists or null otherwise
     */
    protected static Bucket getBucket(String bucketName, AmazonS3 s3) {
        Optional<Bucket> value = s3.listBuckets().stream().filter(b -> b.getName().equals(bucketName)).findFirst();
        return value.isPresent() ? value.get() : null;
    }

    /**
     * Creates an S3 bucket if it does not exist and returns its name.
     *
     * @param bucketName
     * @param s3
     * @return
     */
    protected static Bucket createBucketIfNotExists(String bucketName, AmazonS3 s3) {
        Bucket b;
        if (s3.doesBucketExistV2(bucketName)) {
            b = getBucket(bucketName, s3);
        } else {
            logger.info("Bucket " + bucketName + " does not exist. Creating bucket ...");
            b = s3.createBucket(bucketName);
            logger.info("Bucket " + bucketName + " created successfully!");
        }
        return b;
    }

}
