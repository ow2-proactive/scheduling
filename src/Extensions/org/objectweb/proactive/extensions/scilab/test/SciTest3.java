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

import java.util.HashMap;
import java.util.Vector;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.scilab.GeneralResult;
import org.objectweb.proactive.extensions.scilab.SciDeployEngine;
import org.objectweb.proactive.extensions.scilab.SciEngine;
import org.objectweb.proactive.extensions.scilab.SciTask;


public class SciTest3 {
    private String[] arrayEngine;
    private HashMap<String, SciEngine> mapEngine;
    private int countEngine = 0;

    public SciTest3(String nameVN, String pathDescriptor, String[] arrayEngine) {
        Vector<BooleanWrapper> listStateEngine = new Vector<BooleanWrapper>();
        this.arrayEngine = arrayEngine;
        SciData m1 = new SciDoubleMatrix("a", 1, 1, new double[] { 10 });
        SciData m2 = new SciDoubleMatrix("b", 1, 1, new double[] { 20 });

        //Deployment
        mapEngine = SciDeployEngine.deploy(nameVN, pathDescriptor, arrayEngine);

        //Activation
        for (int i = 0; i < mapEngine.size(); i++) {
            listStateEngine.add((mapEngine.get(arrayEngine[i])).activate());
        }

        ProFuture.waitForAll(listStateEngine);

        for (int i = 0; i < mapEngine.size(); i++) {
            if (!listStateEngine.get(i).booleanValue()) {
                System.out.println("->Activation Error");
                return;
            }
        }

        //Computation
        SciEngine sciEngine;

        SciTask task1 = new SciTask("id1");
        task1.addDataIn(m1);
        task1.addDataIn(m2);
        task1.addDataOut("x");
        task1.setJob("x = a+b;");

        sciEngine = mapEngine.get(this.getNextEngine());

        //asynchronous call
        GeneralResult result1 = sciEngine.execute(task1);

        SciTask task3 = new SciTask("id3");
        task3.addDataIn(m1);
        task3.addDataOut("a");
        task3.setJob("a = a*2;");

        sciEngine = mapEngine.get(this.getNextEngine());
        //asynchronous call
        GeneralResult result3 = sciEngine.execute(task3);

        SciTask task2 = new SciTask("id2");

        //wait value
        task2.addDataIn((SciData) result1.get("x").getData());
        task2.addDataIn(m2);
        task2.addDataOut("y");
        task2.setJob("y = x+b;");

        sciEngine = mapEngine.get(this.getNextEngine());
        //asynchronous call
        GeneralResult result2 = sciEngine.execute(task2);

        System.out.println(result1.get("x").toString());
        System.out.println(result3.get("a").toString());
        System.out.println(result2.get("y").toString());

        //SciDeployEngine.killAll();
        System.exit(0);
    }

    //get id of the nex engine 
    public String getNextEngine() {
        countEngine++;
        if (countEngine == mapEngine.size()) {
            countEngine = 0;
        }

        return arrayEngine[countEngine];
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Invalid number of parameter : " + args.length);
            return;
        }

        int nbEngine = Integer.parseInt(args[2]);
        String[] arrayId = new String[nbEngine];

        for (int i = 0; i < nbEngine; i++) {
            arrayId[i] = "Scilab" + i;
        }

        new SciTest3(args[0], args[1], arrayId);
    }
}
