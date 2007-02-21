package org.objectweb.proactive.core.gc;


/**
 * This is probably the smallest class in ProActive. So, let's compensate with
 * comments. The DGC has to keep track of every Proxy pointing to a given
 * Active Object, as the only way to declare that an AO does not reference
 * another AO anymore is to observe that all these Proxy have disappeared.
 * Instead of doing this by keeping all Proxy in a list, we give a reference to
 * the same instance of a GCTag to all the Proxy pointing to the same AO. The
 * DGC keeps a weak reference to this GCTag. When this weak reference is
 * cleared, it means all the Proxy pointing to the AO have been garbage
 * collected.
 *
 * This code is:
 * Copyright (C) 2007 Guillaume Chazarain
 * All rights reserved
 */
public class GCTag {
}
