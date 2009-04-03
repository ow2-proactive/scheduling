function B = subsref(RL,S)

if ~RL.resultSet
   RL=updateResults(RL);
end

switch S.type
case '.'
   switch S.subs
       case 'wait'
       B = RL.results;    
   otherwise
       error([S.subs ,' is not a valid property'])
   end
case '()'
   ind = S.subs{:};
   B = {RL.results{ind}};
case '{}'
   ind = S.subs{:};
   B = {RL.results{ind}};
end