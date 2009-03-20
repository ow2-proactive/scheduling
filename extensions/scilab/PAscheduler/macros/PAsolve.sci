function out = PAsolve(funcname, args, varargin)
             global PA_connected

             if ~exists('PA_connected') | PA_connected ~= 1
                 error('A connection to the ProActive scheduler must be established in order to use PAsolve, see PAconnect');
             end
             if (type(funcname) <> 10)
                 error('<funcname> must be a string');
             end
             execstr(strcat(['functype = type(';funcname;');']));
             if (functype <> 13) & (functype <> 11) then
               error('<funcname> must point to a compiled or uncompiled function (not a C or Fortran built-in function)');
             end
             if (type(args) <> 15) then
                error('<args> must have the list type');
             end

             // ------ Debug mode -------
             debugv = 0;

             // retrieving the function code
             funccode = [];
             if length(varargin) > 0
                 if (varargin(1) == 'debug') | (varargin(1) == 'd') | (varargin(1) == '-d') | (varargin(1) == '-debug')
                     debugv = 1;
                     execstr(strcat(['funccode = fun2string(';funcname;');']));
                 else
                     file1 = varargin(1);
                     // Read file function definition into string
                     funccode = [funccode;mgetl(file1)];
                 end
                 if length(varargin) > 1
                     debugv = 1;
                 end
             else
                execstr(strcat(['funccode = fun2string(';funcname;');']));
             end
             s=size(funccode);
             // We flatten the funccode
             funccode_flat='';
             for i=1:(s(1)-1)
                  funccode_flat=funccode_flat+funccode(i)+ascii(31);
             end

             // Create input script list

             inputscripts = [];
             for i=1:length(args)
                 // We transform the argument to executable scilab code
                 argcode = sci2exp(args(i),'in');
                 // The inputscript will contain the argument and the code of the functions
                 inputscript_array=[argcode];
                 // The code defining the function is made of many lines, we pack it by using ASCII 31 delimiters
                 s=size(inputscript_array);
                 inputscript_str='';
                 for j=1:(s(1)-1)
                     inputscript_str=inputscript_str+inputscript_array(j)+ascii(31);
                 end
                 inputscript_str=inputscript_str+inputscript_array(s(1));
                 // We tranform the final packed command as an assigment to evaluate
                 inputscript_assignment = sci2exp(inputscript_str, 'inputscript');
                 // We add the code used to decode the packed instructions
                 inputscript_decode = 'TOKENS = tokens(inputscript,ascii(31)); execstr(TOKENS,''errcatch'',''m'');';
                 // finally we add this set of instructions in our array
                 inputscripts = [inputscripts, strcat([inputscript_assignment;';';inputscript_decode])];
             end

             // The mainscript is used for executing the main function,
             // it creates an out variable, containing the sequence of instructions to execute in order to recreate, in the local environment, the remote result
             if debugv == 1
                 mainscript = strcat(['mode(3)';ascii(31);'output=';funcname;'(in)';ascii(31);ascii(30);'out = sci2exp(output,''''output'''',0)';ascii(31);'disp(out)']);
             else
                 mainscript = strcat(['output=';funcname;'(in);';ascii(31);ascii(30);'out = sci2exp(output,''''output'''',0);']);
             end

             schedulerdir = getenv('SCHEDULER_HOME');

             selectionScript = strcat(['file:',fullfile(schedulerdir,'extensions','scilab','embedded', 'checkScilab.js')]);

             results = sciSolve(inputscripts, funccode_flat, mainscript, selectionScript, debugv);

             out = list();
             for i=1:max(size(results))
                 execstr(results(i));
                 // We should have an output variable created
                 out($+1)=output;
             end


         endfunction

