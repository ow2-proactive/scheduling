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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.naming.directory.InvalidAttributeValueException;

import org.glite.wms.jdlj.Jdl;
import org.glite.wms.jdlj.JobAd;
import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;


public class GroupGLite extends AbstractGroup {
    private static final String JDLPREFIX = "proactiveJDL-";
    private static final String JDLSUFFIX = ".jdl";
    private static final String JOBTYPE_SINGLE = "single";
    private static final String JOBTYPE_MULTIPLE = "multiple";
    private static final String JOBTYPE_PARALLEL = "parallel";
    private static final String GLITE_SUBMIT_COMMAND = "edg-job-submit";
    private static final String GLITE_SCRIPT = "glite-3.1.sh";
    private PathElement scriptLocation = new PathElement("dist/scripts/gcmdeployment/" + GLITE_SCRIPT,
        PathElement.PathBase.PROACTIVE);

    private String jobVO;
    private String jobMyProxyServer;
    private String jobJobType;
    private int jobNodeNumber;
    private String jobExecutable;
    private int jobRetryCount = -1;
    private String jobOutputFile;

    private String rank;
    private String environment;
    private String arguments;
    private String stdout;
    private String stdin;
    private String stderr;
    private List<String> inputSB;
    private List<String> outputSB;
    private int expiryTime = -1;
    private String requirements;
    private boolean hasDataRequirements = false;
    private String configFile;
    private String outputSE;
    private boolean fuzzyRank;

    private String inputData;
    private String dataCatalogType;
    private String dataCatalog;

    private String gcmaCommand;
    private String jobProActiveHome;
    private String jobJavaHome;
    private CommandBuilder commandBuilder;

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        List<String> ret = new ArrayList<String>();
        this.commandBuilder = commandBuilder;
        this.gcmaCommand = Helpers.escapeCommand(commandBuilder.buildCommand(hostInfo, gcma)).replaceFirst(
                "java .* org.objectweb.proactive.extensions.gcmdeployment.core.StartRuntime",
                "org.objectweb.proactive.extensions.gcmdeployment.core.StartRuntime");
        List<String> commands = internalBuildCommands();

        for (String comnand : commands) {
            ret.add(comnand + ""); //+ Helpers.escapeCommand(commandBuilder.buildCommand(hostInfo, gcma)));
        }

