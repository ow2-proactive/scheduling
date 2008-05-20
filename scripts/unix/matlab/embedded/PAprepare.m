%   PAprepare() - prepares classpath for ProActive
%
%   Usage:
%       >> PAprepare(proactive);  // prepares proactive using the given proactive path
%       >> PAprepare();           // prepares proactive by asking the user to select the proactive path
%
%   Inputs:
%       proactive - path to proactive
%
%   Ouputs: none
%
%/*
% * ################################################################
% *
% * ProActive: The Java(TM) library for Parallel, Distributed,
% *            Concurrent computing with Security and Mobility
% *
% * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
% * Contact: proactive@objectweb.org
% *
% * This library is free software; you can redistribute it and/or
% * modify it under the terms of the GNU General Public License
% * as published by the Free Software Foundation; either version
% * 2 of the License, or any later version.
% *
% * This library is distributed in the hope that it will be useful,
% * but WITHOUT ANY WARRANTY; without even the implied warranty of
% * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
% * General Public License for more details.
% *
% * You should have received a copy of the GNU General Public License
% * along with this library; if not, write to the Free Software
% * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
% * USA
% *
% *  Initial developer(s):               The ProActive Team
% *                        http://proactive.inria.fr/team_members.htm
% *  Contributor(s):
% *
% * ################################################################
% */
function varargout = PAprepare(varargin)
if nargin == 1
    proactive = uigetdir;
else
    proactive = varargin{1};
end
% Setting the English locale, mandatory for ptolemy
java.util.Locale.setDefault(java.util.Locale.ENGLISH);
% setting proactive root
if (proactive(length(proactive)) ~= filesep)
    proactive = strcat(proactive, filesep);
end
% Adding log4j.configuration file (if present)
if fopen(strcat(proactive,'compile/proactive-log4j')) ~= -1
    java.lang.System.setProperty('log4j.configuration',strcat('file://',proactive,'compile/proactive-log4j'));
end

% Adding class path entries
if fopen(strcat(proactive,'ProActive.jar')) ~= -1
    javaaddpath(strcat(proactive,'ProActive.jar'));
else 
    javaaddpath(strcat(proactive,'classes/Core'));
    javaaddpath(strcat(proactive,'classes/Extensions'));
    javaaddpath(strcat(proactive,'classes/Examples'));
end
javaaddpath(strcat(proactive,'lib/bouncycastle.jar'));
javaaddpath(strcat(proactive,'lib/fractal.jar'));
javaaddpath(strcat(proactive,'lib/ganymed-ssh2-build210.jar'));
javaaddpath(strcat(proactive,'lib/javassist.jar'));
javaaddpath(strcat(proactive,'lib/log4j.jar'));
javaaddpath(strcat(proactive,'lib/commons-cli-1.0.jar'));
javaaddpath(strcat(proactive,'lib/ptolemy.jar'));
javaaddpath(strcat(proactive,'lib/7.5/bin/glnx86/'));
% unuseful (integrated in Matlab)
%javaaddpath(strcat(proactive,'lib/xercesImpl.jar'));
