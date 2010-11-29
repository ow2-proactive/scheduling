function [argsin, argsout] = signature(fun)
argsout = argout(fun);
argsin = argin(fun);
end

function argsout = argout(fun)
            % ARGOUT - Lists output for a Matlab function
            %
            %  ARGSOUT = ARGOUT(FUN) where FUN is the name of an mfile on
            %  the path (or a function handle pertaining to an mfile on the
            %  path) returns a cell array of said function's input arguments. 
           
             
            if isa(fun,'function_handle') && ~isempty(strfind(func2str(fun),'(')),
                % Anonymous function
                argsout = [];
            else
                % For remaining cases, fun probably has a corresponding physical file
                fun = apu_private_fun2str(fun);
                
                % Find and parse the file
                fullName = which(fun);
                if strcmpi(fullName(1:8),'built-in'),
                    argsout = cell(nargout(fun),1);  % Cannot discern names but at least the size of argsout is correct
                else
                    functionSignature = apu_private_firstNonBlank(fullName);
                    if isempty(strfind(functionSignature,'function')),
                        error('argout: failed to find argument functionSignature');
                    elseif isempty(strfind(functionSignature,'=')),
                        argsout = [];
                    elseif isempty(strfind(functionSignature,'[')),
                        a1 = strfind(functionSignature,'function');
                        a2 = strfind(functionSignature,'=');
                        functionSignature = strtrim(functionSignature(a1+8:a2-1));
                        if ~isvarname(functionSignature),
                            error(['argout: Cannot parse output argument list ',functionSignature]);
                        else
                            argsout = {functionSignature};
                        end
                    elseif isempty(strfind(functionSignature,']')),
                        error(['argout: No closing bracket for output arguments found in m-file', fullName]);
                    else
                        a1 = strfind(functionSignature,'[');
                        a2 = strfind(functionSignature,']');
                        entries = functionSignature(a1+1:a2-1);
                        argsout = apu_private_str2cell(entries);
                    end
                end
            end
        end
        
        function argsin = argin(fun)
            % ARGSIN - Listing of input argument names for mfile or anonymous function
            %
            % fun       char or function_handle
            % argsin    cell array of char
            
            if isa(fun,'function_handle') && ~isempty(strfind(func2str(fun),'(')),
                % Likely an anonymous function
                fun = func2str(fun);
                f1 = strfind(fun,'(');
                f2 = strfind(fun,')');
                functionSignature = strtrim(fun(f1+1:f2-1));
                argsin = apu_private_str2cell(functionSignature);
            else
                % For remaining cases, fun probably has a corresponding physical file
                fun = apu_private_fun2str(fun);
                
                % Find the file
                fileName = which(fun);
                if isempty(fileName),
                    error(['argin could not find ',fun,' in Matlab path']);
                elseif strcmpi(fileName(1:8),'built-in'),
                    % All we can do is return the correct size cell array
                    argsin = cell(nargin(fun),1);
                else
                    % Regular m file
                    functionSignature = apu_private_firstNonBlank(fileName);
                    [a, b, c, match] =  regexp(functionSignature,[fun,'\s*[\(A-Za-z0-9\,\_\s]*\)']);
                    if isempty(match),
                        argsin = [];
                    else
                        match = match{1};
                        f = strfind(match,'(');
                        match = match(f(1)+1:end);
                        argsin = apu_private_str2cell(match);
                    end
                end
            end
            
        end
    
    function  firstLine = apu_private_firstNonBlank(fileName)
            % Returns the first non-commented line in an mFile, presumed to
            % be the function signature
            fid = fopen(fileName,'rt');
            firstLine = strtrim(fgetl(fid));
            while (isempty(firstLine) || strcmpi(firstLine(1),'%')),
                firstLine=strtrim(fgetl(fid));
            end
            fclose(fid);
        end
        
        function s = apu_private_fun2str(fun)
            % Returns the name of the function, stripping of '@' and
            % converting from function handle where necessary
            %
            % fun     char or function_handle
            % s       char
            
            if isa(fun,'function_handle'),
                s = func2str(fun); % Could be a handle to an mfile (like @cos)
            elseif ischar(fun),
                s = fun;
            else
                error('Expecting function_handle or char');
            end
            
            if ischar(s) && s(1)=='@',
                s = s(2:end);     % String representation with unnecessary @
            end
            if length(s)>=3 && strcmpi(s(end-1:end),'.m'),
                s = s(1:end-2);  % Drop the trailing .m if supplied something like "cos.m"
            end
            
        end
        
        function y = apu_private_str2cell(s)
            %Specialized converter takes a argument list into cell array
            %
            % s   char   (comma separated list such as 'alpha,   beta,  homer')
            % y   cell array of char
            
            if ~isempty(s),
                s = strtrim(s);
                % Remove parenthesis
                if ~isempty(strfind('[({',s(1))),
                    s = s(2:end);
                end
                if ~isempty(strfind('})]',s(end))),
                    s = s(1:end-1);
                end
                s = strtrim(s);
                if ~isempty(s),
                    breaks = strfind([s,','],',');
                    r = [1,breaks(1:end-1)+1];
                    nEntries = length(breaks);
                    y = cell(1,nEntries);
                    for k=1:nEntries,
                        y{k} = strtrim(s(r(k):breaks(k)-1));
                    end;
                else
                    y = {};
                end
            else
                y = {};
            end
        end
