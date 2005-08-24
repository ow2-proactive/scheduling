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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignedObject;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.examples.garden.Flower;
import org.objectweb.proactive.ext.security.crypto.AuthenticationException;
import org.objectweb.proactive.ext.security.crypto.AuthenticationTicket;
import org.objectweb.proactive.ext.security.crypto.AuthenticationTicketProperty;
import org.objectweb.proactive.ext.security.crypto.ConfidentialityTicket;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.crypto.RandomLongGenerator;
import org.objectweb.proactive.ext.security.crypto.Session;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;
import org.xml.sax.SAXException;

import sun.rmi.server.MarshalOutputStream;


/**
 * @author acontes
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ProActiveSecurityManager implements Serializable {
    protected static Logger logger = Logger.getLogger(ProActiveSecurityManager.class.getName());

    /* contains all active sessions for the current active object */
    protected Hashtable sessions;

    /* random generator used for generating sesssion key */
    protected transient RandomLongGenerator randomLongGenerator;

    /* Policy server */
    protected PolicyServer policyServer;

    /* Active object certificate */
    protected X509Certificate certificate;
    protected byte[] encodedCertificate;

    /* Active Object private Key */
    protected PrivateKey privateKey;

    /* owner certificate */
    protected transient X509Certificate parentCertificate;
    protected PublicKey publicKey;
    protected byte[] privateKeyEncoded;
    protected X509Certificate[] trustedCertificationAuthority;

    //protected XMLPropertiesStore policiesRules;
    protected transient UniversalBody myBody;
    protected String VNName;

    // protected UniversalBody body;	

    /**
     * This a the default constructor to use with the ProActiveSecurityManager
     */
    public ProActiveSecurityManager() {
        sessions = new Hashtable();
    }

    /**
     * Method ProActiveSecurityManager.
     * @throws IOException if the file doesn't exist
     */
    public ProActiveSecurityManager(X509Certificate certificate, PrivateKey pk,
        PolicyServer ps) {
        Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(myProvider);
        this.policyServer = ps;
        this.certificate = certificate;
        this.privateKey = pk;
        publicKey = certificate.getPublicKey();
        sessions = new Hashtable();
    }

    public ProActiveSecurityManager(String file) throws java.io.IOException {
        Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(myProvider);
        sessions = new Hashtable();

        if ((new File(file)).exists()) {
            //this.policiesRules = new XMLPropertiesStore(file);
            try {
                this.policyServer = ProActiveSecurityDescriptorHandler.createPolicyServer(file);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            //            setCertificate();
            //            setPrivateKey();
            //            setTrustedCertificationAuthority();
            //            publicKey = certificate.getPublicKey();
        }
        logger.debug("psm" + file +
            " +-+--+-+-++-+-+-++-++-+--+-+-+-+-+-+-+-+-+-+-+-++--+-+-+-+-+-+-+-+ ");
    }

    /**
     * @param server
     */
    public ProActiveSecurityManager(PolicyServer server) {
        Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(myProvider);
        sessions = new Hashtable();
        this.policyServer = server;
        this.certificate = server.getCertificate();
        this.privateKey = server.getPrivateKey();
        this.publicKey = certificate.getPublicKey();
    }

    public void setBody(UniversalBody body) {
        myBody = body;
    }

    /**
     * Method setCertificate.
     * set the certificate of the active object
     */
    private void setCertificate() {

        /*        String certificateFile = policiesRules.getValueAsString(SecurityConstants.XML_CERTIFICATE)
           .trim();
           X509Certificate certificate = null;
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
           this.certificate = certificate;
         */
    }

    /**
     * Method getPolicyTo.
     * @param securityContext the object certificate we want to get the policy from
     * @return Policy policy attributes
     */
    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException {
        if (policyServer == null) {
            // if Active Obect policy server is null, maybe a runtime poilicy server exists
            try {
                policyServer = RuntimeFactory.getDefaultRuntime()
                                             .getPolicyServer();
                if (policyServer == null) {
                    throw new SecurityNotAvailableException(
                        "No Runtime nor Active Object Policy server found");
                }
            } catch (ProActiveException e) {
                throw new SecurityNotAvailableException(
                    "No Runtime nor Active Object Policy server found");
            }
        }
        return policyServer.getPolicy(securityContext);
    }

    /**
     * Method getPolicyTo.
     * @param certificate the object certificate we want to get the policy from
     * @return Policy policy attributes
     */
    public Policy getPolicyTo(X509Certificate certificate) {
        return policyServer.getPolicyTo(certificate);
    }

    /**
     * Method getPolicyTo.
     * @return Policy policy attributes
     */
    public Communication getPolicyTo(String type, String from, String to)
        throws SecurityNotAvailableException {
        if (policyServer == null) {
            throw new SecurityNotAvailableException();
        }
        return policyServer.getPolicyTo(type, from, to);
    }

    /**
     * Method setTrustedCertificationAuthority.
     * Loads external trusted certification authority if exist
     * Done once when the ProActiveSecurityManager is created
     */
    private void setTrustedCertificationAuthority() {

        /*      X509Certificate[] trustedCertificationAuthority = null;
           X509Certificate certificate;
           String file = "";
           try {
               org.w3c.dom.Node[] nodes = policiesRules.getAllNodes(SecurityConstants.XML_TRUSTED_CERTIFICATION_AUTHORITY);
               if (nodes == null) {
           //   logger.info(" No Trusted Certification Authority");
                   return;
               }
               int i = 0;
               // initialize the array of trusted CA
               trustedCertificationAuthority = new X509Certificate[nodes.length];
               // prepare to read CA certificate
               InputStream inStream = null;
               // read all certificate from disk and save it in memory
               for (; i < nodes.length; i++) {
                   // get the file path
                   file = policiesRules.getValueAsString(SecurityConstants.XML_CERTIFICATION_AUTHORITY_CERTIFICATE,
                           nodes[i]);
                   file = file.trim();
                   //initialize the reader
                   inStream = new FileInputStream(file);
                   CertificateFactory cfe = CertificateFactory.getInstance("X.509");
                   certificate = (X509Certificate) cfe.generateCertificate(inStream);
                   // Add the certificate
                   trustedCertificationAuthority[i] = certificate;
               }
               // close the stream
               if (inStream != null) {
                   inStream.close();
               }
           } catch (java.security.cert.CertificateException e) {
               System.out.println(
                   "An error occurs while loading authority certification certificate");
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           }
           this.trustedCertificationAuthority = trustedCertificationAuthority;
         */
    }

    private void decodePrivateKey() {
        RSAPrivateKey privateKey = null;

        try {
            KeyFactory key_factory = KeyFactory.getInstance("RSA", "BC");
            PKCS8EncodedKeySpec key_spec = new PKCS8EncodedKeySpec(privateKeyEncoded);
            privateKey = (RSAPrivateKey) key_factory.generatePrivate(key_spec);
        } catch (java.security.spec.InvalidKeySpecException e) {
            System.out.println("private key invalide");
            e.printStackTrace();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (java.security.NoSuchProviderException e) {
            e.printStackTrace();
        }

        this.privateKey = privateKey;
    }

    /**
     * Method setPrivateKey.
     * sets the private key of the active object
     */
    private void setPrivateKey() {
        //  logger.info("Loading private key ...");

        /*
           String privateKeyFile = policiesRules.getValueAsString(SecurityConstants.XML_PRIVATE_KEY)
                                                .trim();
           RSAPrivateKey privateKey = null;
           PKCS8EncodedKeySpec key_spec = null;
           byte[] key_bytes = null;
           try {
               FileInputStream fis = new FileInputStream(privateKeyFile);
               ByteArrayOutputStream key_baos = new ByteArrayOutputStream();
               int aByte = 0;
               while ((aByte = fis.read()) != -1) {
                   key_baos.write(aByte);
               }
               fis.close();
               key_bytes = key_baos.toByteArray();
               key_baos.close();
               KeyFactory key_factory = KeyFactory.getInstance("RSA", "BC");
               key_spec = new PKCS8EncodedKeySpec(key_bytes);
               privateKey = (RSAPrivateKey) key_factory.generatePrivate(key_spec);
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
           this.privateKeyEncoded = key_bytes;
           this.privateKey = privateKey;
         */

        //   logger.info("Loading private key done ...");
    }

    /**
     * Method initiateSession. This method is the entry point for an secured communication. We get local and distant policies,
     * compute it, and generate the result policy, then if needed, we start an symmetric key exchange to encrypt the communication.
     * @param distantBody
     * @throws CommunicationForbiddenException
     * @throws AuthenticationException
     */
    public void initiateSession(int type, UniversalBody distantBody)
        throws CommunicationForbiddenException, 
            org.objectweb.proactive.ext.security.crypto.AuthenticationException, 
            RenegotiateSessionException, SecurityNotAvailableException {
        X509Certificate distantBodyCertificate = null;
        Communication localPolicy = null;
        Communication distantBodyPolicy = null;

        PolicyServer runtimePolicyServer = null;

        distantBody = distantBody.getRemoteAdapter();

        /*
           // get runtime Policy Server if exists
           try {
               runtimePolicyServer = RuntimeFactory.getDefaultRuntime()
                                                   .getPolicyServer();
           } catch (ProActiveException e1) {
               e1.printStackTrace();
           }
           // identify local Virtual Node
           Node n = null;
           //System.out.println ("myBody is intanceof HalfBody " + (myBody instanceof HalfBody) + myBody + " " + myBody.getClass());
           logger.debug(" myBody.getNodeURL() : " + myBody.getNodeURL() +
               "VNNAME " + VNName);

           if (VNName == null) {
               // && (! myBody.getNodeURL().equals("LOCAL"))) {
               // can be null if security was not enable at lauching time
               // retrieving node's virtual node name
               try {
                   //logger.debug (" myBody.getNodeURL() : "+ myBody.getNodeURL());
                   System.out.println("NODE LOCAL");
                   if (myBody.getNodeURL().equals("LOCAL")) {
                       VNName = NodeFactory.getDefaultNode().getVnName();
                   } else {
                       System.out.println("NODE PAS LOCAL");
                       n = NodeFactory.getNode(myBody.getNodeURL());
                       VNName = ProActiveSecurity.retrieveVNName(n.getNodeInformation()
                                                                  .getName());
                   }
               } catch (NodeException e2) {
                   e2.printStackTrace();
               }
               //} else {
               //        VNName = "*";
           }
           if (n != null) {
               logger.debug("sender : node ' " + n.getNodeInformation().getURL() +
                   "  " + n.getNodeInformation().getName() +
                   "' - virtual node : '" + VNName);
           }
           String distantOAVirtualNode = null;
           try {
               distantOAVirtualNode = distantBody.getVNName();
           } catch (IOException e3) {
               e3.printStackTrace();
           }
         */
        Communication runtimePolicy;
        Communication VNPolicy;
        Communication distantPolicy;
        runtimePolicy = VNPolicy = distantBodyPolicy = null;
        ArrayList arrayFrom = new ArrayList();
        ArrayList arrayTo = new ArrayList();

        /*
           if (VNName == null) {

           } else {

         */

        // retrienes entities from source 
        try {
            arrayFrom = myBody.getEntities();
        } catch (SecurityNotAvailableException e2) {
            e2.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        // no entity found
        if (arrayFrom.size() == 0) {
            arrayFrom.add(new DefaultEntity());
        }

        //arrayFrom.add(new EntityVirtualNode(VNName));
        //}
        // retrieves entities from destination
        try {
            arrayTo = distantBody.getEntities();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        if (arrayTo.size() == 0) {
            arrayTo.add(new DefaultEntity());
        }

        ///arrayTo.add(new EntityVirtualNode(distantOAVirtualNode));
        //}

        /*
           if (runtimePolicyServer != null) {
                   if (distantOAVirtualNode == null) {
                       // distant Active Object is not security aware
                       distantOAVirtualNode = "*";
                   }

         */

        // retrieve distant policy from local object
        SecurityContext sc = new SecurityContext(SecurityContext.COMMUNICATION_SEND_REQUEST_TO,
                arrayFrom, arrayTo);

        sc = policyServer.getPolicy(sc);

        localPolicy = sc.getSendRequest();

        /*
           } else {
               logger.debug("No Runtime policy server installed : VN[ " + VNName +
                   "], node " + myBody.getNodeURL());
               localPolicy = new Communication();
           }
         */
        if (!localPolicy.isCommunicationAllowed()) {
            throw new CommunicationForbiddenException(
                "Sending request is denied");
        }

        // retrieve policy from distant object
        SecurityContext scDistant = new SecurityContext(SecurityContext.COMMUNICATION_RECEIVE_REQUEST_FROM,
                arrayFrom, arrayTo);

        try {
            scDistant = distantBody.getPolicy(scDistant);
        } catch (SecurityNotAvailableException e1) {
            // TODOSECURITY Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODOSECURITY Auto-generated catch block
            e1.printStackTrace();
        }

        distantPolicy = scDistant.getReceiveRequest();

        if (!distantPolicy.isCommunicationAllowed()) {
            throw new CommunicationForbiddenException(
                "Receiving request denied ");
        }

        if (distantBodyPolicy == null) {
            distantBodyPolicy = new Communication();
        }

        //compute policy
        Communication resultPolicy = Communication.computeCommunication(localPolicy,
                distantBodyPolicy);

        //scDistant.setProposedPolicy(resultPolicy);
        //distantBody.getPolicy(scDistant);
        long sessionID = 0;
        try {
            sessionID = distantBody.startNewSession(resultPolicy);
        } catch (IOException e) {
            logger.warn("can't start a new session");
            e.printStackTrace();
            throw new org.objectweb.proactive.ext.security.crypto.AuthenticationException();
        }

        Session session = null;

        try {
            session = new Session(sessionID, resultPolicy);

            session.distantBody = distantBody;
            session.setDistantOACertificate(distantBodyCertificate);
            sessions.put(new Long(sessionID), session);

            if (distantBodyCertificate != null) {
                session.setDistantOAPublicKey(distantBodyCertificate.getPublicKey());
            } else {
                session.setDistantOAPublicKey(distantBody.getPublicKey());
            }

            logger.debug("VN[" + VNName + "]:" + myBody + " -> VN " +
                "Key echange session id :" + sessionID);
            if (session.getCommunication().isConfidentialityEnabled()) {
                keyNegociationSenderSide(distantBody, sessionID);
            }
        } catch (KeyExchangeException e) {
            logger.warn("Key exchange exception ");
            e.printStackTrace();
            throw new CommunicationForbiddenException();
        } catch (java.io.IOException e) {
            logger.warn("exception thrown while initiating the session");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void terminateSession(UniversalBody body, long sessionID) {
        terminateSession(sessionID);
    }

    public void terminateSession(long sessionID) {
        synchronized (sessions) {
            sessions.remove(new Long(sessionID));

            Session s = (Session) sessions.get(new Long(sessionID));
            if (s == null) {
                System.out.println("Session " + sessionID +
                    " deleted, new size " + sessions.size());
            } else {
                System.out.println("ARRRRGGGGGGG Session " + sessionID +
                    " not deleted");
            }
        }
    }

    public long startNewSession(Communication po) {
        long id = 0;
        Policy defaultPolicy = new Policy();
        if (!defaultPolicy.equals(po)) {
            try {
                Session ses = null;
                id = new Random().nextLong() + System.currentTimeMillis();

                Session newSession = ses = new Session(id, po);
                sessions.put(new Long(id), newSession);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return id;
    }

    /**
     * Method encrypt.
     * @param sessionID the session we use to encrypt the Object
     * @param object the object to encrypt
     * @return byte[][] encrypted result
     */
    public byte[][] encrypt(long sessionID, Object object) {
        Session session = (Session) sessions.get(new Long(sessionID));

        if (session != null) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();

                MarshalOutputStream out = new MarshalOutputStream(bout);
                out.writeObject(object);
                out.flush();
                out.close();

                byte[] byteArray = bout.toByteArray();

                bout.close();

                return session.writePDU(byteArray);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //         return encryptionEngine.encrypt(message, ((Session) sessions.get(s)).getSessionKey(id));
        }

        return null;
    }

    /**
     * Method decrypt.
     * @param sessionID the session we use to decrypt the message
     * @param message the message to decrypt
     * @return byte[] the decrypted message returns as byte array
     */
    public byte[] decrypt(long sessionID, byte[][] message)
        throws RenegotiateSessionException {
        Session session = (Session) sessions.get(new Long(sessionID));
        if (session != null) {
            try {
                System.out.println("decrypting request");
                return session.readPDU(message[0], message[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("session is null");
        }

        /*else {
           Object o;
           if (myBody instanceof BodyImpl) {
               o = ((Flower) ((BodyImpl) myBody).getReifiedObject()).getName();
           } else {
               o = "HalfBody ";
           }
           logger.warn(o + "I have not find " + sessionID +
               " session to decrypt the message ");
           throw new RenegotiateSessionException(myBody.getRemoteAdapter());
           }
         */
        return null;
    }

    public boolean mutualAuthenticationSenderSide(UniversalBody distantBody,
        X509Certificate distantBodyCertificate) throws AuthenticationException {
        checkCertificate(distantBodyCertificate);
        unilateralAuthenticationSenderSide(distantBody);

        return true;
    }

    /**
     * Method checkCertificate. Checks the validity of an certificate
     * @param distantBodyCertificate the certificate to check
     * @return boolean. returns true if the certificate is valid, false otherwise
     */
    private boolean checkCertificate(X509Certificate distantBodyCertificate) {
        //  logger.info("Checking distant OA certificate validity");
        try {
            distantBodyCertificate.checkValidity();
        } catch (CertificateExpiredException e) {
            logger.warn(distantBodyCertificate.getSubjectDN() +
                " has expired, negociation stopped");

            return false;
        } catch (CertificateNotYetValidException e) {
            logger.warn(distantBodyCertificate.getSubjectDN() +
                " is not yet valid, negociation stopped");

            return false;
        }

        //     logger.info("Retrieving DistantOA Domain Server");
        String domainLocation = distantBodyCertificate.getIssuerDN().getName();

        return true;
    }

    public boolean unilateralAuthenticationSenderSide(UniversalBody distantBody)
        throws AuthenticationException {
        long rb = randomLongGenerator.generateLong(32);
        AuthenticationTicket authenticationTicket = new AuthenticationTicket();
        String B = certificate.getIssuerDN().getName();
        long ra = authenticationTicket.random;
        String addresse = authenticationTicket.identity;

        if (addresse.equals(B) == false) {
            throw new AuthenticationException(
                "SessionInitializer : WRONG IDENTITY");
        }

        // Emitter Certificate Checking
        X509Certificate emitterCertificate = authenticationTicket.certificate;
        String A = emitterCertificate.getIssuerDN().getName();

        // A is the sessionInitializer
        checkCertificate(emitterCertificate);

        AuthenticationTicketProperty properties = new AuthenticationTicketProperty();

        try {
            properties = (AuthenticationTicketProperty) ((SignedObject) authenticationTicket.signedAuthenticationTicketProperty).getObject();
        } catch (Exception e) {
            System.out.println(
                "SessionInitializer : Exception in AuthenticationTicketProperty extraction : " +
                e);
        }

        if (properties.random1 != ra) {
            throw new AuthenticationException("SessionInitializer : wrong ra");
        }

        if (properties.random2 != rb) {
            throw new AuthenticationException("SessionInitializer : wrong rb");
        }

        if (properties.identity.equals(B) == false) {
            throw new AuthenticationException("SessionInitializer : wrong B");
        }

        //    this.authentication = true;
        return true;
    }

    /**
     * Method keyNegociationSenderSide. starts the challenge to negociate a session key.
     * @param distantOA distant active object we want to communicate to.
     * @param sessionID the id of the session we will use
     * @return boolean returns true if the negociation has succeed.
     * @throws KeyExchangeException
     */
    public boolean keyNegociationSenderSide(UniversalBody distantOA,
        long sessionID) throws KeyExchangeException {
        Session session = (Session) sessions.get(new Long(sessionID));
        distantOA = distantOA.getRemoteAdapter();
        if (session == null) {
            throw new KeyExchangeException("the session is null");
        }

        try {
            // Step 1. public key exchange for authentication
            //
            // Send a HELLO to server + my random value.
            // The server will now respond with its Hello + random.
            //  se_rand is the server response
            // Read the HELLO back from the server and collect
            // the Server Random value.
            //
            session.sec_rand.nextBytes(session.cl_rand);
            session.se_rand = distantOA.randomValue(sessionID, session.cl_rand);

            // Next send my public key from the key pair that is only
            // used for encryption/decryption purposes. Then sign the whole
            // exchange with my signing only key pair.
            //
            //
            // Set up Signature Class.
            //
            byte[] my_pub;
            byte[] my_cert;
            byte[] sig_code;
            Signature sig = Signature.getInstance("MD5withRSA", "BC");

            //
            // Init signature with the private key used for signing.
            //
            sig.initSign(privateKey, session.sec_rand);

            //
            // All signatures incorporate the client random and the server
            // random values.
            //
            sig.update(session.cl_rand); // Incorporated into every sig.
            sig.update(session.se_rand);

            //
            // Get my public key (for encryption) as a byte array.
            //
            my_pub = publicKey.getEncoded();

            //
            // Get my certificate (for sig validation and auth) as a byte array.
            //
            my_cert = certificate.getEncoded();
            sig.update(my_pub); // Incorporate public key into signature.
            sig.update(my_cert); // Incorporate certificate into signature.

            sig_code = sig.sign();

            //            System.out.println(session);
            //
            // complete the PDU and send it to the server
            //
            byte[][] tab = distantOA.publicKeyExchange(sessionID,
                    myBody.getRemoteAdapter(), my_pub, my_cert, sig_code);

            //
            // Now server should respond with its public key exchange message.
            // If it does not I must break as the protocol has been broken.
            //
            //
            // Read in Server Public key.
            //
            byte[] pub_key = tab[0];

            //
            // Before we can use the public key we must convert it back
            // to a Key object by using the KeyFactory and the appropriate
            // KeySpec.. In this case the X509EncodedKeySpec is the correct one.
            //
            X509EncodedKeySpec key_spec = new X509EncodedKeySpec(pub_key);
            KeyFactory key_fact = KeyFactory.getInstance("RSA", "BC");

            //
            // Recover Servers Public key.
            //
            session.distantOAPublicKey = key_fact.generatePublic(key_spec);

            //
            // Read in the encoded form of the X509Certificate that the
            // server uses. For authentication of its identity.
            //
            byte[] cert = tab[1];

            //
            // Set up a Certificate Factory to process the raw certificate
            // back into an X509 Certificate
            //
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            //
            // Recover Servers Certificate
            //
            session.distantOACertificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                        cert));

            // NOTE:
            // At this point it should be noted that the client must employ
            // some mechanism to validate and authenticate the servers
            // certificate.
            //              PublicKey se_public = distantOA.getPublicKey();
            //             X509Certificate server_cert = distantOA.getCertificate();
            //
            // Read in Signature code.
            //
            sig_code = tab[2];

            //
            // Now we must verify the data that we received against
            // the signature code at the end of the pdu.
            //
            //
            // Using the authentication certificate sent by the server.
            //
            synchronized (sig) {
                sig.initVerify(session.distantOACertificate);
                sig.update(session.cl_rand); // Incorporate in Client Random.
                sig.update(session.se_rand); // Incorporate in Server Random.
                sig.update(pub_key); // Incorporate in Public key (as sent in encoded form).
                sig.update(cert); // Incorporate in Certificate. (as sent in encoded form).

                if (!sig.verify(sig_code)) {
                    throw new Exception(
                        "(CLIENT)Signature failed on Public key exchange data unit");
                }
            }

            // ==== confidentiality part : secret key exchange 
            //
            // Now that we have successfully exchanged public keys
            // The client now needs to being a SecretKey Exchange process.
            // First we need to generate some secrets using the appropriate
            // KeyGenerator. When using the JCE you should always use a
            // KeyGenerator instance specifically set up for your target cipher.
            // The KeyGenerator code will generate a secret that is "safe" for use
            // and filter out any keys that may be weak or broken for that cipher.
            //
            KeyGenerator key_gen = KeyGenerator.getInstance("AES", "BC"); // Get instance for AES
            synchronized (key_gen) {
                key_gen.init(128, session.sec_rand); // Use a 192 bit key size.
                session.cl_aes_key = key_gen.generateKey(); // Generate the  SecretKey

                key_gen.init(160, session.sec_rand); // Set up for a 160 bit key.
                session.cl_hmac_key = key_gen.generateKey(); // Generate key for HMAC.
            }
            session.cl_iv = new IvParameterSpec(new byte[16]);

            Object i = new Object();
            byte[] aes_key;
            byte[] iv;
            byte[] mac;
            byte[] lock;
            byte[] sigtab;

            //
            // I have added this extra piece of data so that an attacker
            // cannot resign or modify the secret exchange without being one of the recipients.
            //
            byte[] tmp_lock = new byte[24];
            Cipher aes_lock = null;
            synchronized (i) {
                //
                // Next we need it instantiate the Cipher class that will be used by
                // the client. As this is the client side the cipher needs to be set
                // up for Encryption.
                //
                session.cl_cipher.init(Cipher.ENCRYPT_MODE, session.cl_aes_key,
                    session.cl_iv, session.sec_rand);

                //
                // Set up Client side MAC with key.
                //
                session.cl_mac.init(session.cl_hmac_key);

                //
                // Load byte array with random data.
                //
                session.sec_rand.nextBytes(tmp_lock);

                //
                // Set up RSA for encryption.
                //
                session.rsa_eng.init(Cipher.ENCRYPT_MODE,
                    session.distantOAPublicKey);

                //
                // Set up and instace of AES for the Signature locking data.
                //
                aes_lock = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
                aes_lock.init(Cipher.ENCRYPT_MODE, session.cl_aes_key,
                    session.cl_iv, session.sec_rand);

                //
                //  Secret Keys Exchange. = confidentiality 
                //
                //
                // Set up Signature so that the server can validate our data.
                //
                sig.initSign(privateKey);
                sig.update(session.cl_rand); // Incorporate Client Random
                sig.update(session.se_rand); // Incorporate Server Random.

                aes_key = session.rsa_eng.doFinal(session.cl_aes_key.getEncoded()); // Encrypt the encoded AES key.
                sig.update(aes_key); // Incorporate into signature.

                iv = session.rsa_eng.doFinal(session.cl_iv.getIV()); // Encrypt the IV.
                sig.update(iv); // Incorporate into signature.

                mac = session.rsa_eng.doFinal(session.cl_hmac_key.getEncoded()); // Encrypt and encode MAC key.
                sig.update(mac); // Incorporate into signature.

                lock = aes_lock.doFinal(tmp_lock); // Encrypt lock data.
                sig.update(tmp_lock); // Incorporate plain text of lock data into signature.

                // send to server and get the results
                sigtab = sig.sign();
            }

            byte[][] tabresult = distantOA.secretKeyExchange(sessionID,
                    aes_key, iv, mac, lock, sigtab);

            //
            // Read back the server secret.
            //
            // The server should respond with its secret if it does not then
            // the protocol is broken..
            //
            //
            // Set up RSA in decrypt mode so that we can decrypt their
            // server's secrets.
            //
            byte[] aes_key_enc;

            //
            // Read back the server secret.
            //
            // The server should respond with its secret if it does not then
            // the protocol is broken..
            //
            //
            // Set up RSA in decrypt mode so that we can decrypt their
            // server's secrets.
            //
            byte[] iv_enc;

            //
            // Read back the server secret.
            //
            // The server should respond with its secret if it does not then
            // the protocol is broken..
            //
            //
            // Set up RSA in decrypt mode so that we can decrypt their
            // server's secrets.
            //
            byte[] hmac_key_enc;

            //
            // Read back the server secret.
            //
            // The server should respond with its secret if it does not then
            // the protocol is broken..
            //
            //
            // Set up RSA in decrypt mode so that we can decrypt their
            // server's secrets.
            //
            byte[] tmp_loc;

            synchronized (session.rsa_eng) {
                session.rsa_eng.init(Cipher.DECRYPT_MODE, privateKey,
                    session.sec_rand);

                //
                // Read in secret key.
                //
                aes_key_enc = tabresult[0];

                //
                // Read in IV
                //
                iv_enc = tabresult[1];

                //
                // Read in HMAC key.
                //
                hmac_key_enc = tabresult[2];

                //
                // Read in lock
                //
                tmp_lock = tabresult[3];

                //
                // Now we must validate the received data.
                // NOTE: the need to decrypt the tmp_lock data.
                //
                //
                // Set up AES lock so we can decrypt lock data.
                //
                SecretKey sk = (SecretKey) new SecretKeySpec(session.rsa_eng.doFinal(
                            aes_key_enc), "AES");
                IvParameterSpec ivspec = new IvParameterSpec(session.rsa_eng.doFinal(
                            iv_enc));
                aes_lock.init(Cipher.DECRYPT_MODE, sk, ivspec);
                sig.initVerify(session.distantOACertificate); // Set up using server's certificate.
                sig.update(session.cl_rand);
                sig.update(session.se_rand);
                sig.update(aes_key_enc);
                sig.update(iv_enc);
                sig.update(hmac_key_enc);
                sig.update(aes_lock.doFinal(tmp_lock));

                if (!sig.verify(tabresult[4])) {
                    throw new Exception(
                        "Signature failed on Public key exchange data unit");
                } else {
                    //   System.out.println("Client: Server PDU for secret key exchange signature passed");
                }

                //
                // At this point we have successfully exchanged secrets..
                // So now we need to set up a Cipher class to allow us to
                // Decrypt data sent from the server to the client.
                //
                // 
                session.se_aes_key = (SecretKey) new SecretKeySpec(session.rsa_eng.doFinal(
                            aes_key_enc), "AES");
                session.se_iv = new IvParameterSpec(session.rsa_eng.doFinal(
                            iv_enc));
                session.se_cipher.init(Cipher.DECRYPT_MODE, session.se_aes_key,
                    session.se_iv);

                //
                // We also need to set up the MAC so that we can validate
                // data sent from the server.
                //
                session.se_hmac_key = (SecretKey) new SecretKeySpec(session.rsa_eng.doFinal(
                            hmac_key_enc), "AES");
                session.se_mac.init(session.se_hmac_key);
            }

            //  System.out.println("session key end");
            //
            // Set up session to generate appropriate PDUs.
            // To see this shape of the PDUs that are used to exchange
            // encrypted data.
            //
            // setup for sending and receiving are automaticly done if we succeed
            // the key echange
        } catch (Exception e) {
            e.printStackTrace();
            throw new KeyExchangeException();

            //e.printStackTrace();
        }

        return true;
    }

    public AuthenticationTicket mutualAuthenticationReceiverSide(
        AuthenticationTicket authenticationTicket, long randomID)
        throws org.objectweb.proactive.ext.security.crypto.AuthenticationException {
        return null;
    }

    /**
     * Method generateSessionKey. generates a session key using Rijndael algorithms.
     * @return Key a symetric key used to encrypt/decrypt communications between active objects
     */
    private Key generateSessionKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("Rijndael", "BC");
            keyGen.init(128, new SecureRandom());

            return keyGen.generateKey();
        } catch (java.security.NoSuchProviderException e) {
            e.printStackTrace();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public AuthenticationTicket unilateralAuthenticationReceiverSide(
        long randomID, long rb, String emittor) throws AuthenticationException {
        return null;
    }

    public ConfidentialityTicket keyNegociationReceiverSide(
        ConfidentialityTicket confidentialityTicket, long randomID)
        throws KeyExchangeException {
        return null;
    }

    public byte[] randomValue(long sessionID, byte[] cl_rand)
        throws Exception {
        // server side
        // Step one..
        // Upon receipt of client Hello the server process reads
        // in a byte array of 32 random bytes. The server process
        // then responds with a byte array of random data.
        //
        // 	System.out.println("RAndomValue sessionID : " + new Long(sessionID));
        //        	System.out.println("++++++++++++++++++ List opened sessions : ++++++++++++++++++++++++");
        //         System.out.println("Certificat : " + certificate.getSubjectDN());
        //        for (Enumeration e = sessions.elements() ; e.hasMoreElements() ;) {
        //          System.out.println(e.nextElement());
        //      }
        //        System.out.println("++++++++++++++++++ End List opened sessions : ++++++++++++++++++++++++");
        Session session = (Session) sessions.get(new Long(sessionID));

        //	System.out.println("fsdfsda session : " + session);
        if (session == null) {
            throw new KeyExchangeException(
                "Session not started,session is null");
        }

        try {
            //         System.out.println("Server: got HELLO from client");
            session.cl_rand = cl_rand;

            //
            // Generate Server's Random;
            //
            session.sec_rand.nextBytes(session.se_rand); // Fill with random data.

            //   System.out.println("Server: Sending my HELLO to client");
        } catch (Exception e) {
            System.out.println("Server: Hello failed");
            e.printStackTrace();
        }

        return session.se_rand;
    }

    public byte[][] publicKeyExchange(long sessionID,
        UniversalBody distantBody, byte[] pub_key, byte[] cert, byte[] sig_code)
        throws Exception {
        // server side
        // Step two..
        // 1. The server reads in the clients public key.
        // 2. The clients signing certificate.
        // 3. A signature.
        //
        // Note that the signature is composed of:
        //    Client public key;
        //    Concatenation of Clients random and Server random;
        //    Clients signing certificate.
        //
        // By including the shared randoms we effectively make
        // this exchange unique each time a session is started.
        // This would stop an attacker from replaying a previously
        // recorded exchange.
        //
        Session session = (Session) sessions.get(new Long(sessionID));

        if (session == null) {
            throw new KeyExchangeException("Session not started");
        }

        // System.out.println("Server: Reading public key + cert + sig from client" + sessionID);
        // setting distant OA proxy
        session.distantBody = distantBody;

        // Read public key
        //
        // Now we must take in the encoded public key and
        // use a KeyFactory to turn it back into a Key Object.
        //
        X509EncodedKeySpec key_spec = new X509EncodedKeySpec(pub_key);
        KeyFactory key_fact = KeyFactory.getInstance("RSA", "BC");
        session.distantOAPublicKey = key_fact.generatePublic(key_spec); // Generate the public key.

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // System.out.println("certif :" + cert);
        session.distantOACertificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                    cert)); // Convert.

        Signature sig = null;

        sig = Signature.getInstance("MD5withRSA", "BC"); // Set up Signer.
        sig.initVerify(session.distantOAPublicKey); // Initialize with clients signing certificate.
        sig.update(session.cl_rand); // Incorporate client random.
        sig.update(session.se_rand); // Incorporate server random.
        sig.update(pub_key); // Incorporate encoded public key.
        sig.update(cert); // Incorporate encoded certificate.

        if (!sig.verify(sig_code)) {
            System.out.println(session);
            logger.warn("Signature failed on Public key exchange data unit");
            throw new Exception(
                "Signature failed on Public key exchange data unit");
        } else {
            //    System.out.println("Server: Client PDU signature passed");
        }

        //   System.out.println("Server: Sending my public key + cert + sig to client.");
        //
        // Send server public key to client.
        //
        //
        // Set up signer.
        //
        sig.initSign(privateKey);
        sig.update(session.cl_rand);
        sig.update(session.se_rand);

        //
        // Get my public key (for encryption) as a byte array.
        //
        byte[] my_pub = certificate.getPublicKey().getEncoded();

        //
        // Get my certificate (for sig validation and auth) as
        // a byte array.
        //
        byte[] my_cert = certificate.getEncoded();
        sig.update(my_pub);
        sig.update(my_cert);

        byte[][] result = new byte[4][];
        result[0] = certificate.getPublicKey().getEncoded();
        result[1] = certificate.getEncoded();
        sig_code = sig.sign();
        result[2] = sig_code;

        // return the results to the client
        return result;
    }

    public static String displayByte(byte[] tab) {
        String s = "";

        for (int i = 0; i < tab.length; i++) {
            s += tab[i];
        }

        return s;
    }

    /**
     * Method secretKeyExchange. exchamge secret between objects
     * @param sessionID the session
     * @param aesKey the private key
     * @param iv
     * @param macKey the MAC key
     * @param lockData
     * @param signature signature of aesKey,iv, macKey and lockData
     * @return byte[][]
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] aesKey, byte[] iv,
        byte[] macKey, byte[] lockData, byte[] signature) {
        byte[][] result = new byte[5][];

        try {
            Session session = (Session) sessions.get(new Long(sessionID));

            if (session == null) {
                throw new KeyExchangeException("Session not started");
            }

            // Part 3: The Secret Exchange..
            //
            // 1. Read SecretKey encrypted with RSA.
            // 2. Read IV encrypted with RSA.
            // 3. Read HMAC key encrypted with RSA.
            // 4. Read in signature.
            //
            // NOTE: No need to send cert again. Certificate
            // sent in previous stage.
            //
            Cipher aes_lock = Cipher.getInstance("AES/CBC/PKCS7Padding");

            //
            // Read in secret key .
            //
            byte[] aes_key_enc = aesKey;

            //
            // Read in IV
            //
            byte[] iv_enc = iv;

            //
            // Read in HMAC key.
            //
            byte[] hmac_key_enc = macKey;

            //
            // Read in lock
            //
            byte[] tmp_lock = lockData;

            //
            // Validate message against client's signing certificate
            // exchanged in the previous stage.
            //
            // But first we need to decrypt the lock data to incorperate into the exchange.
            //
            //      System.out.println("MyBody is " + myBody);
            //		System.out.println("session.rsa_eng is " + session.rsa_eng);
            //		System.out.println(" aes_key_enc " + aes_key_enc);
            //	System.out.println(" certi " +  certificate);
            Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
            Security.addProvider(myProvider);
            session.rsa_eng = Cipher.getInstance("RSA/None/OAEPPadding", "BC"); // RSA Cipher.

            session.rsa_eng.init(Cipher.DECRYPT_MODE, privateKey);

            SecretKey sk = (SecretKey) new SecretKeySpec(session.rsa_eng.doFinal(
                        aes_key_enc), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(session.rsa_eng.doFinal(
                        iv_enc));
            aes_lock.init(Cipher.DECRYPT_MODE, sk, ivspec);

            Signature sig = Signature.getInstance("MD5withRSA", "BC");
            sig.initVerify(session.distantOACertificate);
            sig.update(session.cl_rand);
            sig.update(session.se_rand);
            sig.update(aes_key_enc);
            sig.update(iv_enc);
            sig.update(hmac_key_enc);
            sig.update(aes_lock.doFinal(tmp_lock));

            if (!sig.verify(signature)) {
                throw new Exception(
                    "(Server) :Signature failed on Public key exchange data unit");
            } else {
                //       System.out.println("Server: Client PDU for secret key exchange signature passed");
            }

            //
            // Now we can set up a Cipher instance that will decrypt
            // data sent from the client.
            //
            session.se_aes_key = (SecretKey) new SecretKeySpec(session.rsa_eng.doFinal(
                        aes_key_enc), "AES");
            session.se_iv = new IvParameterSpec(session.rsa_eng.doFinal(iv_enc));
            session.se_cipher.init(Cipher.DECRYPT_MODE, session.se_aes_key,
                session.se_iv);

            //
            // Set up the MAC to validate data sent from the client
            // side.
            //
            session.se_mac_enc = session.rsa_eng.doFinal(hmac_key_enc);
            session.se_hmac_key = (SecretKey) new SecretKeySpec(session.se_mac_enc,
                    "AES");
            session.se_mac.init(session.se_hmac_key);

            //
            // Now send my secrets back to client encrypted with
            // public key exchanged by client.
            //
            //
            // Generate my secrets.
            //
            KeyGenerator key_gen = KeyGenerator.getInstance("AES", "BC");
            key_gen.init(192, session.sec_rand);
            session.cl_aes_key = key_gen.generateKey();
            key_gen.init(160, session.sec_rand);
            session.cl_hmac_key = key_gen.generateKey();

            // initialization of IV 
            session.cl_iv = new IvParameterSpec(new byte[16]);

            session.cl_cipher.init(Cipher.ENCRYPT_MODE, session.cl_aes_key,
                session.cl_iv, session.sec_rand);

            byte[] my_iv = session.cl_cipher.getIV();
            session.cl_iv = new IvParameterSpec(my_iv);
            session.cl_mac.init(session.cl_hmac_key);
            tmp_lock = new byte[24];
            session.sec_rand.nextBytes(tmp_lock);

            //
            // Set up RSA for encryption.
            //
            sig.initSign(privateKey);
            sig.update(session.cl_rand);
            sig.update(session.se_rand);
            session.rsa_eng.init(Cipher.ENCRYPT_MODE,
                session.distantOAPublicKey, session.sec_rand);

            //
            // Encrypt and send AES key.
            //
            result[0] = session.rsa_eng.doFinal(session.cl_aes_key.getEncoded());
            sig.update(result[0]);

            //
            // Encrypt and send IV for cipher.
            //
            result[1] = session.rsa_eng.doFinal(my_iv);
            sig.update(result[1]);

            //
            // Encrypt and send MAC key..
            //
            result[2] = session.rsa_eng.doFinal(session.cl_hmac_key.getEncoded());
            sig.update(result[2]);

            //
            // Encrypt and send LOCK data..
            //
            aes_lock.init(Cipher.ENCRYPT_MODE, session.cl_aes_key,
                new IvParameterSpec(my_iv), session.sec_rand);
            result[3] = aes_lock.doFinal(tmp_lock);
            sig.update(tmp_lock); // Incorporate plain text into signature.

            result[4] = sig.sign();

            //
            // Now we have finished exchanging symmetric keys we
            // can set up our PDU generators to send encrypted
            // messages.
            //
            //  System.out.println("Key exchanged and session is up");
        } catch (Exception e) {
            System.out.println("Invalid Key Exchange server side !");
            e.printStackTrace();
        }

        return result;
    }

    // implements Serializable
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        //	privateKeyEncoded = privateKey.getEncoded();
        try {
            if (certificate != null) {
                encodedCertificate = certificate.getEncoded();
            }
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        logger = Logger.getLogger(
                "org.objectweb.proactive.ext.security.ProActiveSecurityManager");

        //privateKeyEncoded = in.read();
        // Add bouncycastle security provider
        //    Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        //      Security.addProvider(myProvider);
        //  Security.insertProviderAt(myProvider, 0);
        // decompressing the private Key
        //        logger.info("decompressing private key " + privateKey);
        //decodePrivateKey();
        randomLongGenerator = new RandomLongGenerator();

        if (encodedCertificate != null) {
            certificate = ProActiveSecurity.decodeCertificate(encodedCertificate);

            this.certificate = policyServer.getCertificate();
            this.privateKey = policyServer.getPrivateKey();
            this.publicKey = certificate.getPublicKey();
        }

        //    logger.info("creating randomgenerator " + randomLongGenerator);
        //    logger.info("Security Manager restarted");
    }

    //    public long getSessionIDTo(UniversalBody distantBody) {
    //
    //        Session session = null;
    //
    //        for (Enumeration e = sessions.elements(); e.hasMoreElements();) {
    //            session = (Session)e.nextElement();
    //
    //            //      System.out.println("session " + session);
    //            //      System.out.println("session distantBody " + session.distantBody);
    //            //       System.out.println("distantBody " + distantBody);
    //            if (session != null) {
    //
    //                if ((session.distantBody != null) && 
    //                    (session.distantBody.equals(distantBody))) {
    //                    logger.info(
    //                            "find an already initialized session" + session);
    //
    //                    return session.sessionID;
    //                }
    //            }
    //        }
    //
    //        return (long)0;
    //    }
    public long getSessionIDTo(X509Certificate cert) {
        Object o;
        Object o1;
        o = o1 = null;
        if (myBody instanceof BodyImpl) {
            o1 = ((BodyImpl) myBody).getReifiedObject();
            if (o1 instanceof Flower) {
                o = ((Flower) o1).getName();
            } else {
                o = o1;
            }
        }

        //	System.out.println(o + "----------------------");
        //		System.out.println(o + "Source :" + certificate.getSubjectDN());
        //  	System.out.println(o + "Target :" + cert.getSubjectDN());
        Session session = null;
        if (sessions == null) {
            return (long) 0;
        }

        for (Enumeration e = sessions.elements(); e.hasMoreElements();) {
            session = (Session) e.nextElement();

            /*
               System.out.println("-----------------\nsession " + session);
               System.out.println("session distantBody " + session.distantOACertificate.getSubjectDN());
               System.out.println("distantBodyCertificate " + certificate.getSubjectDN());
               System.out.println("-----------------\n");
             */
            if (session != null) {
                //			System.out.println(o + "tested :" + session.distantOACertificate.getSubjectDN());
                if ((cert != null) && (session.distantOACertificate != null) &&
                        cert.getSubjectDN().equals(session.distantOACertificate.getSubjectDN())) {
                    // logger.info("find an already initialized session" + session);
                    //certificate.equals(session.distantOACertificate);
                    //				System.out.println(o+"=====yes========");
                    return session.sessionID;
                }
            }
        }

        //	System.out.println("=======no======");

        /* We didn't find a session */
        return (long) 0;
    }

    /**
     * Method getPublicKey.
     * @return PublicKey the public key of the active object
     */
    public PublicKey getPublicKey() {
        return certificate.getPublicKey();
    }

    /**
     *
     */
    public void setParentCertificate(X509Certificate certificate) {
        parentCertificate = certificate;
    }

    public Hashtable getOpenedConnexion() {
        Hashtable table = null;
        if (sessions == null) {
            return table;
        }

        table = new Hashtable();

        for (Enumeration e = sessions.keys(); e.hasMoreElements();) {
            Long l = (Long) e.nextElement();
            table.put(l, l.toString());
        }

        return table;
    }

    /**
     * allows to set the name of the current virtual node
     * @param string the name of the current Virtual Node if any
     */
    public void setVNName(String string) {
        //  System.out.println("setting vn node name " + string);
        this.VNName = string;

        //policyServer.setVNName(string);
    }

    /**
     * @return virtual node name where object has been created
     */
    public String getVNName() {
        return VNName;
    }

    /**
     * @return policy server
     */
    public PolicyServer getPolicyServer() {
        return policyServer;
    }

    /**
     * @return certificate as byte array
     */
    public byte[] getCertificateEncoded() {
        try {
            return certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Set object policy server
     * @param policyServer
     */
    public void setPolicyServer(PolicyServer policyServer) {
        this.policyServer = policyServer;
    }

    /**
     * @param type
     * @param from
     * @param to
     * @return communication attributes
     */
    public Communication getPolicyFrom(String type, String from, String to) {
        return null;
    }

    /**
     * @return entities that inforces security policy on the object
     */
    public ArrayList getEntities() {
        ProActiveRuntimeImpl proActiveRuntime = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();

        Node n = null;
        ArrayList a = new ArrayList();

        /*
           try {
               n = NodeFactory.getNode(myBody.getNodeURL());
           } catch (NodeException e) {
               e.printStackTrace();
           }
         */

        //     PolicyServer p = proActiveRuntime.getNodePolicyServer(n.getNodeInformation()
        //                                                            .getName());
        try {
            if ((policyServer != null) && (VNName != null)) {
                EntityVirtualNode entityVirtualNode = new EntityVirtualNode(VNName,
                        policyServer.getApplicationCertificate(),
                        policyServer.getCertificate());
                a.add(entityVirtualNode);
                return a;
            } else {
                a.add(new DefaultEntity());
            }
        } catch (Exception e) {
            //System.out.println(" exception in node " +
            //  n.getNodeInformation().getName() + "  " +
            //  n.getNodeInformation().getName());
            e.printStackTrace();
        }
        return a;
    }

    public Session getSession(long id) {
        return (Session) sessions.get(new Long(id));
    }
}
