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

import org.bouncycastle.asn1.x509.X509Name;

import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.JDKKeyPairGenerator;

import java.io.IOException;
import java.io.Serializable;

import java.math.BigInteger;

import java.security.KeyPair;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;

import java.text.DateFormat;

import java.util.Date;
import java.util.Hashtable;
import java.util.Random;
import java.util.logging.Logger;


public class DefaultProActiveSecurityManager extends ProActiveSecurityManager
    implements Serializable {
    private transient Logger logger;

    public DefaultProActiveSecurityManager() {
        //	Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        //      Security.addProvider(myProvider);
        //		  Security.insertProviderAt(myProvider, 0);
    }

    public DefaultProActiveSecurityManager(String vide)
        throws Exception {
        super(vide);
        sessions = new Hashtable();
        logger = Logger.getLogger(
                "org.objectweb.proactive.ext.security.DefaultProActiveSecurityManager");

        Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(myProvider);

        /* generation of a default certificate */
        KeyPair keyPair = null;
        SecureRandom rand = new SecureRandom();

        JDKKeyPairGenerator.RSA keyPairGen = new JDKKeyPairGenerator.RSA();

        keyPairGen.initialize(1024, rand);

        keyPair = keyPairGen.generateKeyPair();

        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        X509V3CertificateGenerator certifGenerator = new X509V3CertificateGenerator();

        X509Certificate certif = null;

        DateFormat convert = DateFormat.getDateInstance();

        certifGenerator.setPublicKey(publicKey);

        String subjectCN = "CN=Generic Certificate" + new Random().nextLong() +
            ", OU=Generic Certificate, EmailAddress=none";

        //  System.out.println("DefaultCertificate subjectCN " + subjectCN);
        X509Name subject = new X509Name(subjectCN);
        X509Name issuer = new X509Name(
                "CN=Generic Certificate, OU=Generic Certificate, EmailAddress=none");

        certifGenerator.setSubjectDN(subject);
        certifGenerator.setIssuerDN(issuer);
        certifGenerator.setSignatureAlgorithm("MD5withRSA");

        //    GregorianCalendar start = new GregorianCalendar(2002, Calendar.JUNE, 13);
        //  GregorianCalendar end = new GregorianCalendar(2004, Calendar.JUNE, 31);
        Date start = new Date(System.currentTimeMillis() - 50000);
        Date stop = new Date(System.currentTimeMillis() + 50000);

        certifGenerator.setNotAfter(stop);
        certifGenerator.setNotBefore(start);
        certifGenerator.setPublicKey(publicKey);
        certifGenerator.setSerialNumber(new BigInteger("1"));

        certificate = certifGenerator.generateX509Certificate(privateKey, "BC");

        byte[] t = certificate.getEncoded();
        certificate = ProActiveSecurity.decodeCertificate(t);

        // System.out.println("Generic certificate created " + certificate.getSubjectDN());
        //  new RuntimeException().printStackTrace();
        //   System.out.println("******************** instantiated DefaultPSM ao Thread " + Thread.currentThread().getName() + "******************");
        // throw new SecurityException();
    }

    /*
        public synchronized void initiateSession(UniversalBody distantBody) throws CommunicationForbiddenException, AuthenticationException {
            X509Certificate distantBodyCertificate = null;
            Policy localPolicy = null;
            Policy distantBodyPolicy = null;

            long sessionID = 0;

            try {
                sessionID = distantBody.startNewSession();
                //                System.out.println("new session ID is : " + sessionID);
            } catch (IOException e) {
                logger.warning("can't start a new session");
                e.printStackTrace();
                throw new org.objectweb.proactive.ext.security.crypto.AuthenticationException();
            } catch (RenegotiateSessionException e) {
                    terminateSession(sessionID);
                            //e.printStackTrace();
                    }

            Session session = null;

            try {
                session = new Session(sessionID);
            //    session.setPolicy(resultPolicy);
            } catch (Exception e) {
                e.printStackTrace();
            }

            session.distantBody = distantBody;
            try {
                            byte [] certE = distantBody.getRemoteAdapter().getCertificateEncoded();
                                                       X509Certificate cert = ProActiveSecurity.decodeCertificate(certE);

                            session.setDistantOACertificate(cert);
                    } catch (IOException e2) {
                            e2.printStackTrace();
                    }
            sessions.put(new Long(sessionID), session);

            if (distantBodyCertificate != null) {
                session.setDistantOAPublicKey(distantBodyCertificate.getPublicKey());
            } else {
                try {
                                    session.setDistantOAPublicKey(distantBody.getPublicKey());
                            } catch (IOException e1) {
                                    e1.printStackTrace();
                            }
            }

            try {
              //  logger.info("Key exchange "  + sessionID);
                keyNegociationSenderSide(distantBody, sessionID);
              //  System.out.println("Session from " + certificate.getSubjectDN() + " to " + session.distantOACertificate.getSubjectDN());
            } catch (KeyExchangeException e) {
                logger.warning("Key exchange exception ");
                e.printStackTrace();
            }

            //     if ( localPolicy.isAuthenticationEnabled() && distantBodyPolicy.isAuthenticationEnabled() ) {
            //            mutualAuthenticationSenderSide(distantBody,distantBodyCertificate);
            //       }
        }
    */
    /*  public X509Certificate getCertificate() {
           logger.info("asked for my certificate, replied null");
           return null;
       }*/
    public ProActiveSecurityManager getProActiveSecurityManager()
        throws java.io.IOException {
        return this;
    }

    public Policy getPolicyFrom(X509Certificate certificate) {
        //     logger.info("asked for my policy FROM, replied default policy");
        return new Policy();
    }

    public Policy getPolicyTo(X509Certificate certificate) {
        //   logger.info("asked for my policy TO, replied default policy");
        return new Policy();
    }

    public Communication getPolicyTo(String type, String from, String to) {
        //   logger.info("asked for my policy TO, replied default policy");
        return new Communication();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        //System.out.println("reconstruit un DPSM");
        logger = Logger.getLogger(
                "org.objectweb.proactive.ext.security.DefaultProActiveSecurityManager");
    }
}
