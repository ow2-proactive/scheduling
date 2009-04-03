function B = subsref(R,S)

if ~R.resultSet
   R=updateResults(R);
end

switch S.type
case '.'
   switch S.subs
       case 'wait'
       B = R.result;
   otherwise
       error([S.subs ,' is not a valid property'])
   end
end