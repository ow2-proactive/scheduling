package org.ow2.proactive.resourcemanager.nodesource.infrastructure;/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.ProActiveException;
import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;
import org.apache.log4j.Logger;


/**
 *
 * Amazon EC2 Node deployer backend
 * <p>
 * Contains a Java wrapper for EC2 operations ; Requires proper Amazon credentials
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 *
 */
public class EC2Deployer implements java.io.Serializable {

    /** logger */
    protected static Logger logger = Logger.getLogger(EC2Deployer.class);

    /** Access Key */
    private String AWS_AKEY;

    /** Secret Key */
    private String AWS_SKEY;

    /** Amazon username */
    private String AWS_USER;

    /** KeyPair container */
    private String AWS_KPINFO;

    /** KeyPair name */
    private String keyName;

    /** Deployed instances */
    private List<String> instanceIds;

    /** Activity checker */
    private boolean active;

    /** Minimum instances to deploy */
    private int minInstances;

    /** Maximum instances to deploy */
    private int maxInstances;

    /** Current number of deployed instances */
    private int currentInstances;

    /** instance type: smaller is cheaper; bigger is faster;
     * x86_64 AMIs requires extra large, or will fail to deploy */
    private InstanceType instanceType;

    /**
     * Once an image descriptor is retrieved, cache it 
     */
    private Map<String, ImageDescription> cachedImageDescriptors =
            Collections.synchronizedMap(new HashMap<String, ImageDescription>());

    /**
     * EC2 server URL - the EC2 zone used depends on this url
     * Leave null for ec2 default behavior
     */
    private String ec2RegionHost = null;

    public String getEc2RegionHost() {
        return ec2RegionHost;
    }

    public void setEc2RegionHost(String ec2ServerURL) {
        this.ec2RegionHost = ec2ServerURL;
    }

    /**
     * Constructs a new node deployer for Amazon EC2
     */
    public EC2Deployer() {
        this.instanceIds = new ArrayList<>();
        this.active = false;
        this.minInstances = 1;
        this.maxInstances = 1;
        this.instanceType = InstanceType.DEFAULT;
    }

    /**
     * Constructs a new node deployer/killer for Amazon EC2
     *
     * @param aws_accesskey
     *            Amazon access key
     * @param aws_secretkey
     *            Amazon secret key
     * @param aws_user
     *            Amazon user name
     */
    public EC2Deployer(String aws_accesskey, String aws_secretkey, String aws_user) {
        this();
        this.resetKeys(aws_accesskey, aws_secretkey, aws_user);
    }

    /**
     * Reset amazon deployment
     *
     * @param aws_accesskey
     *            Amazon access key
     * @param aws_secretkey
     *            Amazon secret key
     * @param aws_user
     *            Amazon user name
     * @return a Java EC2 Wrapper with the new credentials
     */
    public Jec2 resetKeys(String aws_accesskey, String aws_secretkey, String aws_user) {
        Jec2 EC2Requester;

        this.AWS_AKEY = aws_accesskey;
        this.AWS_SKEY = aws_secretkey;
        this.AWS_USER = aws_user;
        EC2Requester = new Jec2(this.AWS_AKEY, this.AWS_SKEY);

        keyName = AWS_USER + "-" + AWS_AKEY.charAt(0) + AWS_SKEY.charAt(0);
        try {
            //			if (terminateAllInstances(true)) {
            //				EC2Requester.deleteKeyPair(keyName);
            this.AWS_KPINFO = EC2Requester.createKeyPair(keyName).getKeyName();
            //			}
        } catch (EC2Exception e) {
            // this should happen frequently,
            // as keys can't be generated more than once without being deleted,

            logger.warn("Can't regen keypair ", e);
        }
        this.active = true;
        return EC2Requester;
    }

    private Jec2 getEC2Wrapper() {
        Jec2 jec2 = resetKeys(this.AWS_AKEY, this.AWS_SKEY, this.AWS_USER);
        if (ec2RegionHost != null) {
            jec2.setRegionUrl(ec2RegionHost);
        }
        return jec2;
    }

    /**
     * Retrieves all available images on AmazonS3
     *
     * @param all
     *            if true Get all AMI, if false, get only user's AMI
     * @return User's or All AMI from Amazon S3
     */
    public List<ImageDescription> getAvailableImages(boolean all) {

        Jec2 ec2req = getEC2Wrapper();

        if (ec2req == null)
            return null;

        List<String> params = new ArrayList<>();

        if (!all)
            params.add(AWS_USER);

        List<ImageDescription> images = null;

        try {
            images = ec2req.describeImagesByOwner(params);
        } catch (EC2Exception e) {
            logger.error("Unable to get image description", e);
        }

        return images;
    }

