/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package benchmark;

import java.io.File;
import java.io.IOException; 

import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.xml.sax.SAXException;

import testsuite.group.Group;
import testsuite.manager.ProActiveBenchManager;
import benchmark.objectcreation.newactive.BenchNewActive;
import benchmark.objectcreation.turnactive.BenchTurnActive;


/**
 * @author Alexandre di Costanzo
 *
 */
public class BenchCenter extends ProActiveBenchManager {

    /**
     *
     */
    public BenchCenter() {
        super("ProActive's Benchmarks", "Manage all benchmarks.");
    }

    /**
     * @param name
     * @param description
     */
    public BenchCenter(String name, String description) {
        super(name, description);
    }

    /**
     * @param xmlDescriptor
     * @throws IOException
     * @throws SAXException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public BenchCenter(File xmlDescriptor)
        throws IOException, SAXException, ClassNotFoundException, 
            InstantiationException, IllegalAccessException {
        super(xmlDescriptor);
    }

    /**
     * @see testsuite.manager.AbstractManager#initManager()
     */
    public void initManager() throws Exception {
        super.initManager();
        Object[] paramsSame = { getSameVMNode() };
        Object[] paramsLocal = { getLocalVMNode() };
        Object[] paramsRemote = { getRemoteVMNode() };
        String path = getClass()
                          .getResource("/" +
                getClass().getName().replace('.', '/') + ".class").getPath()
                          .replaceAll("/benchmark/.*", "");
        File classPath = new File(path);

        // Function call
        Group sameVMVoid = new Group("Same VM return void",
                "All functions call on active objects are in same VM.",
                classPath, "benchmark.functionscall.voidreturn", paramsSame,
                false, null);
        Group localVMVoid = new Group("Local VM return void",
                "All functions call on active objects are in other local VM.",
                classPath, "benchmark.functionscall.voidreturn", paramsLocal,
                false, null);
        Group remoteVMVoid = new Group("Remote VM return void",
                "All functions call on active objects are in remote VM.",
                classPath, "benchmark.functionscall.voidreturn", paramsRemote,
                false, null);

        add(sameVMVoid);
        add(localVMVoid);
        add(remoteVMVoid);

        Group sameVMInt = new Group("Same VM return int",
                "All functions call on active objects are in same VM.",
                classPath, "benchmark.functionscall.intreturn", paramsSame,
                false, null);
        Group localVMInt = new Group("Local VM return int",
                "All functions call on active objects are in other local VM.",
                classPath, "benchmark.functionscall.intreturn", paramsLocal,
                false, null);
        Group remoteVMInt = new Group("Remote VM return int",
                "All functions call on active objects are in remote VM.",
                classPath, "benchmark.functionscall.intreturn", paramsRemote,
                false, null);

        add(sameVMInt);
        add(localVMInt);
        add(remoteVMInt);

        Group sameVMString = new Group("Same VM return String",
                "All functions call on active objects are in same VM.",
                classPath, "benchmark.functionscall.stringreturn", paramsSame,
                false, null);
        Group localVMString = new Group("Local VM return String",
                "All functions call on active objects are in other local VM.",
                classPath, "benchmark.functionscall.stringreturn", paramsLocal,
                false, null);
        Group remoteVMString = new Group("Remote VM return String",
                "All functions call on active objects are in remote VM.",
                classPath, "benchmark.functionscall.stringreturn",
                paramsRemote, false, null);

        add(sameVMString);
        add(localVMString);
        add(remoteVMString);

        Group sameVMReifObj = new Group("Same VM return ReifiableObject",
                "All functions call on active objects are in same VM.",
                classPath, "benchmark.functionscall.reifiableobjreturn",
                paramsSame, false, null);
        Group localVMReifObj = new Group("Local VM return ReifiableObject",
                "All functions call on active objects are in other local VM.",
                classPath, "benchmark.functionscall.reifiableobjreturn",
                paramsLocal, false, null);
        Group remoteVMReifObj = new Group("Remote VM return ReifiableObject",
                "All functions call on active objects are in remote VM.",
                classPath, "benchmark.functionscall.reifiableobjreturn",
                paramsRemote, false, null);

        add(sameVMReifObj);
        add(localVMReifObj);
        add(remoteVMReifObj);

        // RMI with ProActive
        Group sameVMIntRMI = new Group("RMI Same VM return int",
                "All functions call on active objects are in same VM.",
                classPath, "benchmark.rmi.functionscall.intreturn", paramsSame,
                false, null);
        Group localVMIntRMI = new Group("RMI Local VM return int",
                "All functions call on active objects are in other local VM.",
                classPath, "benchmark.rmi.functionscall.intreturn",
                paramsLocal, false, null);
        Group remoteVMIntRMI = new Group("RMI Remote VM return int",
                "All functions call on active objects are in remote VM.",
                classPath, "benchmark.rmi.functionscall.intreturn",
                paramsRemote, false, null);

        add(sameVMIntRMI);
        add(localVMIntRMI);
        add(remoteVMIntRMI);

        Group sameVMReifObjRMI = new Group("RMI Same VM return ReifiableObject",
                "All functions call on active objects are in same VM.",
                classPath, "benchmark.rmi.functionscall.reifiableobjreturn",
                paramsSame, false, null);
        Group localVMReifObjRMI = new Group("RMI Local VM return ReifiableObject",
                "All functions call on active objects are in other local VM.",
                classPath, "benchmark.rmi.functionscall.reifiableobjreturn",
                paramsLocal, false, null);
        Group remoteVMReifObjRMI = new Group("RMI Remote VM return ReifiableObject",
                "All functions call on active objects are in remote VM.",
                classPath, "benchmark.rmi.functionscall.reifiableobjreturn",
                paramsRemote, false, null);

        add(sameVMReifObjRMI);
        add(localVMReifObjRMI);
        add(remoteVMReifObjRMI);

        Group sameVMStringRMI = new Group("RMI Same VM return String",
                "All functions call on active objects are in same VM.",
                classPath, "benchmark.rmi.functionscall.stringreturn",
                paramsSame, false, null);
        Group localVMStringRMI = new Group("RMI Local VM return String",
                "All functions call on active objects are in other local VM.",
                classPath, "benchmark.rmi.functionscall.stringreturn",
                paramsLocal, false, null);
        Group remoteVMStringRMI = new Group("RMI Remote VM return String",
                "All functions call on active objects are in remote VM.",
                classPath, "benchmark.rmi.functionscall.stringreturn",
                paramsRemote, false, null);

        add(sameVMStringRMI);
        add(localVMStringRMI);
        add(remoteVMStringRMI);

        Group sameVMVoidRMI = new Group("RMI Same VM return void",
                "All functions call on active objects are in same VM.",
                classPath, "benchmark.rmi.functionscall.voidreturn",
                paramsSame, false, null);
        Group localVMVoidRMI = new Group("RMI Local VM return void",
                "All functions call on active objects are in other local VM.",
                classPath, "benchmark.rmi.functionscall.voidreturn",
                paramsLocal, false, null);
        Group remoteVMVoidRMI = new Group("RMI Remote VM return void",
                "All functions call on active objects are in remote VM.",
                classPath, "benchmark.rmi.functionscall.voidreturn",
                paramsRemote, false, null);

        add(sameVMVoidRMI);
        add(localVMVoidRMI);
        add(remoteVMVoidRMI);

        // Object Creation
        Group objectCreation = new Group("Object Creation",
                "Object Creation with newActive and turnActive.");

        BenchNewActive test = new BenchNewActive(getSameVMNode());
        test.setName(test.getName() + " -- Same VM");
        objectCreation.add(test);
        BenchTurnActive test2 = new BenchTurnActive(getSameVMNode());
        test2.setName(test2.getName() + " -- Same VM");
        objectCreation.add(test2);

        test = new BenchNewActive(getLocalVMNode());
        test.setName(test.getName() + " -- Local VM");
        objectCreation.add(test);
        test2 = new BenchTurnActive(getLocalVMNode());
        test2.setName(test2.getName() + " -- Local VM");
        objectCreation.add(test2);

        test = new BenchNewActive(getRemoteVMNode());
        test.setName(test.getName() + " -- Remote VM");
        objectCreation.add(test);
        test2 = new BenchTurnActive(getRemoteVMNode());
        test2.setName(test2.getName() + " -- Remote VM");
        objectCreation.add(test2);
        add(objectCreation);
 
		// NFE 
		  Group nfe = new Group("NFE", "Non Functional Exception Mechanism");
        
		  // Test handler setting
		/*  BenchSettingNFE benchNFEsameVM;
		  benchNFEsameVM = new BenchSettingNFE(getSameVMNode(), 0);
		  benchNFEsameVM.setName(benchNFEsameVM.getName() + " on same VM with 0 handler");
		  nfe.add(benchNFEsameVM);
		  for (int nb_handler=1; nb_handler<=1024; nb_handler*=2) {
			  benchNFEsameVM = new BenchSettingNFE(getSameVMNode(), nb_handler);
			  benchNFEsameVM.setName(benchNFEsameVM.getName() + " on same VM with " + nb_handler + " handlers");
			  nfe.add(benchNFEsameVM);
		  }
		  BenchSettingNFE benchNFElocalVM;
		  benchNFElocalVM = new BenchSettingNFE(getLocalVMNode(), 0);
		  benchNFElocalVM.setName(benchNFElocalVM.getName() + " on local VM with 0 handler");
		  nfe.add(benchNFElocalVM);
		  for (int nb_handler=1; nb_handler<=1024; nb_handler*=2) {
			  benchNFElocalVM = new BenchSettingNFE(getLocalVMNode(), nb_handler);
			  benchNFElocalVM.setName(benchNFElocalVM.getName() + " on local VM with " + nb_handler + " handlers");
			  nfe.add(benchNFElocalVM);
		  }
		  BenchSettingNFE benchNFEremoteVM;
		  benchNFEremoteVM = new BenchSettingNFE(getRemoteVMNode(), 0);
		  benchNFEremoteVM.setName(benchNFEremoteVM.getName() + " on remote VM with 0 handler");
		  nfe.add(benchNFEremoteVM);
		  for (int nb_handler=1; nb_handler<=1024; nb_handler*=2) {
			  benchNFEremoteVM = new BenchSettingNFE(getRemoteVMNode(), nb_handler);
			  benchNFEremoteVM.setName(benchNFEremoteVM.getName() + " on remote VM with " + nb_handler + " handlers");
			  nfe.add(benchNFEremoteVM);
		  }

		  // Test NFE mechanism in action (i.e. migration)		
		  BenchMigrationHandlerizable benchMigNFE;
		  benchMigNFE = new BenchMigrationHandlerizable(getSameVMNode(), getLocalVMNode(), 0, "handler");
		  benchMigNFE.setName(benchMigNFE.getName() + " with 0 standard handler on a Local VM");
		  nfe.add(benchMigNFE);		
		  for (int nb_handler=1; nb_handler<=1024; nb_handler*=2) {
				  benchMigNFE = new BenchMigrationHandlerizable(getSameVMNode(), getLocalVMNode(), nb_handler, "handler");
				  benchMigNFE.setName(benchMigNFE.getName() + " with " + nb_handler + " standard handler on a Local VM");
				  nfe.add(benchMigNFE);
		  }

		  benchMigNFE = new BenchMigrationHandlerizable(getSameVMNode(), getRemoteVMNode(), 0, "handler");
		  benchMigNFE.setName(benchMigNFE.getName() + " with 0 standard handler on a Remote VM");
		  nfe.add(benchMigNFE);
		  for (int nb_handler=1; nb_handler<=1024; nb_handler*=2) {
				  benchMigNFE = new BenchMigrationHandlerizable(getSameVMNode(), getRemoteVMNode(), nb_handler, "handler");
				  benchMigNFE.setName(benchMigNFE.getName() + " with " + nb_handler + " standard handler on a Remote VM");
				  nfe.add(benchMigNFE);
		  }
		
		  for (int nb_handler=1; nb_handler<=1024; nb_handler*=2) {
					  benchMigNFE = new BenchMigrationHandlerizable(getSameVMNode(), getLocalVMNode(), nb_handler, "handlerMedium");
					  benchMigNFE.setName(benchMigNFE.getName() + " with " + nb_handler + " medium-sized handler on a Local VM");
					  nfe.add(benchMigNFE);
		  }
		
		  for (int nb_handler=1; nb_handler<=1024; nb_handler*=2) {
					  benchMigNFE = new BenchMigrationHandlerizable(getSameVMNode(), getRemoteVMNode(), nb_handler, "handlerMedium");
					  benchMigNFE.setName(benchMigNFE.getName() + " with " + nb_handler + " medium-sized handler on a Remote VM");
					  nfe.add(benchMigNFE);
		  }
		
		  for (int nb_handler=1; nb_handler<=1024; nb_handler*=2) {
					  benchMigNFE = new BenchMigrationHandlerizable(getSameVMNode(), getLocalVMNode(), nb_handler, "handlerLarge");
					  benchMigNFE.setName(benchMigNFE.getName() + " with " + nb_handler + " large-sized handler on a Local VM");
					  nfe.add(benchMigNFE);
		  }
		
		  for (int nb_handler=1; nb_handler<=1024; nb_handler*=2) {
					  benchMigNFE = new BenchMigrationHandlerizable(getSameVMNode(), getRemoteVMNode(), nb_handler, "handlerLarge");
					  benchMigNFE.setName(benchMigNFE.getName() + " with " + nb_handler + " large-sized handler on a Remote VM");
					  nfe.add(benchMigNFE);
		  }
		
		  add(nfe);*/ 
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();
        System.out.println("Start benchmark ...");
        BenchCenter center = null;
        String path = BenchCenter.class.getResource(
                "/benchmark/BenchCenter.xml").getPath();
        File xml = new File(path);
        try {
            center = new BenchCenter(xml);
            center.execute();
            System.out.println(
                "You can see results in benchmark.hmtl file in your ProActive directory.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
