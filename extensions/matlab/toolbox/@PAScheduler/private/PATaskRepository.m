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
    if ischar(varargin{1}) && isa(varargin{2},'org.ow2.proactive.scheduler.ext.matsci.client.MatSciJobPermanentInfo')
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