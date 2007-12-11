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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Abstract class implementing GeneralTask, handles setting job from a file and variable lists
 * @author fviale
 *
 */
@PublicAPI
public abstract class AbstractGeneralTask implements GeneralTask {
    String id;
    String job;
    String jobInit;
    protected ArrayList<String> listDataOut;

    public AbstractGeneralTask(String id) {
        this.id = id;
        this.listDataOut = new ArrayList<String>();
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public void setJobInit(String jobInit) {
        this.jobInit = jobInit;
    }

    public void setJob(File fileJob) throws FileNotFoundException, IOException {
        StringBuffer strBuffer = new StringBuffer();

        FileReader reader = new FileReader(fileJob);
        int c;

        while ((c = reader.read()) != -1) {
            strBuffer.append((char) c);
        }

        this.job = strBuffer.toString();

        reader.close();
    }

    public String getId() {
        return id;
    }

    public String getJobInit() {
        return jobInit;
    }

    public ArrayList<String> getListDataOut() {
        return listDataOut;
    }

    public void setListDataOut(ArrayList<String> listDataOut) {
        this.listDataOut = listDataOut;
    }

    public void addDataOut(String data) {
        this.listDataOut.add(data);
    }
}
