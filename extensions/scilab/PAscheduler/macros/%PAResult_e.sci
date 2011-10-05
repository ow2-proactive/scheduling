function b=%PAResult_e(i1, R)
    //disp('PAResult_p : '+typeof(l))
    select i1
    case 'val'
        b = PAResult_PAwaitFor(R);
    case 'logs'
        b = jinvoke(R.logs,'toString');
    case 'iserror'
        b = jinvoke(R.iserror, 'get');
    case 'jobid'
        b = R.jobid;
         
    else
        error('PAResult::Unknown attribute : '''+i1+'''');    
    end

endfunction