        return ret;
    }

    @Override
    public List<String> internalBuildCommands() {
        List<String> commands = new ArrayList<String>();
        JobAd jad = createBasicJobAd();
        int jobNumber;

        jad = this.setupProActiveJad(jad);

        File jdlFile = null;
        try {
            jdlFile = File.createTempFile(JDLPREFIX, JDLSUFFIX, null);
            jad.toFile(jdlFile.getPath());
        } catch (IOException e) {
            System.err.println("error creating temporary JDL file");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("error writing on temporary JDL file");
            e.printStackTrace();
        }

        System.out.println("File    -> " + jdlFile);

        if (this.getJobJobType().equalsIgnoreCase(JOBTYPE_MULTIPLE)) {
            jobNumber = this.getJobNodeNumber();
        } else {
            jobNumber = 1;
        }

        for (int i = 0; i < jobNumber; i++) {
            commands.add(this.makeSingleCommand(jdlFile.getPath()));
        }

        return commands;
    }

    private String makeSingleCommand(String JdlPath) {
        StringBuilder command = new StringBuilder(GLITE_SUBMIT_COMMAND);
        command.append(" ");

        if (this.getConfigFile() != null) {
            command.append("--config-vo ");
            command.append(this.getConfigFile());
            command.append(" ");
        }

        if (this.getJobOutputFile() != null) {
            command.append("-o ");
            command.append(this.getJobOutputFile());
            command.append(" ");
        }
        command.append(JdlPath);

        return command.toString();

    }

    private JobAd setupProActiveJad(JobAd oldJad) {
        JobAd jad = oldJad;
        StringBuilder arguments = new StringBuilder("");
        StringBuilder executable = new StringBuilder("");
        StringBuilder requirements = new StringBuilder("");

        if (this.getJobJobType().equalsIgnoreCase("native")) {
            return jad;
        } else if (this.getJobJobType().equalsIgnoreCase("parallel")) {
            if (this.getRequirements() != null) {
                requirements.append(this.getRequirements());
                requirements.append(" && ");
            }
            requirements
                    .append("(other.GlueCEInfoLRMSType == \"PBS\") || (other.GlueCEInfoLRMSType == \"LSF\")");
        }

        arguments.append(this.getJobJobType());
        arguments.append(" ");

        arguments.append(this.getJobJavaHome());
        arguments.append(" ");
        arguments.append(this.getJobProActiveHome());
        arguments.append(" ");
        arguments.append(gcmaCommand);

        executable.append(GLITE_SCRIPT);

        try {
            jad.addAttribute(Jdl.INPUTSB, scriptLocation.getFullPath(hostInfo, commandBuilder));

            jad.delAttribute(Jdl.ARGUMENTS);
            jad.addAttribute(Jdl.ARGUMENTS, arguments.toString());

            jad.delAttribute(Jdl.EXECUTABLE);
            jad.addAttribute(Jdl.EXECUTABLE, executable.toString());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvalidAttributeValueException e) {
            e.printStackTrace();
        }
        return jad;
    }

    public JobAd createBasicJobAd() {
        JobAd jad = new JobAd();

        try {

            /*DAG jobs are not supported for the moment, so type=job by default*/
            jad.addAttribute(Jdl.TYPE, "job");

            if (this.getJobVO() != null)
                jad.addAttribute(Jdl.VIRTUAL_ORGANISATION, this.getJobVO());

            if (this.getJobMyProxyServer() != null)
                jad.addAttribute(Jdl.MYPROXY, this.getJobMyProxyServer());

            if (this.getJobExecutable() != null)
                jad.addAttribute(Jdl.EXECUTABLE, this.getJobExecutable());

            if (this.getJobRetryCount() > 0)
                jad.addAttribute(Jdl.RETRYCOUNT, this.getJobRetryCount());

            /*setting jobtype*/
            if (this.getJobJobType().equalsIgnoreCase(JOBTYPE_SINGLE)) {
                jad.addAttribute(Jdl.JOBTYPE, Jdl.JOBTYPE_NORMAL);

            }
            if (this.getJobJobType().equalsIgnoreCase(JOBTYPE_MULTIPLE)) {
                jad.addAttribute(Jdl.JOBTYPE, Jdl.JOBTYPE_NORMAL);

            }
            if (this.getJobJobType().equalsIgnoreCase(JOBTYPE_PARALLEL)) {
                jad.addAttribute(Jdl.JOBTYPE, Jdl.JOBTYPE_MPICH);
                jad.addAttribute(Jdl.NODENUMB, this.getJobNodeNumber());
            }
            /*end setting jobtype*/

            if (this.getRank() != null)
                jad.setAttributeExpr(Jdl.RANK, this.getRank());

            if (this.getRequirements() != null)
                jad.setAttributeExpr(Jdl.REQUIREMENTS, this.getRequirements());

            if (this.getEnvironment() != null)
                jad.addAttribute(Jdl.ENVIRONMENT, this.getEnvironment());

            if (this.getArguments() != null)
                jad.addAttribute(Jdl.ARGUMENTS, this.getArguments());

            if (this.getStderr() != null)
                jad.addAttribute(Jdl.STDERROR, this.getStderr());

            if (this.getStdout() != null)
                jad.addAttribute(Jdl.STDOUTPUT, this.getStdout());

            if (this.getStdin() != null) {
                /*stdin must be on input sandbox*/
                if (this.getInputSB().contains(this.getStdin()))
                    jad.addAttribute(Jdl.STDINPUT, this.getInputData());
                else
                    throw new IllegalArgumentException();
            }

            if ((this.getInputSB() != null) && (this.getInputSB().size() > 0)) {
                for (int i = 0; i < this.getInputSB().size(); i++) {
                    String entry = (String) getInputSB().get(i);
                    jad.addAttribute(Jdl.INPUTSB, entry);
                }
            }

            if ((this.getOutputSB() != null) && (this.getOutputSB().size() > 0)) {
                for (int i = 0; i < this.getOutputSB().size(); i++) {
                    String entry = (String) getOutputSB().get(i);
                    jad.addAttribute(Jdl.OUTPUTSB, entry);
                }
            }

            if (this.getExpiryTime() > 0) {
                // not yet supported by WMS 3.1
                //jad.addAttribute(Jdl., Integer.toString(this.getExpiryTime());
            }

            if (this.hasDataRequirements()) {
                if (this.getInputData() != null && this.getDataCatalog() != null &&
                    this.getDataCatalogType() != null) {

                } else {
                    throw new IllegalArgumentException();
                }

            }

            if (this.getOutputSE() != null)
                jad.addAttribute(Jdl.OUTPUT_SE, this.getOutputSE());

            if (this.fuzzyRank)
                jad.setAttribute(Jdl.FUZZY_RANK, "true");

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvalidAttributeValueException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jad;
    }

    public String getJobVO() {
        return jobVO;
    }

    public void setJobVO(String jobVO) {
        this.jobVO = jobVO;
    }

    public String getJobMyProxyServer() {
        return jobMyProxyServer;
    }

    public void setJobMyProxyServer(String jobMyProxyServer) {
        this.jobMyProxyServer = jobMyProxyServer;
    }

    public String getJobJobType() {
        return jobJobType;
    }

    public void setJobJobType(String jobJobType) {
        this.jobJobType = jobJobType;
    }

    public int getJobNodeNumber() {
        return jobNodeNumber;
    }

    public void setJobNodeNumber(String jobNodeNumber) {
        this.jobNodeNumber = Integer.parseInt(jobNodeNumber);
    }

    public String getJobExecutable() {
        return jobExecutable;
    }

    public void setJobExecutable(String jobExecutable) {
        this.jobExecutable = jobExecutable;
    }

    public int getJobRetryCount() {
        return jobRetryCount;
    }

    public void setJobRetryCount(String jobRetryCount) {
        this.jobRetryCount = Integer.parseInt(jobRetryCount);
    }

    public void setJobOutputFile(String outFile) {
        this.jobOutputFile = outFile;
    }

    public String getJobOutputFile() {
        return jobOutputFile;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setArguments(String args) {
        this.arguments = args;
    }

    public String getArguments() {
        return this.arguments;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStdin() {
        return stdin;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public List<String> getInputSB() {
        return inputSB;
    }

    public void setInputSB(List<String> inputSB) {
        this.inputSB = inputSB;
    }

    public List<String> getOutputSB() {
        return outputSB;
    }

    public void setOutputSB(List<String> outputSB) {
        this.outputSB = outputSB;
    }

    public int getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(String nodeValue) {
        this.expiryTime = Integer.parseInt(nodeValue);
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public boolean hasDataRequirements() {
        return hasDataRequirements;
    }

    public void setHasDataRequirements(boolean hasDataRequirements) {
        this.hasDataRequirements = hasDataRequirements;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public String getOutputSE() {
        return outputSE;
    }

    public void setOutputSE(String outputSE) {
        this.outputSE = outputSE;
    }

    public boolean isFuzzyRank() {
        return fuzzyRank;
    }

    public void setFuzzyRank(boolean fuzzyRank) {
        this.fuzzyRank = fuzzyRank;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getDataCatalogType() {
        return dataCatalogType;
    }

    public void setDataCatalogType(String dataCatalogType) {
        this.dataCatalogType = dataCatalogType;
    }

    public String getDataCatalog() {
        return dataCatalog;
    }

    public void setDataCatalog(String dataCatalog) {
        this.dataCatalog = dataCatalog;
    }

    public boolean addInputSBEntry(String entry) {
        if (inputSB == null) {
            inputSB = new LinkedList<String>();
        }
        return inputSB.add(entry);
    }

    public boolean addOutputSBEntry(String entry) {
        if (outputSB == null) {
            outputSB = new LinkedList<String>();
        }
        return outputSB.add(entry);
    }

    public void setJobProActiveHome(String proactiveHome) {
        this.jobProActiveHome = proactiveHome;
    }

    public String getJobProActiveHome() {
        return this.jobProActiveHome;
    }

    public void setJobJavaHome(String javaHome) {
        this.jobJavaHome = javaHome;
    }

    public String getJobJavaHome() {
        return this.jobJavaHome;
    }

}
