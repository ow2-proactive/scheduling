/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s):
*
* ################################################################
*/
package org.objectweb.proactive.ext.security;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509Principal;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.xml.XMLPropertiesStore;


public class PolicyServer implements Serializable {
    protected static Logger logger = Logger.getLogger(PolicyServer.class.getName());
    private static int REQUIRED = 1;
    private static int DENIED = -1;
    private static int OPTIONAL = 0;
    private static String XML_CERTIFICATE = "/Policy/Certificate";
    private static String XML_PRIVATE_KEY = "/Policy/PrivateKey";
    private static String XML_TRUSTED_CERTIFICATION_AUTHORITY = "/Policy/TrustedCertificationAuthority/CertificationAuthority";
    private static String XML_CERTIFICATION_AUTHORITY_CERTIFICATE = "Certificate";
    private XMLPropertiesStore p;
    private Hashtable certificates;
    private Policy[] policy;
    private String VNName;
    protected X509Certificate certificate;
    protected PrivateKey privateKey;
    protected ArrayList policies;
    protected X509Certificate applicationCertificate;
    protected PrivateKey applicationPrivateKey;
    protected String f;
    protected String applicationName;

    public PolicyServer() {
        Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(myProvider);
    }

    /*public PolicyServer(String file) {
        try {
            p = new XMLPropertiesStore(file);
        } catch (IOException e) {
            logger.warn("can't find file " + file);
            e.printStackTrace();
        }

        certificates = new Hashtable();
        storeCertificate(p.getAllNodes("/Policy/Rules/Rule/From"));
        storeCertificate(p.getAllNodes("/Policy/Rules/Rule/To"));

        System.out.println("done");




    }
    */

    /**
     * Method storeCertificate.
     * read all certificates and store them in a hashtable
     * used when the security is turned on. When the active object migrate,
     * it takes all the certificates with it.
     */
    private void storeCertificate(org.w3c.dom.Node[] nodes) {
        int i = 0;
        for (; i < nodes.length; i++) {
            String targetType = p.getValueAsString("Entity/@type", nodes[i])
                                 .trim();
            if ((targetType != null) && targetType.equals("certificate")) {
                String certificateFile = p.getValueAsString("Target", nodes[i])
                                          .trim();
                System.out.println("Storing certificate " + certificateFile);
                if (certificateFile.equals("Default")) {
                    break;
                }
                try {
                    InputStream inStream = new FileInputStream(certificateFile);
                    CertificateFactory cfe = CertificateFactory.getInstance(
                            "X.509");
                    X509Certificate certificate = (X509Certificate) cfe.generateCertificate(inStream);
                    certificates.put(certificateFile, certificate);
                    inStream.close();
                } catch (IOException e) {
                    logger.warn(" Certificate file " + certificateFile +
                        " not found");
                    e.printStackTrace();
                } catch (java.security.cert.CertificateException e) {
                    logger.warn(
                        "An error occurs while loading active object certificate");
                    e.printStackTrace();
                }
            }
        }
    }

    private int convert(String name) {
        if (name.equals("required") || name.equals("allowed") ||
                name.equals("authorized")) {
            return REQUIRED;
        } else if (name.equals("denied")) {
            return DENIED;
        } else {
            return OPTIONAL;
        }
    }

