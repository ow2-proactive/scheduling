package org.objectweb.proactive.core.component.controller;

import java.net.URL;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.identity.ProActiveComponentImpl;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class MigrationControllerImpl extends AbstractProActiveController
    implements MigrationController {
    public MigrationControllerImpl(Component owner) {
        super(owner);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance()
                                               .createFcItfType(Constants.MIGRATION_CONTROLLER,
                    MigrationController.class.getName(), TypeFactory.SERVER,
                    TypeFactory.MANDATORY, TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException(
                "cannot create controller type for controller " +
                this.getClass().getName());
        }
    }

    public void migrateTo(Node node) throws MigrationException {
        // need to migrate gathercast futures handlers active objects first
    	((ProActiveComponentImpl)owner).migrateControllersDependentActiveObjectsTo(node);
        ProActive.migrateTo(node);
    }

    public void migrateTo(URL url) throws MigrationException {
        try {
            migrateTo(NodeFactory.getNode(url.toString()));
        } catch (NodeException e) {
            throw new MigrationException("Cannot find node with URL " + url);
        }
    }

    public void migrateTo(String stringUrl) throws MigrationException {
        try {
            migrateTo(NodeFactory.getNode(stringUrl));
        } catch (NodeException e) {
            throw new MigrationException("Cannot find node with URL " +
                stringUrl);
        }
    }
}
