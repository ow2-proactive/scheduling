function RL=updateResults(RL)
   reslist = org.objectweb.proactive.api.PAFuture.getFutureValue(RL.future);
   n = reslist.size();
   results = cell(1, n);
   for i=1:n
        results{i}=parse_token_output(reslist.get(i-1));
    end
    RL.results = results;
    RL.resultSet = true;
end