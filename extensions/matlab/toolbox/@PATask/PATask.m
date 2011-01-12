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
function varargout = PATask(varargin)
persistent lastid
if exist('lastid','var') == 1 && isa(lastid,'int64')
    lastid=int64(double(lastid)+1);
else
    lastid = int64(0);
end
if nargin > 0
    checkValidity('Func',varargin{1});
    this.Func = varargin{1};
    if isa(this.Func, 'function_handle')
        try 
            arginins = signature(this.Func);
            j=1;
            argins = {};
            for i=1:length(arginins)
                if ~isnumeric(arginins{i})
                    argins{j} = arginins{i};
                    j=j+1;
                end
            end
        catch
            argins = {};
        end
    else
        argins = {};
    end
    if iscell(varargin{2})
        this.Params = varargin{2};
    elseif isnumeric(varargin{2}) && isempty(varargin{2})
        this.Params = {};
    else
        this.Params = {varargin{2}};
    end
    %checkValidity('Out',varargin{3});
    %this.Out = varargin{3};
    checkValidity('Description',varargin{3});
    this.Description = varargin{3};
    checkValidity('InputFiles',varargin{4});
    this.InputFiles = varargin{4};
    checkValidity('OutputFiles',varargin{5});
    this.OutputFiles = varargin{5};
    checkValidity('Compose',varargin{6});
    this.Compose=varargin{6};
    checkValidity('SelectionScript', varargin{7})
    this.SelectionScript = varargin{7};
    if ischar(this.SelectionScript)
        this.SelectionScript = ['file:' strrep(this.SelectionScript, '\', '/')];
    end
    if ~this.Compose
        % Check parameters assigment
        np = length(this.Params);
        if ~ismember('varargin', argins)
            if length(argins) ~= np
                warning('Number of parameters differs from function''s number of inputs');
            end
        end
    end
    this.id = lastid;
else
    this.Func = [];
    this.Params = {};
    this.Description = [];
    this.InputFiles = [];
    this.OutputFiles = [];
    this.Compose=false;
    this.SelectionScript = [];
    this.id = lastid;
end
for i=1:nargout
    varargout{i}=[];
    varargout{i} = class(this,'PATask');
end

