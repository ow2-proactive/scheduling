function [ok, msg]=TestPATask(timeout)
    mode(-1);
    funcprot(0);
    rand("seed", 1);
    
    if ~exists('timeout')
    if (getos() == "Windows")
        timeout = 500000;
    else
        timeout = 200000;
    end
end

    // Evaluate bias and variance for these lambdas 
    //lambdas=[0.1:0.1:0.5 1:10 100:100:1000 1000:1000:10000];
    lambdas=[0.1:0.1:0.5 1:10 100:100:1000 1000:1000:10000];
    //lambdas=[0.1];
    //lambdas=['a' 'a' 'a'];

    b_bias = []; b_variance = []; // Store results here

    fs = filesep();

    // Loading source file
    srcName = 'estimate.sci';
    //src = strcat([tstdir, fs, srcName]);

    tic();
    sz=size(lambdas,2);
    tsk = PATask(1,sz);
    tsk(1,1:sz).Func = 'myestimate';
    tsk(1,1:sz).Sources = srcName;

    for i=1:size(lambdas,2) // For each lambda  
        lambda = lambdas(i);
        // Input file for each task
        inputfileName = 'input_'+string(i)+'.dat';
        save(inputfileName, lambda);
        //inputfile = tstdir + fs + inputfileName;
        tsk(1,i).InputFiles = inputfileName;

        tsk(1,i).Description = 'estimate('+string(lambda)+')';  
        // Output file for each task
        outputfileName =  'output_'+string(i)+'.dat';
        //outputfile = tstdir+fs+outputfileName;
        tsk(1,i).OutputFiles = outputfileName;
        tsk(1,i).Params = list(inputfileName, outputfileName);   
    end
    resl = PAsolve(tsk);

    val = PAwaitFor(resl,timeout);
    for i=1:length(val) // For each lambda
        load( 'output_'+ string(i)+ '.dat');
        b_bias($+1) = res(1);
        b_variance($+1) = res(2);
    end

    printf("\n Time: %f\n", toc());
    

    [ok,msg]=checkValuesEst(b_bias,b_variance);    
    if ~ok error(msg); end   

endfunction

function [ok,msg]=checkValuesEst(b_bias,b_variance)
    ok_bias = [0.0045490 0.0126257 0.0200381 0.0269282 0.0333926 0.0612466 0.1042142 0.1381027 0.1666017 0.1914026 0.2134579 0.2333707 0.2515521 0.2682983 0.2838306 0.6953426 0.8033789 0.8537557 0.8833246 0.9028600 0.9167554 0.9271556 0.9352365 0.9416984 0.9469846 0.9469846 0.9721750 0.9811320 0.9857258 0.9885205 0.9903999 0.9917505 0.9927679 0.9935619 0.9941988];
    ok_var =  [0.0953152 0.0970627 0.1008779 0.1055931 0.1106495 0.1348885 0.1702436 0.1936701 0.2103328 0.2227736 0.2323662 0.2399276 0.2459802 0.2508765 0.2548648 0.2085607 0.1525602 0.1199462 0.0987936 0.0839817 0.0730335 0.0646118 0.0579326 0.0525055 0.0480085 0.0480085 0.0258637 0.0177006 0.0134544 0.0108514 0.0090923 0.0078240 0.0068662 0.0061174 0.0055158];
    if length(b_bias) ~= length(ok_bias)
        ok = %f;
        msg = 'Wrong number of outputs in bias';
        return;
    elseif length(b_variance) ~= length(ok_var)
        ok = %f;
        msg = 'Wrong number of outputs in var';
        return;
    end
    ok=%t; msg=[];

endfunction

function resf=myestimate(filein,fileout)
    load(filein);
    [bias,var]=estimate(lambda);
    res = list(bias,var);
    save(fileout, res);
    resf=%t;
endfunction




