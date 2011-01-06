function varargout = subsref(this,S)


val{1}=this;
for i=1:length(S)
    s=S(i);
    switch s.type
        case '.'
            switch s.subs
                case 'val'
                    val{i+1} = PAwaitFor(val{i});                    
                case 'iserror'
                    sz=size(val{i});
                    for l=1:sz(1)
                        for m=1:sz(2)
                            Z=val{i}(l,m);
                            try
                                PAwaitFor(Z);
                                if sz(1) == 1 && sz(2) == 1
                                    val{i+1} = 0;
                                else
                                    val{i+1}{(l-1)*sz(2)+m} = 0;
                                end
                            catch ME
                                if sz(1) == 1 && sz(2) == 1
                                    val{i+1} = Z.iserror.get();
                                else
                                    val{i+1}{(l-1)*sz(2)+m} = Z.iserror.get();
                                end                                
                            end
                        end
                    end
                case 'jobid'
                    sz=size(val{i});
                    for l=1:sz(1)
                        for m=1:sz(2)
                            Z=val{i}(l,m);
                            if sz(1) == 1 && sz(2) == 1
                                val{i+1} = Z.jobid;
                            else
                                val{i+1}{(l-1)*sz(2)+m} = Z.jobid;
                            end
                        end
                    end
                case 'logs'
                    sz=size(val{i});
                    for l=1:sz(1)
                        for m=1:sz(2)
                            Z=val{i}(l,m);

                            if ~Z.logsPrinted.get()
                                try
                                    PAwaitFor(Z);
                                catch ME
                                    ME.stack;

                                end
                            end
                            if sz(1) == 1 && sz(2) == 1
                                val{i+1} =char(Z.logs.toString());
                            else
                                val{i+1}{(l-1)*sz(2)+m} =char(Z.logs.toString());
                            end
                        end
                    end
                otherwise
                    error([s.subs ,' is not a valid property'])
            end
        case '()'
            %j=s.subs{1};
            K.type='()';
            K.subs=s.subs;
            val{i+1} = builtin('subsref', val{i}, K);
        otherwise
            error([s.type ,' not supported'])
    end
end
n = numel(val{i});
if n > 1
    %length(varargout) > 1 && nargout <= 1
    if nargout > 1
        varargout = cell(1,n);
        for j = 1 : n
            varargout{j} = val{i+1}{j};
        end
    else
        varargout = { val{i+1} };
    end
else
    varargout = { val{i+1} };
end
