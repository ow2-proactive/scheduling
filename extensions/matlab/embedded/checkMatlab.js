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
importPackage(org.objectweb.proactive.api);
importPackage(org.ow2.proactive.scheduler.ext.matlab);

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
//toolboxmap.put("simulink", "res=new_system('toto');");
toolboxmap.put("simulink", "license(\'checkout\',\'simulink\');");
// Control System
//toolboxmap.put("control", "s = tf(\'s\');");
toolboxmap.put("control", "license(\'checkout\',\'control_toolbox\');");
// Fixed Point
// The fixed point toolbox has a very weird licencing scheme, it takes no toolbox licence
// if the logging mode is not activated, here the command simulates that a licence is being taken
// If you want to use fixed point without the logging, replace the matlab command below with a dummy commmand like "a=1;"
toolboxmap.put("fixedpoint", "pref = fipref(\'LoggingMode\',\'on\');a = fi;");
// Image Processing
//toolboxmap.put("images", "s = imcomplement(uint8([ 255 10 75; 44 225 100]));");
toolboxmap.put("images", "license(\'checkout\',\'image_toolbox\');");
// Neural Networks
//toolboxmap.put("nnet", "net = newlin([-1 1],1,[0 1],0.01);");
toolboxmap.put("nnet", "license(\'checkout\',\'neural_network_toolbox\');");
// Optimization
//toolboxmap.put("optim", "[x,fval] = fminunc(@(x) 3*x(1)^2 + 2*x(1)*x(2) + x(2),[1,1]);");
toolboxmap.put("optim", "license(\'checkout\',\'optimization_toolbox\');");
// PDE
//toolboxmap.put("pde", "[p,e,t]=initmesh(\'lshapeg\');");
toolboxmap.put("pde", "license(\'checkout\',\'pde_toolbox\');");
// Robust Control
//toolboxmap.put("robust", "a = ucomplex(\'A\',4+3*j);");
toolboxmap.put("robust", "license(\'checkout\',\'robust_toolbox\');");
// Signal Processing
//toolboxmap.put("signal", "t = 0:0.001:2;y = chirp(t,0,1,150);");
toolboxmap.put("signal", "license(\'checkout\',\'signal_toolbox\');");
// Spline
//toolboxmap.put("splines", "points=[0 1 1 0 -1 -1 0 0; 0 0 1 2 1 0 -1 -2];a = cscvn(points);");
toolboxmap.put("splines", "license(\'checkout\',\'spline_toolbox\');");
// Statistics
//toolboxmap.put("stats", "a = [0.5 1; 2 4];y = betapdf(0.5,a,a);");
toolboxmap.put("stats", "license(\'checkout\',\'statistics_toolbox\')");
// Symbolic Maths
//toolboxmap.put("symbolic", "syms a b c d;det([a, b; c, d]);");
toolboxmap.put("symbolic", "license(\'checkout\',\'symbolic_toolbox\')");
// System Identification
//toolboxmap.put("ident", "a=[1 2 3]; data = iddata(a,[],1);");
toolboxmap.put("ident", "license(\'checkout\',\'identification_toolbox\')");
// Virtual Reality
toolboxmap.put("vr", "myworld = vrworld([]);");
// Simulink Control Design
//toolboxmap.put("slcontrol", "io(1)=linio(\'magball/Controller\',1);");
toolboxmap.put("slcontrol", "license(\'checkout\',\'simulink_control_design\')");
// Simulink Stateflow
toolboxmap.put("stateflow", "object = sfclipboard;");
//toolboxmap.put("stateflow", "license(\'checkout\',\'simulink_stateflow\')");
// Compiler
//toolboxmap.put("compiler", "mcc -m cosh;");
toolboxmap.put("compiler", "license(\'checkout\',\'compiler\')");

finalcode = "out=1;";

// this must be enabled to debug child MatlabTask
debug=false;

// these two values are used to create synchronisation files between the different processes;

// Name of the ProActive Node where is this script is launched
nodeName = PAActiveObject.getNode().getVMInformation().getName().replace('-', '_')+"_"+PAActiveObject.getNode().getNodeInformation().getName().replace('-', '_');

// system temp dir
tmpPath = System.getProperty("java.io.tmpdir");

// log file writer used for debugging
logFile = java.io.File(tmpPath,nodeName+".log");
if (!logFile.exists()) {
    logFile.createNewFile();
}
logWriter = java.io.PrintStream(java.io.BufferedOutputStream(java.io.FileOutputStream(logFile, true)));

