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
package nonregressiontest.component;

import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class PrimitiveComponentE implements I1, BindingController {
    protected final static Logger logger = ProActiveLogger.getLogger(
            "nonregressiontests.components");
    I2List multicastServerItf = null;

    public void bindFc(String clientItfName, Object serverItf)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        if ("i2Multicast".equals(clientItfName) &&
                (serverItf instanceof I2List)) {
            multicastServerItf = (I2List) serverItf;
        } else {
            logger.error(
                "Binding impossible : wrong client interface name (expected i2Multicast)");
        }
    }

    public String[] listFc() {
        return new String[] { "i2Multicast" };
    }

    public Object lookupFc(String clientItfName)
        throws NoSuchInterfaceException {
        if ("i2Multicast".equals(clientItfName)) {
            return multicastServerItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String clientItfName)
        throws NoSuchInterfaceException, IllegalBindingException, 
            IllegalLifeCycleException {
        if ("i2Multicast".equals(clientItfName)) {
            multicastServerItf = null;
        }
    }

    public Message processInputMessage(Message message) {
        if (multicastServerItf != null) {
            List<Message> msgs = multicastServerItf.processOutputMessage(message.append(
                        MESSAGE));

            Message result = new Message();

            for (Message msg : msgs) {
                result.append(msg);
            }

            return result.append(MESSAGE);
        } else {
            logger.error("cannot forward message (binding missing)");
            message.setInvalid();
            return message;
        }
    }
}
