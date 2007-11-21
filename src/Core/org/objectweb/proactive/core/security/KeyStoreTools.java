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
package org.objectweb.proactive.core.security;

import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import org.objectweb.proactive.core.security.SecurityConstants.EntityType;


public abstract class KeyStoreTools {
    private static final String KEYSTORE_ENTITY_KEY_PATH = "entityCertificate";
    private static final String KEYSTORE_ENTITY_PATH = "entityEntry_";
    private static final String KEYSTORE_APPLICATION_KEY_PATH = "applicationKey";
    private static final String KEYSTORE_APPLICATION_PATH = "applicationCertificate_";
    private static final String KEYSTORE_USER_PATH = "userCertificate_";
    private static final String KEYSTORE_DOMAIN_PATH = "domainCertificate_";
    private static final String PRIVATE_KEY_PASSWORD = "wafti";

    public static TypedCertificate getSelfCertificate(KeyStore keystore,
        EntityType type)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        TypedCertificate cert = getCertificate(keystore,
                KEYSTORE_ENTITY_KEY_PATH);
        return new TypedCertificate(cert.getCert(), type, cert.getPrivateKey());
    }

    public static TypedCertificateList getSelfCertificateChain(
        KeyStore keystore, EntityType type)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        return getCertificateChain(keystore, getSelfCertificate(keystore, type));
    }

    public static PrivateKey getSelfPrivateKey(KeyStore keystore)
        throws UnrecoverableKeyException, KeyStoreException,
            NoSuchAlgorithmException {
        return getPrivateKey(keystore, KEYSTORE_ENTITY_KEY_PATH);
    }

    public static TypedCertificate getApplicationCertificate(KeyStore keystore)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        return getCertificate(keystore, KEYSTORE_APPLICATION_KEY_PATH);
    }

    public static TypedCertificateList getApplicationCertificateChain(
        KeyStore keystore)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        return getCertificateChain(keystore, getApplicationCertificate(keystore));
    }

    public static PrivateKey getApplicationPrivateKey(KeyStore keystore)
        throws UnrecoverableKeyException, KeyStoreException,
            NoSuchAlgorithmException {
        return getPrivateKey(keystore, KEYSTORE_APPLICATION_KEY_PATH);
    }

    public static int getApplicationLevel(KeyStore keystore)
        throws KeyStoreException {
        return keystore.getCertificateChain(KEYSTORE_APPLICATION_KEY_PATH).length;
    }

    public static TypedCertificate getCertificate(KeyStore keystore,
        String alias)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        PrivateKey pk = null;
        if (keystore.isKeyEntry(alias)) {
            pk = getPrivateKey(keystore, alias);
        }
        return new TypedCertificate((X509Certificate) keystore.getCertificate(
                alias), pathToType(alias), pk);
    }

    public static TypedCertificate getCertificate(KeyStore keystore,
        EntityType type, String name)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        if ((getApplicationCertificate(keystore).getCert() != null) &&
                getApplicationCertificate(keystore).getCert()
                        .getSubjectX500Principal().getName().equals(name)) {
            return getApplicationCertificate(keystore);
        }

        return getCertificate(keystore, typeToPath(type) + name);
    }

    private static PrivateKey getPrivateKey(KeyStore keystore, String alias)
        throws UnrecoverableKeyException, KeyStoreException,
            NoSuchAlgorithmException {
        return (PrivateKey) keystore.getKey(alias,
            PRIVATE_KEY_PASSWORD.toCharArray());
    }

    private static TypedCertificate getParentCertificate(KeyStore keystore,
        TypedCertificate certificate)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        return getCertificate(keystore, certificate.getType().getParentType(),
            certificate.getCert().getIssuerX500Principal().getName());
    }

    public static TypedCertificateList getCertificateChain(KeyStore keystore,
        TypedCertificate certificate)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        String issuer = certificate.getCert().getIssuerX500Principal().getName();
        String subject = certificate.getCert().getSubjectX500Principal()
                                    .getName();
        if (subject.equals(issuer)) {
            TypedCertificateList list = new TypedCertificateList();
            list.add(certificate);
            return list;
        }

        TypedCertificateList list = getCertificateChain(keystore,
                getParentCertificate(keystore, certificate));
        list.add(0, certificate);
        return list;
    }

    public static int getLevel(KeyStore keystore, TypedCertificate certificate)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        String issuer = certificate.getCert().getIssuerX500Principal().getName();
        String subject = certificate.getCert().getSubjectX500Principal()
                                    .getName();
        if (subject.equals(issuer)) {
            return 1;
        }
        return getLevel(keystore, getParentCertificate(keystore, certificate)) +
        1;
    }

    public static void newCertificate(KeyStore keystore,
        TypedCertificate certificate) throws KeyStoreException {
        TrustedCertificateEntry certificateEntry = new TrustedCertificateEntry(certificate.getCert());
        String alias = typeToPath(certificate.getType()) +
            certificate.getCert().getSubjectX500Principal().getName();
        if (keystore.containsAlias(alias)) {
            keystore.deleteEntry(alias);
        }

        keystore.setEntry(alias, certificateEntry, null);
    }

    public static void newPrivateKey(KeyStore keystore,
        TypedCertificate certificate)
        throws UnrecoverableKeyException, KeyStoreException,
            NoSuchAlgorithmException {
        String path = typeToPath(certificate.getType()) +
            certificate.getCert().getSubjectX500Principal().getName();
        PrivateKeyEntry keyEntry = new PrivateKeyEntry(certificate.getPrivateKey(),
                getCertificateChain(keystore, certificate).certsToArray());
        if (!keystore.containsAlias(path)) {
            ProtectionParameter pp = new PasswordProtection(PRIVATE_KEY_PASSWORD.toCharArray());
            keystore.setEntry(path, keyEntry, pp);
        }
    }

    public static void newEntity(KeyStore keystore, TypedCertificate certificate)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        PrivateKeyEntry keyEntry = new PrivateKeyEntry(certificate.getPrivateKey(),
                getCertificateChain(keystore, certificate).certsToArray());
        if (keystore.containsAlias(KEYSTORE_ENTITY_KEY_PATH)) {
            keystore.deleteEntry(KEYSTORE_ENTITY_KEY_PATH);
        }

        ProtectionParameter pp = new PasswordProtection(PRIVATE_KEY_PASSWORD.toCharArray());
        keystore.setEntry(KEYSTORE_ENTITY_KEY_PATH, keyEntry, pp);
    }

    public static void newApplicationPrivateKey(KeyStore keystore,
        TypedCertificate certificate)
        throws KeyStoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException {
        PrivateKeyEntry keyEntry = new PrivateKeyEntry(certificate.getPrivateKey(),
                getCertificateChain(keystore, certificate).certsToArray());
        if (keystore.containsAlias(KEYSTORE_ENTITY_KEY_PATH)) {
            keystore.deleteEntry(KEYSTORE_ENTITY_KEY_PATH);
        }

        ProtectionParameter pp = new PasswordProtection(PRIVATE_KEY_PASSWORD.toCharArray());
        keystore.setEntry(KEYSTORE_APPLICATION_KEY_PATH, keyEntry, pp);
    }

    public static String typeToPath(EntityType type) {
        switch (type) {
        case ENTITY:
            return KEYSTORE_ENTITY_PATH;
        case APPLICATION:
            return KEYSTORE_APPLICATION_PATH;
        case USER:
            return KEYSTORE_USER_PATH;
        case DOMAIN:
            return KEYSTORE_DOMAIN_PATH;
        default:
            return null;
        }
    }

    public static EntityType pathToType(String path) {
        if (path.contains(KEYSTORE_ENTITY_PATH) ||
                path.equals(KEYSTORE_ENTITY_KEY_PATH)) {
            return EntityType.ENTITY;
        }
        if (path.contains(KEYSTORE_APPLICATION_PATH) ||
                path.equals(KEYSTORE_APPLICATION_KEY_PATH)) {
            return EntityType.APPLICATION;
        }
        if (path.contains(KEYSTORE_USER_PATH)) {
            return EntityType.USER;
        }
        if (path.contains(KEYSTORE_DOMAIN_PATH)) {
            return EntityType.DOMAIN;
        }
        return EntityType.UNKNOWN;
    }
}
