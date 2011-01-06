rand("seed", 1);

// Evaluate bias and variance for these lambdas 
//lambdas=[0.1:0.1:0.5 1:10 100:100:1000 1000:1000:10000];
lambdas=[0.1:0.1:0.5 1:10 100:100:1000 1000:1000:10000];
//lambdas=[0.1];
//lambdas=['a' 'a' 'a'];

b_bias = []; b_variance = []; // Store results here
scheduling_dir = getenv('SCHEDULER_HOME');
fs = filesep();
tstdir = strcat([scheduling_dir, fs, 'extensions', fs, 'scilab', fs, 'PAscheduler', fs, 'unit_tests']);
src = strcat([tstdir, fs, 'estimate.sci']);
//exec(src);
inputfile = strcat([tstdir, fs, 'input.dat']);

save(inputfile, lambdas);
inputs = list(inputfile);

tic();

for i=1:size(lambdas,2) // For each lambda
  tsk(1,i).Func = 'estimate';
  tsk(1,i).Params = i;
  tsk(1,i).Sources = src;
  tsk(1,i).InputFiles = list('input.dat');
  outputfile = strcat([tstdir, fs, 'output', string(i), '.dat']);
  tsk(1,i).OutputFiles = list(strcat(['output', string(i), '.dat']));
end
resl = PAsolve(tsk);
//resl = PAsolve('estimate',lambdal,'-d');
for i=1:size(resl) // For each lambda
  load(strcat([tstdir, fs, 'output', string(i), '.dat']));
  b_bias($+1) = res(1);
  b_variance($+1) = res(2);
end

printf("\n Time: %f\n", toc());
//a.data_bounds(:,1) = [1;1000];
drawlater();
plot2d(lambdas, b_bias,logflag='ln');
plot2d(lambdas, b_variance,logflag='ln');
drawnow();




