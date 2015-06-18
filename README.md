# ProActive Workflows & Scheduling

[![Build Status](http://jenkins.activeeon.com/job/scheduling/badge/icon)](http://jenkins.activeeon.com/job/scheduling/)

You can download binaries and access a trial platform for free at:

http://activeeon.com/register/web-download

## Quick start

To run ProActive Scheduler (server part):

    $> ./bin/proactive-server

This will start all components, including 4 local nodes and Web portals.
URLs to access them will be displayed in the server's output.

From here you can submit a job to the Scheduler, using for instance the
XML files in `samples/workflows/`.

To run the command line client:

    $> ./bin/proactive-client

To start a node:

    $> ./bin/proactive-node -r SCHEDULER_URL

## Building from sources

You can build a distribution that contains all binaries with `gradle build`.

This will produce the following archives:

    build/distributions/
    ├── scheduling-XXXXX-SNAPSHOT.tar
    └── scheduling-XXXXX-SNAPSHOT.zip

## Documentation

http://doc.activeeon.com

Enjoy ProActive Workflows & Scheduling!

