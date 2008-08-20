%   parse_token_output() - parses Token results received from a distance engines in a java Ptolemy Token object
%
%   Usage:
%       >> out = parse_token_output(in);
%
%   Inputs:
%       in - java Token object received
%
%   Ouputs:
%       out - Matlab variable corresponding 
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
function out = parse_token_output(in)
if ~isa(in, 'ptolemy.data.Token')
    error('in parameter should be a ptolemy.data.Token');
end
if isa(in, 'ptolemy.data.StringToken') || isa(in, 'ptolemy.data.MatrixToken') || isa(in, 'ptolemy.data.ComplexToken') || isa(in, 'ptolemy.data.ComplexMatrixToken') || isa(in, 'ptolemy.data.ScalarToken')
    out = eval(in.toString());
end
if isa(in, 'ptolemy.data.RecordToken') 
    out = struct;
    labelSet = in.labelSet();
    % We iterate on the tokens
    iterator = labelSet.iterator();
    while iterator.hasNext()
        label = iterator.next();
        token = in.get(label);
        % We recurse on the token values
        out = setfield(out, label, parse_token_output(token));
    end
end
if isa(in, 'ptolemy.data.ArrayToken')
    size = in.length();
    out = cell(1,size);
    for i=1:in.length()
        out{i} = parse_token_output(in.getElement(i-1));
    end
end