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
package org.objectweb.proactive.extensions.scilab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javasci.SciData;
import javasci.Scilab;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class represents a Scilab task
 */
public class SciTask extends AbstractGeneralTask {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_TASK);

    /**
         *
         */
    private static final long serialVersionUID = 1995967729621014754L;
    private static boolean initialized = false;
    private ArrayList<SciData> listDataIn;

    public SciTask(String id) {
        super(id);
        this.listDataIn = new ArrayList<SciData>();
        this.listDataOut = new ArrayList<String>();
    }

    public ArrayList<SciData> getListDataIn() {
        return listDataIn;
    }

    public void setListDataIn(ArrayList<SciData> listDataIn) {
        this.listDataIn = listDataIn;
    }

    public void addDataIn(SciData data) {
        this.listDataIn.add(data);
    }

    public void sendListDataIn() {
        SciData data;
        for (int i = 0; i < listDataIn.size(); i++) {
            data = listDataIn.get(i);
            Scilab.sendData(data);
        }
    }

    public void clearWorkspace() {
        Scilab.Exec("clearglobal();");
        for (int i = 0; i < listDataOut.size(); i++) {
            Scilab.Exec("clear " + listDataOut.get(i) + ";");
        }
    }

    public List<AbstractData> receiveDataOut() throws ScilabException {
        ArrayList<AbstractData> results = new ArrayList<AbstractData>();
        for (int i = 0; i < listDataOut.size(); i++) {
            if (Scilab.ExistVar(listDataOut.get(i))) {
                SciData data = Scilab.receiveDataByName(listDataOut.get(i));
                results.add(new AbstractData(data));
            } else {
                throw new ScilabException("Variable " + listDataOut.get(i) +
                    " cannot be found in the environment");
            }
        }

        return results;
    }

    public void init() {
        if (!initialized) {
            Scilab.init();
        }
    }

    public boolean execute() throws TaskException {
        String fulljob = jobInit + "\n" + job;
        BufferedWriter out;
        File temp;
        boolean isValid;
        try {
            temp = File.createTempFile("scilab", ".sce");
            temp.deleteOnExit();
            out = new BufferedWriter(new FileWriter(temp));
            out.write(fulljob);
            out.close();
        } catch (IOException e) {
            throw new TaskException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("->SciTask:execute:" + temp.getAbsolutePath());
        }

        isValid = Scilab.Exec("exec(''" + temp.getAbsolutePath() + "'');");

        return isValid;
    }

    public String getLastMessage() {
        // TODO not properly implemented
        return "Scilab error code : " + Scilab.GetLastErrorCode();
    }
}
