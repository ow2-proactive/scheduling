/***
 * Fractal ADL Parser
 * Copyright (C) 2002-2004 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */
package org.objectweb.proactive.examples.pi;

import java.util.HashMap;

import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;


public class PiBBPWrapper extends PiBBP implements Runnable, BindingController {
    
    HashMap nameToComputer = new HashMap(); // map between binding names and Components 
    
    public PiBBPWrapper() {
    }

    public void run() {
        nbDecimals_ = 1000;                 // FIXME : this should be a parameter somewhere.  
        piComputer.setScale(nbDecimals_);
        try {
            computeOnGroup(piComputer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] listFc() {
        return new String[] { "computation" };
    }

    public Object lookupFc(final String cItf) {
        return  nameToComputer.get(cItf);
    }

    public void bindFc(final String cItf, final Object sItf) {
        System.out.println("Adding bind called " + cItf + 
                " between " + this.getClass().getName() +
                " and " + sItf.getClass().getName()); 
        if (piComputer == null)
            try {
                piComputer = (PiComp) ProActiveGroup.newGroup(PiComp.class.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        if (cItf.startsWith("computation")) {
            Group group = ProActiveGroup.getGroup(piComputer);
            group.add(sItf);
        }
    }

    public void unbindFc(final String cItf) {
        Group group = ProActiveGroup.getGroup(piComputer);
        Object piComp = nameToComputer.remove(cItf) ;
        group.remove(piComp);
    }
}
