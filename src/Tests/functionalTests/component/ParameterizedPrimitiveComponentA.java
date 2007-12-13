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

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author Matthieu Morel
 */
public class ParameterizedPrimitiveComponentA extends PrimitiveComponentA implements AttributeController {
    public String message;
    public final static String I2_ITF_NAME = "i2";
    private static Logger logger = ProActiveLogger.getLogger("functionalTests");
    I2 i2;

    /**
     *
     */
    public ParameterizedPrimitiveComponentA() {
    }

    // attribute for use by AttributeController 
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public Message processInputMessage(Message message) {
        //      /logger.info("transferring message :" + message.toString());
        if (i2 != null) {
            return (i2.processOutputMessage(message.append(message))).append(message);
        } else {
            logger.error("cannot forward message (binding missing)");
            message.setInvalid();
            return message;
        }
    }
}
