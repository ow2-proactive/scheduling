function varargout = subsref(this,S)

% switch S.type
% case '.'
%    switch S.subs
%        case 'val'
%        B = PAwaitFor(R);
%        case 'logs'
%         if R.logsPrinted.get()
%             B=char(R.logs.toString());
%         else
%            PAwaitFor(R);
%            B=char(R.logs.toString());
%         end
%    otherwise
%        error([S.subs ,' is not a valid property'])
%    end
% end

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
                                val{i+1}((l-1)*sz(2)+m) = 0;
                            catch ME
                                val{i+1}((l-1)*sz(2)+m) = Z.iserror.get();
                            end
                        end
                    end
                case 'id'
                    sz=size(val{i});
                    for l=1:sz(1)
                        for m=1:sz(2)
                            Z=val{i}(l,m);                                                            
                            val{i+1}((l-1)*sz(2)+m) = Z.id;                            
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
%if nargout <= 1

%    varargout={val{i+1}};
%else
%    varargout={val{i+1}{:}};
%end
%else
%    varargout={val{i+1}{:}};
%end