/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.process;

import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;

import java.util.ArrayList;


/**
 * This class contains a list of ExternalProcess processes that have to
 * be executed sequentially
 * @author ProActiveTeam
 * @version 1.0, 01 Dec 2005
 * @since ProActive 3.0
 *
 */
public abstract class AbstractSequentialListProcessDecorator
    implements ExternalProcessDecorator {
    //Array of processes
    protected ArrayList processes;

    // position of the next process to return  
    protected int currentRank = 0;

    public AbstractSequentialListProcessDecorator() {
        processes = new ArrayList();
    }

    /**
     * Add a process to the processes queue
     * @param process
     */
    public void addProcessToList(ExternalProcess process) {
        this.processes.add(process);
    }

    /**
     * Add a process to the processes queue at index rank
     * @param rank
     * @param process
     */
    public void addProcessToList(int rank, ExternalProcess process) {
        this.processes.add(rank, process);
    }

    /**
     * Return the next process to be launched and increase current rank
     * @return process
     */
    public ExternalProcess getFirstProcess() {
        currentRank++;
        return (ExternalProcess) processes.get(0);
    }

    /**
     * Return the next process to be launched and increase current rank
     * @return ExternalProcess
     */
    public ExternalProcess getNextProcess() {
        ExternalProcess res = null;
        if (currentRank < processes.size()) {
            res = (ExternalProcess) processes.get(currentRank);
            currentRank++;
        }
        return res;
    }

    //
    //--------------------------Implements ExternalProcessDecorator----------------------

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcessDecorator#getTargetProcess()
     */
    public ExternalProcess getTargetProcess() {
        return ((ExternalProcessDecorator) processes.get(currentRank)).getTargetProcess();
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcessDecorator#setTargetProcess(org.objectweb.proactive.core.process.ExternalProcess)
     */
    public void setTargetProcess(ExternalProcess targetProcess) {
        ((ExternalProcessDecorator) processes.get(currentRank)).setTargetProcess(targetProcess);
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcessDecorator#getCompositionType()
     */
    public int getCompositionType() {
        return ((ExternalProcess) processes.get(currentRank)).getCompositionType();
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcessDecorator#setCompositionType(int)
     */
    public void setCompositionType(int compositionType) {
        ((ExternalProcessDecorator) processes.get(currentRank)).setCompositionType(compositionType);
    }

    public FileTransferWorkShop getFileTransferWorkShopRetrieve() {

        /* TODO Check if this is the correct place
         * implement this. Then implement it
         */
        return null;
    }

    public FileTransferWorkShop getFileTransferWorkShopDeploy() {

        /* TODO Check if this is the correct place
         * implement this. Then implement it
         */
        return null;
    }

    public void startFileTransfer() {

        /* TODO Check if this is the correct place
         * implement this. Then implement it
         */
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcess#closeStream()
     */
    public void closeStream() {
        ((ExternalProcess) processes.get(currentRank)).closeStream();
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcess#getInputMessageLogger()
     */
    public RemoteProcessMessageLogger getInputMessageLogger() {
        return ((ExternalProcess) processes.get(currentRank)).getInputMessageLogger();
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcess#getErrorMessageLogger()
     */
    public RemoteProcessMessageLogger getErrorMessageLogger() {
        return ((ExternalProcess) processes.get(currentRank)).getErrorMessageLogger();
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcess#getOutputMessageSink()
     */
    public MessageSink getOutputMessageSink() {
        return ((ExternalProcess) processes.get(currentRank)).getOutputMessageSink();
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcess#setInputMessageLogger(org.objectweb.proactive.core.util.RemoteProcessMessageLogger)
     */
    public void setInputMessageLogger(
        RemoteProcessMessageLogger inputMessageLogger) {
        for (int i = 0; i < processes.size(); i++) {
            ((ExternalProcess) processes.get(i)).setInputMessageLogger(inputMessageLogger);
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcess#setErrorMessageLogger(org.objectweb.proactive.core.util.RemoteProcessMessageLogger)
     */
    public void setErrorMessageLogger(
        RemoteProcessMessageLogger errorMessageLogger) {
        for (int i = 0; i < processes.size(); i++) {
            ((ExternalProcess) processes.get(i)).setErrorMessageLogger(errorMessageLogger);
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.ExternalProcess#setOutputMessageSink(org.objectweb.proactive.core.process.MessageSink)
     */
    public void setOutputMessageSink(MessageSink outputMessageSink) {
        for (int i = 0; i < processes.size(); i++) {
            ((ExternalProcess) processes.get(i)).setOutputMessageSink(outputMessageSink);
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getEnvironment()
     */
    public String[] getEnvironment() {
        return ((ExternalProcess) processes.get(currentRank)).getEnvironment();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#setEnvironment(java.lang.String[])
     */
    public void setEnvironment(String[] environment) {
        ((ExternalProcess) processes.get(currentRank)).setEnvironment(environment);
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getUsername()
     */
    public String getUsername() {
        return ((ExternalProcess) processes.get(currentRank)).getUsername();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#setUsername(java.lang.String)
     */
    public void setUsername(String username) {
        ((ExternalProcess) processes.get(currentRank)).setUsername(username);
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getCommand()
     */
    public String getCommand() {
        return null;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "ps";
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return ((ExternalProcessDecorator) processes.get(currentRank)).getNodeNumber();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        return ((ExternalProcessDecorator) processes.get(currentRank)).getFinalProcess();
    }

    public ArrayList getListProcess() {
        return this.processes;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#stopProcess()
     */
    public void stopProcess() {
        ((ExternalProcess) processes.get(currentRank)).stopProcess();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#waitFor()
     */
    public int waitFor() throws InterruptedException {
        int status = 0;
        status = ((ExternalProcess) processes.get(currentRank)).waitFor();
        return status;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#isStarted()
     */
    public boolean isStarted() {
        boolean started = true;
        started = ((ExternalProcess) processes.get(currentRank)).isStarted();
        return started;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#isFinished()
     */
    public boolean isFinished() {
        boolean finished = true;
        finished = ((ExternalProcess) processes.get(currentRank)).isFinished();
        return finished;
    }

    public boolean isSequential() {
        return true;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#setCommandPath(java.lang.String)
     */
    public void setCommandPath(String path) {
        ((ExternalProcess) processes.get(currentRank)).setCommandPath(path);
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getCommandPath()
     */
    public String getCommandPath() {
        return ((ExternalProcess) processes.get(currentRank)).getCommandPath();
    }

    //
    //-----------------protected methods----------------------------------------
    //
    protected abstract ExternalProcess createProcess();

    public int exitValue() throws IllegalThreadStateException {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setStarted(boolean isStarted) {
        // TODO Auto-generated method stub
    }
}