    /**
     * Retrieves all available images on AmazonS3
     * 
     * @param amiId
     *            an unique AMI id
     * @param all
     *            if true Get all AMI, if false, get only user's AMI
     * @return first AMI from Amazon S3 corresponding to pattern
     */
    public ImageDescription getAvailableImages(String amiId, boolean all) {

        synchronized (cachedImageDescriptors) {
            if (cachedImageDescriptors.containsKey(amiId))
                return cachedImageDescriptors.get(amiId);
        }

        Jec2 ec2req = getEC2Wrapper();
        if (ec2req == null)
            return null;

        List<ImageDescription> imgs = this.getAvailableImages(all);

        for (ImageDescription img : imgs) {
            if (img.getImageId().equals(amiId)) {
                //cache it 
                cachedImageDescriptors.put(amiId, img);
                return img;
            }
        }

        logger.error("Could nod find AMI: " + amiId);
        return null;
    }

    /**
     * Gets a set of instances
     *
     * @return a set of instances
     */
    public List<Instance> getInstances() {

        Jec2 ec2req = getEC2Wrapper();

        if (ec2req == null)
            return null;

        List<String> params = new ArrayList<>();
        List<ReservationDescription> res = null;
        List<Instance> instances = new ArrayList<>();

        try {
            res = ec2req.describeInstances(params);
        } catch (EC2Exception e) {
            logger.error("Unable to get instances list", e);
            return null;
        }

        for (ReservationDescription rdesc : res) {
            instances.addAll(rdesc.getInstances());
        }

        return instances;
    }

    /**
     * Returns the hostname of a running instance
     * If the instance is not running, will return an empty string
     * 
     * @param id the unique id of the instance
     * @return the hostname of the running instance corresponding to the id, 
     *         or an empty string
     */
    public String getInstanceHostname(String id) {
        Jec2 ec2req = getEC2Wrapper();

        if (ec2req == null)
            return "";

        try {
            for (ReservationDescription desc : ec2req.describeInstances(new String[] {})) {
                for (Instance inst : desc.getInstances()) {
                    if (id.equals(inst.getInstanceId())) {
                        return inst.getDnsName();
                    }
                }
            }
        } catch (EC2Exception e) {
            return "";
        }
        return "";
    }

    /**
     * Attempts to terminate all instances deployed by this EC2Deployer
     * 
     * @return the number of terminated instances
     */
    public int terminateAll() {
        Jec2 ec2req = getEC2Wrapper();
        int t = 0;
        for (String id : this.instanceIds) {
            try {
                ec2req.terminateInstances(new String[] { id });
                logger.debug("Successfully terminated orphan EC2 node: " + id);
                t++;
            } catch (EC2Exception e) {
                logger.error("Cannot terminate instance " + id + " with IP " + this.getInstanceHostname(id) +
                    ". Do it manually.");
            }
        }
        return t;
    }

    /**
     * Launch a new instance with the provided AMI id
     *
     * @param imageId
     *            an unique AMI id
     * @param userData
     * 			  the user data to use for this deployment
     * @return the Reservation's id
     * @throws ProActiveException
     *             acquisition failed
     */
    public List<Instance> runInstances(String imageId, String userData) throws ProActiveException {
        return this.runInstances(this.minInstances, this.maxInstances, imageId, userData);
    }

    /**
     * Launch a new instance with the provided AMI id
     *
     * @param minNumber
     *            minimal number of instances to deploy
     * @param maxNumber
     *            maximal number of instances to deploy
     * @param imageId
     *            an unique AMI id
     * @param userData
     * 			  the user data to use for this deployment
     * @return the Reservation's id
     * @throws ProActiveException
     *             acquisition failed
     */
    public List<Instance> runInstances(int minNumber, int maxNumber, String imageId, String userData)
            throws ProActiveException {
        ImageDescription imgd = getAvailableImages(imageId, true);

        if (imgd == null) {
            throw new ProActiveException("Could not find AMI : " + imageId);
        }

        return this.runInstances(minNumber, maxNumber, imgd, userData);
    }

