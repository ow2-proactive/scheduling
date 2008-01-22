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
package functionalTests.component;

import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author Matthieu Morel
 */
public class PrimitiveComponentD implements I1, BindingController {
    protected final static Logger logger = ProActiveLogger.getLogger("functionalTestss.components");
    public final static String MESSAGE = "-->d";

    //public final static Message MESSAGE = new Message("-->PrimitiveComponentD");
    public final static String I2_ITF_NAME = "i2";

    // typed collective interface
    I2 i2 = (I2) Fractive.createMulticastClientInterface(I2_ITF_NAME, I2.class.getName());

    // ref on the Group
    Group i2Group = PAGroup.getGroup(i2);

    /**
     *
     */
    public PrimitiveComponentD() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.UserBindingController#addFcBinding(java.lang.String,
     *      java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf) {
        if (clientItfName.equals(I2_ITF_NAME)) {
            i2Group.add(serverItf);
        } else if (clientItfName.startsWith(I2_ITF_NAME)) {
            // conformance to the Fractal API
            i2Group.addNamedElement(clientItfName, serverItf);
        } else {
            logger.error("Binding impossible : wrong client interface name");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see functionalTests.component.creation.Input#processInputMessage(java.lang.String)
     */
    public Message processInputMessage(Message message) {
        //		/logger.info("transferring message :" + message.toString());
        if (i2 != null) {
            //Message msg = new Message(((i2Group.processOutputMessage(message.append(MESSAGE))).append(MESSAGE)));
            Message msg = i2.processOutputMessage(message.append(MESSAGE));
            return msg.append(MESSAGE);
            //return (i2Group.processOutputMessage(message.append(MESSAGE))).append(MESSAGE);
            //return msg;
        } else {
            logger.error("cannot forward message (binding missing)");
            message.setInvalid();
            return message;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        Set itf_names = i2Group.keySet();
        return (String[]) itf_names.toArray(new String[itf_names.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals(I2_ITF_NAME)) {
            return i2;
        } else if (i2Group.containsKey(clientItfName)) {
            return i2Group.getNamedElement(clientItfName);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("cannot find " + I2_ITF_NAME + " interface");
            }
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (clientItfName.equals(I2_ITF_NAME)) {
            i2Group.clear();
            if (logger.isDebugEnabled()) {
                logger.debug(I2_ITF_NAME + " interface unbound");
            }
        } else if (clientItfName.startsWith(I2_ITF_NAME)) {
            i2Group.removeNamedElement(clientItfName);
            if (logger.isDebugEnabled()) {
                logger.debug(clientItfName + " interface unbound");
            }
        } else {
            logger.error("client interface not found");
        }
    }
}
