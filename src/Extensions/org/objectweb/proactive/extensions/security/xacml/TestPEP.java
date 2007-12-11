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
package org.objectweb.proactive.extensions.security.xacml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Subject;


public class TestPEP {
    private TestPDP pdp;

    public TestPEP(String policy) throws Exception {
        pdp = new TestPDP(policy);
    }

    /**
     * Sets up the Subject section of the request. This Request only has one
     * Subject section, and it uses the default category. To create a Subject
     * with a different category, you simply specify the category when you
     * construct the Subject object.
     *
     * @return a Set of Subject instances for inclusion in a Request
     *
     * @throws URISyntaxException
     *             if there is a problem with a URI
     */
    private static Set<Subject> setupSubjects(List<String> from)
        throws URISyntaxException {
        // bundle the attributes in a Subject with the default category
        Set<Attribute> attributes = new HashSet<Attribute>();
        for (String entity : from) {
            attributes.add(new Attribute(new URI(StringAttribute.identifier),
                    null, null, new StringAttribute(entity)));
        }
        Set<Subject> subjects = new HashSet<Subject>();
        subjects.add(new Subject(attributes));

        return subjects;
    }

    /**
     * Creates a Resource specifying the resource-id, a required attribute.
     *
     * @return a Set of Attributes for inclusion in a Request
     *
     * @throws URISyntaxException
     *             if there is a problem with a URI
     */
    private static Set<Attribute> setupResource(List<String> to)
        throws URISyntaxException {
        Set<Attribute> resource = new HashSet<Attribute>();
        for (String entity : to) {
            resource.add(new Attribute(new URI(EvaluationCtx.RESOURCE_ID),
                    null, null, new StringAttribute(entity)));
        }
        return resource;
    }

    /**
     * Creates an Action specifying the action-id, an optional attribute.
     *
     * @return a Set of Attributes for inclusion in a Request
     *
     * @throws URISyntaxException
     *             if there is a problem with a URI
     */
    private static Set<Attribute> setupAction(String actionName)
        throws URISyntaxException {
        Set<Attribute> action = new HashSet<Attribute>();

        // this is a standard URI that can optionally be used to specify
        // the action being requested
        URI actionId = new URI("urn:oasis:names:tc:xacml:1.0:action:action-id");

        // create the action
        action.add(new Attribute(actionId, null, null,
                new StringAttribute(actionName)));

        return action;
    }

    public boolean evaluate(List<String> from, List<String> to, String action)
        throws ParsingException, URISyntaxException {
        RequestCtx request = new RequestCtx(setupSubjects(from),
                setupResource(to), setupAction(action), new HashSet<Attribute>());

        // request.encode(System.out);
        // System.out.println("===");
        ResponseCtx response = pdp.evaluate(request);

        // response.encode(System.out);
        if (response.getResults().size() != 1) {
            return false;
        }

        Result result = (Result) response.getResults().toArray()[0];
        return result.getDecision() == Result.DECISION_PERMIT;
    }

    public static void main(String[] args) {
        try {
            // Create a console handler
            ConsoleHandler handler = new ConsoleHandler();

            // Add to logger
            Logger logger = Logger.getLogger(PDP.class.getName());
            logger.addHandler(handler);
            TestPEP pep = new TestPEP(
                    "/user/nhouillo/home/ws/ProActive/src/Extra/org.objectweb.proactive.extensions.security/xacml/generated.xml");
            List<String> from = new ArrayList<String>();
            from.add("TATA");
            List<String> to = new ArrayList<String>();
            to.add("TOTO");
            System.out.println(pep.evaluate(from, to, "request"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
