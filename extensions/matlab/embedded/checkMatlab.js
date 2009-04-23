/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */

importPackage(java.lang);
importPackage(org.ow2.proactive.scheduler.ext.common.util);

// This script is run on remote computing hosts
// It will test that Matlab is installed on the machine
// and it will try to aquire a licence coin for each job's used toolboxes
// Any failure will select the resource as not available for the scheduled job
windows = false;
toolboxmap = java.util.HashMap();

// *****************************************************************************************
// This sections contains the matlab code used to trigger Matlab flexlm licencing mechanism
// When this script is executed, it will try to acquire from the licence server a licence coin
// for each job's used toolboxes. It contains only references to popular toolboxes,
// For any toolbox not present in this list, a key/value pair must be added
// The key is the toolbox directory name in MATLAB_ROOT/toolbox
// The value is the matlab code to be executed in order to trigger a licence 
// *****************************************************************************************
// Standard Matlab
toolboxmap.put("matlab", "a=1;");
// Simulink
toolboxmap.put("simulink", "opts = simset(\'MaxDataPoints\', 100, \'Refine\', 2);[txtRpt, sRpt] = sldiagnostics(\'vdp\');");
// Control System
toolboxmap.put("control", "s = tf(\'s\');");
// Fixed Point
// The fixed point toolbox has a very weird licencing scheme, it takes no toolbox licence
// if the logging mode is not activated, here the command simulates that a licence is being taken
// If you want to use fixed point without the logging, replace the matlab command below with a dummy commmand like "a=1;" 
toolboxmap.put("fixedpoint", "pref = fipref(\'LoggingMode\',\'on\');a = fi;");
// Image Processing
toolboxmap.put("images", "s = imcomplement(uint8([ 255 10 75; 44 225 100]));");
// Neural Networks
toolboxmap.put("nnet", "net = newlin([-1 1],1,[0 1],0.01);");
// Optimization
toolboxmap.put("optim", "[x,fval] = fminunc(@(x) 3*x(1)^2 + 2*x(1)*x(2) + x(2),[1,1]);");
// PDE
toolboxmap.put("pde", "[p,e,t]=initmesh(\'lshapeg\');");
// Robust Control
toolboxmap.put("robust", "a = ucomplex(\'A\',4+3*j);");
// Signal Processing
toolboxmap.put("signal", "t = 0:0.001:2;y = chirp(t,0,1,150);");
// Spline
toolboxmap.put("splines", "points=[0 1 1 0 -1 -1 0 0; 0 0 1 2 1 0 -1 -2];a = cscvn(points);");
// Statistics
toolboxmap.put("stats", "a = [0.5 1; 2 4];y = betapdf(0.5,a,a);");
// Symbolic Maths
toolboxmap.put("symbolic", "syms a b c d;det([a, b; c, d]);");
// System Identification
toolboxmap.put("ident", "a=[1 2 3]; data = iddata(a,[],1);");
// Virtual Reality
toolboxmap.put("vr", "myworld = vrworld([]);");
// Simulink Control Design
toolboxmap.put("slcontrol", "io(1)=linio(\'magball/Controller\',1);");
// Simulink Stateflow
toolboxmap.put("stateflow", "object = sfclipboard;");
// Compiler
toolboxmap.put("compiler", "mcc -m cosh;");


codebegin = "try;";
codemiddle = "pause(1);"
codeend = "catch ME;disp('licence error');java.lang.System.exit(1);end;quit;"


if (System.getProperty("os.name").startsWith("Windows")) {
    try {
        selected = (java.lang.Runtime.getRuntime().exec("REG QUERY \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Mathworks\\MATLAB\" /s").waitFor() == 0);
    }
    catch(err) {
        selected = false;
    }
    command = "matlab.exe";
    windows = true;
}
else {
    try {
        selected = (java.lang.Runtime.getRuntime().exec("which matlab2007b").waitFor() == 0);
        command = "matlab2007b";
        if (!selected) {
            selected = (java.lang.Runtime.getRuntime().exec("which matlab2007a").waitFor() == 0);
            command = "matlab2007a";
        }
        if (!selected) {
            selected = (java.lang.Runtime.getRuntime().exec("which matlab2006b").waitFor() == 0);
            command = "matlab2006b";
        }
        if (!selected) {
            selected = (java.lang.Runtime.getRuntime().exec("which matlab2006a").waitFor() == 0);
            command = "matlab2006a";
        }
        if (!selected) {
            selected = (java.lang.Runtime.getRuntime().exec("which matlab71").waitFor() == 0);
            command = "matlab71";
        }
        if (!selected) {
            selected = (java.lang.Runtime.getRuntime().exec("which matlab").waitFor() == 0);
            command = "matlab";
        }
    }
    catch(err) {
        selected = false;
    }
}
if (selected) {
    if (windows) {
        cmd_options = ["-automation", "-r"];
    }
    else {
        cmd_options = ["-nodisplay", "-nosplash", "-r"];
    }

    for (i = 0; i < args.length; i++) {
        cmd_array = [command];
        cmd_array = cmd_array.concat(cmd_options);
        arg = args[i];
        if (arg != null) {
            print("Testing licence coin for " + arg + " : " + java.net.InetAddress.getLocalHost().getHostName() + "\n");
            if (toolboxmap.containsKey(arg)) {
                code = toolboxmap.get(arg);
                cmd_array = cmd_array.concat([codebegin + code + codemiddle + code + codeend]);
                rt = java.lang.Runtime.getRuntime();
                process = rt.exec(cmd_array);
                // The following commented code can be used for debugging, use it with care as it starts threads
                // lt2 = IOTools.LoggingThread(process.getErrorStream(), "[ERR]", true);
                // t2 = java.lang.Thread(lt2, "ERR Matlab");
                // t2.setDaemon(true);
                // t2.start();
                res = process.waitFor();
                //t2.stop();
                if (res > 0) {
                    selected = false;
                    print("Unsufficient licence coin for " + arg + " : " + java.net.InetAddress.getLocalHost().getHostName() + "\n");
                    break;
                }
            }
        }
    }
    if (selected) {
        print("Good : " + java.net.InetAddress.getLocalHost().getHostName() + "\n");
        java.lang.System.out.flush();
        java.lang.Thread.sleep(2000);
    }
}
else print("No Matlab installed or system error: " + java.net.InetAddress.getLocalHost().getHostName() + "\n");

