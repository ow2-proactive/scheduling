Peer 2 Peer Configuration Tool

Here are the first codes for the ProActive Peer2Peer Network.

------------

In the folder registry is the Peer2Peer Registry Application, for 
register/unregister nodes using RMI, the source codes are:

P2PRegistry.java     : Interface of the application (see the code for the
                       methods of registration/unregistration).
                       
P2PRegistryImpl.java : Implementation of the Interface, it's a HashMap
                       {String url, Node node} in an Active Object who
                       serve the requests. The application will register by
                       RMI in //localhost/P2PRegistry (more "friendly" that
                       //localhost/Node131415926535...). 
ServerListener.java  : Used for Jini Lookup server, not used by now.
ServiceFinder.java   : Used for Jini Lookup client, not used by now.

--------------

In the folder peerconfiguration are the GUI for the configuration tool and
the daemon for the load balancer:

ScheduleBean.java    : The main thing, this is a "Bean" who stores one row of
                       the GUI table (for scheduler). Why a bean? Because
                       is easy to save/load in a XML file.  The information
                       stored are: 
                       - days : Days to work (using a byte for that: 
                       0 = never, 127 = every day, 1 = only saturdays, 2 =
                       only frydays, etc. Use the method Days2Byte to pass
                       from a String "Sunday Monday Friday" to the byte)
                       - startTime : at what hour the application has to start
                       - workTime  : time to work of the application (24 is all
                       day)
                       - maxLoad   : Max Load allowed (0 = nothing, 0.5 = half,
                       1 = all)
                       - begin     : First day of work.
                       - end       : Last day of work.

                       public boolean WorkToday(); is used to know if the
                       application has to work "today", using the information
                       of the Bean.                        

PeerSetupGUI.java    : Main frame for the Scheduler GUI.  Use the buttons to
                       add/remove/modify the Schedule.  Also, modify the
                       information of the P2PRegistry location and the 
                       protocol used for the communication (RMI by now).
                       THIS IS THE ONLY ONE WITH main METHOD.

RowModification.java : Used for setup the information of every row in the
                       PeerSetupGUI grid.

The other swing's classes are:

CyclingSpinnerListModel.java
DaysCheckCangeListener.java
SliderValueUpdate.java
SpinnerSet.java
SpringUtilities.java
validityCheckCangeListener.java

TODO: 

- GUI well coded (following the OO paradigm, "friendly" codes and well
commented)
- "Save As" of the ProActivePeerSchedule.xml, by now it's saved in the
same path used to CALL the GUI.
- Nice GUI ;-)

---------------------------------

Daemon.java          : The Daemon itself, receives 2 parameters: a String with
                       the location of the XML File and the application to
                       lunch.  This will check if, following the information
                       of the Array of ScheduleBeans, the application has to be
                       launched or killed.
                       The XML File also store two String parameters:
                       Machine_Name (for the P2PRegistry Lookup) and Protocol
                       (idem).  Those parameters are passed to the application,
                       then: this Daemon can be used to launch every
                       application.

                       example:

                       java Daemon /net/home/me/s.xml /net/home/me/lb.sh

                       Launch the "LoadBalancer" using the shell script 
                       /net/home/me/lb.sh and following the schedule in
                       /net/home/me/s.xml

