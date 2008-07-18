package org.ow2.proactive.scheduler.ext.scilab.embedded;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.security.auth.login.LoginException;

import javasci.SciComplexMatrix;
import javasci.SciData;
import javasci.SciDoubleMatrix;
import javasci.SciStringMatrix;
import javasci.Scilab;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.*;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobPriority;


public class ScilabSolver {

    private static AOScilabEnvironment scilabSolver;
    //private static native void initIDs();

    static {
        scilabSolver = null;
        //	System.loadLibrary("ScilabEmbedded");
        //	initIDs();
    }

    public static void solve(String[] inputScripts, String[] mainScripts, String scriptURL, int priority) {
        System.out.println("[ScilabSolver] In Solver");
        ArrayList<SciData> results = null;
        results = scilabSolver.solve(inputScripts, mainScripts, null, JobPriority.findPriority(priority));

        if (results != null) {
            System.out.println(results);
            System.out.println("[ScilabSolver] Solved");

            /*  send result to scilab through javasci interface
            	first check instance type of result and then select
            	appropriate function.
            	
            	Do this for all the results.
             */

            for (Iterator iterator = results.iterator(); iterator.hasNext();) {
                SciData res = (SciData) iterator.next();

                if (res instanceof SciDoubleMatrix) {
                    SciDoubleMatrix sciDMat = (SciDoubleMatrix) res;
                    Scilab.sendDoubleMatrix(sciDMat);
                } else if (res instanceof SciStringMatrix) {
                    SciStringMatrix sciSMat = (SciStringMatrix) res;
                    Scilab.sendStringMatrix(sciSMat);
                }

                else if (res instanceof SciComplexMatrix) {
                    SciComplexMatrix sciCMat = (SciComplexMatrix) res;
                    Scilab.sendComplexMatrix(sciCMat);
                }
            }

            //return results.toArray(new SciData[] {});
        } else {
            System.out.println("[ScilabSolver]Solve returned NULL...");
            //return null;
        }
    }

    public static void createConnection() {
        if (scilabSolver != null)
            return;

        System.out.println("[ScilabSolver] In create Connection");
        try {
            scilabSolver = (AOScilabEnvironment) PAActiveObject.newActive(
                    "org.ow2.proactive.scheduler.ext.scilab.embedded.AOScilabEnvironment", new Object[] {});
        } catch (ActiveObjectCreationException e) {
            System.out.println("[ScilabSolver] Error Creating AOScilabEnvironment AO..");
            e.printStackTrace();
        } catch (NodeException e) {
            System.out.println("[ScilabSolver] Error Connecting to Scheduler..");
            e.printStackTrace();
        }

        scilabSolver.join("//localhost");
        try {
            scilabSolver.login("jl", "jl");
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("[ScilabSolver] leaving create Connection");

    }
}
