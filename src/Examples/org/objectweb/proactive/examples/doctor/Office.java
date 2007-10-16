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
package org.objectweb.proactive.examples.doctor;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class Office {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    //Number of patients being created at startup 
    public static final int NB_PAT = 15;

    //Number of doctors being created at startup 
    public static final int NB_DOC = 5;

    //Gaussian time parameters of patients (time being healthy)
    public static final int MEAN_PAT = 10000;
    public static final int SIGM_PAT = 3000;

    //Gaussian time parameters of doctors (cure finding duration)
    public static final int MEAN_DOC = 13000;
    public static final int SIGM_DOC = 4000;

    //Maximal number of doctors and patients (used to size queues)
    public static final int MAX_PAT = 50;
    public static final int MAX_DOC = 20;

    //State constants definition
    public static final int DOC_UNDEF = -1;
    public static final int PAT_WELL = -2;
    public static final int PAT_SICK = -1;
    java.util.Vector patients;
    java.util.Vector doctors;
    Office me;
    Receptionnist recept;
    DisplayPanel display;
    RandomTime rand;
    OfficeWindow win;

    public Office() {
    }

    public Office(Integer useLess) {
        // Creating patient and doctor vectors
        patients = new java.util.Vector();
        doctors = new java.util.Vector();

        // Creating the display window
        win = new OfficeWindow();
        win.pack();
        win.setTitle("The Salishan problems (3)");
        win.setVisible(true);

        display = win.getDisplay();
    }

    public void init(Office _me, Receptionnist _recept) {
        me = _me;
        recept = _recept;
        createPeople();
    }

    public synchronized void createPeople() {
        int i;
        try {
            rand = (RandomTime) org.objectweb.proactive.api.ProActiveObject.newActive(RandomTime.class.getName(),
                    null);

            for (i = 1; i <= NB_DOC; i++)
                addDoctor(i, MEAN_DOC, SIGM_DOC);

            for (i = 1; i <= NB_PAT; i++)
                addPatient(i, MEAN_PAT, SIGM_PAT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDoctor(int id, long meanTime, long sigmaTime) {
        try {
            Object[] params = {
                    new Integer(id), new Long(meanTime), new Long(sigmaTime), me,
                    rand
                };

            Doctor newDoc = (Doctor) org.objectweb.proactive.api.ProActiveObject.newActive(Doctor.class.getName(),
                    params);
            doctors.insertElementAt(newDoc, id - 1);
            recept.addDoctor(id);
            display.addDoctor(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPatient(int id, long meanTime, long sigmaTime) {
        try {
            Object[] params = {
                    new Integer(id), new Long(meanTime), new Long(sigmaTime), me,
                    rand
                };

            Patient newPat = (Patient) org.objectweb.proactive.api.ProActiveObject.newActive(Patient.class.getName(),
                    params);
            patients.insertElementAt(newPat, id - 1);
            display.addPatient(id);
            Thread.yield();
            newPat.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void doctorCureFound(int doctor, int patient, Cure _cure) {
        display.setPatState(patient, PAT_WELL);
        display.setDocFinished(doctor, patient);

        Patient pat = (Patient) patients.elementAt(patient - 1);
        pat.receiveCure(_cure);
        recept.addDoctor(doctor);
    }

    public synchronized void patientSick(int patient) {
        display.setPatState(patient, PAT_SICK);
        recept.addPatient(patient);
    }

    public synchronized void doctorWithPatient(int doctor, int patient) {
        display.setPatState(patient, doctor);

        Patient pat = (Patient) patients.elementAt(patient - 1);
        Doctor doc = (Doctor) doctors.elementAt(doctor - 1);

        pat.hasDoctor(doctor);
        doc.curePatient(patient);
    }

    public static void main(String[] argv) {
        logger.info("The Salishan problems : Problem 3 - The Doctor's Office");
        try {
            Office off = (Office) org.objectweb.proactive.api.ProActiveObject.newActive(Office.class.getName(),
                    new Object[] { new Integer(0) });
            Receptionnist recept = (Receptionnist) org.objectweb.proactive.api.ProActiveObject.newActive(Receptionnist.class.getName(),
                    new Object[] { off });
            off.init(off, recept);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
