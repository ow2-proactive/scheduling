package org.ow2.proactive.scheduler.common.jmx.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.client.ClientConnector;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;


/**
 *  This example connects remotely a MBean Server and shows all registered MBeans on this server.
 *
 *  Furthermore, you can give as argument an operation that you want to invoke asynchronously on the MBean.
 *
 *  @author The ProActive Team
 *
 */
@SuppressWarnings("serial")
public class TestConnector implements NotificationListener, Serializable {
    private transient ClientConnector cc;
    private transient ProActiveConnection connection;
    private transient JMXConnector connector;
    private String url;
	private ConnectionListener listener;
	private ObjectName beanName = null;

    public static void main(String[] args) {
        new TestConnector();
    }

    /**
     * Default Constructor : read the url and connect to the MBean Server via The ProActive Connector.
     * When connected, gets and show  the JMX domains one can explore
     */
    public TestConnector() {
        System.out.println("Enter the name of the JMX MBean Server :");
        this.url = read();
        try {
            connect();
            //Create the beanName
            this.beanName = new ObjectName("SchedulerFrontend:name=SchedulerMBean");
            //Perform a desired Action cyclically
            while(true) selectOperation();
        } catch (Exception e) {
            System.out.println("Cannot contact the connector, did you start one ? (see connector.[sh|bat])");
            e.printStackTrace();
        }
    }

    private String read() {
        String what = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            what = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return what;
    }

    private void connect() throws Exception {
        System.out.println("Connecting to : " + this.url);
        String serverName = URIBuilder.getNameFromURI(url);

        if ((serverName == null) || serverName.equals("")) {
            serverName = "serverName";
        }
        this.cc = new ClientConnector(this.url, serverName);
        this.cc.connect();
        this.connection = cc.getConnection();
        this.connector = cc.getConnector();
        /* add a connector listener for the Connection Status */
        this.connector.addConnectionNotificationListener((NotificationListener) this, null, null);
        /* creates an active listener */
        this.listener = (ConnectionListener) PAActiveObject.newActive(ConnectionListener.class.getName(), new Object[] { this.connection });
    }

    /**
     *  Perform a selection between the desired actions
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws IOException
     */
    private void selectOperation() throws MalformedObjectNameException, NullPointerException, IOException {
	String choose = "Choose an action:\n\n[ 1 ] Perform an Operation on the SchedulerWrapper MBean\n"+
	                 "[ 2 ] Explore the Values of the Attributes of the SchedulerWrapper MBean\n"+
	                 "[ 3 ] Start listening for Notifications sent by the SchedulerWrapper MBean\n"+
	                 "[ 4 ] Stop listening for Notifications sent by the SchedulerWrapper MBean";
	//Print the Selection String
	System.out.println();
	System.out.println(choose);
	System.out.println();
	System.out.println("Type the related number to perform the desired Action:");
	String selected = read();
	int selectedNumber = Integer.parseInt(selected);
	switch(selectedNumber) {
		case 1: performOperation(); break;
		case 2: getAttributeValues(); break;
		case 3: subscribeForNotifications(); break;
		case 4: unsubscribeForNotifications(); break;
	}
    }

