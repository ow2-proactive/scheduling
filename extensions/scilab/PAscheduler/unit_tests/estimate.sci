function resf = estimate(i)
  load('input.dat');
  lambda = lambdas(i);
  disp('Calculating estimate for lambda = '+string(lambda));
  rand("normal");      // Use normal distribution
  n = 60; m = 50;      // Size of dataset
  b_true = ones(m, 1); // The true coefficients
  noise = 0.3;         // Noise st.dev.
  ntimes = 1000;       // Number of iterations in the inner loop
  
  b_sum = 0;           // Helper variables
  b_sumsquares = 0;
  
  for k=1:ntimes
    // Generate X and y
    X = rand(n, m);
    y = X*b_true + rand(n, 1)*noise;
  
    // Solve for b
    b = inv(X'*X + lambda*eye(m, m))*X'*y;
    
    b_sum = b_sum + b(1);
    b_sumsquares = b_sumsquares + b(1)^2;
  end
  bias = abs(b_sum/ntimes - b_true(1));
  var  = sqrt(b_sumsquares/ntimes - (b_sum/ntimes)^2);
  disp('Calculated bias = '+string(bias));
  disp('Calculated var = '+string(var));
  res = list(bias,var);
  save(strcat(['output', string(i), '.dat']), res);
  resf=%t;
  
  //res(1) = bias;
  //res(2) = var;
endfunction

