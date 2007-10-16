/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.security.crypto;

import java.io.IOException;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.PolicyRule;
import org.objectweb.proactive.core.security.ProActiveSecurity;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class Session implements Serializable {
    // the session identifiant
    public long sessionID;
    protected static Object synchronizationObject = new Object();

    // The clients authentication and signing certificate.
    public X509Certificate distantOACertificate;

    // The clients public key for encryption and decryption.
    public PublicKey distantOAPublicKey;

    // Client Side Cipher.
    public transient Cipher cl_cipher;

    // Server Cipher.
    public transient Cipher se_cipher;

    //	the communication policy
    private Communication communication;

    // RSA Cipher.
    public transient Cipher rsa_eng;

    // Client side MAC
    public transient Mac cl_mac;

    // Server side MAC
    public transient Mac se_mac;
    public byte[] cl_sec_key;
    public byte[] se_sec_key;
    public byte[] cl_mac_enc;
    public byte[] se_mac_enc;
    public transient IvParameterSpec se_iv;
    public transient IvParameterSpec cl_iv;

    /* indicate if all security exchanged have been done
     * if not the sender must wait until this session is validated
     */
    protected boolean isSessionValidated;
    public AlgorithmParameters seCipherAlgParams;
    public AlgorithmParameters clCipherAlgParams;
    public AlgorithmParameters seMacAlgParams;
    public AlgorithmParameters clMacAlgParams;
    public byte[] encodedSeCipherAlgParams;
    public byte[] encodedClCipherAlgParams;
    public byte[] encodedSeMacAlgParams;
    public byte[] encodedClMacAlgParams;

    // Server Random
    public byte[] se_rand;

    // Client Random
    public byte[] cl_rand;
    public SecretKey se_hmac_key;
    public SecretKey se_aes_key;
    public SecretKey cl_hmac_key;
    public SecretKey cl_aes_key;

    //    public boolean cipher = false;
    //  public byte[] iv;
    public transient SecureRandom sec_rand;

    // security context associated to the sesssion
    public SecurityContext securityContext;
    public static int ACT_AS_CLIENT = 1;
    public static int ACT_AS_SERVER = 2;

    public Session() {
    }

    public Session(long sessionID, Communication policy)
        throws Exception {
        this.communication = policy;
        isSessionValidated = false;
        //        synchronized (synchronizationObject) {
        se_rand = new byte[32]; // Server Random
        cl_rand = new byte[32]; // Client Random
        sec_rand = new SecureRandom();
        cl_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        se_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC"); // Server Cipher.
        rsa_eng = Cipher.getInstance("RSA/None/OAEPPadding", "BC"); // RSA Cipher.
        cl_mac = Mac.getInstance("HMACSHA1", "BC"); // Client side MAC
        se_mac = Mac.getInstance("HMACSHA1", "BC"); // Server side MAC
                                                    //      }

        this.sessionID = sessionID;
        distantOACertificate = null; // The clients public key for encryption and decryption.
        distantOAPublicKey = null; // The clients authentication and signing certificate.
    }

    public boolean isID(long ID) {
        if (ID == this.sessionID) {
            return true;
        }

        return false;
    }

    public X509Certificate get_otherPublicCertificate(long id) {
        if (this.sessionID == id) {
            return distantOACertificate;
        }

        return null;
    }

    public long getSessionID() {
        return sessionID;
    }

    public void setDistantOACertificate(X509Certificate distantBodyCertificate) {
        distantOACertificate = distantBodyCertificate;
    }

    public X509Certificate getDistantOACertificate() {
        return distantOACertificate;
    }

    public PublicKey getDistantOAPublicKey() {
        return distantOAPublicKey;
    }

    public void setDistantOAPublicKey(PublicKey distantOAPublicKey) {
        this.distantOAPublicKey = distantOAPublicKey;
    }

    public synchronized byte[][] writePDU(byte[] in, int type)
        throws Exception {
        byte[] mac = null;
        switch (type) {
        case 1:
            // act as client
            if (communication.isIntegrityEnabled()) {
                cl_mac.update(in); // Update plain text into MAC
            }
            if (communication.isConfidentialityEnabled()) {
                try {
                    cl_cipher.init(Cipher.ENCRYPT_MODE, cl_aes_key, cl_iv,
                        sec_rand);
                    // TODO_SECURITY find why I need to force the encrypt_mode here
                    // seems to happen when a method call is sent by an object to itself.
                    // relation with AC ?
                    in = cl_cipher.doFinal(in); // Encrypt data for recipient.
                } catch (Exception bex) {
                    bex.printStackTrace();
                    throw (new IOException("PDU failed to encrypt " +
                        bex.getMessage()));
                }
            }

            if (communication.isIntegrityEnabled()) {
                ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                               .debug("writePDU as client cl_mac :" +
                    displayByte(cl_hmac_key.getEncoded()));
                mac = cl_mac.doFinal();
            }
            break;
        case 2:
            // act as server
            if (communication.isIntegrityEnabled()) {
                se_mac.update(in); // Update plain text into MAC
            }
            if (communication.isConfidentialityEnabled()) {
                try {
                    in = se_cipher.doFinal(in); // Encrypt data for recipient.
                } catch (Exception bex) {
                    bex.printStackTrace();
                    throw (new IOException("PDU failed to encrypt " +
                        bex.getMessage()));
                }
            }

            if (communication.isIntegrityEnabled()) {
                mac = se_mac.doFinal();
            }
            break;
        default:
            break;
        }

        //
        // Load mac with previous MAC value.
        // This forces each exchange into a chain
        // so that if any of the blocks are replayed out
        // of sequence the replayed blocks will fail.
        //
        //        cl_mac.update(mac);
        return new byte[][] { in, mac };
    }

    public static boolean isEqual(byte[] a, byte[] b) {
        if ((a == null) || (b == null)) {
            return (false);
        }

        if (a.length != b.length) {
            return (false);
        }

        for (int t = 0; t < a.length; t++) {
            if (a[t] != b[t]) {
                return (false);
            }
        }

        return (true);
    }

    public synchronized byte[] readPDU(byte[] in, byte[] mac, int type)
        throws IOException {
        // in is the encrypted data
        // mac is the mac
        switch (type) {
        case 1:
            // act as client 
            if (communication.isConfidentialityEnabled()) {
                try {
                    in = se_cipher.doFinal(in);
                } catch (Exception ex) {
                    ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                                   .debug("PDU Cipher code decryption failed, session " +
                        sessionID);
                    throw new IOException("PDU failed to decrypt " +
                        ex.getMessage());
                }
            }
            if (communication.isIntegrityEnabled()) {
                se_mac.update(in); // MAC is taken on plain text.

                byte[] m = null;
                m = se_mac.doFinal();

                if (!isEqual(m, mac)) {
                    ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                                   .debug("PDU Mac code failed , session " +
                        sessionID);
                    throw new IOException("PDU Mac code failed ");
                }
            }
            break;
        case 2:
            // act as server
            if (communication.isConfidentialityEnabled()) {
                try {
                    in = cl_cipher.doFinal(in);
                } catch (Exception ex) {
                    ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                                   .debug("PDU Cipher code decryption failed, session " +
                        sessionID);
                    throw new IOException("PDU failed to decrypt " +
                        ex.getMessage());
                }
            }
            if (communication.isIntegrityEnabled()) {
                cl_mac.update(in); // MAC is taken on plain text.

                byte[] m = null;
                m = cl_mac.doFinal();

                ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                               .debug("readPDU as server cl_mac :" +
                    displayByte(cl_hmac_key.getEncoded()));
                if (!isEqual(m, mac)) {
                    throw new IOException("PDU Mac code failed, session " +
                        sessionID);
                }
            }
            break;
        default:
            break;
        }

        //
        // Load mac with previous MAC value.
        // This forces each exchange into a chain
        // so that if any of the blocks are replayed out
        // of sequence the replayed blocks will fail.
        //
        //        se_mac.update(m);
        return (in);
    }

    // implements Serializable
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.defaultWriteObject();
        if (se_iv != null) {
            out.write(se_iv.getIV());
        } else {
            out.write(new byte[16]);
        }
        if (cl_iv != null) {
            out.write(cl_iv.getIV());
        } else {
            out.write(new byte[16]);
        }

        byte[] cert = new byte[0];
        try {
            if (distantOACertificate != null) {
                cert = distantOACertificate.getEncoded();
            }
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        out.writeInt(cert.length);
        out.write(cert);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        //if (cipher) {
        byte[] temp = new byte[16];
        in.read(temp);

        se_iv = new IvParameterSpec(temp);

        in.read(temp);
        cl_iv = new IvParameterSpec(temp);
        sec_rand = new SecureRandom();

        ProActiveSecurity.loadProvider();

        int i = in.readInt();
        byte[] certEncoded = new byte[i];
        in.read(certEncoded);

        distantOACertificate = ProActiveSecurity.decodeCertificate(certEncoded);

        try {
            cl_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            se_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC"); // Server Cipher.
            rsa_eng = Cipher.getInstance("RSA/None/OAEPPadding", "BC"); // RSA Cipher.
            sec_rand = new SecureRandom();
            cl_mac = Mac.getInstance("HMACSHA1", "BC"); // Client side MAC
            se_mac = Mac.getInstance("HMACSHA1", "BC"); // Server side MAC

            if ((se_iv != null) && (se_aes_key != null)) {
                se_cipher.init(Cipher.DECRYPT_MODE, (SecretKey) se_aes_key,
                    se_iv);
            }

            if ((cl_iv != null) && (cl_aes_key != null)) {
                cl_cipher.init(Cipher.ENCRYPT_MODE, cl_aes_key, cl_iv, sec_rand);
            }

            if ((se_mac != null) && (se_hmac_key != null)) {
                se_mac.init(se_hmac_key);
            }
            if ((cl_mac != null) && (cl_hmac_key != null)) {
                System.out.println("readObject session cl_mac : " +
                    displayByte(cl_hmac_key.getEncoded()));
                cl_mac.init(cl_hmac_key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //    }
    }

    public static String displayByte(byte[] in) {
        byte ch = 0x00;

        int i = 0;

        if ((in == null) || (in.length <= 0)) {
            return null;
        }

        String[] pseudo = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C",
                "D", "E", "F"
            };

        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length) {
            ch = (byte) (in[i] & 0xF0); // Strip off   high nibble

            ch = (byte) (ch >>> 4);
            // shift the bits down
            ch = (byte) (ch & 0x0F);
            //     must do this is high order bit is on!
            out.append(pseudo[(int) ch]); // convert the   nibble to a String Character

            ch = (byte) (in[i] & 0x0F); // Strip off   low nibble 

            out.append(pseudo[(int) ch]); // convert the    nibble to a String Character

            i++;
        }

        String rslt = new String(out);

        return rslt;
    }

    @Override
    public String toString() {
        return "ID : " + sessionID + "\n" + "cl_rand : " +
        displayByte(cl_rand) + "\n" + "se_rand : " + displayByte(se_rand);
    }

    /**
     * Method setPolicy.
     * @param resultPolicy
     */
    public void setPolicy(PolicyRule resultPolicy) {
    }

    public Communication getCommunication() {
        return communication;
    }

    /**
     *
     */
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * @param securityContext The securityContext to set.
     */
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public boolean isSessionValidated() {
        return isSessionValidated;
    }

    public void setSessionValidated(boolean isSessionValidated) {
        this.isSessionValidated = isSessionValidated;
        //   System.out.println("session " + sessionID + " validated ");
    }
}
