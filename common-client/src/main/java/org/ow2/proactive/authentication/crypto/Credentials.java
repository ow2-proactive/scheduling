/*
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
package org.ow2.proactive.authentication.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.interfaces.DHPublicKey;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.apache.commons.codec.binary.Base64;


/**
 * Encapsulates encrypted Scheduler credentials
 * <p>
 * Stores encapsulated Scheduler credentials as well as metadata used to
 * determine which method should be used for decryption: key generation
 * algorithm, key size, and cipher parameters.
 * <p>
 * The credentials are encrypted with a symmetric AES key.
 * The AES key is encrypted using an asymmetric public key:
 * the corresponding private key is required to decrypt the secret AES
 * key, and then decrypt the data.
 * <p>
 * Extensive documentation for these parameters can be found in the Java
 * Cryptography Extension Reference Guide {@link http://java.sun.com/j2se/1.5.0/docs/guide/security/jce/JCERefGuide.html}
 * 
 * 
 * @see org.ow2.proactive.authentication.crypto.KeyPairUtil
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 * 
 */
@PublicAPI
public class Credentials implements Serializable {

    private static final long serialVersionUID = 62L;

    /** Default credentials location */
    private static final String DEFAULT_CREDS = System.getProperty("user.home") + File.separator +
        ".proactive" + File.separator + "security" + File.separator + "creds.enc";

    /** Java properly describing the path to the encrypted credentials on the local drive */
    public static final String credentialsPathProperty = "pa.common.auth.credentials";

    /** Default pubkey location */
    private static final String DEFAULT_PUBKEY = System.getProperty("user.home") + File.separator +
        ".proactive" + File.separator + "security" + File.separator + "pub.key";

    /** Java property describing the path to the public key on the local drive */
    public static final String pubkeyPathProperty = "pa.common.auth.pubkey";

    static {
        File home = new File(DEFAULT_CREDS).getParentFile();
        if (!home.isDirectory()) {
            home.mkdirs();
        }
        home = new File(DEFAULT_PUBKEY).getParentFile();
        if (!home.isDirectory()) {
            home.mkdirs();
        }
    }

    /** key generation algorithm */
    private String algorithm;
    /** key size */
    private int size;
    /** cipher initialization parameters */
    private String cipher;
    /** encrypted data with AES cipher */
    private byte[] data;
    /** AES key encrypted with RSA */
    private byte[] aes;

    /**
     * Default constructor
     * <p>
     * Constructor is kept private, use {@link org.ow2.proactive.authentication.crypto.Credentials#getCredentials()} or
     * {@link org.ow2.proactive.authentication.crypto.Credentials#createCredentials(String, String)} to get intances
     *
     * @param algo Key generation algorithm
     * @param size Key size in bits
     * @param cipher Cipher parameters
     * @param aes Encrypted AES key
     * @param data raw encrypted credentials
     */
    private Credentials(String algo, int size, String cipher, byte[] aes, byte[] data) {
        this.algorithm = algo;
        this.size = size;
        this.cipher = cipher;
        this.aes = aes;
        this.data = data;
    }

    /**
     * Write the contents of a Credentials object to the disk
     * <p>
     * Use the current value of the {@link org.ow2.proactive.authentication.crypto.Credentials#credentialsPathProperty}
     * property to determine the file to which the data will be written
     * <p>
     * Credentials are written to disk in base64 encoded form.
     * <p>
     * See {@link org.ow2.proactive.authentication.crypto.Credentials#getCredentials()} for the inverse operation
     * 
     * @param path file path where the credentials will be written on the disk
     * @throws KeyException Unable to locate or open file, IO error
     */
    public void writeToDisk(String path) throws KeyException {
        File f = new File(path);
        FileOutputStream fs;
        try {
            fs = new FileOutputStream(f);
            fs.write(getBase64());
            fs.close();
        } catch (Exception e) {
            throw new KeyException("Could not write credentials to " + path, e);
        }
    }

