/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.activeobject.migration.strategy;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.migration.MigrationStrategy;
import org.objectweb.proactive.core.migration.MigrationStrategyImpl;
import org.objectweb.proactive.core.migration.MigrationStrategyManager;
import org.objectweb.proactive.core.migration.MigrationStrategyManagerImpl;


public class A implements Serializable, RunActive {

    /**
         *
         */
    int counter = 0;
    private MigrationStrategyManager migrationStrategyManager;
    private MigrationStrategy migrationStrategy;

    public A() {
    }

    public A(String[] nodesUrl) {
        migrationStrategy = new MigrationStrategyImpl();
        int i;
        for (i = 0; i < nodesUrl.length; i++) {
            migrationStrategy.add(nodesUrl[i], "arrived");
        }
    }

    public void runActivity(Body body) {
        if (counter == 0) {
            try {
                migrationStrategyManager = new MigrationStrategyManagerImpl((Migratable) body);
                migrationStrategyManager.onDeparture("leaving");
                migrationStrategyManager.setMigrationStrategy(this.migrationStrategy);
                migrationStrategyManager.startStrategy(body);
            } catch (Exception e) {
                e.printStackTrace();
            }
            counter++;
        }
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        service.fifoServing();
    }

    public void leaving() {
        counter++;
    }

    public void arrived() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }
}
