The Rest API module embeds a Java websocket proxy to allow noVNC clients to connect to VNC servers.

See:
https://github.com/kanaka/noVNC
https://github.com/jribble/Java-Websockify (custom implementation from http://gitorious.ow2.org/ow2-proactive/java-websockify)
Alternatives analysis: https://docs.google.com/a/activeeon.com/document/d/13GbShf2THqxOf4N_JZIGwEVgWieN54KAGN8kKWiOGyg/edit

The websocket proxy, using netty, will be started with the Rest API on a different port.
In order to use secured websockets, a trick has to be used:
 - the client connects to https://websocketproxy:5900
 - the browser will display a warning because the HTTPS certificate is self signed
 - the user can accept this certificate
 - the websocket proxy redirects the client to http://restapi:9090/novnc
 - the Rest API exposes a static HTML page that creates the websocket and connects to https://websocketproxy:5900
The issue is described here: https://bugzilla.mozilla.org/show_bug.cgi?id=594502

A few parameters are required to access https://websocketproxy:5900
 - host: the address of the websocket proxy, needed by the websocket so it knows where to connect
    host must be accessible from the client (does need to be public)
 - port: the port of the websocket proxy, needed by the websocket so it knows where to connect
 - sessionId: session id of the user, must be valid
 - jobId: job id that exposes the PA_REMOTE_CONNECTION string in of its tasks
 - taskName: the task name from the job where the PA_REMOTE_CONNECTION string is written in the logs
 - encrypt: True to indicate that a secured websocket must be used, False otherwise

-------------------------------------------------------------------------------

Using our noVNC integration with Openstack

With Openstack, VNC is started by the hypervisor for each instance.
Openstack has a proxy (websockify) too and a authentication mechanism with a token.
See: http://docs.openstack.org/trunk/openstack-compute/admin/content/getting-started-with-vnc-proxy.html

If we don't want the users to bother with Openstack credentials we proceed this way:

Configure nova so vncserver_listen to have the hypervisor listen to VNC connection from our proxy.
Do the following when starting a new ProActive node on Openstack:
Get the instance uuid:
 wget -q -O - http://169.254.169.254/openstack/latest/meta_data.json
Remote virsh to get vnc port and ip:
 virsh -c qemu+ssh://openstack_host/system vncdisplay dumpxml uuid
Expose it as an environment variable so jobs can retrieve it and print out the PA_REMOTE_CONNECTION string.


Otherwise we could use the nova get-vnc-console command to expose a direct URL to connect to the Openstack noVNC.

-------------------------------------------------------------------------------

How to run a job that creates a display?

Script example

#!/bin/bash

for disp in $(seq 10 100)
do
    echo "Trying to use display $disp and port 5900+$disp"
    Xvnc :$disp -geometry 1280x1024 -SecurityTypes None &
    xvnc_pid=$!
    ps -p $xvnc_pid
    if [ $? -eq 0 ]; then
        # magic string to enable remote visualization
        echo "PA_REMOTE_CONNECTION;$PA_TASK_ID;vnc;$(hostname):$(($disp + 5900))"
        export DISPLAY=:$disp
        # RUN YOUR GUI BASED APPLICATION HERE (with nohup)
        xclock

        kill $xvnc_pid
        echo "[debug] Display closed"
        exit
    fi
done