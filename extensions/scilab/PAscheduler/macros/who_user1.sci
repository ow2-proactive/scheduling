function [locals,globals] = who_user1()
    //get user variables
    [nams,mem]=who('get'); //get all variables
    p=predef(); //number of system variable
    st=stacksize()
    opt=PAoptions();

    nams=nams(1:$-p+1);mem=mem(1:$-p+1);
    //modifiable system variables
    excluded=opt.EnvExcludeList;
    ke=grep(nams,cell2mat(excluded))
    nams(ke)=[];mem(ke)=[];
    typs=[];
    for i=1:size(nams,1)
        execstr('typs(i) = typeof('+nams(i)+')')
    end

    typsexcluded=opt.EnvExcludeTypeList;

    ke=grep(typs,cell2mat(typsexcluded))    
    nams(ke)=[];

    n=size(nams,1);
    if n==0 then return,end

    //format names on n*10 characters
    ll=length(nams)+2;m=int((ll-1)/10)+1;
    for k=1:max(m)
        ks=find(m==k);
        if ks<>[] then nams(ks)=part(nams(ks),1:(k*10));end
    end    
    locals=[];
    globals=[];
    ilocals = 1;
    iglobals=1;
    for i=1:size(nams,1)
        ok = %f;        
        execstr('ok = isglobal('+nams(i)+')');                       
        if ok then            
            globals(iglobals)=nams(i);             
            iglobals = iglobals + 1;
        else
            locals(ilocals)=nams(i);
            ilocals = ilocals + 1;            
        end        
    end     
endfunction

