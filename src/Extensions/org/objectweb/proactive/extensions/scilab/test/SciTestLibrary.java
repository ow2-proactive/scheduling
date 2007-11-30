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

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.api.ProFileTransfer;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.scilab.AbstractGeneralTask;
import org.objectweb.proactive.extensions.scilab.GeneralResult;
import org.objectweb.proactive.extensions.scilab.MSDeployEngine;
import org.objectweb.proactive.extensions.scilab.MSEngine;
import org.objectweb.proactive.extensions.scilab.SciTask;


public class SciTestLibrary {
    private HashMap<String, MSEngine> mapEngine;

    public SciTestLibrary(String nameVN, String pathDescriptor,
        String[] arrayEngine, String localSource, String remoteDest)
        throws ProActiveException {
        //Deployment
        mapEngine = MSDeployEngine.deploy(nameVN, pathDescriptor, arrayEngine);

        //Activation
        Vector<BooleanWrapper> listStateEngine = new Vector<BooleanWrapper>();
        for (int i = 0; i < mapEngine.size(); i++) {
            listStateEngine.add((mapEngine.get(arrayEngine[i])).activate());
        }

        ProFuture.waitForAll(listStateEngine);
        //Transfer
        Object[] arrayKey = mapEngine.keySet().toArray();
        try {
            for (int i = 0; i < arrayKey.length; i++) {
                Node node = MSDeployEngine.getEngineNode((String) arrayKey[i]);
                System.out.println("Sending file to:" +
                    node.getNodeInformation().getURL());
                RemoteFile rfile = ProFileTransfer.push(new File(localSource),
                        node, new File(remoteDest));
                rfile.waitForFinishedTransfer();
            }
        } catch (Exception e) {
            System.out.println("Printing exception");
            e.printStackTrace();
        }

        //Loading
        AbstractGeneralTask sciTaskEnv;
        MSEngine sciEngine;
        for (int i = 0; i < arrayKey.length; i++) {
            sciTaskEnv = new SciTask("sciEnv" + arrayKey[i]);
            sciTaskEnv.setJob("exec('" + remoteDest + "');");
            sciEngine = this.mapEngine.get(arrayKey[i]);
            sciEngine.execute(sciTaskEnv);
        }

        //Call Function
        SciTask sciTaskCallFun;
        GeneralResult sciResult;
        SciData dataIn;
        for (int i = 0; i < arrayKey.length; i++) {
            sciTaskCallFun = new SciTask("sciCallFun" + arrayKey[i]);
            dataIn = new SciDoubleMatrix("x", 1, 1, new double[] { i });
            sciTaskCallFun.addDataIn(dataIn);
            sciTaskCallFun.addDataOut("z");
            sciTaskCallFun.setJob("z" + "= mult(" + dataIn.getName() + "," +
                dataIn.getName() + ");");
            sciEngine = this.mapEngine.get(arrayKey[i]);
            sciResult = sciEngine.execute(sciTaskCallFun);
            System.out.println(sciResult.get("z"));
        }

        for (int i = 0; i < arrayKey.length; i++) {
            sciEngine = this.mapEngine.get(arrayKey[i]);
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
