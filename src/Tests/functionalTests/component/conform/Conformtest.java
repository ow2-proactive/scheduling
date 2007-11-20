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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ControllerDescription;

import functionalTests.ComponentTest;
import functionalTests.component.conform.components.I;


public abstract class Conformtest extends ComponentTest {
    // FcItfName/FcItfSignature/isFcClientItf ^ internal, isFcOptionalItf, isFcCollectionItf
    protected final static String COMP = "component/org.objectweb.proactive.core.component.identity.ProActiveComponent/false,false,false";
    protected final static String BC = "binding-controller/org.objectweb.proactive.core.component.controller.ProActiveBindingController/false,false,false";
    protected final static String CC = "content-controller/org.objectweb.proactive.core.component.controller.ProActiveContentController/false,false,false";
    protected final static String NC = "name-controller/org.objectweb.fractal.api.control.NameController/false,false,false";

    //protected final static String NC = "name-controller/org.objectweb.proactive.core.component.controller.ProActiveNameController/false,false,false";
    protected final static String LC = "lifecycle-controller/org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController/false,false,false";
    protected final static String SC = "super-controller/org.objectweb.proactive.core.component.controller.ProActiveSuperController/false,false,false";
    protected final static String F = "factory/org.objectweb.proactive.core.component.Fractive/false,false,false"; //org.objectweb.proactive.core.component.factory.ProActiveGenericFactory
    protected final static String MC = "migration-controller/org.objectweb.proactive.core.component.controller.MigrationController/false,false,false";
    protected final static String MCC = "multicast-controller/org.objectweb.proactive.core.component.controller.MulticastController/false,false,false";
    protected final static String GC = "gathercast-controller/org.objectweb.proactive.core.component.controller.GathercastController/false,false,false";
    protected final static String CP = "component-parameters-controller/org.objectweb.proactive.core.component.controller.ComponentParametersController/false,false,false";

    //  protected final static String COMP = "component/"+ComponentItf.TYPE.getFcItfSignature()+"/false,false,false";
    //  protected final static String BC = "binding-controller/"+BindingControllerDef.TYPE.getFcItfSignature()+"/false,false,false";
    //  protected final static String CC = "content-controller/"+ContentControllerItf.TYPE.getFcItfSignature()+"/false,false,false";
    //  protected final static String NC = "name-controller/"+NameControllerItf.TYPE.getFcItfSignature()+"/false,false,false";
    //  protected final static String PLC = "lifecycle-controller/"+LifeCycleControllerDef.PRIMITIVE_TYPE.getFcItfSignature()+"/false,false,false";
    //  protected final static String CLC = "lifecycle-controller/"+LifeCycleControllerDef.COMPOSITE_TYPE.getFcItfSignature()+"/false,false,false";
    //  protected final static String SC = "super-controller/"+SuperControllerDef.TYPE.getFcItfSignature()+"/false,false,false";
    //  protected final static String F = "factory/"+FactoryDef.TYPE.getFcItfSignature()+"/false,false,false";

    //  protected final static String COMP = "component/org.objectweb.fractal.api.Component/false,false,false";
    //  protected final static String BC = "binding-controller/org.objectweb.fractal.api.control.BindingController/false,false,false";
    //  protected final static String CC = "content-controller/org.objectweb.fractal.api.control.ContentController/false,false,false";
    //  protected final static String NC = "name-controller/org.objectweb.fractal.api.control.NameController/false,false,false";
    //  protected final static String LC = "lifecycle-controller/org.objectweb.fractal.julia.control.lifecycle.LifeCycleCoordinator/false,false,false";
    //  protected final static String SC = "super-controller/org.objectweb.fractal.julia.control.content.SuperControllerNotifier/false,false,false";
    //  protected final static String F = "factory/org.objectweb.fractal.julia.factory.Template/false,false,false";
    protected final static String PKG = "functionalTests.component.conform.components";
    protected final static ControllerDescription parametricPrimitive = new ControllerDescription("parametricPrimitive",
            Constants.PRIMITIVE,
            "/functionalTests/component/conform/membranes/parametricPrimitive.xml",
            false);
    protected final static ControllerDescription parametricPrimitiveTemplate = new ControllerDescription("parametricPrimitive",
            Constants.PRIMITIVE,
            "/functionalTests/component/conform/membranes/parametricPrimitiveTemplate.xml",
            false);
    protected final static ControllerDescription flatPrimitive = new ControllerDescription("flatPrimitive",
            Constants.PRIMITIVE,
            "/functionalTests/component/conform/membranes/flatPrimitive.xml",
            false);
    protected final static ControllerDescription flatParametricPrimitive = new ControllerDescription("flatParametricPrimitive",
            Constants.PRIMITIVE,
            "/functionalTests/component/conform/membranes/flatParametricPrimitive.xml",
            false);
    protected final static ControllerDescription primitiveTemplate = new ControllerDescription("primitiveTemplate",
            Constants.PRIMITIVE,
            "/functionalTests/component/conform/membranes/primitiveTemplate.xml",
            false);
    protected final static ControllerDescription flatPrimitiveTemplate = new ControllerDescription("flatPrimitiveTemplate",
            Constants.PRIMITIVE,
            "/functionalTests/component/conform/membranes/flatPrimitiveTemplate.xml",
            false);
    protected final static ControllerDescription badPrimitive = new ControllerDescription("badPrimitive",
            Constants.PRIMITIVE,
            "/functionalTests/component/conform/membranes/badPrimitive.xml",
            false);
    protected final static ControllerDescription badParametricPrimitive = new ControllerDescription("badParametricPrimitive",
            Constants.PRIMITIVE,
            "/functionalTests/component/conform/membranes/badParametricPrimitive.xml",
            false);
    protected final static ControllerDescription parametricComposite = new ControllerDescription("parametricComposite",
            Constants.COMPOSITE,
            "/functionalTests/component/conform/membranes/parametricComposite.xml",
            false);
    protected final static ControllerDescription compositeTemplate = new ControllerDescription("parametricComposite",
            Constants.COMPOSITE,
            "/functionalTests/component/conform/membranes/compositeTemplate.xml",
            false);

