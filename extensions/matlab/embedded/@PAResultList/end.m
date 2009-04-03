function l=end(RL,k, n)

if ~RL.resultSet
   RL=updateResults(RL);
end

l = length(RL.results);