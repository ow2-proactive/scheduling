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
package org.objectweb.proactive.ic2d.security.core;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javassist.NotFoundException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.TypedCertificate;


public class CertificateTree implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7966227118791659019L;
    private static KeyPairGenerator keygen;
    private final List<CertificateTree> children;
    private CertificateTree parent;
    private TypedCertificate certificate;

    protected CertificateTree(TypedCertificate certificate) {
        this.children = new ArrayList<CertificateTree>();
        this.certificate = certificate;
        this.parent = null;
    }

    public CertificateTree(String name, int keySize, int validity,
        EntityType type) {
        this(genCert(name, keySize, validity, type));
    }

    private static TypedCertificate genCert(String name, int keySize,
        int validity, EntityType type) {
        if (keygen == null) {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            try {
                keygen = KeyPairGenerator.getInstance("RSA",
                        BouncyCastleProvider.PROVIDER_NAME);
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            } catch (NoSuchProviderException e1) {
                e1.printStackTrace();
            }
        }

        keygen.initialize(keySize);

        KeyPair kp = keygen.genKeyPair();

        try {
            X509Certificate cert = CertTools.genSelfCert(name, validity, null,
                    kp.getPrivate(), kp.getPublic(), true);
            return new TypedCertificate(cert, type, kp.getPrivate());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CertificateTree> getChildren() {
        return this.children;
    }

    public List<TypedCertificate> getCertChain() {
        List<TypedCertificate> chain;
        if (this.parent != null) {
            chain = this.parent.getCertChain();
        } else {
            chain = new ArrayList<TypedCertificate>();
        }
        chain.add(0, this.certificate);
        return chain;
    }

    private void setParent(CertificateTree parent) {
        this.parent = parent;
    }

    public TypedCertificate getCertificate() {
        return this.certificate;
    }

    private CertificateTree getChild(TypedCertificate cert) {
        for (CertificateTree child : this.children) {
            if (child.getCertificate().equals(cert)) {
                return child;
            }
        }
        return null;
    }

    public void add(CertificateTree newChild) {
        if (newChild == null) {
            return;
        }

        CertificateTree existingChild = getChild(newChild.getCertificate());
        if (existingChild == null) {
            this.children.add(newChild);
            newChild.setParent(this);
        } else {
            existingChild.merge(newChild);
        }
    }

    public boolean merge(CertificateTree tree) {
        if ((tree == null) ||
                !tree.getCertificate().equals(this.getCertificate())) {
            return false;
        }

        if ((this.certificate.getPrivateKey() == null) &&
                (tree.getCertificate().getPrivateKey() != null)) {
            this.certificate = tree.getCertificate();
        }
        for (CertificateTree newChild : tree.getChildren()) {
            add(newChild);
        }
        return true;
    }

    public void add(String name, int keySize, int validity, EntityType type) {
        keygen.initialize(keySize);
        KeyPair childKP = keygen.genKeyPair();

        X509Certificate parentCert = this.certificate.getCert();
        PublicKey parentPublicKey = parentCert.getPublicKey();
        PrivateKey parentPrivateKey = this.certificate.getPrivateKey();
        String parentName = parentCert.getSubjectX500Principal().getName();

        try {
            X509Certificate cert = CertTools.genCert(name, validity, null,
                    childKP.getPublic(), true, parentName, parentPrivateKey,
                    parentPublicKey);
            CertificateTree newChild = new CertificateTree(new TypedCertificate(
                        cert, type, childKP.getPrivate()));
            newChild.setParent(this);
            add(newChild);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public TypedCertificate search(String name, EntityType type)
        throws NotFoundException {
        if ((type == this.certificate.getType()) &&
                this.certificate.getCert().getSubjectX500Principal().getName()
                                    .equals(name)) {
            return this.certificate;
        }

        for (CertificateTree child : this.children) {
            try {
                return child.search(name, type);
            } catch (NotFoundException nfe) {
                // let's check the other children
            }
        }

        throw new NotFoundException("Certificate " + name + " : " + type +
            " not found.");
    }

    public boolean remove() {
        if (this.parent == null) {
            return false;
        }

        return this.parent.removeChild(this);
    }

    public boolean removeChild(CertificateTree child) {
        return this.children.remove(child);
    }

    public String getName() {
        String result = this.certificate.getType().toString();
        result += ":";
        result += this.certificate.getCert().getSubjectX500Principal().getName();
        return result;
    }

    public CertificateTree getRoot() {
        if (this.parent == null) {
            return this;
        }
        return this.parent.getRoot();
    }

    public static CertificateTree newTree(
        List<TypedCertificate> certificateChain) {
        CertificateTree parentNode = null;
        CertificateTree childNode = null;
        for (TypedCertificate certificate : certificateChain) {
            parentNode = new CertificateTree(certificate);
            parentNode.add(childNode);

            childNode = parentNode;
        }
        CertificateTree thisNode = childNode;
        while (!thisNode.getChildren().isEmpty()) {
            thisNode = thisNode.getChildren().get(0);
        }

        return thisNode;
    }

    @Override
    public String toString() {
        return this.certificate.toString();
    }
}
