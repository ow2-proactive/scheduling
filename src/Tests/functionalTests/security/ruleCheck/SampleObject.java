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
package functionalTests.security.ruleCheck;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.security.exceptions.RuntimeSecurityException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;


public class SampleObject implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -6487329851736398592L;
    private String name;

    public SampleObject() {
        // mandatory empty comstructor
    }

    public SampleObject(String name) {
        this.name = name;
    }

    public SerializableString doSomething() {
        System.out.println(this.name + " is doing something.");

        return new SerializableString(this.name +
            " did something and returned this.");
    }

    public SerializableString sayhello(SampleObject target) {
        return target.doSomething();
    }

    public void makeTargetDoSomething(SampleObject target) {
        String targetString = "the target";
        try {
            targetString = ((BodyProxy) ((StubObject) target).getProxy()).getBody()
                            .getCertificate().getCert().getIssuerDN().getName();
        } catch (SecurityNotAvailableException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(this.name + " is asking " + targetString +
            " to do something.");

        PAActiveObject.setImmediateService("doSomething");
        SerializableString result = null;
        try {
            result = target.doSomething();
        } catch (RuntimeSecurityException e) {
            System.out.println("-- Security Exception " + e.getMessage());
        }
        System.out.println(this.name + " got a result from " + targetString +
            " >> " + result);
    }
}
