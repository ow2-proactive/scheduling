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
% /*
%   * ################################################################
%   *
%   * ProActive Parallel Suite(TM): The Java(TM) library for
%   *    Parallel, Distributed, Multi-Core Computing for
%   *    Enterprise Grids & Clouds
%   *
%   * Copyright (C) 1997-2011 INRIA/University of
%   *                 Nice-Sophia Antipolis/ActiveEon
%   * Contact: proactive@ow2.org or contact@activeeon.com
%   *
%   * This library is free software; you can redistribute it and/or
%   * modify it under the terms of the GNU Affero General Public License
%   * as published by the Free Software Foundation; version 3 of
%   * the License.
%   *
%   * This library is distributed in the hope that it will be useful,
%   * but WITHOUT ANY WARRANTY; without even the implied warranty of
%   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
%   * Affero General Public License for more details.
%   *
%   * You should have received a copy of the GNU Affero General Public License
%   * along with this library; if not, write to the Free Software
%   * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
%   * USA
%   *
%   * If needed, contact us to obtain a release under GPL Version 2 or 3
%   * or a different license than the AGPL.
%   *
%   *  Initial developer(s):               The ProActive Team
%   *                        http://proactive.inria.fr/team_members.htm
%   *  Contributor(s):
%   *
%   * ################################################################
%   * $$PROACTIVE_INITIAL_DEV$$
%   */
function out = parse_token_output(in)
if ~isa(in, 'ptolemy.data.Token')
    error('in parameter should be a ptolemy.data.Token');
end
if isa(in, 'ptolemy.data.StringToken')
    chr = char(in.toString());
    if chr(1) == '"'
        chr = ['''' chr(2:end-1) ''''];
    end
    out = eval(chr);
end
if isa(in, 'ptolemy.data.MatrixToken') || isa(in, 'ptolemy.data.ComplexToken') || isa(in, 'ptolemy.data.ComplexMatrixToken') || isa(in, 'ptolemy.data.ScalarToken')
    out = eval(char(in.toString()));
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
