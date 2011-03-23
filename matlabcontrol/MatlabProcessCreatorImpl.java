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

import java.io.IOException;

/**
 * The standard implementation of the MATLAB process creator that can
 * spawn a MATLAB process 
 * 
 * @author The ProActive Team
 */
public class MatlabProcessCreatorImpl implements MatlabProcessCreator {
	
    /**
     * Specified location of MATLAB executable. If none is ever provided then
     * an OS specific value is used.
     */
    private final String _matlabLocation;
    
    /**
     * Creates a new instance of this class. 
     * 
     * @param matlabLocation the path to the MATLAB executable
     * @throws MatlabConnectionException
     */
    public MatlabProcessCreatorImpl(String matlabLocation) {
        //Store location/alias of the MATLAB executable
        _matlabLocation = matlabLocation;
    }

    /**
     * Creates a MATLAB process using the {@link java.lang.ProcessBuilder} from the standard API.
     * 
     * @param runArg the argument to be passed to MATLAB using the -r option 
     */
	public void createMatlabProcess(String runArg) throws MatlabConnectionException {
        //Attempt to run MATLAB
        try {            
            Runtime.getRuntime().exec(new String[]{_matlabLocation, "-desktop", "-r", runArg});
        } catch (IOException e) {
            throw new MatlabConnectionException("Could not launch MATLAB. Used location/alias: " + _matlabLocation, e);
        }
    }
	
    /**
     * Returns the location or alias of the MATLAB program. If no location or
     * alias was assigned when constructing this factory then the value
     * returned will be the default which differs depending on the operating
     * system this code is executing on.
     *
     * @return MATLAB location
     */
    public String getMatlabLocation() {
        return _matlabLocation;
    }
}