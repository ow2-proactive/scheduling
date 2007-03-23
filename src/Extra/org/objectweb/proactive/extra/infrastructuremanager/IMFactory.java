package org.objectweb.proactive.extra.infrastructuremanager;

import java.io.IOException;
import java.rmi.AlreadyBoundException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMConstants;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;


public class IMFactory implements IMConstants {
    
	private static final Logger logger = ProActiveLogger
	.getLogger(Loggers.IM_FACTORY);
	
	private static IMCore imcore = null;

    
    /**
     *
     * @throws NodeException
     * @throws ActiveObjectCreationException
     * @throws AlreadyBoundException
     * @throws IOException
     */
    public static void startLocal()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore == null) {
            Node nodeIM = NodeFactory.createNode(NAME_NODE_IM);
            imcore = (IMCore) ProActive.newActive(IMCore.class.getName(), // the class to deploy
                    new Object[] { nodeIM }, nodeIM);

            ProActive.register(imcore,
                "//localhost/" + NAME_ACTIVE_OBJECT_IMCORE);
            if (logger.isInfoEnabled()) {
    			logger.info("New IM core localy started");
    		}
        } else {
        	if (logger.isInfoEnabled()) {
    			logger.info("IM Core already localy running");
    		}
        }
    }

    //----------------------------------------------------------------------//
    // GET IMCORE

    /**
     * Warning : For Invoking this method, call before startLocal() method
     * @return IMCore
     * @throws IOException
     * @throws AlreadyBoundException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    private static IMCore getIMCore()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore != null) {
            return imcore;
        } else {
            return getIMCore("//localhost/IMCORE");
        }
    }

    /**
     * @param urlIM : the location of the actif object IMCORE (For example : "//localhost/IMCORE")
     * @see the name of the IM node in the classe IMConstantes (NAME_ACTIVE_OBJECT_IMCORE)
     * @return IMCore
     * @throws IOException
     * @throws ActiveObjectCreationException
     */
    private static IMCore getIMCore(String urlIM)
        throws ActiveObjectCreationException, IOException {
    	if (logger.isInfoEnabled()) {
			logger.info("lookup of IMCore at the IM url node : " + urlIM);
		}
        IMCore imcoreLookUp;
        imcoreLookUp = (IMCore) ProActive.lookupActive(IMCore.class.getName(),
                urlIM);
        return imcoreLookUp;
    }

    /**
     * Warning : For Invoking this method, call before startLocal() or getIMCore(urlIM)
     * @return IMAdmin
     * @throws IOException
     * @throws AlreadyBoundException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    public static IMAdmin getAdmin()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore != null) {
        	if (logger.isInfoEnabled()) {
    			logger.info("We have started the imcore");
    		}
            return imcore.getAdmin();
        } else {
        	if (logger.isInfoEnabled()) {
    			logger.info("We try to look at localhost for IM");
    		}
            return getIMCore().getAdmin();
        }
    }

    /**
     * @param urlIM : the location of the actif object IMCORE (For example : "//localhost/IMCORE")
     * @return IMAdmin
     * @throws IOException
     * @throws ActiveObjectCreationException
     */
    public static IMAdmin getAdmin(String urlIM)
        throws ActiveObjectCreationException, IOException {
        IMCore imcoreLookUp = getIMCore(urlIM);
        return imcoreLookUp.getAdmin();
    }

    //----------------------------------------------------------------------//
    // GET MONITORING

    /**
     * Warning : For Invoking this method, call before startLocal() or getIMCore(urlIM)
     * @return IMMonitoring
     * @throws IOException
     * @throws AlreadyBoundException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    public static IMMonitoring getMonitoring()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore != null) {
            return imcore.getMonitoring();
        } else {
            return getIMCore().getMonitoring();
        }
    }

    /**
     * @param urlIM : the location of the actif object IMCORE (For example : "//localhost/IMCORE")
     * @return IMMonitoring
     * @throws IOException
     * @throws ActiveObjectCreationException
     */
    public static IMMonitoring getMonitoring(String urlIM)
        throws ActiveObjectCreationException, IOException {
        IMCore imcoreLookUp = getIMCore(urlIM);
        return imcoreLookUp.getMonitoring();
    }

    //----------------------------------------------------------------------//
    // GET USER

    /**
     * Warning : For Invoking this method, call before startLocal() or getIMCore(urlIM)
     * @return IMUser
     * @throws IOException
     * @throws AlreadyBoundException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    public static IMUser getUser()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore != null) {
            return imcore.getUser();
        } else {
            return getIMCore().getUser();
        }
    }

    /**
     * @param urlIM : the location of the actif object IMCORE (For example : "//localhost/IMCORE")
     * @return IMUser
     * @throws IOException
     * @throws ActiveObjectCreationException
     */
    public static IMUser getUser(String urlIM)
        throws ActiveObjectCreationException, IOException {
        IMCore imcoreLookUp = getIMCore(urlIM);
        return imcoreLookUp.getUser();
    }

    //----------------------------------------------------------------------//
    // MAIN : start the IM 
    public static void main(String[] args) {
        try {
            startLocal();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
