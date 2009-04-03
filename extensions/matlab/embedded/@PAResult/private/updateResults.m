function R=updateResults(R)
   reslist = org.objectweb.proactive.api.PAFuture.getFutureValue(R.future);
   n = reslist.size();
   R.result=parse_token_output(reslist.get(0));
   R.resultSet = true;
end