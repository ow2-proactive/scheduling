package matlabcontrol;

/*
 * Copyright (c) 2010, Joshua Kaplan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of matlabcontrol nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Methods that can be called to control MATLAB except for
 * {@link #checkConnection()}.
 * <p/>
 * All of these methods throw RemoteException. RemoteException will be thrown
 * if something occurs to disrupt the communication between this JVM and the
 * one MATLAB is running in. For instance, closing MATLAB will terminate its
 * JVM and then all method calls on this proxy will throw exceptions.
 * <p/>
 * Additionally, many of these methods throw InterruptedException. This occurs
 * if while waiting for MATLAB to return from a method called on it, the thread
 * waiting is interrupted.
 * <p/>
 * For descriptions of what these methods do see the corresponding methods in
 * {@link RemoteMatlabProxy}.
 *
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
interface MatlabInternalProxy extends Remote {
    public void exit(boolean immediate) throws RemoteException, MatlabInvocationException;

    public void setVariable(String variableName, Object value) throws RemoteException, MatlabInvocationException;

    public Object getVariable(String variableName) throws RemoteException, MatlabInvocationException;

    public String eval(String command) throws RemoteException, MatlabInvocationException;

    public void feval(String command, Object[] args) throws RemoteException, MatlabInvocationException;

    public Object returningFeval(String command, Object[] args) throws RemoteException, MatlabInvocationException;

    public Object returningFeval(String command, Object[] args, int returnCount) throws RemoteException, MatlabInvocationException;

    public Object returningEval(String command, int returnCount) throws RemoteException, MatlabInvocationException;

    public void setEchoEval(boolean echo) throws RemoteException, MatlabInvocationException;

    public void waitReady() throws RemoteException;

    /**
     * This method does nothing. It is used internally to check if a connection
     * is still active.
     *
     * @throws RemoteException
     */
    public void checkConnection() throws RemoteException;
}