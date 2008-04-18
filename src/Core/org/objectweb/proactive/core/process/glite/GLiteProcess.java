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
package org.objectweb.proactive.core.process.glite;

import java.io.File;
import java.util.LinkedList;

import javax.naming.directory.InvalidAttributeValueException;

import org.glite.wms.jdlj.Ad;
import org.glite.wms.jdlj.Jdl;
import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.process.filetransfer.FileDependant;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition;


/**
 * GLite Process implementation. This implementation works only for ProActive deployment, and not to
 * submit single commands
 * 
 * JDL specification can be find at:
 * https://edms.cern.ch/file/555796/1/EGEE-JRA1-TEC-555796-JDL-Attributes-v0-8.pdf
 * 
 * @author The ProActive Team
 * @version 1.0, 2006/11/10
 * @since ProActive 3.1
 */
public class GLiteProcess extends AbstractExternalProcessDecorator implements FileDependant {

    /**
     * Firsts parameters
     */
    protected static final String DEFAULT_PROCESSOR_NUMBER = "1";
    protected static final String DEFAULT_COMMAND_PATH = "glite-job-submit";
    protected static final String DEFAULT_FILE_LOCATION = System.getProperty("user.home") + File.separator +
        "public" + File.separator + "JDL";
    protected static final String DEFAULT_STDOUPUT = System.getProperty("user.home") + File.separator +
        "out.log";
    protected static final String DEFAULT_CONFIG_FILE = System.getProperty("user.home") + File.separator +
        "public" + File.separator + "JDL" + File.separator + "vo.conf";
    protected int jobID;
    protected String hostList;
    protected String processor = DEFAULT_PROCESSOR_NUMBER;
    protected String command_path = DEFAULT_COMMAND_PATH;
    protected String interactive = "false";
    protected String filePath = DEFAULT_FILE_LOCATION;
    protected String stdOutput = DEFAULT_STDOUPUT;
    protected String fileName = "job.jdl";
    protected String configFile = DEFAULT_CONFIG_FILE;
    protected String remoteFilePath = null;
    protected boolean confFileOption = false;
    protected boolean jdlRemote = false;
    protected String netServer;
    protected String logBook;

    /* jdl related fields */
    protected GLiteJobAd jad;
    protected int jobNodeNumber = 2;
    protected String jobType;
    protected String jobJobType;
    protected String jobExecutable;
    protected String jobStdOutput;
    protected String jobStdInput;
    protected String jobStdError;
    protected String jobOutput_se;
    protected String jobVO;
    protected String jobRetryCount;
    protected String jobMyProxyServer;
    protected String jobDataAccessProtocol;
    protected String jobStorageIndex;
    protected String jobEnvironment;
    protected String jobRequirements;
    protected String jobRank;
    protected String jobFuzzyRank;
    protected String jobArgument;
    protected LinkedList jobInputSB;
    protected LinkedList jobOutputSB;

    /**
     * Create a new GLiteProcess Used with XML Descriptors
     */
    public GLiteProcess() {
        super();
        setCompositionType(COPY_FILE_AND_APPEND_COMMAND);
        this.hostname = null;
        command_path = DEFAULT_COMMAND_PATH;
        jad = new GLiteJobAd();
    }

    /**
     * Create a new GLiteProcess
     * 
     * @param targetProcess
     *            The target process associated to this process. The target process represents the
     *            process that will be launched with the glite-job-submit command
     */
    public GLiteProcess(ExternalProcess targetProcess) {
        super(targetProcess);
        setCompositionType(COPY_FILE_AND_APPEND_COMMAND);
        this.hostname = null;
        jad = new GLiteJobAd();
    }

