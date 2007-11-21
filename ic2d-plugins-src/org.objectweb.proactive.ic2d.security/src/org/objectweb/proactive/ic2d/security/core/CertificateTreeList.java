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

import java.util.ArrayList;
import java.util.Collection;

import javassist.NotFoundException;

import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.TypedCertificate;


public class CertificateTreeList extends ArrayList<CertificateTree> {

    /**
     *
     */
    private static final long serialVersionUID = 8097072392302851020L;

    public CertificateTreeList() {
        super();
    }

    public CertificateTreeList(CertificateTreeList list) {
        super();
        addAll(list);
    }

    public TypedCertificate search(String name, EntityType type)
        throws NotFoundException {
        for (CertificateTree ct : this) {
            try {
                return ct.search(name, type);
            } catch (NotFoundException nfe) {
                // let's check the other trees
            }
        }

        throw new NotFoundException("Certificate " + name + " : " + type +
            " not found.");
    }

    @Override
    public boolean add(CertificateTree newTree) {
        for (CertificateTree tree : this) {
            if (tree.merge(newTree)) {
                return true;
            }
        }

        return super.add(newTree);
    }

    @Override
    public boolean addAll(Collection<?extends CertificateTree> c) {
        for (CertificateTree tree : c) {
            add(tree);
        }
        return true;
    }

    public boolean remove(CertificateTree tree) {
        if (!tree.remove()) {
            return super.remove(tree);
        }
        return true;
    }
}
