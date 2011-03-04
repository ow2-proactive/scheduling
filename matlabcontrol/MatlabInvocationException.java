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

/**
 * Exception that represents a failure to invoke a method on the MATLAB session.
 * This will most likely occur if the MATLAB session being controlled is no
 * longer open, or MATLAB experiences an internal error.
 *
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
public class MatlabInvocationException extends Exception {
    private static final long serialVersionUID = -1567632755416541619L;

    //Messages
    static final String INTERRUPTED_MSG = "Method could not be completed because the thread was interrupted before MATLAB returned",
            PROXY_NOT_CONNECTED_MSG = "This proxy is no longer connected to MATLAB",
            CONTROLLER_NO_CONNECTION_MSG = "There is no connection to MATLAB",
            UNKNOWN_REMOTE_REASON_MSG = "Method could not be invoked for an unknown reason",
            UNMARSHALLING_MSG = "Object attempting to be returned cannot be sent across Java Virtual Machines",
            INTERNAL_EXCEPTION_MSG = "Method could not return a value because of an internal MATLAB exception",
            MAIN_THREAD_MSG = "This method cannot be invoked on the main MATLAB thread",
            NON_MAIN_THREAD_MSG = "This method must be invoked on the main MATLAB thread";

    MatlabInvocationException(String msg) {
        super(msg);
    }

    MatlabInvocationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}