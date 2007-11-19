/***
 * Julia: France Telecom's implementation of the Fractal API
 * Copyright (C) 2001-2002 France Telecom R&D
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
package functionalTests.component.conform;

import functionalTests.component.conform.components.C;


public class TestBindingControllerTemplate extends TestBindingController {
    public TestBindingControllerTemplate() {
        isTemplate = true;
    }

    protected void setUpComponents() throws Exception {
        c = gf.newFcInstance(t, flatPrimitiveTemplate, C.class.getName());
        d = gf.newFcInstance(t, flatPrimitiveTemplate, C.class.getName());
        e = gf.newFcInstance(u, flatPrimitiveTemplate, C.class.getName());
    }
}
