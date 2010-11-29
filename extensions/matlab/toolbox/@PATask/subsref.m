function varargout = subsref(this,S)

val{1}=this;
for i=1:length(S)
    s=S(i);
    switch s.type
        case '.'
            sz=size(val{i});
            for l=1:sz(1)
                for m=1:sz(2)
                    Z=val{i}(l,m);
                    switch s.subs
                        case 'Func'

                            val{i+1}{(l-1)*sz(2)+m} = Z.Func;
                        case 'Params'
                            val{i+1}{(l-1)*sz(2)+m} = Z.Params;
                            %                 l=S.subs{1};
                            %                 j=1;
                            %                 for i=l
                            %                     K.type='()';
                            %                     K.subs={i};
                            %                     PP = builtin('subsref', P, K);
                            %                     varargout{j} = PP.param;
                            %                 end
                        case 'InputFiles'
                            val{i+1}{(l-1)*sz(2)+m} = Z.InputFiles;
                        case 'OutputFiles'
                            val{i+1}{(l-1)*sz(2)+m} = Z.OutputFiles;
                        case 'Description'
                            val{i+1}{(l-1)*sz(2)+m} = Z.Description;
                        case 'Compose'
                            val{i+1}{(l-1)*sz(2)+m} = Z.Compose;
                        case 'SelectionScript'
                            val{i+1}{(l-1)*sz(2)+m} = Z.SelectionScript;
                        otherwise
                            error([s.subs ,' is not a valid property'])
                    end
                end
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
    if iscell(val{i+1})
        varargout = cell(1,n);
        for j = 1 : n
            varargout{j} = val{i+1}{j};
        end
    else
        varargout = { val{i+1} };
    end
else
    if iscell(val{i+1})
        varargout = {val{i+1}{1}};
    else
        varargout = {val{i+1}};
    end
end
