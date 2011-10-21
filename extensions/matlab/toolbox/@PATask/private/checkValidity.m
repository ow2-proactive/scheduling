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
function varargout=checkValidity(attrib, val)
switch attrib    
    case 'Func'
        if ~(isa(val, 'function_handle') || (isnumeric(val) && isempty(val)))
            error('Func must be a function handle');
        end
    case 'Description'
        if ~(ischar(val) || (isnumeric(val) && isempty(val)))
            error('Description must be a string');
        end
    case 'InputFiles'
        if ~(iscellstr(val) || (isnumeric(val) && isempty(val)))
            error('InputFiles must be a cell array of strings');
        end
    case 'OutputFiles'
        if ~(iscellstr(val) || (isnumeric(val) && isempty(val)))
            error('OutputFiles must be a cell array of strings');
        end    
    case 'Compose'
        if ~(islogical(val) || (isnumeric(val) && isempty(val)))
            error('Compose must be logical');
        end  
    case 'SelectionScript'
        if ~(ischar(val) || (isnumeric(val) && isempty(val)))
            error('SelectionScript must be a string');
        end
    case 'NbNodes'
        if ~isnumeric(val) || ~isscalar(val) || val <= 1 || round(val) ~= val 
            error('NbNodes must be a positive integer');
        end
    case 'Topology'
        values = {'arbitrary', 'bestProximity', 'thresholdProximity', 'singleHost', 'singleHostExclusive', 'multipleHostsExclusive', 'differentHostsExclusive' };
        if ~((ischar(val) && ismember(val, values)) || (isnumeric(val) && isempty(val)))
            error('Wrong Topology value');
        end
    case 'ThresholdProximity'
        if ~isnumeric(val) || ~isscalar(val) || val <= 0 || round(val) ~= val
            error('ThresholdProximity must be a positive integer');
        end
end
