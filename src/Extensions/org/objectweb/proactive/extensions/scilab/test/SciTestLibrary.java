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
package org.objectweb.proactive.ext.scilab.test;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.ext.scilab.SciDeployEngine;
import org.objectweb.proactive.ext.scilab.SciEngine;
import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.SciTask;
import org.objectweb.proactive.filetransfer.FileTransfer;
import org.objectweb.proactive.filetransfer.FileVector;


public class SciTestLibrary {
    private HashMap mapEngine;

    public SciTestLibrary(String nameVN, String pathDescriptor,
        String[] arrayEngine, String localSource, String remoteDest)
        throws ProActiveException {
        //Deployment
        mapEngine = SciDeployEngine.deploy(nameVN, pathDescriptor, arrayEngine);

        //Activation
        Vector<BooleanWrapper> listStateEngine = new Vector<BooleanWrapper>();
        for (int i = 0; i < mapEngine.size(); i++) {
            listStateEngine.add(((SciEngine) mapEngine.get(arrayEngine[i])).activate());
        }

        ProActive.waitForAll(listStateEngine);
        //Transfer
        Object[] arrayKey = mapEngine.keySet().toArray();
        try {
            FileVector fv;
            for (int i = 0; i < arrayKey.length; i++) {
                Node node = SciDeployEngine.getEngineNode((String) arrayKey[i]);
                System.out.println("Sending file to:" +
                    node.getNodeInformation().getURL());
                fv = FileTransfer.pushFile(node, new File(localSource),
                        new File(remoteDest));
                fv.waitForAll();
            }
        } catch (Exception e) {
            System.out.println("Printing exception");
            e.printStackTrace();
        }

        //Loading
        SciTask sciTaskEnv;
        SciEngine sciEngine;
        for (int i = 0; i < arrayKey.length; i++) {
            sciTaskEnv = new SciTask("sciEnv" + arrayKey[i]);
            sciTaskEnv.setJob("exec('" + remoteDest + "');");
            sciEngine = (SciEngine) this.mapEngine.get(arrayKey[i]);
            sciEngine.execute(sciTaskEnv);
        }

        //Call Function
        SciTask sciTaskCallFun;
        SciResult sciResult;
        SciData dataIn;
        SciData dataOut;
        for (int i = 0; i < arrayKey.length; i++) {
            sciTaskCallFun = new SciTask("sciCallFun" + arrayKey[i]);
            dataIn = new SciDoubleMatrix("x", 1, 1, new double[] { i });
            dataOut = new SciData("z");
            sciTaskCallFun.addDataIn(dataIn);
            sciTaskCallFun.addDataOut(dataOut);
            sciTaskCallFun.setJob(dataOut.getName() + "= mult(" +
                dataIn.getName() + "," + dataIn.getName() + ");");
            sciEngine = (SciEngine) this.mapEngine.get(arrayKey[i]);
            sciResult = sciEngine.execute(sciTaskCallFun);
            System.out.println(sciResult.get(dataOut.getName()));
        }

        for (int i = 0; i < arrayKey.length; i++) {
            sciEngine = (SciEngine) this.mapEngine.get(arrayKey[i]);
            try {
                sciEngine.exit();
            } catch (RuntimeException e) {
            }
        }
        System.exit(0);
    }

    public static void main(String[] args) throws ProActiveException {
        if (args.length != 5) {
            System.out.println("Invalid number of parameter : " + args.length);
            return;
        }

        int nbEngine = Integer.parseInt(args[2]);
        String[] arrayId = new String[nbEngine];

        for (int i = 0; i < nbEngine; i++) {
            arrayId[i] = "Scilab" + i;
        }

        new SciTestLibrary(args[0], args[1], arrayId, args[3], args[4]);
    }
}
