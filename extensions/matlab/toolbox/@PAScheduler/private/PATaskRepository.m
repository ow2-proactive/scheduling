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
function varargout=PATaskRepository(varargin)
mlock
persistent jobinforepository
persistent taskinforepository
persistent taskstoreceive
persistent jobuncomplete
if exist('jobinforepository','var') == 1 && isa(jobinforepository,'java.util.HashMap')
else
    jobinforepository = java.util.HashMap();
    taskstoreceive = java.util.HashMap();
end

if nargin == 2
    if ischar(varargin{1}) && isa(varargin{2},'org.ow2.proactive.scheduler.ext.matsci.client.common.data.MatSciJobPermanentInfo')
        %% registering job
        key = char(varargin{1});
        jkey = ['j' char(varargin{1})];
        jinfo = varargin{2};
        jobinforepository.put(key, jinfo);
        rts = java.util.HashSet(jinfo.getFinalTaskNames());
        taskstoreceive.put(key, rts);
        jobuncomplete = [jobuncomplete {key}];
    elseif ischar(varargin{1}) && ischar(varargin{2})
        switch varargin{2}
            case 'jobinfo'
                %% returning jobinfo
                key = char(varargin{1});
                varargout{1}=jobinforepository.get(key);
            case 'alltasks'
                %% returning all tasks
                key = char(varargin{1});
                jinfo = jobinforepository.get(key);                
                ts = jinfo.getFinalTaskNames();
                it = ts.iterator();
                answer = {};
                while it.hasNext()
                    answer = [answer {char(it.next())}];
                end
                varargout{1} = answer;
            case 'toreceive'
                %% returning tasks to receive
                 key = char(varargin{1});
                ts = taskstoreceive.get(key);
                it = ts.iterator();
                answer = {};
                while it.hasNext()
                    answer = [answer {char(it.next())}];
                end
                varargout{1} = answer;
            otherwise
                error(['PATaskRepository::Wrong argument : ' varargin{2}])
               
        end
    else
        error(['PATaskRepository::Wrong arguments : ' class(varargin{1}) class(varargin{2})]);
    end
elseif nargin == 3
    if ischar(varargin{1}) && ischar(varargin{2}) && isstruct(varargin{3})
        %% registering taskinfo
        jkey = ['j' char(varargin{1})];
        tkey = ['t' char(varargin{2})];
        taskinforepository.(jkey).(tkey) = varargin{3};
    elseif ischar(varargin{1}) && ischar(varargin{2}) && ischar(varargin{3})
        switch varargin{3}
            case 'received'
                %% receiving task
                jjkey = char(varargin{1});
                jtkey = char(varargin{2});
                jkey = ['j' char(varargin{1})];
                
                ts = taskstoreceive.get(jjkey);
                ts.remove(jtkey);
                if ts.size() == 0
                    jobuncomplete = setdiff(jobuncomplete, jjkey);
                    jinfo = jobinforepository.get(jjkey); 
                    fts = jinfo.getFinalTaskNames();
                    it = fts.iterator(); 
                    sched = PAScheduler;
                    while it.hasNext()
                        tkey = ['t' char(it.next())];
                        tinfo = taskinforepository.(jkey).(tkey);
                        sched.PAaddFileToClean(jjkey, tinfo.outFile);
                    end
                end
            case 'taskinfo'
                %% returning taskinfo
                jkey = ['j' char(varargin{1})];
                tkey = ['t' char(varargin{2})];
                varargout{1} = taskinforepository.(jkey).(tkey);
            otherwise
                error(['PATaskRepository::Wrong argument : ' varargin{3}])
        end
    else
        error(['PATaskRepository::Wrong arguments : ' class(varargin{1}) class(varargin{2}) class(varargin{3})]);
    end
elseif nargin == 1
    if ischar(varargin{1})
        switch varargin{1}
            case 'alljobs'
                ks = jobinforepository.keySet();
                it = ks.iterator();
                answer = {};
                while it.hasNext()
                    answer = [answer {char(it.next())}];
                end
                varargout{1}=answer;
            case 'uncomplete'
                %% returning all non complete jobs 
                varargout{1}=jobuncomplete;    
            case 'save'
                %% saving all info
                opt = PAoptions();
                save(opt.DisconnectedModeFile, 'jobinforepository', 'taskinforepository', 'taskstoreceive', 'jobuncomplete');
            case 'load'
                opt = PAoptions();
                if exist(opt.DisconnectedModeFile,'file')
                    load(opt.DisconnectedModeFile, 'jobinforepository', 'taskinforepository', 'taskstoreceive', 'jobuncomplete');
                else
                    error(['can''t find ' opt.DisconnectedModeFile]);
                end
            otherwise
                error(['PATaskRepository::Wrong argument : ' varargin{1}]);
        end
    else
        error(['PATaskRepository::Wrong arguments : ' class(varargin{1})]);
    end   
else
    error('Wrong number of arguments');
end