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

import java.io.Serializable;


/*
import javasci.SciData;
import javasci.SciDoubleMatrix;

import org.objectweb.proactive.extensions.calcium.Calcium;
import org.objectweb.proactive.extensions.calcium.ResourceManager;
import org.objectweb.proactive.extensions.calcium.Stream;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.exceptions.PanicException;
import org.objectweb.proactive.extensions.calcium.futures.Future;
import org.objectweb.proactive.extensions.calcium.interfaces.Execute;
import org.objectweb.proactive.extensions.calcium.interfaces.Skeleton;
import org.objectweb.proactive.extensions.calcium.proactive.ProActiveManager;
import org.objectweb.proactive.extensions.calcium.skeletons.Farm;
import org.objectweb.proactive.extensions.calcium.skeletons.Seq;
import org.objectweb.proactive.extensions.calcium.statistics.StatsGlobal;
import org.objectweb.proactive.extensions.scilab.SciEngineWorker;
import org.objectweb.proactive.extensions.scilab.SciResult;
import org.objectweb.proactive.extensions.scilab.SciTask;
*/
public class SciTestCalcium implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4818541143376680296L;

    /*
            private Skeleton<SciTask> root;
            private String descriptorPath;
            private String nameVN;

            private class SciExecute implements Execute<SciTask>{
                    public SciTask execute(SciTask sciTask) {
                            SciResult sciResult = SciEngineWorker.executeTask(sciTask);
                            sciTask.setListDataOut(sciResult.getList());
                            return sciTask;
                    }
            }

            public SciTestCalcium(String nameVN, String descriptorPath){
                    this.nameVN = nameVN;
                    this.descriptorPath = descriptorPath;
            }

            public void solve() throws Exception{
                    ResourceManager manager= new ProActiveManager(descriptorPath, nameVN);
                    //ResourceManager manager= new MultiThreadedManager(1);

                    Calcium calcium = new Calcium(manager);
                    this.root = new Farm<SciTask>(new Seq<SciTask>(new SciExecute()));
                    Stream<SciTask> stream = calcium.newStream(root);


                    SciTask sciTask;
                    Vector<Future<SciTask>> futures= new Vector<Future<SciTask>>(10);
                    for(int i=0; i<10; i++){
                            sciTask = new SciTask("ScilabTask" + i);
                            SciDoubleMatrix sciMatrix = new SciDoubleMatrix("M", 1, 1, new double[]{i});

                            sciTask.addDataIn(sciMatrix);
                            sciTask.addDataOut(sciMatrix);
                            sciTask.setJob(sciMatrix.getName() + "=" +  sciMatrix.getName() + "* 2;");
                            futures.add(stream.input(sciTask));
                    }

                    calcium.boot();

                    try {
                            for(Future<SciTask> future:futures){
                                    SciTask res = future.get();
                                    for(int i=0; i< res.getListDataOut().size(); i++){
                                            SciData sciData = (SciData) res.getListDataOut().get(i);
                                            System.out.println(sciData.toString());
                                    }
                            }
                    } catch (Exception e) {
                            e.printStackTrace();
                    }
                    calcium.shutdown();
                    StatsGlobal stats = calcium.getStatsGlobal();
                    System.out.println(stats);
            }

            public static void main(String[] args) throws Exception{
                    if(args.length !=2){
                            System.out.println("Invalid number of parameter : " + args.length);
                            return;
                    }

                    SciTestCalcium st = new SciTestCalcium(args[0], args[1]);
                    st.solve();
            }

    */
}
