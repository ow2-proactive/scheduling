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
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.objectweb.proactive.core.security.ProActiveSecurity;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class Session implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = 3314095815395811127L;

    // the session identifiant
    private long distantSessionID = 0;

    //    protected static Object synchronizationObject = new Object();

    // The clients authentication and signing certificate.
    private TypedCertificate distantOACertificate;

    //    // The clients public key for encryption and decryption.
    //    private PublicKey distantAOPublicKey;

    // Client Side Cipher.
    public transient Cipher cl_cipher;

    // Server Cipher.
    public transient Cipher se_cipher;

    //    //	the communication policy
    //    private SecurityContext communication;

    // RSA Cipher.
    public transient Cipher rsa_eng;

    // Client side MAC
    public transient Mac cl_mac;

    // Server side MAC
    public transient Mac se_mac;
    private byte[] cl_sec_key;
    private byte[] se_sec_key;
    public byte[] cl_mac_enc;
    private byte[] se_mac_enc;
    public transient IvParameterSpec se_iv;
    public transient IvParameterSpec cl_iv;

    /* indicate if all security exchanged have been done
     * if not the sender must wait until this session is validated
     */
    private boolean isSessionValidated;
    private AlgorithmParameters seCipherAlgParams;
    private AlgorithmParameters clCipherAlgParams;
    private AlgorithmParameters seMacAlgParams;
    private AlgorithmParameters clMacAlgParams;
    private byte[] encodedSeCipherAlgParams;
    private byte[] encodedClCipherAlgParams;
    private byte[] encodedSeMacAlgParams;
    private byte[] encodedClMacAlgParams;

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
    private SecurityContext securityContext;
    public enum ActAs {CLIENT,
        SERVER;
    }
    public Session() {
    }

    public Session(long distantId, SecurityContext securityContext,
        TypedCertificate certificate) throws SessionException {
        this.securityContext = securityContext;
        this.isSessionValidated = false;
        //        synchronized (synchronizationObject) {
        this.se_rand = new byte[32]; // Server Random
        this.cl_rand = new byte[32]; // Client Random
        this.sec_rand = new SecureRandom();
        try {
            this.cl_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            this.se_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC"); // Server Cipher.
            this.rsa_eng = Cipher.getInstance("RSA/None/OAEPPadding", "BC"); // RSA Cipher.
            this.cl_mac = Mac.getInstance("HMACSHA1", "BC"); // Client side MAC
            this.se_mac = Mac.getInstance("HMACSHA1", "BC"); // Server side MAC
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new SessionException("Impossible to create the session.");
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            throw new SessionException("Impossible to create the session.");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            throw new SessionException("Impossible to create the session.");
        }
        //      }
        this.distantSessionID = distantId;
        this.distantOACertificate = certificate; // The clients public key for encryption and decryption.
                                                 //        this.distantAOPublicKey = null; // The clients authentication and signing certificate.
    }

    //    public boolean isID(long ID) {
    //        if (ID == this.sessionID) {
    //            return true;
    //        }
    //
    //        return false;
    //    }

    //    public X509Certificate get_otherPublicCertificate(long id) {
    //        if (this.sessionID == id) {
    //            return distantOACertificate;
    //        }
    //
    //        return null;
    //    }
    public long getDistantSessionID() {
        return this.distantSessionID;
    }

    public void setDistantSessionID(long id) {
        this.distantSessionID = id;
    }

    //    public void setDistantOACertificate(TypedCertificate distantBodyCertificate) {
    //        this.distantOACertificate = distantBodyCertificate;
    //    }
    public TypedCertificate getDistantCertificate() {
        return this.distantOACertificate;
    }

    public PublicKey getDistantPublicKey() {
        return this.distantOACertificate.getCert().getPublicKey();
    }

    //    public PublicKey getDistantOAPublicKey() {
    //        return distantOAPublicKey;
    //    }
    //
    //    public void setDistantOAPublicKey(PublicKey distantOAPublicKey) {
    //        this.distantOAPublicKey = distantOAPublicKey;
    //    }
    public synchronized byte[][] writePDU(byte[] in, ActAs type)
        throws Exception {
        byte[] mac = null;
        switch (type) {
        case CLIENT:
            // act as client
            if (this.securityContext.getSendRequest().isIntegrityEnabled()) {
                this.cl_mac.update(in); // Update plain text into MAC
            }
            if (this.securityContext.getSendRequest().isConfidentialityEnabled()) {
                try {
                    this.cl_cipher.init(Cipher.ENCRYPT_MODE, this.cl_aes_key,
                        this.cl_iv, this.sec_rand);
                    // TODO_SECURITY find why I need to force the encrypt_mode here
                    // seems to happen when a method call is sent by an object to itself.
                    // relation with AC ?
                    in = this.cl_cipher.doFinal(in); // Encrypt data for recipient.
                } catch (Exception bex) {
                    bex.printStackTrace();
                    throw new IOException("PDU failed to encrypt " +
                        bex.getMessage());
                }
            }

            if (this.securityContext.getSendRequest().isIntegrityEnabled()) {
                ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                               .debug("writePDU as client cl_mac :" +
                    displayByte(this.cl_hmac_key.getEncoded()));
                mac = this.cl_mac.doFinal();
            }
            break;
        case SERVER:
            // act as server
            if (this.securityContext.getSendReply().isIntegrityEnabled()) {
                this.se_mac.update(in); // Update plain text into MAC
            }
            if (this.securityContext.getSendReply().isConfidentialityEnabled()) {
                try {
                    in = this.se_cipher.doFinal(in); // Encrypt data for recipient.
                } catch (Exception bex) {
                    bex.printStackTrace();
                    throw new IOException("PDU failed to encrypt " +
                        bex.getMessage());
                }
            }

            if (this.securityContext.getSendReply().isIntegrityEnabled()) {
                mac = this.se_mac.doFinal();
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
            return false;
        }

        if (a.length != b.length) {
            return false;
        }

        for (int t = 0; t < a.length; t++) {
            if (a[t] != b[t]) {
                return false;
            }
        }

        return true;
    }

    public synchronized byte[] readPDU(byte[] in, byte[] mac, ActAs type)
        throws IOException {
        // in is the encrypted data
        // mac is the mac
        switch (type) {
        case CLIENT:
            // act as client 
            if (this.securityContext.getReceiveReply().isConfidentialityEnabled()) {
                try {
                    in = this.se_cipher.doFinal(in);
                } catch (Exception ex) {
                    ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                                   .debug("PDU Cipher code decryption failed, session " +
                        this.distantSessionID);
                    throw new IOException("PDU failed to decrypt " +
                        ex.getMessage());
                }
            }
            if (this.securityContext.getReceiveReply().isIntegrityEnabled()) {
                this.se_mac.update(in); // MAC is taken on plain text.

                byte[] m = null;
                m = this.se_mac.doFinal();

                if (!isEqual(m, mac)) {
                    ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                                   .debug("PDU Mac code failed , session " +
                        this.distantSessionID);
                    throw new IOException("PDU Mac code failed ");
                }
            }
            break;
        case SERVER:
            // act as server
            if (this.securityContext.getReceiveRequest()
                                        .isConfidentialityEnabled()) {
                try {
                    in = this.cl_cipher.doFinal(in);
                } catch (Exception ex) {
                    ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                                   .debug("PDU Cipher code decryption failed, session " +
                        this.distantSessionID);
                    throw new IOException("PDU failed to decrypt " +
                        ex.getMessage());
                }
            }
            if (this.securityContext.getReceiveRequest().isIntegrityEnabled()) {
                this.cl_mac.update(in); // MAC is taken on plain text.

                byte[] m = null;
                m = this.cl_mac.doFinal();

                ProActiveLogger.getLogger(Loggers.SECURITY_SESSION)
                               .debug("readPDU as server cl_mac :" +
                    displayByte(this.cl_hmac_key.getEncoded()));
                if (!isEqual(m, mac)) {
                    throw new IOException("PDU Mac code failed, session " +
                        this.distantSessionID);
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
        return in;
    }

    // implements Serializable
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.defaultWriteObject();
        if (this.se_iv != null) {
            out.write(this.se_iv.getIV());
        } else {
            out.write(new byte[16]);
        }
        if (this.cl_iv != null) {
            out.write(this.cl_iv.getIV());
        } else {
            out.write(new byte[16]);
        }

        //        byte[] cert = new byte[0];
        //        try {
        //            if (distantOACertificate != null) {
        //                cert = distantOACertificate.getEncoded();
        //            }
        //        } catch (CertificateEncodingException e) {
        //            e.printStackTrace();
        //        }
        //        out.writeInt(cert.length);
        //        out.write(cert);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        //if (cipher) {
        byte[] temp = new byte[16];
        in.read(temp);

        this.se_iv = new IvParameterSpec(temp);

        in.read(temp);
        this.cl_iv = new IvParameterSpec(temp);
        this.sec_rand = new SecureRandom();

        ProActiveSecurity.loadProvider();

        //        int i = in.readInt();
        //        byte[] certEncoded = new byte[i];
        //        in.read(certEncoded);
        //
        //        distantOACertificate = ProActiveSecurity.decodeCertificate(certEncoded);
        try {
            this.cl_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            this.se_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC"); // Server Cipher.
            this.rsa_eng = Cipher.getInstance("RSA/None/OAEPPadding", "BC"); // RSA Cipher.
            this.sec_rand = new SecureRandom();
            this.cl_mac = Mac.getInstance("HMACSHA1", "BC"); // Client side MAC
            this.se_mac = Mac.getInstance("HMACSHA1", "BC"); // Server side MAC

            if ((this.se_iv != null) && (this.se_aes_key != null)) {
                this.se_cipher.init(Cipher.DECRYPT_MODE, this.se_aes_key,
                    this.se_iv);
            }

            if ((this.cl_iv != null) && (this.cl_aes_key != null)) {
                this.cl_cipher.init(Cipher.ENCRYPT_MODE, this.cl_aes_key,
                    this.cl_iv, this.sec_rand);
            }

            if ((this.se_mac != null) && (this.se_hmac_key != null)) {
                this.se_mac.init(this.se_hmac_key);
            }
            if ((this.cl_mac != null) && (this.cl_hmac_key != null)) {
                System.out.println("readObject session cl_mac : " +
                    displayByte(this.cl_hmac_key.getEncoded()));
                this.cl_mac.init(this.cl_hmac_key);
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
            out.append(pseudo[ch]); // convert the   nibble to a String Character

            ch = (byte) (in[i] & 0x0F); // Strip off   low nibble 

            out.append(pseudo[ch]); // convert the    nibble to a String Character

            i++;
        }

        String rslt = new String(out);

        return rslt;
    }

    @Override
    public String toString() {
        return "ID : " + this.distantSessionID + "\n" + "cl_rand : " +
        displayByte(this.cl_rand) + "\n" + "se_rand : " +
        displayByte(this.se_rand);
    }

    //    /**
    //     * Method setPolicy.
    //     * @param resultPolicy
    //     */
    //    public void setPolicy(PolicyRule resultPolicy) {
    //    }

    //    public Communication getCommunication() {
    //        return communication;
    //    }

    /**
     *
     */
    public SecurityContext getSecurityContext() {
        return this.securityContext;
    }

    //    /**
    //     * @param securityContext The securityContext to set.
    //     */
    //    public void setSecurityContext(SecurityContext securityContext) {
    //        this.securityContext = securityContext;
    //    }
    public boolean isSessionValidated() {
        return this.isSessionValidated;
    }

    public void validate() {
        this.isSessionValidated = true;
    }
}
