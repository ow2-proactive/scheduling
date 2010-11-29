function out = subsasgn(this, S, A)
%S.type
%S.subs
if isempty(this)
    this = PATask;
end
if isempty(S) && strcmp(class(A),'PATask')
    this = PATask(A.Func,A.Params,A.Description,A.InputFiles,A.OutputFiles,A.Compose,A.SelectionScript);
end

if length(S) == 1
    switch S.type
        case '.'
            checkValidity(S.subs,A);
            out = builtin('subsasgn',this,S,A);
        case '()'
            if strcmp(class(A),'PATask')                
                if length(S.subs) == 1
                    il = S.subs{1};
                    if isscalar(A)
                        
                        for i=1:length(S.subs{1})
                            this(il(i))=PATask(A.Func,A.Params,A.Description,A.InputFiles,A.OutputFiles,A.Compose,A.SelectionScript);
                        end
                    else
                        for i=1:length(S.subs{1})
                            this(il(i))=PATask(A(i).Func,A(i).Params,A(i).Description,A(i).InputFiles,A(i).OutputFiles,A(i).Compose,A(i).SelectionScript);
                        end
                    end
                elseif length(S.subs) == 2
                    il = S.subs{1};
                    jl = S.subs{2};
                    if size(A,1) == 1 && size(A,2) == 1
                        for i=1:length(il)
                            for j=1:length(jl)
                                this(il(i),jl(j))=PATask(A.Func,A.Params,A.Description,A.InputFiles,A.OutputFiles,A.Compose,A.SelectionScript);
                            end
                        end
                    else                        
                        for i=1:length(il)
                            for j=1:length(jl)                              
                                this(il(i),jl(j))=PATask(A(i,j).Func,A(i,j).Params,A(i,j).Description,A(i,j).InputFiles,A(i,j).OutputFiles,A(i,j).Compose,A(i,j).SelectionScript);
                            end
                        end
                    end
                else
                    error('PATask:subsasgn',...
                        'arrays with more than 2 dimensions not supported')
                end
                out=this;
                %out(S.subs{:})=PATask(A.Func,A.Params,A.Description,A.InputFiles,A.OutputFiles);
            else
                error('PATask:subsasgn',...
                    'Not a supported subscripted assignment')
            end
        case '{}'
            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')

    end
elseif length(S) == 2
    switch S(1).type
        case '.'
            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')
        case '()'
            if strcmp(class(this),'PATask')
                newS=substruct('()',S(1).subs);
                subList = builtin('subsref',this,newS);
                %substr=num2str(S(1).subs{1});
                %for k=2:length(S(1).subs)
                % substr=[substr ','  num2str(S(1).subs{k})];
                %end

                %eval(['subList=this(' substr ');']);
            else
                error('PATask:subsasgn',...
                    'Not a supported subscripted assignment')
            end
        case '{}'
            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')

    end

    switch S(2).type
        case '.'
            checkValidity(S(2).subs,A);
            [n,m] = size(subList);
            for i=1:n
                for j=1:m
                    if strcmp(S(2).subs, 'Params') && ~iscell(A)
                        subList(i,j) = builtin('subsasgn',subList(i,j),S(2),{A});
                    else
                        subList(i,j) = builtin('subsasgn',subList(i,j),S(2),A);
                    end
                end
            end
        case '()'

            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')

        case '{}'
            error('PATask:subsasgn',...
                'Not a supported subscripted assignment')

    end
    builtin('subsasgn',this,newS,subList);
    %eval(['this(' substr ')=subList;']);
    out=this;


else
    error('PATask:subsasgn',...
        'Not a supported subscripted assignment')
end





