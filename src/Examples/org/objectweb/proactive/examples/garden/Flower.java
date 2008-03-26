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
package org.objectweb.proactive.examples.garden;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * Simple example that creates flower objects in different nodes
 * (virtual machines) and passes remote references between them. When
 * one flower object receives a reference of another flower it
 * displays its name and the name of the remote reference.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class Flower {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
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
        logger.info("I am flower " + this.myName + " and I received a reference on flower " + f.getName());
    }

    public int bob() {
        return 90;
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();
        try {
            // It's springtime ! Let's create flowers everywhere !
            String nodeName1 = "vm1";
            String nodeName2 = "vm2";
            if (args.length >= 1) {
                nodeName1 = args[0];
            }
            if (args.length >= 2) {
                nodeName2 = args[1];
            }
            logger.info("Node 1 : " + nodeName1);
            logger.info("Node 2 : " + nodeName2);
            Flower a = (Flower) org.objectweb.proactive.api.PAActiveObject.newActive(Flower.class.getName(),
                    new Object[] { "Amaryllis - LOCAL" });
            Flower b = (Flower) org.objectweb.proactive.api.PAActiveObject.newActive(Flower.class.getName(),
                    new Object[] { "Bouton d'Or - LOCAL" });
            Flower c = (Flower) org.objectweb.proactive.api.PAActiveObject.newActive(Flower.class.getName(),
                    new Object[] { "Coquelicot - vm1" }, nodeName1);
            Flower d = (Flower) org.objectweb.proactive.api.PAActiveObject.newActive(Flower.class.getName(),
                    new Object[] { "Daliah - vm1" }, nodeName1);
            Flower e = (Flower) org.objectweb.proactive.api.PAActiveObject.newActive(Flower.class.getName(),
                    new Object[] { "Eglantine - vm2" }, nodeName2);
            Flower f = (Flower) org.objectweb.proactive.api.PAActiveObject.newActive(Flower.class.getName(),
                    new Object[] { "Rose - vm2" }, nodeName2);

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
            e.acceptReference(f);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }
}
