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
package org.objectweb.proactive.ext.security.crypto;

import java.io.IOException;
import java.io.Serializable;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.Policy;
import org.objectweb.proactive.ext.security.ProActiveSecurity;
import org.objectweb.proactive.ext.security.SecurityContext;


public class Session implements Serializable {
    // the session identifiant
    public long sessionID;

    // The clients authentication and signing certificate.
    public X509Certificate distantOACertificate;

    // The clients public key for encryption and decryption.
    public PublicKey distantOAPublicKey;

    //  The distant body
    public UniversalBody distantBody;

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

    // Server Random
    public byte[] se_rand;

    // Client Random
    public byte[] cl_rand;

    //    public byte[] cl_mac_mig;
    //    public byte[] se_mac_mig;
    public SecretKey se_hmac_key;
    public SecretKey se_aes_key;
    public SecretKey cl_hmac_key;
    public SecretKey cl_aes_key;

    //    public boolean cipher = false;
    //  public byte[] iv;
    public transient SecureRandom sec_rand;

    // security context associated to the sesssion
    public SecurityContext securityContext;
    
    public Session() {
    }

    public Session(long sessionID, Communication policy) throws Exception {
        this.communication = policy;

        se_rand = new byte[32]; // Server Random
        cl_rand = new byte[32]; // Client Random
        sec_rand = new SecureRandom();
        cl_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        se_cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC"); // Server Cipher.
        rsa_eng = Cipher.getInstance("RSA/None/OAEPPadding", "BC"); // RSA Cipher.
        cl_mac = Mac.getInstance("HMACSHA1", "BC"); // Client side MAC
        se_mac = Mac.getInstance("HMACSHA1", "BC"); // Server side MAC
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

    public byte[][] writePDU(byte[] in) throws Exception {
        byte[] mac = null;
        if (communication.isIntegrityEnabled()) {
            cl_mac.update(in); // Update plain text into MAC
            System.out.println("Session : integrity enabled  ");
        }

        if (communication.isConfidentialityEnabled()) {
            try {
                in = cl_cipher.doFinal(in); // Encrypt data for recipient.
                System.out.println("Session : integrity confidentiality ");
            } catch (Exception bex) {
                bex.printStackTrace();
                throw (new IOException("PDU failed to encrypt " +
                    bex.getMessage()));
            }
        }
        
        if (communication.isIntegrityEnabled()) {
            mac = cl_mac.doFinal();
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

    public byte[] readPDU(byte[] in, byte[] mac) throws Exception {
        // in is the encrypted data
        // mac is the mac
        if (communication.isConfidentialityEnabled()) {
            try {
                in = se_cipher.doFinal(in);
            } catch (Exception ex) {
                System.out.println("PDU Mac code decryption failed ");
                throw new IOException("PDU failed to decrypt " +
                    ex.getMessage());
            }
        }
        if (communication.isIntegrityEnabled()) {
            se_mac.update(in); // MAC is taken on plain text.

            byte[] m = null;
            m = se_mac.doFinal();

            if (!isEqual(m, mac)) {
                System.out.println("PDU Mac code failed ");
                throw new IOException("PDU Mac code failed ");
            }
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

        // Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        // Security.addProvider(myProvider);
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

            // cl_cipher.init (Cipher.ENCRYPT_MODE, (SecretKey)new SecretKeySpec(aes_key.getEncoded(),"AES"), new IvParameterSpec(iv));
            if ((cl_iv != null) && (cl_aes_key != null)) {
                cl_cipher.init(Cipher.ENCRYPT_MODE, cl_aes_key, cl_iv, sec_rand);
            }

            // cl_cipher.init(Cipher.ENCRYPT_MODE, aes_key, sec_rand);
            // se_cipher.init(Cipher.DECRYPT_MODE, aes_key, sec_rand);
            //se_mac.init((SecretKey)new SecretKeySpec(hmac_key.getEncoded(), "AES"));
            //    cl_mac.init((SecretKey)new SecretKeySpec(hmac_key.getEncoded(), "AES"));
            // cl_mac.update(cl_mac_mig);
            // se_mac.update(se_mac_mig);
            //  cl_mac.init(hmac_key);
            //  se_mac.init(hmac_key);
            //   System.out.println("Session readobject se_mac :  " + se_mac);
            //   System.out.println("Session readobject se_hmac_key :  " + se_hmac_key);
            if ((se_mac != null) && (se_hmac_key != null)) {
                se_mac.init(se_hmac_key);
            }
            if ((cl_mac != null) && (se_hmac_key != null)) {
                cl_mac.init(cl_hmac_key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //    }
    }

    private String displayByte(byte[] tab) {
        String s = "";

        for (int i = 0; i < tab.length; i++) {
            s += tab[i];
        }

        return s;
    }

    public String toString() {
        return "ID : " + sessionID + "\n" + "cl_rand : " +
        displayByte(cl_rand) + "\n" + "se_rand : " + displayByte(se_rand);
    }

    /**
     * Method setPolicy.
     * @param resultPolicy
     */
    public void setPolicy(Policy resultPolicy) {
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
}
