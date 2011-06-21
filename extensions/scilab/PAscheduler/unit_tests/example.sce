mode(-1);
funcprot(0);
rand("seed", 1);

// Evaluate bias and variance for these lambdas 
//lambdas=[0.1:0.1:0.5 1:10 100:100:1000 1000:1000:10000];
lambdas=[0.1:0.1:0.5 1:10 100:100:1000 1000:1000:10000];
//lambdas=[0.1];
//lambdas=['a' 'a' 'a'];

b_bias = []; b_variance = []; // Store results here

fs = filesep();
tstdir = get_absolute_file_path('example.sce');

// Loading source file
src = strcat([tstdir, fs, 'estimate.sci']);
exec(src);

// Global Input File
inputfile = strcat([tstdir, fs, 'input.dat']);

save(inputfile, lambdas);
inputs = list(inputfile);

tic();

for i=1:size(lambdas,2) // For each lambda
  tsk(1,i).Func = 'estimate';
  tsk(1,i).Params = i;
  tsk(1,i).InputFiles = list('input.dat');
  // Output file for each task
  outputfile = strcat([tstdir, fs, 'output', string(i), '.dat']);
  tsk(1,i).OutputFiles = list(strcat(['output', string(i), '.dat']));
end
resl = PAsolve(tsk);


for i=1:size(resl) // For each lambda
  load(strcat([tstdir, fs, 'output', string(i), '.dat']));
  b_bias($+1) = res(1);
  b_variance($+1) = res(2);
end

printf("\n Time: %f\n", toc());

drawlater();
plot2d(lambdas, b_bias,logflag='ln');
plot2d(lambdas, b_variance,logflag='ln');
drawnow();




