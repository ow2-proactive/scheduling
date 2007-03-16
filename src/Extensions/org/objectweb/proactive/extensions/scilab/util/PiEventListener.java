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
package org.objectweb.proactive.extensions.scilab.util;

import javasci.SciDoubleMatrix;

import org.objectweb.proactive.extensions.scilab.SciResult;
import org.objectweb.proactive.extensions.scilab.monitor.SciEvent;
import org.objectweb.proactive.extensions.scilab.monitor.SciEventListener;
import org.objectweb.proactive.extensions.scilab.monitor.SciTaskInfo;
import org.objectweb.proactive.extensions.scilab.monitor.ScilabService;


/**
 * This class is a listener for Pi calculation events in order to retrieve the result of the parallel computation
 */
public class PiEventListener implements SciEventListener {
    private FutureDoubleMatrix res;
    private int nbBloc;
    private int count;
    private double pi;
    private ScilabService service;

    public PiEventListener(ScilabService service, int nbBloc,
        FutureDoubleMatrix res) {
        this.service = service;
        this.res = res;
        this.nbBloc = nbBloc;
    }

    public void actionPerformed(SciEvent evt) {
        SciTaskInfo sciTaskInfo = (SciTaskInfo) evt.getSource();

        if (sciTaskInfo.getState() != SciTaskInfo.SUCCEEDED) {

            /*if(sciTaskInfo.getState() == SciTaskInfo.ABORT){
                    System.out.println("***************** Task:" + sciTaskInfo.getIdTask() + " ABORT ********************");
            }*/
            return;
        }

        System.out.println("IDTASK: " + sciTaskInfo.getIdTask() + " IDRES: " +
            res.getName());
        if (!sciTaskInfo.getIdTask().startsWith(res.getName())) {
            return;
        }

        service.removeTask(sciTaskInfo.getIdTask());

        System.out.println("---------------- Task:" + sciTaskInfo.getIdTask() +
            " " + sciTaskInfo.getIdEngine() + " " +
            service.getMapTaskRun().size() + " SUCCESS -----------------");

        SciResult sciResult = sciTaskInfo.getSciResult();

        //System.out.println(sciTaskInfo.getTimeGlobal() +" " + sciResult.getTimeExecution());
        if (!sciResult.getId().startsWith(res.getName())) {
            return;
        }

        SciDoubleMatrix sciData = (SciDoubleMatrix) sciResult.getList().get(0);
        pi += sciData.getData()[0];

        count++;

        //System.out.println("COUNT = " + count + "  NBBLOC = " + nbBloc);
        if (count == nbBloc) {
            res.set(new double[] { pi });
            service.removeEventListenerEngine(this);
        }
    }
}
