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
package org.objectweb.proactive.extensions.scilab.test;

import java.util.List;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.scilab.AbstractData;
import org.objectweb.proactive.extensions.scilab.GeneralResult;
import org.objectweb.proactive.extensions.scilab.SciDeployEngine;
import org.objectweb.proactive.extensions.scilab.SciEngine;
import org.objectweb.proactive.extensions.scilab.SciTask;


public class SciTest1 {
    public static void main(String[] args) throws Exception {
        //initialized dispatcher engine
        SciData m1 = new SciDoubleMatrix("a", 1, 1, new double[] { 15 });
        SciData m2 = new SciDoubleMatrix("b", 1, 1, new double[] { 23 });
        SciData m3 = new SciDoubleMatrix("x", 1, 1);

        SciTask task = new SciTask("id");
        task.addDataIn(m1);
        task.addDataIn(m2);
        task.addDataIn(m3);
        task.addDataOut("x");
        task.setJob("x = a+b;");

        // local deployment
        SciEngine engine = SciDeployEngine.deploy("ScilabEngine");
        BooleanWrapper isActivate = engine.activate();

        if (isActivate.booleanValue()) {
            System.out.println("->Scilab engine is not activate");
        }

        GeneralResult sciResult = engine.execute(task);
        List<AbstractData> listResult = sciResult.getList();

        for (AbstractData data : listResult) {
            System.out.println(data);
        }

        engine.exit();
        System.exit(0);
    }
}
