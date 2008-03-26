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
package org.objectweb.proactive.core.process.unicore;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.unicore.User;
import org.unicore.Vsite;
import org.unicore.resources.CapacityResource;
import org.unicore.resources.Memory;
import org.unicore.resources.Node;
import org.unicore.resources.Priority;
import org.unicore.resources.PriorityValue;
import org.unicore.resources.Processor;
import org.unicore.resources.RunTime;

import com.pallas.unicore.client.Client;
import com.pallas.unicore.client.UserDefaults;
import com.pallas.unicore.client.plugins.script.ScriptContainer;
import com.pallas.unicore.client.util.ClientPluginManager;
import com.pallas.unicore.client.util.TaskPlugable;
import com.pallas.unicore.client.xml.XMLObjectWriter;
import com.pallas.unicore.container.ActionContainer;
import com.pallas.unicore.container.JobContainer;
import com.pallas.unicore.extensions.FileImport;
import com.pallas.unicore.extensions.NamedResourceSet;
import com.pallas.unicore.extensions.Usite;
import com.pallas.unicore.extensions.Usite.Type;
import com.pallas.unicore.requests.SubmitJob;
import com.pallas.unicore.resourcemanager.ResourceManager;
import com.pallas.unicore.resourcemanager.ResourceTray;


/**
 * @author The ProActive Team
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class UnicoreProActiveClient {
    static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT_PROCESS);
    private JobContainer jc;
    private NamedResourceSet namedResourceSet;
    boolean jobIsBuilt = false;

    // Unicore Site conf parameters
    private UnicoreParameters uParam;
    static final String CLIENTUSAGE = "-jobName JobName -keypassword KeyPassWord -keyfilepath KeyFilePath "
        + "-unicoredir UnicoreDir -submitJob [true|false] -saveJob [true|false] "
        + "-usitename Name -usitetype [CLASSIC|REGISTRY] -usiteurl Url "
        + "-vsitename Name -vsitenodes Nodes -vsiteprocessors Processors "
        + "-vsitememory Memory -vsiteruntime Runtime "
        + "-vsitepriority [high|low|normal|development|whenever]";

    public UnicoreProActiveClient(UnicoreParameters uParam) {
        this.uParam = uParam;

        //Prompt the user for a keypassword if not
        //specified in the descriptor file
        if ((uParam.getKeyPassword() == null) || (uParam.getKeyPassword().length() <= 0)) {
            UnicorePasswordGUI upGUI = new UnicorePasswordGUI();
            uParam.setKeyPassword(upGUI.getKeyPassword());
        }
    }

    /**
     * This method uses reflection to access private fields in
     * com.pallas.unicore.client.Client; Settings this fields is mandatory
     * before creating a Client object.
     *
     * @param noGui
     *            Should be true, since the idea is not to use the GUI
     * @param unicoreDir
     *            Home dir of client instalation. Needed by the Unicore library
     *            to load configurations.
     * @param testGrid
     *            Indicates weather or not to use Testing site configurations.
     */
    private void initStaticClientFields(Boolean noGui, String unicoreDir, Boolean testGrid) {
        try {
            Class<?> clientClass = Client.class;

            Field fNoGui = clientClass.getDeclaredField("noGui");
            Field fUnicoreDir = clientClass.getDeclaredField("unicoreDir");
            Field fTestGrid = clientClass.getDeclaredField("testGrid");

            fNoGui.setAccessible(true);
            fUnicoreDir.setAccessible(true);
            fTestGrid.setAccessible(true);

            //System.out.println(f.getName()+ "=" + (Boolean)f.get(null));
            fNoGui.set(null, noGui); //  field ==> (object==null)
            fUnicoreDir.set(null, unicoreDir);
            fTestGrid.set(null, testGrid);
        } catch (Exception e) {
            System.err.println("Error in getField");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Tries to find a Vsite for a given Usite. Refreshing the ResourceManager
     * cache of Vsites is recomended before calling this method:
     * ResourceManager.loadVsites(jc.getUsite()); Notice that calling
     * loadVsites(...) will establish a connection to the Usite to search for
     * available Vsites.
     *
     * @param us
     *            A Usite object that contains de information on a Unicore Site.
     * @param name
     *            The name of the Vsite to match on the Usite. This match is
     *            case sensitive.
     * @return The Vsite object representing the Virtual Site that matches the
     *         requested name on the remote Unicore Site. If no Vsite is found
     *         with the given name on the Usite, null is returned.
     */
    private Vsite getVsitebyName(Usite us, String name) {
        Vsite vs;
        Vector vSiteVector = ResourceManager.getVsites(us);

        for (int i = 0; i < vSiteVector.size(); i++) {
            vs = (Vsite) vSiteVector.elementAt(i);
            if (vs.getName().equals(name)) {
                return vs;
            }
        }

        System.err.println("Error Vsite (" + name + ") not found for: " + us.toString());
        return null;
    }

    /**
     * This method initializes the ResourceManager through the Client object.
     * Keep in mind that further initialization for ResourceManager is required
     * depending on the feature used.
     *
     * @param keystorefile
     *            Fullpath to file that holdes the certificate keystore. This
     *            usually corresponds to $HOMEDIR/.unicore/keystore
     * @param keypassword
     *            Password needed to access the certificate.
     */
    private void initResourceManager(String keystorefile, String keypassword) {
        ResourceManager.loadImages();
        Client client = new Client();
        try {
            ResourceManager.loadKeystore(keystorefile, keypassword.toCharArray());
        } catch (Exception e) {
            System.err.println("Key not   loaded:" + keystorefile);
            e.printStackTrace();
        }
    }

    private TaskPlugable getTaskPlugin(String name) {
        ClientPluginManager pluginManager = Client.getPluginManager();
        pluginManager.stopPlugins();
        UserDefaults userDefaults = ResourceManager.getUserDefaults();
        pluginManager.scanPlugins(userDefaults.getPluginDirectory());
        pluginManager.startPlugins(ResourceManager.getCurrentInstance());
        Vector taskplugins = pluginManager.getTaskPlugins();
        for (int i = 0; i < taskplugins.size(); i++) {
            TaskPlugable tp = (TaskPlugable) taskplugins.elementAt(i);
            if (tp.getName().equals(name)) {
                return tp;
            }
        }
        return null;
    }

    private JobContainer getNewJob(String name, String filename) {
        JobContainer jc = new JobContainer();

        //jc.setIdentifier();
        jc.setName(name); //Internal job name
        jc.setFilename(filename); //Filename when saved
        jc.setIgnoreFailure(false); //Set ignore failure flag
        //jc.setDependencies(); //Set dependencies

        return jc;
    }

    private ScriptContainer addScriptTask(String taskName, String scriptContent) {
        TaskPlugable plugableScript = getTaskPlugin("Script");
        if (plugableScript == null) {
            System.err.println("ERROR ScripTask object not found!!!");
            return null;
        }

        ScriptContainer sc = (ScriptContainer) plugableScript.getContainerInstance(jc);

        sc.setName(taskName);
        sc.setIgnoreFailure(false);
        sc.setCommandLine("");
        sc.setStderr("");
        sc.setStdin("");
        sc.setStdout("");
        sc.setScriptContents(scriptContent);

        jc.addTask(sc);
        return sc;
    }

    private Usite getNewUsite(String urlString, String usiteName, Type Usite) {
        URL usiteURL = null;
        try {
            usiteURL = new URL(urlString);
        } catch (Exception e) {
            System.err.println("URL Generation Error");
            e.printStackTrace();
        }
        Usite us = new Usite(usiteURL, usiteName, Usite);
        return us;
    }

    private void setCapResDefaultMaxMin(CapacityResource candidate) {
        NamedResourceSet namedResourceSet = ResourceManager.getResourceSet(jc.getVsite());

        CapacityResource cr = (CapacityResource) namedResourceSet.findResource(candidate);

        candidate.setDefaultRequest(cr.getDefaultRequest());
        candidate.setMaxRequest(cr.getMaxRequest());
        candidate.setMinRequest(cr.getMinRequest());
    }

    private boolean checkCapacityResource(CapacityResource candidate) {
        if ((candidate.getRequest() > candidate.getMaxRequest()) ||
            (candidate.getRequest() < candidate.getMinRequest())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error with Capacity: max=" + candidate.getMaxRequest() + " min=" +
                    candidate.getMinRequest() + " default=" + candidate.getDefaultRequest() + " request=" +
                    candidate.getRequest());
            }

            return false;
        }

        return true;
    }

    private void setCapacityResource(CapacityResource cr, int request) {
        setCapResDefaultMaxMin(cr);
        cr.setRequest(request);
        if (!checkCapacityResource(cr)) {
            System.err.println("Using default " + cr.getClass().getName() + " request: " +
                cr.getDefaultRequest());
            cr.setRequest(cr.getDefaultRequest());
        }
    }

    private Node getNode(int numnodes) {
        Node node = new Node();
        setCapacityResource(node, numnodes);
        return node;
    }

    private Processor getProcessor(int numprocessor) {
        Processor p = new Processor();
        setCapacityResource(p, numprocessor);
        return p;
    }

    private Memory getMemory(int nummemory) {
        Memory mem = new Memory();
        setCapacityResource(mem, nummemory);
        return mem;
    }

    private RunTime getRuntime(int numseconds) {
        RunTime rt = new RunTime();
        setCapacityResource(rt, numseconds);
        return rt;
    }

    private Priority getPriority(PriorityValue priValue) {
        Priority pri = new Priority();
        pri.setValue(priValue);
        return pri;
    }

    /**
     * Initial configuration for unicore client lib. Also creats a job object.
     */
    private void buildJob() {
        initStaticClientFields(new Boolean(true), uParam.getUnicoreDir(), new Boolean(false));
        initResourceManager(uParam.getKeyFilePath(), uParam.getKeyPassword());

        jc = getNewJob(uParam.getJobName(), uParam.getJobName());
    }

    /**
     * Build the Usite configuration parameters
     */
    private void buildUsite() {
        //************** USITE *********************
        Usite us = getNewUsite(uParam.getUsiteUrl(), uParam.getUsiteName(), uParam.getUsiteType());

        jc.setUsite(us);
    }

    /**
     * Build the Vsite configuration parameters
     */
    private void buildVsite() {
        //*************** VSITE ***********************
        ResourceManager.loadVsites(jc.getUsite());
        Vsite vs = getVsitebyName(jc.getUsite(), uParam.getVsiteName());
        jc.setVsite(vs);

        //*************** USER ************************
        User user = ResourceManager.getUser();
        jc.setUser(user);
    }

    /**
     * Build the Resouece configuration parameters
     */
    private void buildResources() {
        //*************** Requested Resources *********
        //Create a new ResourceTray if needed
        //Create a new resourceSet encapsulator
        namedResourceSet = new NamedResourceSet("ProActive Descriptor Defined Resources");
        namedResourceSet.removeAllElements();

        //Add the resource to the NamedResourceSet
        Node node = getNode(uParam.getVsiteNodes());
        namedResourceSet.add(node);
        Processor pro = getProcessor(uParam.getVsiteProcessors());
        namedResourceSet.add(pro);
        Memory mem = getMemory(uParam.getVsiteMemory());
        namedResourceSet.add(mem);
        RunTime runtime = getRuntime(uParam.getVsiteRuntime());
        namedResourceSet.add(runtime);
        Priority pri = getPriority(uParam.getVsitePriority());
        namedResourceSet.add(pri);
    }

    /**
     * Build the Script configuration parameters. Also links the resources to
     * the job.
     */
    private void buildScriptTask() {
        //*************** Set TASKS *****************
        //mv files + command
        ScriptContainer sc = addScriptTask("ProActiveTask", uParam.getDestMoveCommand() +
            uParam.getScriptContent());

        // Add file imports
        buildImportFiles(sc);

        //Put the NamedresourceSet on the Job resourceTray
        ResourceTray resourceTray = jc.getResourceTray();
        resourceTray.put(sc.getIdentifier(), namedResourceSet);
    }

    private void buildImportFiles(ScriptContainer sc) {
        String[] files = uParam.getDeployAllFilesAndDirectories();

        //Add each file to the ScriptContainer
        for (int i = 0; i < files.length; i++) {
            String[] fileInfo = files[i].split(",");

            //TODO improve the syntax checking
            if (fileInfo.length != 2) {
                System.err.println("UnicoreProActiveClient Syntax error Skipping: " + files[i]);
                continue;
            }

            //Storage, srcName, srcDest, overwrite, isAscii
            FileImport fi = new FileImport("Local", fileInfo[0], UnicoreParameters.getFileName(fileInfo[1]),
                true, false);

            sc.addFileImport(fi);
        }
    }

    /**
     * Before calling the build() method, this function sets the desired script
     * content to be executed on the unicore server. Calling this function is
     * equivalent to setting the scriptContent through a UnicoreParameter
     * object. Note that calling this function after build() will not affect on
     * the resulting submition job.
     *
     * @param scriptContent
     *            Contains the desired script to be executed on the remote
     *            Unicore server.
     */
    public void setScriptContent(String scriptContent) {
        if (jobIsBuilt) {
            System.err.println("Error Job already built." + "ScriptContent change will not affect job");
        }
        uParam.setScriptContent(scriptContent);
    }

    /**
     * After the parameters have been set through UnicoreParameters uParam, this
     * methods builds the parameteres into the unicore client library. Invoking
     * this method before submitting the job is mandatory.
     */
    public void build() {
        if (jobIsBuilt) {
            System.err.println("Error. Job has already been built." + "Can not build it again");
        }

        buildJob();
        buildUsite();
        buildVsite();
        buildResources();
        buildScriptTask();

        jobIsBuilt = true;
    }

    /**
     * Submits the current Job to the Unicore Server. This method must be called
     * after: -Configuring all the uParam Parameters -Call build() to set up the
     * request
     */
    public void submitJob() {
        if (!uParam.isSubmitJob()) {
            return;
        }

        if (!jobIsBuilt) {
            System.err.println("Error in Unicore submitJob."
                + " Must call buildJob() first. Job not submitted.");
            return;
        }

        long beginning = System.currentTimeMillis();
        long end = beginning;

        SubmitJob submit = new SubmitJob(jc);
        submit.start();
        while (jc.getState() != ActionContainer.STATE_SUBMITTED) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted");
                e.printStackTrace();
            }
        }

        end = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("Job submission took:" + (end - beginning) + "[ms]");
        }
    }

    /**
     * Saves the current Job to disc. The object is saved in two ways, a
     * serialized version suffixed with .ajo, and a .xml version. It is
     * important to configure the Job FileName before calling saveJob(...), this
     * can be done with: jc.setFilename(String filename); Currently the FileName
     * is the same as the JobName.
     */
    public void saveJob() {
        if (!uParam.isSaveJob()) {
            return;
        }

        if (!jobIsBuilt) {
            System.err.println("Error in Unicore submitJob."
                + " Must call buildJob() first. Job not submitted.");
            return;
        }

        //		System.out.println(uParam);
        if ((jc.getFilename() == null) || (jc.getFilename().length() < 0)) {
            System.err.println("Saving job aborted. Filename not configured");
            return;
        }

        boolean succeeded = ResourceManager.writeObjectToFile(jc, jc.getFilename() + ".ajo");

        try {
            long timing = System.currentTimeMillis();

            //FileOutputStream f = new FileOutputStream(name);
            XMLObjectWriter dout = new XMLObjectWriter(new File(jc.getFilename() + ".xml"));
            dout.writeObjectXML(jc);

            timing = System.currentTimeMillis() - timing;
            System.out.println("Writing job in XML format to file " + jc.getFilename() + " in " + timing +
                " milliseconds");
        } catch (Exception e) {
            System.out.println("Exception during XML serialization...");
            e.printStackTrace();
        }
    }

    /**
     * Direct usage of client is discouraged.
     * Recomended usage is through a ProActive Descriptor file.
     */
    public static void main(String[] args) {

        /*
        UnicoreParameters uParam = new UnicoreParameters();

        uParam.setUnicoreDir("/home/mleyton/.unicore");
        uParam.setKeyFilePath("/home/mleyton/.unicore/keystore");
        uParam.setKeyPassword("testing");
        uParam.setJobName("ProActiveClientJob");
        uParam.setScriptContent("java -version");
        uParam.setUsiteName("Gate Europe");
        uParam.setUsiteUrl("http://testgrid.unicorepro.com:4000");
        uParam.setUsiteType("CLASSIC");
        uParam.setVsiteName("SUPRENUM");

        uParam.setVsiteNodes(1); uParam.setVsiteProcessors(1);
        uParam.setVsiteMemory(16); uParam.setVsiteRuntime(300);
        uParam.setVsitePriority("high");

        uParam.setSubmitJob(false);
         */
        UnicoreProActiveClient upc = new UnicoreProActiveClient(parseArgs(args));

        //DEBUG
        //System.out.println("After parsing in forked method");
        //System.out.println(upc.uParam);
        upc.build();
        upc.saveJob();
        upc.submitJob();
        System.exit(0);
    }

    public static UnicoreParameters parseArgs(String[] args) {
        UnicoreParameters uParam = new UnicoreParameters();

        int i = 0;
        int j;
        String arg;
        String argvalue;
        char flag;

        while ((i < args.length) && args[i].startsWith("-")) {
            arg = args[i++];

            if ((i >= args.length) || (arg.charAt(0) != '-')) {
                System.err.println(arg + " requires a parameter");
                System.err.println(CLIENTUSAGE);
                System.exit(1);
            }
            uParam.setParameter(arg, args[i++]);
        } //while		

        if (i < args.length) {
            StringBuilder sb = new StringBuilder();
            while (i < args.length)
                sb.append(args[i++]).append(" ");

            uParam.setScriptContent(sb.toString());
        } else {
            System.err.println("Missing command");
            System.err.println(CLIENTUSAGE);
            System.exit(1);
        }

        //debug
        //System.out.println("UNICORE PROACTIVE CLIENT");
        //System.out.println(uParam);
        return uParam;
    } //parseArgs
}
