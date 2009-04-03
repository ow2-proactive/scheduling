%   PAeval() - evaluates a matlab function on a remote ressource
%              This is a convenient method, it does the same as PAsolve with a singleton list
%
%   Usage:
%       >> result = PAeval(function, arg[, debug]);
%
%   Inputs:
%       function - a handle to a Matlab or user-defined function
%       arg -  the function parameter it can be a numeric or char array, a cell or a struct
%              Functions, or other Matlab user-defined classes are not supported.
%       debug - '-debug' if the computation needs to be run in debug mode
%
%   Ouputs:
%       result - The remote call result
%
%   Example: res = PAeval(@factorial, 5)
%
%/*
% * ################################################################
% *
% * ProActive: The Java(TM) library for Parallel, Distributed,
% *            Concurrent computing with Security and Mobility
% *
% * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
% * Contact: proactive@ow2.org
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
function res = PAeval(varargin)

if (nargin <2) || (nargin > 3)
    error(['wrong number of arguments: ' int2str(nargin)]);
end

func = varargin{1};
arg = varargin{2};
if (nargin == 3) && (strcmp(varargin{3},'-debug') == 1 || strcmp(varargin{3},'-d') == 1)
    debug = true;
else
    debug = false;
end

if debug
  res = PAsolve(func, {arg}, '-debug');
else
  res = PAsolve(func, {arg});
end