    /**
     * Retrieves a public key stored in a local file
     * <p>
     * 
     * @param pubPath path to the public key on the local filesystem
     * @return the key encapsulated in a regular JCE container
     * @throws KeyException the key could not be retrieved or is malformed
     */
    public static PublicKey getPublicKey(String pubPath) throws KeyException {
        byte[] bytes;
        File f = new File(pubPath);
        FileInputStream fin;

        String algo = "", tmp = "";

        // recover public key bytes
        try {
            fin = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fin);
            int read, tot = 0;
            while ((read = in.read()) != '\n') {
                algo += (char) read;
                tot++;
            }
            tot++;
            while ((read = in.read()) != '\n') {
                tmp += (char) read;
                tot++;
            }
            tot++;

            bytes = new byte[(int) f.length() - tot];
            in.readFully(bytes);
            in.close();
        } catch (Exception e) {
            throw new KeyException("Could not retrieve public key from " + pubPath, e);
        }

        // reconstruct public key
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(bytes);
        PublicKey pubKey;
        KeyFactory keyFactory;

        try {
            keyFactory = KeyFactory.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyException("Cannot initialize key factory", e);
        }

        try {
            pubKey = keyFactory.generatePublic(pubKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new KeyException("Cannot re-generate public key", e);
        }

        return pubKey;
    }

    /**
     * Retrieves a private key stored in a local file
     * <p>
     * Tries to guess the algorithm used for keypair generation which
     * is not included in the file. According to {@link http://download.oracle.com/javase/1.5.0/docs/guide/security/CryptoSpec.html#AppA},
     * the algorithm can be only one of "RSA" or "DSA", so this method will try using both.
     * If the algorithm used to generate the key is neither RSA or DSA
     * (highly unlikely), this method cannot recreate the private key, but {@link #decrypt(String)}
     * maybe will.
     * 
     * @param pubPath
     *            path to the public key on the local filesystem
     * @return the key encapsulated in a regular JCE container
     * @throws KeyException
     *             the key could not be retrieved or is malformed, or the algorithm used
     *             for generation is different from the ones used by this method
     */
    public static PrivateKey getPrivateKey(String privPath) throws KeyException {
        return getPrivateKey(privPath, new String[] { "RSA", "DSA" });
    }

    /**
     * Retrieves a private key stored in a local file
     * <p>
     * Tries to guess the algorithm used for keypair generation which
     * is not included in the file. According to {@link http://download.oracle.com/javase/1.5.0/docs/guide/security/CryptoSpec.html#AppA},
     * the algorithm can be only one of "RSA" or "DSA", so we can just try both using the
     * <code>algorithms</code> param. If the algorithm used to generate the key is neither RSA or DSA
     * (highly unlikely), this method cannot recreate the private key, but {@link #decrypt(String)}
     * maybe will.
     * 
     * @param pubPath
     *            path to the public key on the local filesystem
     * @param algorithms a list of algorithms to try for creating the PK. Recommanded value:
     * 			{"RSA","DSA"}
     * @return the key encapsulated in a regular JCE container
     * @throws KeyException
     *             the key could not be retrieved or is malformed, or the algorithm used for generation
     *             is not one of <code>algorithms</code>
     */
    public static PrivateKey getPrivateKey(String privPath, String[] algorithms) throws KeyException {

        PrivateKey privKey = null;

        for (String algo : algorithms) {
            try {
                KeyFactory keyFactory;
                keyFactory = KeyFactory.getInstance(algo);

                // recover private key bytes
                byte[] bytes;
                try {
                    File pkFile = new File(privPath);
                    DataInputStream pkStream = new DataInputStream(new FileInputStream(pkFile));
                    bytes = new byte[(int) pkFile.length()];
                    pkStream.readFully(bytes);
                    pkStream.close();
                } catch (Exception e) {
                    throw new KeyException("Could not recover private key (algo=" + algo + ")", e);
                }

                // reconstruct private key
                PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(bytes);
                try {
                    privKey = keyFactory.generatePrivate(privKeySpec);
                } catch (InvalidKeySpecException e) {
                    throw new KeyException("Cannot re-generate private key  (algo=" + algo + ")", e);
                }
            } catch (Exception e) {
            }
        }

        if (privKey == null) {
            String str = "Could not generate Private Key (algorithms: ";
            for (String algo : algorithms) {
                str += algo + " ";
            }
            str += ")";
            throw new KeyException(str);
        }

        return privKey;
    }

    /**
     * Retrieves a credentials from disk
     * <p>
     * See {@link org.ow2.proactive.authentication.crypto.Credentials#writeToDisk()} for details on how information is
     * stored on disk.
     * 
     * @return the Credentials object represented by the file saved at the file
     *         described by the property {@link org.ow2.proactive.authentication.crypto.Credentials#credentialsPathProperty}
     * @throws KeyException Credentials could not be recovered
     */
    public static Credentials getCredentials() throws KeyException {
        return getCredentials(getCredentialsPath());
    }

