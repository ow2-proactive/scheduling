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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Passes method calls off to the {@link JMIWrapper}. This proxy is necessary because
 * fields that {@link JMIWrapper} uses cannot be marshalled, as is required by RMI.
 * <p/>
 * These methods are documented in {@link JMIWrapper}.
 *
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
class MatlabInternalProxyImpl extends UnicastRemoteObject implements MatlabInternalProxy {
    private static final long serialVersionUID = 1L;

    private final JMIWrapper _wrapper;

    public MatlabInternalProxyImpl(JMIWrapper wrapper) throws RemoteException {
        _wrapper = wrapper;
    }

    public void setVariable(String variableName, Object value) throws RemoteException, MatlabInvocationException {
        _wrapper.setVariable(variableName, value);
    }

    public Object getVariable(String variableName) throws RemoteException, MatlabInvocationException {
        return _wrapper.getVariable(variableName);
    }

    public void exit() throws RemoteException, MatlabInvocationException {
        _wrapper.exit();
    }

    public Object returningFeval(String command, Object[] args) throws RemoteException, MatlabInvocationException {
        return _wrapper.returningFeval(command, args);
    }

    public Object returningFeval(String command, Object[] args, int returnCount) throws RemoteException, MatlabInvocationException {
        return _wrapper.returningFeval(command, args, returnCount);
    }

    public Object returningEval(String command, int returnCount) throws RemoteException, MatlabInvocationException {
        return _wrapper.returningEval(command, returnCount);
    }

    public void eval(String command) throws RemoteException, MatlabInvocationException {
        _wrapper.eval(command);
    }

    public String eval2(String command) throws RemoteException, MatlabInvocationException {
        return _wrapper.eval2(command);
    }

    public Object evalStreamOutput(final String command) throws RemoteException, MatlabInvocationException {
        return _wrapper.evalStreamOutput(command);
    }

    public void feval(String command, Object[] args) throws RemoteException, MatlabInvocationException {
        _wrapper.feval(command, args);
    }

    public void setEchoEval(boolean echo) throws RemoteException {
        _wrapper.setEchoEval(echo);
    }

    public void checkConnection() throws RemoteException {
    }

    public void waitReady() throws RemoteException {
        _wrapper.waitReady();
    }
}