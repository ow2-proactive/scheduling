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
package org.objectweb.proactive.ext.security.test;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ext.security.Secure;
import org.objectweb.proactive.ext.security.SecurityContext;


/**
 * <p>
 * Simple example that creates flower objects in different nodes
 * (virtual machines) and passes remote references between them. When
 * one flower object receives a reference of another flower it
 * displays its name and the name of the remote reference.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class Flower implements Serializable, Secure {
    static Logger logger = Logger.getLogger(Flower.class.getName());
    private String myName;

    public Flower() {
        super();
    }

    public Flower(String name) {
        this.myName = name;
        logger.info("I am flower " + this.myName + " just been created");
    }

    public String getName() {
        return this.myName;
    }

    public void acceptReference(Flower f) {
        logger.info("I am flower " + this.myName +
            " and I received a reference on flower " + f.getName());
    }

    public int bob() {
        return 90;
    }

    public void migrateTo(Node node) throws MigrationException {
        ProActive.migrateTo(node);
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();

        try {
            ProActiveDescriptor proActiveDescriptor = ProActive.getProactiveDescriptor(
                    "file:" + args[0]);

            // It's springtime ! Let's create flowers everywhere !
            String nodeName1 = "vm1";
            String nodeName2 = "vm2";

            proActiveDescriptor.activateMappings();

            logger.info("Node 1 : " + nodeName1);
            logger.info("Node 2 : " + nodeName2);
            Flower a = (Flower) org.objectweb.proactive.ProActive.newActive(Flower.class.getName(),
                    new Object[] { "Amaryllis - LOCAL" },
                    proActiveDescriptor.getVirtualNode("Locale").getNode());

            //Thread.sleep(1000);
            Flower b = (Flower) org.objectweb.proactive.ProActive.newActive(Flower.class.getName(),
                    new Object[] { "Bouton d'Or - LOCAL" },
                    proActiveDescriptor.getVirtualNode("Locale").getNode());

            // Thread.sleep(1000);
            Flower c = (Flower) org.objectweb.proactive.ProActive.newActive(Flower.class.getName(),
                    new Object[] { "Coquelicot - vm1" },
                    proActiveDescriptor.getVirtualNode(nodeName1).getNode());

            //Thread.sleep(1000);
            Flower d = (Flower) org.objectweb.proactive.ProActive.newActive(Flower.class.getName(),
                    new Object[] { "Daliah - vm1" },
                    proActiveDescriptor.getVirtualNode(nodeName1).getNode());

            //Thread.sleep(1000);
            Flower e = (Flower) org.objectweb.proactive.ProActive.newActive(Flower.class.getName(),
                    new Object[] { "Eglantine - vm2" },
                    proActiveDescriptor.getVirtualNode(nodeName2).getNode());

            //Thread.sleep(1000);
            Flower f = (Flower) org.objectweb.proactive.ProActive.newActive(Flower.class.getName(),
                    new Object[] { "Rose - vm2" },
                    proActiveDescriptor.getVirtualNode(nodeName2).getNode());

            // Now let's test all setups
            // It is understood that all flowers are active objects
            // Pass a local Flower to a local Flower
            a.acceptReference(b);

            // Pass a local Flower to a remote Flower
            d.acceptReference(b);

            // Pass a remote Flower to a local Flower
            a.acceptReference(c);

            // Pass a remote Flower to a remote Flower
            e.acceptReference(c);

            // Special case : pass a remote Flower to a remote Flower
            // When both flowers actually sit on the same host, and this
            // host is different from the local host
            System.out.println(
                "---------------------------- Accept Reference -----------------------");
            e.acceptReference(f);

            System.out.println(
                "---------------------------- Migration ------------------------------");
            c.migrateTo(proActiveDescriptor.getVirtualNode(nodeName2).getNode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.ext.security.Secure#receiveRequest(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext receiveRequest(SecurityContext sc) {
        System.out.println("received a secure request from : " +
            ((X509Certificate) sc.getEntitiesFrom().get(0)).getSubjectDN());

        return sc;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.ext.security.Secure#execute(org.objectweb.proactive.ext.security.SecurityContext)
     */
    public SecurityContext execute(SecurityContext sc) {
        return null;
    }
}
