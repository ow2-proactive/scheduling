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
package functionalTests.activeobject.acontinuation;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;


public class A implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5362621066324170354L;
    boolean isFuture = true;
    private A deleguate;
    Id id;
    Id idSent;

    public A() {
    }

    public A(String name) {
        this.id = new Id(name);
    }

    public void initFirstDeleguate() throws Exception {
        this.deleguate = (A) ProActiveObject.newActive(A.class.getName(),
                new Object[] { "deleguate1" });
        deleguate.initSecondDeleguate();
    }

    public void initSecondDeleguate() throws Exception {
        this.deleguate = (A) ProActiveObject.newActive(A.class.getName(),
                new Object[] { "deleguate2" });
    }

    public Id getId(String name) {
        if (id.getName().equals(name)) {
            return id;
        } else {
            return deleguate.getInternalId(name);
        }
    }

    public Id getInternalId(String name) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return deleguate.getId(name);
    }

    public Id getIdforFuture() {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return id;
    }

    public A getA(A a) {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return a;
    }

    public A delegatedGetA(A a) {
        return this.deleguate.getA(a);
    }

    public void forwardID(Id id) {
        if (deleguate != null) {
            deleguate.forwardID(id);
        }

        isFuture = ProFuture.isAwaited(id);
        idSent = id;
    }

    public boolean isSuccessful() {
        if (!isFuture) {
            return false;
        }

        if (deleguate != null) {
            return deleguate.isSuccessful();
        }

        return isFuture;
    }

    public String getFinalResult() {
        if (deleguate != null) {
            return deleguate.getFinalResult();
        } else {
            return idSent.getName();
        }
    }

    public String getIdName() {
        return id.getName();
    }
}