    /**
     *  Get the Values of the Attributes of the SchedulerWrapper MBean
     */
    private void getAttributeValues() {
	System.out.println();
	System.out.println("Values Of SchedulerWrapper MBean Attributes: \n");
	Object valueStateValue = connection.getAttributeAsynchronous(this.beanName, "StateValue").getObject();
	System.out.println("StateValue" + " =\t " + valueStateValue);
	Object valueTotalNumberOfJobs = connection.getAttributeAsynchronous(this.beanName, "TotalNumberOfJobs").getObject();
	System.out.println("TotalNumberOfJobs" + " =\t " + valueTotalNumberOfJobs);
	Object valueNumberOfPendingJobs = connection.getAttributeAsynchronous(this.beanName, "NumberOfPendingJobs").getObject();
	System.out.println("NumberOfPendingJobs" + " =\t " + valueNumberOfPendingJobs);
	Object valueNumberOfRunningJobs = connection.getAttributeAsynchronous(this.beanName, "NumberOfRunningJobs").getObject();
	System.out.println("NumberOfRunningJobs" + " =\t " + valueNumberOfRunningJobs);
	Object valueNumberOfFinishedJobs = connection.getAttributeAsynchronous(this.beanName, "NumberOfFinishedJobs").getObject();
	System.out.println("NumberOfFinishedJobs" + " =\t " + valueNumberOfFinishedJobs);
	Object valueTotalNumberOfTasks = connection.getAttributeAsynchronous(this.beanName, "TotalNumberOfTasks").getObject();
	System.out.println("TotalNumberOfTasks" + " =\t " + valueTotalNumberOfTasks);
	Object valueNumberOfPendingTasks = connection.getAttributeAsynchronous(this.beanName, "NumberOfPendingTasks").getObject();
	System.out.println("NumberOfPendingTasks" + " =\t " + valueNumberOfPendingTasks);
	Object valueNumberOfRunningTasks = connection.getAttributeAsynchronous(this.beanName, "NumberOfRunningTasks").getObject();
	System.out.println("NumberOfRunningTasks" + " =\t " + valueNumberOfRunningTasks);
	Object valueNumberOfFinishedTasks = connection.getAttributeAsynchronous(this.beanName, "NumberOfFinishedTasks").getObject();
	System.out.println("NumberOfFinishedTasks" + " =\t " + valueNumberOfFinishedTasks);
	Object valueNumberOfConnectedUsers = connection.getAttributeAsynchronous(this.beanName, "NumberOfConnectedUsers").getObject();
	System.out.println("NumberOfConnectedUsers" + " =\t " + valueNumberOfConnectedUsers);
    }

    /**
     *  Perform a desired Operation on the Scheduler MBean
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     */
    private void performOperation() throws MalformedObjectNameException, NullPointerException {
	String choose = "Choose an Operation to perform on the Scheduler Wrapper MBean:\n\n[ 1 ] getAllSubmittedJobs\n[ 2 ] getAllSubmittedTasks\n"+
					   "[ 3 ] getJobInfo(id: long)\n[ 4 ] getTaskInfo(id: long)\n[ 5 ] getAllConnectedUsersInfo";
	//Print the Selection String
	System.out.println();
	System.out.println(choose);
	System.out.println();
	System.out.println("Type the related number to perform the desired Operation:");
        //Switch on the Operation to make
	String operation = null;
	String selected = read();
	int selectedNumber = Integer.parseInt(selected);
	// Get the parameter and his type
	Object param = null;
	String typeOfParam = null;
	// Switch for all possible Operations
	switch(selectedNumber) {
		case 1: operation = "getAllSubmittedJobs"; break;
		case 2: operation = "getAllSubmittedTasks"; break;
		case 3:
			{
				operation = "getJobInfo";
				System.out.println();
			System.out.println("Insert the Job Id:");
			param = Long.parseLong(read());
			typeOfParam = "long";
				break;
			}
		case 4:
			{
				operation = "getTaskInfo";
				System.out.println();
			System.out.println("Insert the Task Id:");
			param = Long.parseLong(read());
			typeOfParam = "long";
				break;
			}
		case 5: operation = "getAllConnectedUsersInfo"; break;
	}
	//Perform the selected Operation on the SchedulerWrapper MBean
	if(param!=null) {
		connection.invokeAsynchronous(beanName, operation, new Object[]{ param }, new String[]{ typeOfParam });
	} else {
		connection.invokeAsynchronous(beanName, operation, new Object[]{}, new String[]{});
	}
    }

    /** Add the source MBean (SchedulerWrapper in our case) to listen for his Notifications */
    public void subscribeForNotifications() throws MalformedObjectNameException, NullPointerException, IOException {
	this.listener.listenTo(beanName, null, null);
	System.out.println();
	System.out.println("[INFO] Listening for Notifications coming from SchedulerWrapper MBean");
    }

    /** Remove the source MBean (SchedulerWrapper in our case) from listening for his Notifications */
    public void unsubscribeForNotifications() throws MalformedObjectNameException, NullPointerException, IOException {
	this.listener.stopListening(beanName);
	System.out.println();
	System.out.println("[INFO] Stopped listening for Notifications coming from SchedulerWrapper MBean");
    }

    /***********************************************************************************************
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification,
     *      java.lang.Object)
     */
    public void handleNotification(Notification notification, Object handback) {
        System.out.println("---> Notification = " + notification);
    }
}