    /**
     * Launch a new instance with provided AMI
     *
     * @param min
     *            minimal number of instances to deploy
     * @param max
     *            maximal number of instances to deploy
     * @param imgd
     *            an image description containing AMI id
     * @param userData
     * 			  the user data to use for this deployment
     * @return the Reservation's id
     * @throws ProActiveException
     *             acquisition failed
     */
    public List<Instance> runInstances(int min, int max, ImageDescription imgd, String userData)
            throws ProActiveException {

        Jec2 ec2req = getEC2Wrapper();

        if (ec2req == null) {
            throw new ProActiveException();
        }

        if (this.currentInstances + min > this.maxInstances) {
            max = this.maxInstances - this.currentInstances;
        }

        if (min > max) {
            min = max;
        }

        if (imgd == null) {
            imgd = this.getAvailableImages(false).get(0);
        }

        try {
            //Do not force large instance, small works fine on windows. Let the user chose. 

            if (imgd.getArchitecture().equals("x86_64")) {
                if (instanceType != InstanceType.XLARGE && instanceType != InstanceType.XLARGE_HCPU &&
                    instanceType != InstanceType.LARGE) {
                    logger.warn("AMI " + imgd.getImageId() + " is  " + imgd.getPlatform() + " x86_64 Arch," +
                        " it might not be compatible with the chosen Instance Type " +
                        instanceType.getTypeId());
                    //instanceType = InstanceType.LARGE;
                }
            }

            ReservationDescription rdesc = ec2req.runInstances(imgd.getImageId(), min, max,
                    new ArrayList<String>(), userData, this.AWS_KPINFO, instanceType);
            int number = rdesc.getInstances().size();

            for (Instance inst : rdesc.getInstances()) {
                this.instanceIds.add(inst.getInstanceId());
            }
            currentInstances += number;

            logger.debug("Created " + number + " instance" + ((number != 1) ? "s" : ""));

            return rdesc.getInstances();
        } catch (EC2Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * Terminate a running instance
     * 
     * @param inst the instance to terminate
     * @return true upon success, or false
     */
    public boolean terminateInstance(Instance inst) {
        Jec2 ec2req = getEC2Wrapper();

        if (ec2req == null)
            return false;

        try {
            ec2req.terminateInstances(new String[] { inst.getInstanceId() });
            this.currentInstances--;
            return true;

        } catch (EC2Exception e) {
            logger.error("Failed to terminate instance: " + inst, e);
            return false;
        }
    }

    /**
     * Try to terminate an instance from EC2 with IP/Host addr
     *
     * @param hostname
     *            hostname of the node
     * @param ip
     *            ip of the node
     *
     * @return True on success, false otherwise
     */
    public boolean terminateInstanceByAddr(InetAddress addr) {
        Jec2 ec2req = getEC2Wrapper();

        if (ec2req == null)
            return false;

        List<Instance> instances = this.getInstances();

        for (Instance i : instances) {
            try {
                InetAddress inetAddr = InetAddress.getByName(i.getDnsName());
                if (inetAddr.equals(addr)) {
                    terminateInstance(i);
                }
            } catch (UnknownHostException e1) {
                logger.error("Unable to resolve instance Inet Address: " + i.getDnsName(), e1);
            }
        }
        return false;
    }

    /**
     * 
     * @return the number of instances currently running
     */
    public int getCurrentInstances() {
        return currentInstances;
    }

    /**
     *
     * @return the maximum number of instances to attempt to reserve
     */
    public int getMaxInstances() {
        return maxInstances;
    }

    /**
     * Sets the number of instances to request
     *
     * @param min
     *            Minimum number of instance to attempt to reserve
     * @param max
     *            Maximum number of instance to attempt to reserve
     */
    public void setNumInstances(int min, int max) {
        this.minInstances = Math.max(min, 1);
        this.maxInstances = Math.max(max, minInstances);
    }

    /**
     *
     * @return <code>true</code> if this infrastructure is allowed to acquire more nodes
     */
    public boolean canGetMoreNodes() {
        return (currentInstances < maxInstances);
    }

    /**
     * Sets the instance type
     *
     * the smaller the cheaper;
     * the larger the faster;
     * 64bit AMI need to be run on xlarge instances
     *
     * @param it The type of hardware on which nodes will be deployed
     * @throws IllegalArgumentException when the provided String does not match any
     * existing instance type
     */
    public void setInstanceType(String it) {
        this.instanceType = InstanceType.getTypeFromString(it);
        if (instanceType == null) {
            throw new IllegalArgumentException("Invalid instance type: " + it);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "EC2Deployer :: " + "User[" + this.AWS_USER + "] " + "Status[" +
            ((this.active) ? "active" : "unactive") + "] ";
        //  	"Instances[" + this.getInstances(true).size() + "]";
    }
}
