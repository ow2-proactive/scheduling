package org.ow2.proactive.scheduler.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;


/**
 * @author gsigety
 *
 */
public class FileLoginModule implements LoginModule {

    /** connection logger */
    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONNECTION);

    /**
     *  JAAS call back handler used to get authentication request parameters 
     */
    private CallbackHandler callbackHandler;

    /** authentication status */
    private boolean succeeded = false;

    /** The file where to store the allowed user//password */
    private String loginFile = PASchedulerProperties.SCHEDULER_LOGIN_FILENAME.getValueAsString();

    /** The file where to store group management */
    private String groupFile = PASchedulerProperties.SCHEDULER_GROUP_FILENAME.getValueAsString();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {

        //test that login file path is an absolute path or not
        if (!(new File(this.loginFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            this.loginFile = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + File.separator +
                this.loginFile;
        }

        //test that group file path is an absolute path or not
        if (!(new File(this.groupFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            this.groupFile = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + File.separator +
                this.groupFile;
        }

        //test login file existence
        if (!(new File(this.loginFile).exists())) {
            throw new RuntimeException("The file " + this.loginFile + " has not been found \n" +
                "Scheduler is unable to perform user authentication by file method");
        }

        //test group file existence
        if (!(new File(this.groupFile).exists())) {
            throw new RuntimeException("The file " + this.groupFile + " has not been found \n" +
                "Scheduler is unable to perform user authentication by file method");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Using Login file at : " + this.loginFile);
            logger.debug("Using Group file at : " + this.groupFile);
        }
        this.callbackHandler = callbackHandler;
    }

    /**
     * @see javax.security.auth.spi.LoginModule#login()
     */
    public boolean login() throws LoginException {
        // prompt for a user name and password
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available "
                + "to garner authentication information from the user");
        }

        Callback[] callbacks = new Callback[] { new NoCallback() };

        String username = null;
        String password = null;
        String reqGroup = null;
        GroupHierarchy groupsHierarchy = null;
        String[] hierarchyArray = null;

        try {

            // gets the username, password, group Membership, and group Hierarchy from callback handler
            callbackHandler.handle(callbacks);
            Map<String, Object> params = ((NoCallback) callbacks[0]).get();
            username = (String) params.get("username");
            password = (String) params.get("pw");
            reqGroup = (String) params.get("group");
            hierarchyArray = (String[]) params.get("groupsHierarchy");
            groupsHierarchy = new GroupHierarchy(hierarchyArray);

            params.clear();
            ((NoCallback) callbacks[0]).clear();
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            uce.printStackTrace();
            throw new LoginException("Error: " + uce.getCallback().toString() +
                " not available to garner authentication information from the user");

        }

        if (logger.isDebugEnabled()) {
            logger.debug("File authentication requested for user : " + username);
            String hierarchyRepresentation = "";
            for (String s : hierarchyArray) {
                hierarchyRepresentation += (s + " ");
            }
            logger.debug("requested group : " + reqGroup + ", group hierarchy : " + hierarchyRepresentation);
        }

        Properties props = new Properties();

        try {
            props.load(new FileInputStream(new File(loginFile)));
        } catch (FileNotFoundException e) {
            throw new LoginException(e.toString());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }

        // verify the username and password
        if (!props.containsKey(username) || !props.get(username).equals(password)) {
            succeeded = false;
            logger.info("Incorrect Username/Password");
            throw new FailedLoginException("Incorrect Username/Password");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("authentication succeeded, checking group");
        }

        if (reqGroup == null) {
            succeeded = false;
            logger.info("No group has been specified for authentication");
            throw new FailedLoginException("No group has been specified for authentication");
        }

        Properties groups = new Properties();

        try {
            groups.load(new FileInputStream(new File(groupFile)));
        } catch (FileNotFoundException e) {
            throw new LoginException(e.toString());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        }

        String group = (String) groups.get(username);

        if (group == null) {
            succeeded = false;
            logger.info("User doesn't belong to a group");
            throw new FailedLoginException("User doesn't belong to a group");
        }

        if (groupsHierarchy == null) {
            succeeded = false;
            logger.info("Groups hierarchy not found");
            throw new FailedLoginException("Groups hierarchy not found");
        }

        try {
            if (!groupsHierarchy.isAbove(group, reqGroup)) {
                succeeded = false;
                logger.info("User group not matching");
                throw new FailedLoginException("User group not matching");
            }
        } catch (GroupException e) {
            e.printStackTrace();
            throw new FailedLoginException("Groups hierarchy not found");
        }

        logger.info("authentication succeeded");
        succeeded = true;
        return true;
    }

    public boolean commit() throws LoginException {
        return succeeded;
    }

    public boolean abort() throws LoginException {
        boolean result = succeeded;
        succeeded = false;

        return result;
    }

    public boolean logout() throws LoginException {
        succeeded = false;

        return true;
    }
}