    /**
     * Retrieves a credentials from disk
     * <p>
     * See {@link org.ow2.proactive.authentication.crypto.Credentials#writeToDisk()} for details on how information is
     * stored on disk.
     * 
     * @param path to the file in which credentials are stored
     * @return the Credentials object represented by the file located at <code>path</code>
     * @throws KeyException Credentials could not be recovered
     */
    public static Credentials getCredentials(String path) throws KeyException {
        File f = new File(path);
        byte[] bytes = new byte[(int) f.length()];
        FileInputStream fin;
        try {
            fin = new FileInputStream(f);
            fin.read(bytes);
            fin.close();
        } catch (Exception e) {
            throw new KeyException("Could not read credentials from " + path, e);
        }
        return getCredentialsBase64(bytes);
    }

    /**
     * Constructs a Credentials given an InputStream
     * 
     * @param is contains the base64 representation of a Credentials upon read
     * @return the Credentials object contained in the InputStream
     * @throws KeyException the Credentials data was read but could not be reconstructed
     * @throws IOException the Credentials data could not be read from the stream
     */
    public static Credentials getCredentials(InputStream is) throws KeyException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while (true) {
            len = is.read(buf);
            if (len > 0) {
                out.write(buf, 0, len);
            } else {
                break;
            }
        }
        byte[] bytes = out.toByteArray();
        out.close();
        return Credentials.getCredentialsBase64(bytes);
    }

    /**
     * Creates a Credentials given its base64 encoded representation
     * 
     * @param base64enc the Credentials representation as a base64 encoded byte array,
     *  as returned by {@link Credentials#getBase64()}
     * @return the Credentials object corresponding the <code>base64en</code> representation
     * @throws KeyException
     */
    public static Credentials getCredentialsBase64(byte[] base64enc) throws KeyException {
        String algo = "", cipher = "", tmp = "";
        byte[] data;
        byte[] aes;
        int size;
        byte[] asciiEnc;

        try {
            asciiEnc = Base64.decodeBase64(base64enc);
        } catch (Exception e) {
            throw new KeyException("Unable to decode base64 credentials", e);
        }

        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(asciiEnc));
            int read, tot = 0;
            while ((read = in.read()) != '\n') {
                if (read == -1)
                    throw new KeyException("Failed to parse malformed credentials");
                algo += (char) read;
                tot++;
            }
            tot++;
            while ((read = in.read()) != '\n') {
                if (read == -1)
                    throw new KeyException("Failed to parse malformed credentials");
                tmp += (char) read;
                tot++;
            }
            tot++;
            size = Integer.parseInt(tmp);
            while ((read = in.read()) != '\n') {
                if (read == -1)
                    throw new KeyException("Failed to parse malformed credentials");
                cipher += (char) read;
                tot++;
            }
            tot++;
            aes = new byte[size / 8];
            for (int i = 0; i < size / 8; i++) {
                aes[i] = (byte) in.read();
                tot++;
            }

            data = new byte[asciiEnc.length - tot];
            in.readFully(data);
        } catch (Exception e) {
            throw new KeyException("Could not decode credentials", e);
        }

        return new Credentials(algo, size, cipher, aes, data);
    }

    /**
     * Returns a representation of this credentials as a base64 encoded byte array
     * <p>
     * Prior to base64 encoding, format is the following:
     * <ul>
     * <li>The key generation algorithm, in human readable format, on a single
     * line
     * <li>The key size, in human readable format, on a single line
     * <li>The cipher parameters, in human readable format, on a single line
     * <li>The encrypted AES key, which should be exactly <code>size / 8</code> bytes
     * <li>The encrypted data, which can be of arbitrary length, should occupy the rest of the file
     * </ul>
     * @throws KeyException
     */
    public byte[] getBase64() throws KeyException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            b.write((algorithm + '\n').getBytes());
            b.write(("" + size + '\n').getBytes());
            b.write((cipher + '\n').getBytes());
            b.write(this.aes);
            b.write(this.data);
        } catch (IOException e) {

        }
        byte[] ret;
        try {
            ret = Base64.encodeBase64(b.toByteArray());
        } catch (Exception e) {
            throw new KeyException("Unable to encode credentials to base64", e);
        }
        return ret;
    }

    /**
     * 
     * @return the path to the Credentials to use in this runtime
     */
    public static String getCredentialsPath() {
        String path = System.getProperty(credentialsPathProperty);
        if (path == null) {
            path = DEFAULT_CREDS;
        }
        return path;
    }

    /**
     * 
     * @return the path to the public key to use in this runtime
     */
    public static String getPubKeyPath() {
        String path = System.getProperty(pubkeyPathProperty);
        if (path == null) {
            path = DEFAULT_PUBKEY;
        }
        return path;
    }

    /**
     * Creates new encrypted credentials
     * <p>
     * See {@link org.ow2.proactive.authentication.crypto.Credentials#createCredentials(CredData, PublicKey))
     *
     * @param cc the data to be encrypted
     * @param pubPath path to the public key
     * @return the Credentials object containing the encrypted data
     * @throws KeyException key generation or encryption failed
     */
    public static Credentials createCredentials(final CredData cc, final String pubPath) throws KeyException {
        PublicKey pubKey = getPublicKey(pubPath);
        return createCredentials(cc, pubKey);
    }

    /**
     * Creates new encrypted credentials
     * <p>
     * See {@link org.ow2.proactive.authentication.crypto.Credentials#createCredentials(CredData, PublicKey, String)
     *
     * @param cc the data to be encrypted
     * @param pubKey the public key
     * @return the Credentials object containing the encrypted data
     * @throws KeyException key generation or encryption failed
     */
    public static Credentials createCredentials(final CredData cc, final PublicKey pubKey)
            throws KeyException {
        return createCredentials(cc, pubKey, "RSA/ECB/PKCS1Padding");
    }

    /**
     * Creates new encrypted credentials
     * <p>
     * Encrypts the message '<code>credData</code>' using the
     * public key <code>pubKey</code> and <code>cipher</code>
     * and store it in a new Credentials object.
     *
     * @see KeyPairUtil#encrypt(String, String, String, byte[])
     * @param cc, the class containing the data to be crypted
     * @param pubKey public key used for encryption
     * @param cipher cipher parameters: combination of transformations
     * @return the Credentials object containing the encrypted data
     * @throws KeyException key generation or encryption failed
     */
    public static Credentials createCredentials(final CredData cc, final PublicKey pubKey, final String cipher)
            throws KeyException {
        // serialize clear credentials to byte array
        byte[] clearCred;
        try {
            clearCred = ObjectToByteConverter.ObjectStream.convert(cc);
        } catch (IOException e1) {
            throw new KeyException(e1.getMessage());
        }

        HybridEncryptionUtil.HybridEncryptedData encryptedData = HybridEncryptionUtil.encrypt(pubKey, cipher,
                clearCred);
        byte[] encAes = encryptedData.getEncryptedSymmetricKey();
        byte[] encData = encryptedData.getEncryptedData();

        int size = keySize(pubKey);
        return new Credentials(pubKey.getAlgorithm(), size, cipher, encAes, encData);
    }

    /**
     * Decrypts the encapsulated credentials
     *
     * @see org.ow2.proactive.authentication.crypto.KeyPairUtil#decrypt(String, String, String, byte[])
     * @param privPath path to the private key file
     * @return the credential data containing the clear data:login, password and key
     * @throws KeyException decryption failure, malformed data
     */
    public CredData decrypt(String privPath) throws KeyException {
        PrivateKey privKey = Credentials.getPrivateKey(privPath, new String[] { algorithm });
        return decrypt(privKey);
    }

    /**
     * Decrypts the encapsulated credentials
     *
     * @see org.ow2.proactive.authentication.crypto.KeyPairUtil#decrypt(String, String, String, byte[])
     * @param privKey the private key
     * @return the credential data containing the clear data:login, password and key
     * @throws KeyException decryption failure, malformed data
     */
    public CredData decrypt(PrivateKey privKey) throws KeyException {
        byte[] decryptedData = HybridEncryptionUtil.decrypt(privKey, this.cipher,
                new HybridEncryptionUtil.HybridEncryptedData(aes, data));

        // deserialize clear credentials and obtain login & password
        try {
            return (CredData) ByteToObjectConverter.ObjectStream.convert(decryptedData);
        } catch (Exception e) {
            throw new KeyException(e.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + algorithm + " " + size + "b " + cipher + "]";
    }

    /**
     * Creates new encrypted credentials
     * <p>
     * See {@link org.ow2.proactive.authentication.crypto.Credentials#createCredentials(String, String, String, String)
     * 
     * @param login the login to encrypt
     * @param password the corresponding password to encrypt
     * @param pubPath path to the public key
     * @return the Credentials object containing the encrypted data
     * @throws KeyException key generation or encryption failed
     */
    @Deprecated
    public static Credentials createCredentials(String login, String password, String pubPath)
            throws KeyException {
        return createCredentials(login, password, pubPath, "RSA/ECB/PKCS1Padding");
    }

    /**
     * Creates new encrypted credentials
     * <p>
     * See {@link org.ow2.proactive.authentication.crypto.Credentials#createCredentials(String, String, String, String)
     * 
     * @param login the login to encrypt
     * @param password the corresponding password to encrypt
     * @param pubKey the public key
     * @return the Credentials object containing the encrypted data
     * @throws KeyException key generation or encryption failed
     */
    @Deprecated
    public static Credentials createCredentials(String login, String password, PublicKey pubKey)
            throws KeyException {
        return createCredentials(login, password, null, pubKey, "RSA/ECB/PKCS1Padding");
    }

    /**
     * Creates new encrypted credentials
     * <p>
     * Encrypts the message '<code>login</code>:<code>password</code>' using the
     * public key at <code>pubPath</code> and <code>cipher</code>
     * and store it in a new Credentials object.
     * 
     * @see KeyPairUtil#encrypt(String, String, String, byte[])
     * @param login the login to encrypt
     * @param password the corresponding password to encrypt
     * @param pubPath path to the public key used for encryption
     * @param cipher cipher parameters: combination of transformations
     * @return the Credentials object containing the encrypted data
     * @throws KeyException key generation or encryption failed
     */
    @Deprecated
    public static Credentials createCredentials(String login, String password, String pubPath, String cipher)
            throws KeyException {
        PublicKey pubKey = getPublicKey(pubPath);
        return createCredentials(login, password, null, pubKey, cipher);
    }

    /**
     * Creates new encrypted credentials
     * <p>
     * Encrypts the message '<code>login</code>:<code>password</code>' using the
     * public key <code>pubKey</code> and <code>cipher</code>
     * and store it in a new Credentials object.
     * 
     * @see KeyPairUtil#encrypt(String, String, String, byte[])
     * @param login the login to encrypt
     * @param password the corresponding password to encrypt
     * @param pubKey public key used for encryption
     * @param cipher cipher parameters: combination of transformations
     * @return the Credentials object containing the encrypted data
     * @throws KeyException key generation or encryption failed
     */
    @Deprecated
    public static Credentials createCredentials(String login, String password, byte[] datakey,
            PublicKey pubKey, String cipher) throws KeyException {

        CredData cc = new CredData();
        cc.setLogin(CredData.parseLogin(login));
        cc.setDomain(CredData.parseDomain(login));
        cc.setPassword(password);
        cc.setKey(datakey);

        // serialize clear credentials to byte array
        byte[] clearCred;
        try {
            clearCred = ObjectToByteConverter.ObjectStream.convert(cc);
        } catch (IOException e1) {
            throw new KeyException(e1.getMessage());
        }

        int size = keySize(pubKey);

        HybridEncryptionUtil.HybridEncryptedData encryptedData = HybridEncryptionUtil.encrypt(pubKey, cipher,
                clearCred);
        byte[] encAes = encryptedData.getEncryptedSymmetricKey();
        byte[] encData = encryptedData.getEncryptedData();

        return new Credentials(pubKey.getAlgorithm(), size, cipher, encAes, encData);
    }

    private static int keySize(PublicKey pubKey) {
        int size = -1;
        if (pubKey instanceof RSAPublicKey) {
            size = ((RSAPublicKey) pubKey).getModulus().bitLength();
        } else if (pubKey instanceof DSAPublicKey) {
            size = ((DSAPublicKey) pubKey).getParams().getP().bitLength();
        } else if (pubKey instanceof DHPublicKey) {
            size = ((DHPublicKey) pubKey).getParams().getP().bitLength();
        }
        return size;
    }

}
