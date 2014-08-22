Setup ../performance
Run
    gradle build extras
Run
    ant execute-test -Dtarget.test.scenario=rm-portal-clients -Dappserver.deploy.result.serverUrl=http://localhost:8080/rest


    ant execute-test -Dtarget.test.scenario=scheduler-portal-clients -Dappserver.deploy.result.serverUrl=http://localhost:8080/rest -DtestTime=1