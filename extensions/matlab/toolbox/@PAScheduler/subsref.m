function varargout = subsref(this,S)

str=indexstr(S); %Converts S into an equivalent indexing expression
%contained in a string.

if nargout >= 1
    outstr = '[';
    for i = 1:nargout
        outstr = [outstr 'varargout{' num2str(i) '} '];
    end
    outstr = [outstr ']'];
    eval([outstr '=this' str ';']) ;
    %varargout=eval(['{this' str '};']) ;
else
    eval(['this' str ';']) ;
end


function st=indexstr(K)
name=inputname(1);

%DEFAULT
if isempty(name), name='S'; end
st='';
for ii=1:length(K)
    switch K(ii).type
        case '.'
            st=[st '.' K(ii).subs];
        case '()'
            insert=[name '(' num2str(ii) ')'];
            st=[st '(' insert '.subs{:})'];
        case '{}'
            insert=[name '(' num2str(ii) ')'];
            st=[st '{' insert '.subs{:}}'];
    end
end