    /**
     * Create the jdl file with all the options specified in the descriptor. Creation will take
     * place in the host that submit gLite job
     */
    public void buildJdlFile() {
        StringBuffer gLiteCommand = new StringBuffer();
        String args;
        gLiteCommand.append(command_path);
        String initial_args = ((JVMProcess) getTargetProcess()).getCommand();

        /**
         * gLiteStartRuntime.sh must be in InputSandBox within xml descriptor
         */
        if (this.getJobType().equals("MPICH")) {
            args = this.getJobExecutable() +
                initial_args.substring(initial_args.indexOf("/bin/java") + "/bin/java".length());
            /* arguments will be parsed again at script level within gLite environment */
            this.setJobExecutable("gLiteStartRuntime.sh");
        } else {
            args = initial_args.substring(initial_args.indexOf("/bin/java") + "/bin/java".length());
        }

        args = checkSyntax(args);

        try {

            /* multiple job */
            if (this.getJobType().equals("job") && this.getJobJobType().equals("mpich")) {
                this.jad.addAttribute(Jdl.TYPE, "job");
                this.jad.addAttribute(Jdl.JOBTYPE, Jdl.JOBTYPE_MPICH);
                this.jad.addAttribute(Jdl.NODENUMB, this.getJobNodeNumber());

                /* single job, number of nodes doesn t matter (so far) */
            } else if (this.getJobType() != null) {
                this.jad.addAttribute(Jdl.TYPE, "job");
            }

            if (this.getJobExecutable() != null) {
                this.jad.addAttribute(Jdl.EXECUTABLE, this.getJobExecutable());
            }

            if (this.getJobStdOutput() != null) {
                this.jad.addAttribute(Jdl.STDOUTPUT, this.getJobStdOutput());
            }

            if (this.getJobStdInput() != null) {
                this.jad.addAttribute(Jdl.STDINPUT, this.getJobStdInput());
            }

            if (this.getJobStdError() != null) {
                this.jad.addAttribute(Jdl.STDERROR, this.getJobStdError());
            }

            if (this.getJobOutput_se() != null) {
                this.jad.addAttribute(Jdl.OUTPUT_SE, this.getJobOutput_se());
            }

            if (this.getJobVO() != null) {
                this.jad.addAttribute(Jdl.VIRTUAL_ORGANISATION, this.getJobVO());
            }

            if (this.getJobRetryCount() != null) {
                this.jad.addAttribute(Jdl.RETRYCOUNT, Integer.parseInt(getJobRetryCount()));
            }

            if (this.getJobMyProxyServer() != null) {
                this.jad.addAttribute(Jdl.MYPROXY, this.getJobMyProxyServer());
            }

            if (this.getJobEnvironment() != null) {
                this.jad.addAttribute(Jdl.ENVIRONMENT, this.getJobEnvironment());
            }

            if (this.getJobRequirements() != null) {
                this.jad.setAttributeExpr(Jdl.REQUIREMENTS, this.getJobRequirements());
            }

            if (this.getJobRank() != null) {
                this.jad.setAttributeExpr(Jdl.RANK, this.getJobRank());
            }

            if (this.getJobDataAccessProtocol() != null) {
                this.jad.addAttribute(Jdl.DATA_ACCESS, this.getJobDataAccessProtocol());
            }

            if (this.getJobStorageIndex() != null) {
                this.jad.addAttribute(Jdl.OD_STORAGE_ELEMENT, this.getJobStorageIndex());
            }

            if (this.getJobFuzzyRank() != null) {
                this.jad.addAttribute(Jdl.FUZZY_RANK, this.getJobFuzzyRank());
            }

            if ((this.jobInputSB != null) && (this.jobInputSB.size() > 0)) {
                for (int i = 0; i < this.jobInputSB.size(); i++) {
                    String entry = (String) jobInputSB.get(i);
                    this.jad.addAttribute(Jdl.INPUTSB, entry);
                }
            }
            if ((this.jobOutputSB != null) && (this.jobOutputSB.size() > 0)) {
                for (int i = 0; i < this.jobOutputSB.size(); i++) {
                    String entry = (String) jobOutputSB.get(i);
                    this.jad.addAttribute(Jdl.OUTPUTSB, entry);
                }
            }

            if (jad.hasAttribute(Jdl.ARGUMENTS)) {
                jad.delAttribute(Jdl.ARGUMENTS);
            }
            jad.setAttribute(Jdl.ARGUMENTS, args);
            jad.toFile(filePath + "/" + fileName);

            //examples of requirements
            //jad.setAttributeExpr(Jdl.REQUIREMENTS,"other.GlueCEUniqueID ==\"pps-ce.egee.cesga.es:2119/blah-pbs-picard\"");
            //jad.setAttributeExpr(Jdl.REQUIREMENTS, "!(RegExp(\"*lxb2039*\",other.GlueCEUniqueID))");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvalidAttributeValueException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String internalBuildCommand() {
        return buildGLiteCommand();
    }

    /**
     * Create jdl file Mandatory attributes : Requirements, rank'
     * 
     * @return Empty string. Command line is not necessary there.
     * @throws Exception
     */
    protected String buildGLiteCommand() {
        String path = filePath;
        buildJdlFile();

        if (jdlRemote) {
            path = remoteFilePath;
        }

        if (!confFileOption) {
            return DEFAULT_COMMAND_PATH + " " + path + File.separator + fileName;
        }

        return DEFAULT_COMMAND_PATH + " --config-vo " + configFile + " " + path + File.separator + fileName;
    }

    /**
     * Check is java arguments are well formatted.
     * 
     * @param args
     *            arguments
     * @return java argments well formatted
     */
    private String checkSyntax(String args) {
        String formatted_args = "";
        String[] splitted_args = args.split("\\s");
        for (int i = 0; i < splitted_args.length; i++) {
            if (!(splitted_args[i].indexOf("=") < 0)) {
                splitted_args[i] = "\"" + splitted_args[i] + "\"";
            }
            formatted_args = formatted_args + " " + splitted_args[i];
        }
        return formatted_args;
    }

    /**
     * ********************************************************************* GETTERS AND SETTERS *
     * **********************************************************************
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "glite_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return this.getJobNodeNumber();
    }

    /**
     * @return String the number of processor requested for the job
     */
    public String getProcessorNumber() {
        return processor;
    }

    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return Returns the filePath.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath
     *            The filePath to set.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return Returns the command_path.
     */
    public String getCommand_path() {
        return command_path;
    }

    /**
     * @param command_path
     *            The command_path to set.
     */
    public void setCommand_path(String command_path) {
        this.command_path = command_path;
    }

    /**
     * @return Returns the jad.
     */
    public GLiteJobAd getJad() {
        return jad;
    }

    /**
     * @param attrName
     *            attributes to add to the GliteJobAd object
     * @param attrValue
     *            value of the atributes
     * @throws InvalidAttributeValueException
     * @throws IllegalArgumentException
     */
    public void addAtt(String attrName, Ad attrValue) throws Exception {
        jad.addAttribute(attrName, attrValue);
    }

    /**
     * @param attrName
     *            attributes to add to the GliteJobAd object
     * @param attrValue
     *            value of the added attrName
     * @throws InvalidAttributeValueException
     * @throws IllegalArgumentException
     */
    public void addAtt(String attrName, int attrValue) throws Exception {
        jad.addAttribute(attrName, attrValue);
    }

    /**
     * @param attrName
     *            attributes to add to the GliteJobAd object
     * @param attrValue
     *            value of the added attrName
     * @throws InvalidAttributeValueException
     * @throws IllegalArgumentException
     */
    public void addAtt(String attrName, double attrValue) throws Exception {
        jad.addAttribute(attrName, attrValue);
    }

    /**
     * @param attrName
     *            attributes to add to the GliteJobAd object
     * @param attrValue
     *            value of the added attrName
     * @throws InvalidAttributeValueException
     * @throws IllegalArgumentException
     */
    public void addAtt(String attrName, String attrValue) throws Exception {
        jad.addAttribute(attrName, attrValue);
    }

    /**
     * @param attrName
     *            attributes to add to the GliteJobAd object
     * @param attrValue
     *            value of the added attrName
     * @throws InvalidAttributeValueException
     * @throws IllegalArgumentException
     */
    public void addAtt(String attrName, boolean attrValue) throws Exception {
        jad.addAttribute(attrName, attrValue);
    }

    /**
     * @return Returns the netServer.
     */
    public String getNetServer() {
        return netServer;
    }

    /**
     * @param netServer
     *            The netServer to set.
     */
    public void setNetServer(String netServer) {
        this.netServer = netServer;
    }

    /**
     * @return Returns the configFile.
     */
    public String getConfigFile() {
        return configFile;
    }

    /**
     * @param configFile
     *            The configFile to set.
     */
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setConfigFileOption(boolean b) {
        confFileOption = b;
    }

    /**
     * @return Returns the jdlRemote.
     */
    public boolean isJdlRemote() {
        return jdlRemote;
    }

    /**
     * @param jdlRemote
     *            The jdlRemote to set.
     */
    public void setJdlRemote(boolean jdlRemote) {
        this.jdlRemote = jdlRemote;
    }

    /**
     * @return Returns the remoteFilePath.
     */
    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    /**
     * @param remoteFilePath
     *            The remoteFilePath to set.
     */
    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

    public FileTransferDefinition getFileTransfertDefinition() {
        FileTransferDefinition ft = new FileTransferDefinition("gliteProcess");
        ft.addFile(filePath + "/" + fileName, remoteFilePath + "/" + fileName);
        return ft;
    }

    /**
     * 
     * @return number of desidered CPUs (just useful if jobType = mpich)
     */
    public int getJobNodeNumber() {
        return jobNodeNumber;
    }

    /**
     * 
     * @param jobNodeNumber
     *            number of desidered CPUs (just useful if jobType = mpich)
     */
    public void setJobNodeNumber(int jobNodeNumber) {
        this.jobNodeNumber = jobNodeNumber;
    }

    /**
     * @return type (so far,just "Job" is supported)
     */
    public String getJobType() {
        return jobType;
    }

    /**
     * @param jobType
     *            type (so far,just "Job" is supported)
     */
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    /**
     * 
     * @return jobtype (so far, just "normal" and "mpich" are supported)
     */
    public String getJobJobType() {
        return jobJobType;
    }

    /**
     * 
     * @param jobJobType
     *            jobtype (so far, just "normal" and "mpich" are supported)
     */
    public void setJobJobType(String jobJobType) {
        this.jobJobType = jobJobType;
    }

    /**
     * @return Executable command (usually absolute java command)
     */
    public String getJobExecutable() {
        return jobExecutable;
    }

    /**
     * @param jobExecutable
     *            Executable command (usually absolute java command)
     */
    public void setJobExecutable(String jobExecutable) {
        this.jobExecutable = jobExecutable;
    }

    /**
     * @return output filename (must also figure in the OutputSandbox to be usefull)
     */
    public String getJobStdOutput() {
        return jobStdOutput;
    }

    /**
     * @param jobStdOutput
     *            output filename (must also figure in the OutputSandbox to be usefull)
     */
    public void setJobStdOutput(String jobStdOutput) {
        this.jobStdOutput = jobStdOutput;
    }

    /**
     * @return input filename
     */
    public String getJobStdInput() {
        return jobStdInput;
    }

    /**
     * @param jobStdInput
     *            input filename
     */
    public void setJobStdInput(String jobStdInput) {
        this.jobStdInput = jobStdInput;
    }

    /**
     * @return stderr filename (must also figure in the OutputSandbox to be usefull)
     */
    public String getJobStdError() {
        return jobStdError;
    }

    /**
     * @param jobStdError
     *            stderr filename (must also figure in the OutputSandbox to be usefull)
     */
    public void setJobStdError(String jobStdError) {
        this.jobStdError = jobStdError;
    }

    /**
     * @return output se (URL of the Storage Element where the user wants to store the output data)
     */
    public String getJobOutput_se() {
        return jobOutput_se;
    }

    /**
     * @param jobOutput_se
     *            output se (URL of the Storage Element where the user wants to store the output
     *            data).
     */
    public void setJobOutput_se(String jobOutput_se) {
        this.jobOutput_se = jobOutput_se;
    }

    /**
     * @return Virtual Organization name
     */
    public String getJobVO() {
        return jobVO;
    }

    /**
     * @param jobVO
     *            Virtual Organization name
     */
    public void setJobVO(String jobVO) {
        this.jobVO = jobVO;
    }

    /**
     * @return maximum number of deep job re-submissions to be done in case of failure due to some
     *         grid component (i.e. not to the job itself).
     */
    public String getJobRetryCount() {
        return jobRetryCount;
    }

    /**
     * @param jobRetryCount
     *            maximum number of deep job re-submissions to be done in case of failure due to
     *            some grid component (i.e. not to the job itself).
     */
    public void setJobRetryCount(String jobRetryCount) {
        this.jobRetryCount = jobRetryCount;
    }

    /**
     * @return hostname of a MyProxy server where the user has registered her/his long-term proxy
     *         certificate.
     */
    public String getJobMyProxyServer() {
        return jobMyProxyServer;
    }

    /**
     * @param jobMyProxyServer
     *            hostname of a MyProxy server where the user has registered her/his long-term proxy
     *            certificate.
     */
    public void setJobMyProxyServer(String jobMyProxyServer) {
        this.jobMyProxyServer = jobMyProxyServer;
    }

    /**
     * @return string or list of strings representing the protocol or the list of protocols that the
     *         application is able to "speak" for accessing files listed in InputData on a given SE.
     */
    public String getJobDataAccessProtocol() {
        return jobDataAccessProtocol;
    }

    /**
     * @param jobDataAccessProtocol
     *            string or list of strings representing the protocol or the list of protocols that
     *            the application is able to "speak" for accessing files listed in InputData on a
     *            given SE.
     */
    public void setJobDataAccessProtocol(String jobDataAccessProtocol) {
        this.jobDataAccessProtocol = jobDataAccessProtocol;
    }

    /**
     * @return attribute kept for backward compatibility and will be soon deprecated. Use
     *         DataRequirements attribute 3.14 instead.
     */
    public String getJobStorageIndex() {
        return jobStorageIndex;
    }

    /**
     * @param jobStorageIndex
     *            attribute kept for backward compatibility and will be soon deprecated. Use
     *            DataRequirements attribute 3.14 instead.
     */
    public void setJobStorageIndex(String jobStorageIndex) {
        this.jobStorageIndex = jobStorageIndex;
    }

    /**
     * @return list of string representing environment settings that have to be performed on the
     *         execution machine and are needed by the job to run properly.
     */
    public String getJobEnvironment() {
        return jobEnvironment;
    }

    /**
     * @param jobEnvironment
     *            list of string representing environment settings that have to be performed on the
     *            execution machine and are needed by the job to run properly.
     */
    public void setJobEnvironment(String jobEnvironment) {
        this.jobEnvironment = jobEnvironment;
    }

    /**
     * @return list of string representing environment settings that have to be performed on the
     *         execution machine and are needed by the job to run properly.
     */
    public String getJobRequirements() {
        return jobRequirements;
    }

    /**
     * @param jobRequirements
     *            Boolean ClassAd expression that uses C-like operators. It represents job
     *            requirements on resources. The Requirements expression can contain attributes that
     *            describe the
     * 
     */
    public void setJobRequirements(String jobRequirements) {
        this.jobRequirements = jobRequirements;
    }

    /**
     * @return ClassAd Floating-Point expression that states how to rank CEs that have already met
     *         the Requirements expression.
     */
    public String getJobRank() {
        return jobRank;
    }

    /**
     * @param jobRank
     *            ClassAd Floating-Point expression that states how to rank CEs that have already
     *            met the Requirements expression.
     */
    public void setJobRank(String jobRank) {
        this.jobRank = jobRank;
    }

    /**
     * @return a Boolean (true/false) attribute that enables fuzzyness in the ranking computation.
     */
    public String getJobFuzzyRank() {
        return jobFuzzyRank;
    }

    /**
     * @param jobFuzzyRank
     *            a Boolean (true/false) attribute that enables fuzzyness in the ranking
     *            computation.
     */
    public void setJobFuzzyRank(String jobFuzzyRank) {
        this.jobFuzzyRank = jobFuzzyRank;
    }

    /**
     * @param entry
     *            string representing a file that will be in the gLite InputSandbox
     * @return true if sucsessfully added, false if not (does not asses file properties)
     */
    public boolean addInputSBEntry(String entry) {
        if (jobInputSB == null) {
            jobInputSB = new LinkedList();
        }
        return jobInputSB.add(entry);
    }

    /**
     * @param entry
     *            string representing a file that will be in the gLite OutputSandbox
     * @return job fails in the case file does not outputed during job
     */
    public boolean addOutputSBEntry(String entry) {
        if (jobOutputSB == null) {
            jobOutputSB = new LinkedList();
        }
        return jobOutputSB.add(entry);
    }

    /**
     * @return arguments to the jobExecutable
     */
    public String getJobArgument() {
        return jobArgument;
    }

    /**
     * @param jobArgument
     *            arguments to the jobExecutable
     */
    public void setJobArgument(String jobArgument) {
        this.jobArgument = jobArgument;
    }

    /***********************************************************************************************
     * END OF GETTERS AND SETTERS *
     **********************************************************************************************/
}