nodeDir = java.io.File(tmpPath,nodeName);
if (!nodeDir.exists()) {
	nodeDir.mkdir();
}


selected = false;
host = java.net.InetAddress.getLocalHost().getHostName();

logWriter.println(java.util.Date()+" : Executing selection script on " + host);

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

    // we first try to see if Matlab is available
    if (windows) {
  	  cmd_options = ["/MLAutomation","-Embedding","-r"];
    }
    else {
	  cmd_options = ["-nodisplay", "-nosplash", "-r"];
    }

    testF = java.io.File(nodeDir,"matlabTest.lock");

    //testF.createNewFile();
    logWriter.println(java.util.Date()+" : Waiting creation of lock file: "+ testF.getAbsolutePath());
    logWriter.println(java.util.Date()+" : Trying to start a Matlab session");
    cmd_array = [command];
    cmd_array = cmd_array.concat(cmd_options);
    //cmd_array = cmd_array.concat("delete('"+testF.getAbsolutePath()+"');quit;");
    cmd_array = cmd_array.concat("fid = fopen('"+testF.getAbsolutePath()+"','w');fclose(fid);pause(2);quit;");
    
    rt = java.lang.Runtime.getRuntime();
    process = rt.exec(cmd_array);
    cpt = 0;
    selected = false;
    while (cpt < 200) {
    //process.waitFor();
        if (testF.exists()) {
            cpt = 200;
            selected = true;
        }
        else {
            Thread.sleep(50);
            logWriter.println(java.util.Date()+ java.net.InetAddress.getLocalHost().getHostName() + " iter " + cpt);
            cpt++;
        }

    }
    if (!selected) {
        logWriter.println(java.util.Date()+" Unsufficient licence coin for matlab. " + java.net.InetAddress.getLocalHost().getHostName());
        testF["delete"]();
        selected = false;
    }

//    goon = true;
//    while(goon) {
//        java.lang.Thread.sleep(50);
//        try {
//            process.waitFor();
//            goon = false;
//            selected = false;
//            logWriter.println(java.util.Date()+" Unsufficient licence coin for matlab. " + java.net.InetAddress.getLocalHost().getHostName());
//        }
//        catch(err1) {
//
//        }
//        if (!testF.exists()) {
//            selected = true;
//            goon = false;
//        }
//
//    }

    //process.waitFor();
    //if (testF.exists()) {
    //    selected = false;
    //    logWriter.println(java.util.Date()+" Unsufficient licence coin for matlab. " + java.net.InetAddress.getLocalHost().getHostName());
    //}

    if (selected) {


    // we build the matlab code that will be run to trigger the licence token acquisitions
    fullcode = "";
    allargs = "";
    for (i = 0; i < args.length; i++) {
        arg = args[i];
        if ((arg != null) && toolboxmap.containsKey(arg)) {
            allargs += " " + arg;
            code = toolboxmap.get(arg);
            fullcode += code;
        }
    }
    fullcode += finalcode;
    if (allargs != "") {
        logWriter.println(java.util.Date()+" : Testing licence coin for " + allargs + " : " + host );
    }

   // task = MatlabTask("i='PROACTIVE_INITIALIZATION_CODE';",fullcode);
	task = MatlabTask();
    mapInit = java.util.HashMap();
    mapInit.put("script",fullcode);
   // mapInit.put("input","i='PROACTIVE_INITIALIZATION_CODE';");
    mapInit.put("debug", "true");
    mapInit.put("keepEngine", "true");
    task.init(mapInit);

    try {
        res = task.execute([]);
        res2 = org.objectweb.proactive.api.PAFuture.getFutureValue(res);
        selected = true;
    }
    catch(err) {
        logWriter.println(java.util.Date()+" : Error occured ");
        err.javaException.printStackTrace(logWriter);
        selected = false;
    }
    //ptk = org.ow2.proactive.scheduler.util.process.ProcessTreeKiller.get();
    //ptk.kill(process);
    try {

       process.destroy();
    }
    catch(err) {
        err.javaException.printStackTrace(logWriter);        
    }

    if (selected) {
        logWriter.println(java.util.Date()+" : Good : " + java.net.InetAddress.getLocalHost().getHostName());
    }
  }
}
else logWriter.println(java.util.Date()+" : No Matlab installed or system error: " + java.net.InetAddress.getLocalHost().getHostName());

logWriter.close();

