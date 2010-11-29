function varargout = PAwaitFor(this,timeout)
s=size(this);
exception = [];
for i=1:s(1)
    for j=1:s(2)
        R=this(i,j);
        f = R.future;
        if exist('timeout','var') == 1

            RaL = org.objectweb.proactive.api.PAFuture.getFutureValue(f,timeout);
        else
            RaL = org.objectweb.proactive.api.PAFuture.getFutureValue(f);
        end        
        if RaL.isOK()
            if ~R.logsPrinted.get()
                logs = RaL.getLogs();
                R.logs.append(logs);
                R.logsPrinted.set(true);
                java.lang.System.out.println(logs);
            end            
            if R.transferVariables
               if R.resultSet.get()
                  A{i,j} = R.resultAcc();
               else
                   load(R.outFile);
                   A{i,j} = out;
                   R.resultAcc(out);
                   R.resultSet.set(true);
               end
            else
                K=RaL.getResult();
                A{i,j} = parse_token_output(K);
            end
        elseif RaL.isMatSciError();
            if ~R.logsPrinted.get()
                logs = RaL.getLogs();
                R.logs.append(logs);
                R.logsPrinted.set(true);
                java.lang.System.err.println(logs);                
            end
            R.iserror.set(true);
            exception = MException('PAResult:PAwaitFor','Error during remote script execution');
        else
            if ~R.logsPrinted.get()
                logs = RaL.getLogs();
                if isjava(logs)
                    R.logs.append(logs);
                    R.logsPrinted.set(true);
                    java.lang.System.err.println(logs);
                end
            end
            e = RaL.getException();
            err = java.lang.System.err;
            e.printStackTrace(err);
            R.iserror.set(true);
            exception = MException('PAResult:PAwaitFor','Internal Error');            
        end
        clean(R);
    end
end

if isa(exception,'MException')
    throw(exception);
end
if isscalar(A)
    A=A{1};
end
if nargout <= 1
    varargout{1}=A;
else
    for i=1:s(1)
        for j=1:s(2)
            varargout{(i-1)*s(2)+j} = A{i,j};
        end
    end
end