    protected void checkInterface(I i) {
        i.m(true);
        i.m((byte) 1);
        i.m((char) 1);
        i.m((short) 1);
        i.m(1);
        i.m((long) 1);
        i.m((float) 1);
        i.m((double) 1);
        i.m("1");
        i.m(new String[] { "1" });

        assertEquals(true, i.n(true, null));
        assertEquals((byte) 1, i.n((byte) 1, null));
        assertEquals((char) 1, i.n((char) 1, (double) 0));
        assertEquals((short) 1, i.n((short) 1, (float) 0));
        assertEquals(1, i.n(1, (long) 0));
        assertEquals((long) 1, i.n((long) 1, 0));
        assertEquals(1, i.n((float) 1, (short) 0), 0);
        assertEquals(1, i.n((double) 1, (char) 0), 0);
        assertEquals("1", i.n("1", (byte) 0));
    }

    private boolean containsAll(Collection<?> c1, Collection<?> c) {
        Iterator<?> e = c.iterator();
        while (e.hasNext()) {
            Object o = e.next();

            //System.err.println("containall:" + o);
            if (!c1.contains(o)) {
                //System.err.println("containallDIFF:\n" + o + "\n" + c1);
                return false;
            }
        }
        return true;
    }

    protected void checkComponent(Component c, Set itfs)
        throws Exception {
        Set extItfs = getExternalItfs(c);
        //System.err.println("containAll: " + containsAll(itfs, extItfs));
        assertEquals("Wrong external interface list", itfs, extItfs);
        Iterator i = itfs.iterator();
        while (i.hasNext()) {
            String itf = (String) i.next();
            String compItf = null;
            try {
                compItf = getItf((Interface) c.getFcInterface(getItfName(itf)),
                        false);
            } catch (NoSuchInterfaceException e) {
                fail("Missing external interface: " + itf);
            }
            assertEquals("Wrong external interface", itf, compItf);
        }

        // FIXME In the ProActive implementation external are also internal interfaces

        //        ContentController cc;
        //        try {
        //            cc = Fractal.getContentController(c);
        //        } catch (NoSuchInterfaceException e) {
        //            return;
        //        }
        //
        //        itfs = new HashSet(itfs);
        //        i = itfs.iterator();
        //        while (i.hasNext()) {
        //            String itf = (String) i.next();
        //            if (itf.startsWith("component/") ||
        //                    (itf.indexOf("-controller/") != -1)) {
        //                i.remove();
        //            }
        //        }
        //
        //        Set intItfs = getInternalItfs(cc);
        //        assertEquals("Wrong internal interface list", itfs, intItfs);
        //        i = itfs.iterator();
        //        while (i.hasNext()) {
        //            String itf = (String) i.next();
        //            String compItf = null;
        //            try {
        //                compItf = getItf((Interface) cc.getFcInternalInterface(
        //                            getItfName(itf)), true);
        //            } catch (NoSuchInterfaceException e) {
        //                fail("Missing internal interface: " + itf);
        //            }
        //            assertEquals("Wrong internal interface", itf, compItf);
        //        }
    }

    protected Set getExternalItfs(Component c) {
        HashSet result = new HashSet();
        Object[] extItfs = c.getFcInterfaces();
        for (int i = 0; i < extItfs.length; ++i) {
            String itf = getItf((Interface) extItfs[i], false);
            if (!result.add(itf)) {
                fail("Duplicated interface: " + itf);
            }
        }
        return result;
    }

    private Set getInternalItfs(ContentController cc) {
        HashSet result = new HashSet();
        Object[] extItfs = cc.getFcInternalInterfaces();
        for (int i = 0; i < extItfs.length; ++i) {
            String itf = getItf((Interface) extItfs[i], true);
            if (!result.add(itf)) {
                fail("Duplicated interface: " + itf);
            }
        }
        return result;
    }

    protected static String getItf(Interface itf, boolean internal) {
        InterfaceType itfType = (InterfaceType) itf.getFcItfType();
        return getItf(itf.getFcItfName(), itfType.getFcItfSignature(),
            itfType.isFcClientItf() ^ internal, itfType.isFcOptionalItf(),
            itfType.isFcCollectionItf());
    }

    private static String getItf(String name, String signature,
        boolean isClient, boolean isOptional, boolean isCollection) {
        return name + '/' + signature + '/' + isClient + ',' + isOptional +
        ',' + isCollection;
    }

    private static String getItfName(String itf) {
        return itf.substring(0, itf.indexOf('/'));
    }
}
