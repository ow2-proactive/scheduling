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
function varargout = PAaddFileToClean(varargin)
mlock
persistent filesperjob
if exist('filesperjob','var') == 1 && isstruct(filesperjob)
else
    filesperjob.a = [];
end
if nargin == 2
    key = ['j' char(varargin{1})];
    if ~isfield(filesperjob, key)
        if iscell(varargin{2})
            filesperjob.(key) = varargin{2};
        else
            filesperjob.(key) = {varargin{2}};
        end
    else
        if iscell(varargin{2})
            filesperjob.(key) = union(filesperjob.(key),varargin{2});
        else
            filesperjob.(key) = union(filesperjob.(key),{varargin{2}});
        end
    end    
elseif nargin == 1
    key = ['j' char(varargin{1})];
    try
        files = filesperjob.(key);
        warning('off')
        for i=1:length(files)
            try
                delete(files{i});
            catch
            end
        end
        warning('on')
    catch
    end
else
    error('Wrong number of arguments');
end