    public Policy getPolicyTo(X509Certificate distantOA) {
        int[] tab = new int[4];
        int i = 0;
        boolean cont = true;

        if (distantOA != null) {
            //        logger.info("looking for a policy to " + distantOA.getIssuerDN());
        } else {
            //         logger.info("looking for a policy to an masked/unsecure distantOA");
        }

        org.w3c.dom.Node[] nodes = p.getAllNodes("/Policy/Rules/Rule");
        Policy defaultPolicy = null;

        for (; i < nodes.length; i++) {
            String certificateName = p.getValueAsString("Target", nodes[i])
                                      .trim();

            if (certificateName.equals("Default")) {
                String s = p.getValueAsString("Communication/To/@value",
                        nodes[i]).trim();
                tab[0] = convert(s);
                s = p.getValueAsString("Communication/To/Attributes/@authentication",
                        nodes[i]).trim();
                tab[1] = convert(s);
                tab[2] = convert(p.getValueAsString(
                            "Communication/To/Attributes/@confidentiality",
                            nodes[i]).trim());
                tab[3] = convert(p.getValueAsString(
                            "Communication/To/Attributes/@integrity", nodes[i])
                                  .trim());
                defaultPolicy = new Policy(); //(tab[0], tab[1], tab[2], tab[3]);
            } else {
                X509Certificate cert = (X509Certificate) certificates.get(certificateName);
                if (cert.equals(distantOA)) {
                    tab[0] = convert(p.getValueAsString(
                                "Communication/To/@value", nodes[i]).trim());
                    tab[1] = convert(p.getValueAsString(
                                "Communication/To/Attributes/@authentication",
                                nodes[i]).trim());
                    tab[2] = convert(p.getValueAsString(
                                "Communication/To/Attributes/@confidentiality",
                                nodes[i]).trim());
                    tab[3] = convert(p.getValueAsString(
                                "Communication/To/Attributes/@integrity",
                                nodes[i]).trim());

                    Policy policy = new Policy(); //(tab[0], tab[1], tab[2], tab[3]);

                    return policy;
                }
            }
        }

        //       logger.info("sending my default policy" + defaultPolicy);
        return defaultPolicy;
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException {
        Communication result = null;
        ArrayList entitiesFrom = securityContext.getEntitiesFrom();
        ArrayList entitiesTo = securityContext.getEntitiesTo();
        ArrayList localEntities = new ArrayList();

        if (policies == null) {
            logger.debug("trying to find a policy whereas none has been set" +
                this + "    " + policies);
            throw new SecurityNotAvailableException();
        }

        // setting local entities
        //     localEntities.add(new EntityVirtualNode(VNName));
        //org.w3c.dom.Node[] rules = p.getAllNodes("/Policy/Rules/Rule");
        Policy policy = null;
        Policy matchingPolicy = null;
        Policy defaultPolicy = new Policy();
        Communication communication;
        Communication defaultCommunication = new Communication();
        if (entitiesFrom == null) {
            entitiesFrom = new ArrayList();
            entitiesFrom.add(new DefaultEntity());
        }
        if (entitiesTo == null) {
            entitiesTo = new ArrayList();
            entitiesTo.add(new DefaultEntity());
        }

        //System.out.println("from " + virtualNodeFrom + " -> to " + virtualNodeTo);
        int[] tab = new int[4];
        boolean matchingFrom;
        boolean matchingTo;
        boolean matchingFromDefault;
        boolean matchingToDefault;
        matchingFrom = matchingTo = matchingFromDefault = matchingToDefault = false;
        int length = policies.size();

        String s = "From :";
        for (int i = 0; i < entitiesFrom.size(); i++)
            s += (((Entity) entitiesFrom.get(i)) + " ");
        System.out.println(s);
        s = "To :";
        for (int i = 0; i < entitiesTo.size(); i++)
            s += (((Entity) entitiesTo.get(i)) + " ");
        System.out.println(s);
        for (int i = 0; i < length; i++) {
            policy = (Policy) policies.get(i);

            ArrayList policyEntitiesFrom = policy.getEntitiesFrom();
            for (int j = 0; !matchingFrom && (j < policyEntitiesFrom.size());
                    j++) {
                Entity policyEntityFrom = (Entity) policyEntitiesFrom.get(j);

               System.out.println("testing from" + policyEntityFrom);
                for (int z = 0; !matchingFrom && (z < entitiesFrom.size());
                        z++) {
                    Entity entity = (Entity) entitiesFrom.get(z);

                    //       System.out.println("testing from ---------" + entity);
                    if (policyEntityFrom instanceof DefaultEntity) {
                        matchingFromDefault = true;
                    } else if (policyEntityFrom.equals(entity)) {
                    	System.out.println("Matching From " + policyEntityFrom);
                        matchingFrom = true;
                    }
                }
            }

            //     System.out.println("testing from  matching :" + matchingFrom +
            //         " -- matchingFromDefault " + matchingFromDefault);
            ArrayList policyEntitiesTo = policy.getEntitiesTo();

            for (int j = 0; !matchingTo && (j < policyEntitiesTo.size());
                    j++) {
                Entity policyEntityTo = (Entity) policyEntitiesTo.get(j);

                 System.out.println("testing to" + policyEntityTo );
                for (int z = 0; !matchingTo && (z < entitiesTo.size()); z++) {
                    Entity entity = (Entity) entitiesTo.get(z);

                      System.out.println("testing to -------------" + entity);
                    if (policyEntityTo instanceof DefaultEntity) {
                        matchingToDefault = true;
                    } else if (policyEntityTo.equals(entity)) {
						System.out.println("Matching To " + policyEntityTo);
                        matchingTo = true;
                    }
                }
            }

            //    System.out.println("testing to  matching :" + matchingTo +
            //       " -- matchingToDefault " + matchingToDefault);
            if (matchingFrom && matchingTo) {
                matchingPolicy = policy;
                System.out.println("matching policy " + policy);
                break;
            }
            if (matchingToDefault && matchingFromDefault) {
                defaultPolicy = policy;
            }
            matchingToDefault = matchingFromDefault = false;
            matchingTo = matchingFrom = false;
            //       System.out.println("-- reset matching --");
        }

        if (matchingPolicy == null) {
            matchingPolicy = defaultPolicy;
        }

        if (matchingPolicy == null) {
            logger.warn("default Policy is null !!!!!!!!!!!!!!");
        }

        System.out.println("Policy is : " + matchingPolicy);

        //  TODO split receive of a request or a reply 
        if ((securityContext.getType() == SecurityContext.COMMUNICATION_RECEIVE_REQUEST_FROM) ||
                (securityContext.getType() == SecurityContext.COMMUNICATION_RECEIVE_REPLY_FROM)) {
            communication = matchingPolicy.getCommunicationReply();
            communication.setCommunication(1);
            securityContext.setReceiveReply(communication);
            securityContext.setReceiveRequest(communication);
        } else {
            communication = matchingPolicy.getCommunicationRequest();
            System.out.println("communication is " + communication);
            communication.setCommunication(1);
            securityContext.setSendReply(communication);
            securityContext.setSendRequest(communication);
        }

        if (securityContext.getType() == SecurityContext.MIGRATION_TO) {
            System.out.println(policy);
            securityContext.setMigration(matchingPolicy.isMigration());
        }

        return securityContext;
    }

    public Communication getPolicyTo(String type, String virtualNodeFrom,
        String virtualNodeTo) throws SecurityNotAvailableException {
        //        if (p == null) {
        //            logger.debug("SEcurityNamfndjdhuidss crac r cd boium");
        //            throw new SecurityNotAvailableException();
        //        }
        if (true) {
            throw new RuntimeException("DEPRECATED METHOD : UPDATE !!!");
        }
        return null;
    }

    public int[] computePolicy(int[] from, int[] to)
        throws ComputePolicyException {
        //    logger.info("calculating composed policy");
        if (((from[0] == REQUIRED) && (to[0] == DENIED)) ||
                ((from[1] == REQUIRED) && (to[1] == DENIED)) ||
                ((from[2] == REQUIRED) && (to[2] == DENIED)) ||
                ((from[0] == DENIED) && (to[0] == REQUIRED)) ||
                ((from[1] == DENIED) && (to[1] == REQUIRED)) ||
                ((from[2] == DENIED) && (to[2] == REQUIRED))) {
            throw new ComputePolicyException("incompatible policies");
        }

        return new int[] { from[0] + to[0], from[1] + to[1], from[2] + to[2] };
    }

    public boolean CanSendRequestTo(X509Certificate distantOA) {
        return false;
    }

    public boolean CanReceiveRequestFrom(X509Certificate distantOA) {
        return false;
    }

    public boolean CanSendReplyTo(X509Certificate distantOA) {
        return false;
    }

    public boolean CanReceiveReplyFrom(X509Certificate distantOA) {
        return false;
    }

    public boolean CanMigrateTo(X509Certificate distantOA) {
        return false;
    }

    public boolean canMigrateTo(String type, String from, String to) {
        Communication pol = null;
        try {
            System.out.println("Migration from " + from + "to" + to);
            ArrayList arrayFrom = new ArrayList();
            ArrayList arrayTo = new ArrayList();

            //       arrayFrom.add(new EntityVirtualNode(from));
            //      arrayTo.add(new EntityVirtualNode(to));
            SecurityContext sc = new SecurityContext(SecurityContext.MIGRATION_TO,
                    arrayFrom, arrayTo);
            return getPolicy(sc).isMigration();
        } catch (SecurityNotAvailableException e) {
            // no security all is permitted
            return true;
        }
    }

    public String toString() {
        String s = null;
        s = "file: " + f + "\n";
        for (int i = 0; i < policies.size(); i++) {
            s += policies.get(i);
        }

        return s;
    }

    // implements Serializable
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    /**
     * @param string
     */
    public void setVNName(String string) {
        this.VNName = string;
    }

    /**
     * @return
     */
    public String getVNName() {
        return VNName;
    }

    /**
     * @param string
     */
    public void setPrivateKey(String privateKeyFile) {
        logger.debug("Loading private key ...");

        RSAPrivateKey privateKey = null;
        PKCS8EncodedKeySpec keySpec = null;

        byte[] key_bytes = null;

        try {
            FileInputStream fis = new FileInputStream(privateKeyFile.trim());

            ByteArrayOutputStream key_baos = new ByteArrayOutputStream();
            byte[] input = new byte[fis.available()];
            fis.read(input, 0, input.length);
            //     int aByte = 0;
            //   while ((aByte = fis.read()) != -1) {
            //      key_baos.write(aByte);
            // }
            fis.close();

            //key_bytes = key_baos.toByteArray();
            //key_baos.close();
            KeyFactory key_factory = KeyFactory.getInstance("RSA", "BC");
            keySpec = new PKCS8EncodedKeySpec(input);
            privateKey = (RSAPrivateKey) key_factory.generatePrivate(keySpec);
        } catch (IOException e) {
            System.out.println("Private Key not found : file " +
                privateKeyFile + " not found");
            e.printStackTrace();
        } catch (java.security.spec.InvalidKeySpecException e) {
            System.out.println("private key invalide :" + privateKeyFile);
            e.printStackTrace();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (java.security.NoSuchProviderException e) {
            e.printStackTrace();
        }

        this.privateKey = privateKey;
        logger.info("Loading private key done ...");
    }

    /**
     * @param string
     */
    public void setCertificate(String certificateFile) {
        try {
            InputStream inStream = new FileInputStream(certificateFile);
            CertificateFactory cfe = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cfe.generateCertificate(inStream);
            inStream.close();
        } catch (IOException e) {
            logger.warn(" Certificate file " + certificateFile + " not found");
            e.printStackTrace();
        } catch (java.security.cert.CertificateException e) {
            logger.warn(
                "An error occurs while loading active object certificate");
            e.printStackTrace();
        }
        logger.debug("certificate loaded");
    }

    /**
     * @param policies
     */
    public void setPolicies(ArrayList policies) {
        logger.info("storing policies");
        this.policies = policies;
    }

    /**
     * @param uri
     */
    public void setFile(String uri) {
        // TODO remove it !!!!! only for test
        f = uri;
    }

    /**
     * @return
     */
    public X509Certificate getApplicationCertificate() {
        return this.applicationCertificate;
    }

    /**
     * @param certificate
     */
    public void setApplicationCertificate(String pathToApplicationcertificate) {
        try {
            InputStream inStream = new FileInputStream(pathToApplicationcertificate);
            CertificateFactory cfe = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cfe.generateCertificate(inStream);
            inStream.close();
        } catch (IOException e) {
            logger.warn(" Certificate file " + pathToApplicationcertificate +
                " not found");
            e.printStackTrace();
        } catch (java.security.cert.CertificateException e) {
            logger.warn(
                "An error occurs while loading active object certificate");
            e.printStackTrace();
        }
        this.applicationCertificate = certificate;
    //    logger.debug("Application certificate loaded" + applicationCertificate);
    }

    /**
     * @param key
     */
    public void setApplicationPrivateKey(String pathToApplicationPrivateKey) {
        if (applicationPrivateKey == null) {
            RSAPrivateKey privateKey = null;
            PKCS8EncodedKeySpec keySpec = null;

            byte[] key_bytes = null;

            try {
                FileInputStream fis = new FileInputStream(pathToApplicationPrivateKey.trim());

                ByteArrayOutputStream key_baos = new ByteArrayOutputStream();
                byte[] input = new byte[fis.available()];
                fis.read(input, 0, input.length);
                //     int aByte = 0;
                //   while ((aByte = fis.read()) != -1) {
                //      key_baos.write(aByte);
                // }
                fis.close();

                //key_bytes = key_baos.toByteArray();
                //key_baos.close();
                KeyFactory key_factory = KeyFactory.getInstance("RSA", "BC");
                keySpec = new PKCS8EncodedKeySpec(input);
                privateKey = (RSAPrivateKey) key_factory.generatePrivate(keySpec);
            } catch (IOException e) {
                System.out.println("Private Key not found : file " +
                    pathToApplicationPrivateKey + " not found");
                e.printStackTrace();
            } catch (java.security.spec.InvalidKeySpecException e) {
                System.out.println("private key invalide :" +
                    pathToApplicationPrivateKey);
                e.printStackTrace();
            } catch (java.security.NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (java.security.NoSuchProviderException e) {
                e.printStackTrace();
            }

            this.applicationPrivateKey = privateKey;
            logger.info("Loading private key done ...");
        }
    }

    /**
     * @param vnName
     * @param vmInformation
     */
    public void generateNodeCertificate(String vnName,
        VMInformation vmInformation) {
        if (certificate != null) {
            // Node certificate already generated
            return;
        }

        Object[] secret = null;

        // create node certificate
        if (applicationCertificate != null) {
            X509Name name = new X509Name(applicationCertificate.getSubjectDN()
                                                               .getName());
            Vector vName = name.getValues();
            Vector order = name.getOIDs();

            int index = order.indexOf(X509Principal.CN);

            String subject = applicationName + "  " + vnName;

            vName.set(index, subject);

            name = new X509Name(order, vName);

            secret = ProActiveSecurity.generateCertificate(name.toString(),
                    applicationCertificate.getSubjectDN().toString(),
                    applicationPrivateKey, applicationCertificate.getPublicKey());
            this.certificate = (X509Certificate) secret[0];
            this.privateKey = (PrivateKey) secret[1];
        }
    }

    /**
     * @return certificate of the entity
     */
    public X509Certificate getCertificate() {
        return certificate;
    }

    /**
     * @param
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }
}
