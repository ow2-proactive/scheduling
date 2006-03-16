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
package org.objectweb.proactive.examples.components.c3d;

import org.objectweb.fractal.api.NoSuchInterfaceException;

import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.examples.c3d.Dispatcher;
import org.objectweb.proactive.examples.c3d.gui.NameAndHostDialog;

import java.io.IOException;

import java.net.UnknownHostException;

import javax.naming.NamingException;


public class NameAndHostDialogForComponent extends NameAndHostDialog {
    private static final String COMPONENT_ALIAS = "Dispatcher";

    public NameAndHostDialogForComponent(String localHost) {
        super(localHost, null); // no need to specify virtual node in the component case.
    }

    protected void tryTheLookup() {
        try {
            this.hostName = UrlBuilder.getHostNameFromUrl(hostNameTextField.getText());
            this.portNumber = UrlBuilder.getPortFromUrl(hostNameTextField.getText());

            String protocol = UrlBuilder.getProtocol(hostNameTextField.getText());

            System.out.println(protocol + " port " + portNumber + " host " +
                hostName);
            System.out.println(UrlBuilder.buildUrl(this.hostName,
                    COMPONENT_ALIAS, protocol, this.portNumber));

            ProActiveComponentRepresentative a;
            a = Fractive.lookup(UrlBuilder.buildUrl(this.hostName,
                        COMPONENT_ALIAS, protocol, this.portNumber));
            this.c3dDispatcher = (Dispatcher) a.getFcInterface(
                    "user2dispatcher");
            setVisible(false);
        } catch (UnknownHostException e) {
            treatException(e, "Sorry, host name '" + hostName + "' not found.");
        } catch (IOException e) {
            treatException(e, "Sorry, lookup failed on '" + hostName + "'.");
        } catch (NamingException e) {
            treatException(e,
                "Sorry, lookup failed on '" + hostName +
                "', no Component registered with name " + COMPONENT_ALIAS +
                ".");
        } catch (NoSuchInterfaceException e) {
            treatException(e,
                "Sorry, lookup failed on '" + hostName +
                "', component registered with name " + COMPONENT_ALIAS +
                " does not have the correct interface.");
        }
    }
}
