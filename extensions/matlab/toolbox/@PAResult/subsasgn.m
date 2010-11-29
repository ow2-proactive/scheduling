function out = subsasgn(this, S, A)
%S.type
%S.subs

if (isempty(this) && isempty(S)) && strcmp(class(A),'PAResult')
    this = A;
end

if length(S) == 1
    switch S.type
        case '()'
            if strcmp(class(A),'PAResult')
                if length(S.subs) == 1
                    il = S.subs{1};
                    if isscalar(A)
                        for i=1:length(il)
                            if isnumeric(this)
                                this=A;
                            else
                                this(il(i))=A;
                            end
                        end
                    else
                        for i=1:length(il)
                            if isnumeric(this)
                                this=A(i);
                            else
                                this(il(i))=A(i);
                            end
                        end
                    end
                elseif length(S.subs) == 2
                    il = S.subs{1};
                    jl = S.subs{2};
                    if size(A,1) == 1 && size(A,2) == 1

                        for i=1:length(il)
                            for j=1:length(jl)
                                this(il(i),jl(j))=A;
                            end
                        end
                    else
                        for i=1:size(A,1)
                            for j=1:size(A,2)
                                this(il(i),jl(j))=A(i,j);
                            end
                        end
                    end
                else
                    error('PAResult:subsasgn',...
                        'arrays with more than 2 dimensions not supported')
                end
                out=this;
                %out(S.subs{:})=PATask(A.Func,A.Params,A.Description,A.InputFiles,A.OutputFiles);
            else
                error('PAResult:subsasgn',...
                    'Not a supported subscripted assignment')
            end
        case '{}'
            error('PAResult:subsasgn',...
                'Not a supported subscripted assignment')

    end
elseif length(S) == 2
    error('PAResult:subsasgn',...
        'Not a supported subscripted assignment')
    

else
    error('PAResult:subsasgn',...
        'Not a supported subscripted assignment')
end
