-------- Scheduler and Resource Manager JMX plugins for Munin ---------

This directory contains Munin plugins, in order to monitor ProActive Scheduler and
Resource Manager with JMX protocol.

Munin is a network/system monitoring application that presents output in graphs through a web interface.
See http://munin.projects.linpro.no

These plugins are based on Munin JMX plugins.
See : http://muninexchange.projects.linpro.no

-------- Installation ---------

Pre-requsisites are:
- installed munin-node
- a SUN Java version 5 JRE or higher

1) Files from "plugins" folder must be copied to /usr/share/munin/plugins (or another - wherever your munin plugins are located)
2) Make sure that the jmx_ script is executable : chmod a+x /usr/share/munin/plugins/jmx_
4) Create links from the /etc/munin/plugins folder to the /usr/share/munin/plugins/jmx_ as this :

ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_pa_rm
ln -s /usr/share/munin/plugins/jmx_ /etc/munin/plugins/jmx_pa_scheduler

Links must be created without '.conf' extension for pa_rm plugin and pa_scheduler plugins

5) Set JMX URLs for Resource Manager and Scheduler in Munin config file :
edit /etc/munin/plugin-conf.d/munin-node and add these lines :

[jmx_pa_rm]
env.jmxurl service:jmx:rmi:///jndi/rmi://<host>:<port>/JMXRMAgent

[jmx_pa_scheduler]
env.jmxurl service:jmx:rmi:///jndi/rmi://<host>:<port>/JMXSchedulerAgent

Where host is the machine hosting Scheduler and Resource Manager, and port is the port used by Scheduler
and Resource Manager. Basically the port is set in <SCHEDULER_INSTALL_DIR>/config/proactive/ProActiveConfiguration.xml,
in the tag <prop key="proactive.rmi.port" value="1099"/>. If not set, port default value is 1099.

6) set java environment variables for Munin JMX plugin :

edit /usr/share/munin/plugins/jmx_ , and add these two lines (at the beginning of the file) :

JAVA_HOME=/path/to/java_installation
PATH=$JAVA_HOME/bin:$PATH

7) restart Munin node :

launch as root :
/etc/init.d/munin-node restart

ProActive Resource Manager and Scheduler are now monitored by munin.

8) Debug your Scheduler and Resource Manager plugins :
as root, launch
munin-run jmx_pa_rm config
munin-run jmx_pa_scheduler

You should see scheduler load and Resource manager's nodes states values displayed.

Verify munin logs to check that no errors happened :
as root, type :
less /var/log/munin/munin-node.log
less /var/log/munin/munin-graph.log
