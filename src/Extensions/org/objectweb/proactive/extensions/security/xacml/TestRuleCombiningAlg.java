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
import java.util.Iterator;
import java.util.List;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.combine.RuleCombiningAlgorithm;
import com.sun.xacml.ctx.Result;


public class TestRuleCombiningAlg extends RuleCombiningAlgorithm {
    public TestRuleCombiningAlg() throws URISyntaxException {
        super(new URI("rule-combining-alg:most-specific"));
    }

    @Override
    public Result combine(EvaluationCtx context, List rules) {
        Iterator<Rule> it = rules.iterator();

        while (it.hasNext()) {
            // get the next Rule, and evaluate it
            Rule rule = (it.next());
            Result result = rule.evaluate(context);

            // if it returns Permit, then the alg returns Permit
            if (result.getDecision() == Result.DECISION_PERMIT) {
                return result;
            }
        }

        // if nothing returned Permit, then the alg returns Deny
        return new Result(Result.DECISION_DENY);
    }
}
